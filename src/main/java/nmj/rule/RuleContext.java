package nmj.rule;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class RuleContext<Model> {

    private static final Logger log = LoggerFactory.getLogger(RuleContext.class);
    private final Rules<Model> rules;
    private final Model model;
    private final Map<String, InfoCache.ModelInst> modelInst;
    private final InfoCache.ValueMap valueMap;
    private final Lazy<Model> proxy;

    public static <Model> RuleContext<Model> create(Rules<Model> rule, Consumer<Model> consumer) {
        if (rule == null || consumer == null) {
            throw new IllegalArgumentException();
        }
        return new RuleContext<>(rule, consumer);
    }

    /**
     * 获取代理对象, 用于快捷(强类型的方式)获取模型字段, 此种方式如果获取不到值, 不会抛出异常, 只会返回null
     *
     * @return
     */
    public Model getProxy() {
        return proxy.get();
    }

    public <T> T get(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("模型名称无效");
        }
        final InfoCache.ValueHolder valueHolder = get(name, new HashSet<>(128));
        if (valueHolder.isCompleted()) {
            return valueHolder.getValue();
        }
        throw new IllegalStateException("模型获取失败 " + name);
    }

    public <T> T getOrElse(String name, T orElse) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException();
        }
        final InfoCache.ValueHolder valueHolder = get(name, new HashSet<>(128));
        if (valueHolder.isCompleted()) {
            return valueHolder.getValue();
        }
        return orElse;
    }

    public <T> T getOrNull(String name) {
        return getOrElse(name, null);
    }

    private RuleContext(Rules<Model> rules, Consumer<Model> consumer) {
        this.rules = rules;
        InfoCache.DefaultCons<Model> cons = InfoCache.getConstructor(this.rules.getClass());
        this.model = cons.newInstance();
        this.proxy = new Lazy<>(() -> {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(cons.getDeclaringClass());
            enhancer.setCallback(new RuleCallback<>(this));
            return (Model) enhancer.create();
        });
        this.modelInst = InfoCache.getModelInst(this.rules.getClass());
        this.valueMap = new InfoCache.ValueMap(modelInst.size() * 2);
        // 初始化model对象
        consumer.accept(this.model);
    }

    /**
     * 核心代码 递归地有序地获取值
     * @param name
     * @param visited
     * @return
     */
    private InfoCache.ValueHolder get(String name, Set<InfoCache.RuleInst> visited) {
        final InfoCache.ModelInst modelInst = this.modelInst.get(name);
        if (modelInst == null) {
            throw new IllegalArgumentException("模型 " + name + " 在类 " + this.model.getClass().getName() + " 和其父类中均未定义");
        }
        final InfoCache.ValueHolder valueHolder = valueMap.get(name, model, modelInst);
        if (valueHolder.isCompleted()) {
            return valueHolder;
        }
        for (InfoCache.RuleStore ruleStore : modelInst.getRules()) {
            // 优先按顺序执行有序规则
            List<InfoCache.RuleInst> orderedRules = ruleStore.getOrderedRules();
            for (InfoCache.RuleInst or : orderedRules) {
                if (visited.contains(or)) {
                    continue;
                }
                visited.add(or);
                final List<InfoCache.ModelInst> args = or.getArgs();
                final int size = args.size();
                if (size == 0) {
                    Object value = or.getValue(rules);
                    valueHolder.setValue(value);
                    if (valueHolder.isCompleted()) {
                        return valueHolder;
                    }
                    continue;
                }
                boolean success = true;
                for (final InfoCache.ModelInst arg : args) {
                    final String argName = arg.getName();
                    final InfoCache.ValueHolder argValue = valueMap.get(argName, model, arg);
                    if (argValue.isCompleted()) {
                        continue;
                    }
                    // 尝试获取值(递归调用)
                    get(argName, visited);
                    if (argValue.isCompleted()) {
                        continue;
                    }
                    success = false;
                    break;
                }
                // 存在这种情况的, 所有又加个这个看起来啰嗦的判断
                if (valueHolder.isCompleted()) {
                    return valueHolder;
                }
                if (success) {
                    final Object value = or.getValue(rules, valueMap);
                    valueHolder.setValue(value);
                    if (valueHolder.isCompleted()) {
                        return valueHolder;
                    }
                }
            }
            // 对于普通规则, 执行逻辑为 1. 优先执行当前参数已经就绪的 2. 之后按照第一次执行时预估的成本cost由小到大执行
            List<InfoCache.RuleInst> generalRules = ruleStore.getGeneralRules();
            for (InfoCache.RuleInst gr : generalRules) {
                // 优先尝试当前参数已经都准备好的
                Object value = gr.tryIfAllArgsReady(rules, model, valueMap, visited);
                valueHolder.setValue(value);
                if (valueHolder.isCompleted()) {
                    return valueHolder;
                }
            }
            for (InfoCache.RuleInst gr : generalRules) {
                if (visited.contains(gr)) {
                    continue;
                }
                visited.add(gr);
                final List<InfoCache.ModelInst> args = gr.getArgs();
                boolean success = true;
                for (final InfoCache.ModelInst arg : args) {
                    final String argName = arg.getName();
                    final InfoCache.ValueHolder argValue = valueMap.get(argName, model, arg);
                    if (argValue.isCompleted()) {
                        continue;
                    }
                    // 尝试获取值(递归调用)
                    get(argName, visited);
                    if (argValue.isCompleted()) {
                        continue;
                    }
                    success = false;
                    break;
                }
                // 存在这种情况的, 所有又加个这个看起来啰嗦的判断
                if (valueHolder.isCompleted()) {
                    return valueHolder;
                }
                if (success) {
                    final Object value = gr.getValue(rules, valueMap);
                    valueHolder.setValue(value);
                    if (valueHolder.isCompleted()) {
                        return valueHolder;
                    }
                }
            }
        }
        return valueHolder;
    }

    private <T> T getOrNull(Method method) {
        String name = InfoCache.getModelName(method);
        if (name == null) {
            log.warn("{} fieldName not found!", method.toGenericString());
            return null;
        }
        return getOrNull(name);
    }

    /**
     * 懒加载指定对象
     *
     * @param <Model>
     */
    private static final class Lazy<Model> {
        private final Supplier<Model> supplier;
        private Optional<Model> value;

        public Lazy(Supplier<Model> supplier) {
            this.supplier = supplier;
            this.value = null;
        }

        public Model get() {
            if (value == null) {
                value = Optional.ofNullable(supplier.get());
            }
            return value.orElse(null);
        }
    }

    private static final class RuleCallback<Model> implements MethodInterceptor {

        private final RuleContext<Model> context;

        public RuleCallback(RuleContext<Model> context) {
            this.context = context;
        }

        @Override
        public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            return context.getOrNull(method);
        }
    }
}
