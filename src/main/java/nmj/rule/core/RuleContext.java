package nmj.rule.core;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import nmj.rule.Rules;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class RuleContext<Model> {

    private final Rules<Model> rule;
    private final Model model;
    private final Map<String, InfoCache.ModelInst> modelInst;
    private final Lazy<Model> proxy;

    public static <Model> RuleContext<Model> create(Rules<Model> rule, Consumer<Model> consumer) {
        if (rule == null || consumer == null) {
            throw new IllegalArgumentException();
        }
        return new RuleContext<>(rule, consumer);
    }

    /**
     *  获取代理对象, 用于快捷(强类型的方式)获取模型字段
     * @return
     */
    public Model getProxy() {
        return proxy.get();
    }

    private RuleContext(Rules<Model> rule, Consumer<Model> consumer) {
        this.rule = InfoCache.getTarget(rule);
        InfoCache.DefaultCons<Model> cons = InfoCache.getConstructor(this.rule.getClass());
        this.model = cons.newInstance();
        consumer.accept(this.model);
        this.proxy = new Lazy<>(() -> {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(cons.getDeclaringClass());
            enhancer.setCallback(new RuleCallback<>(this));
            return (Model) enhancer.create();
        });
        this.modelInst = InfoCache.getModelInst(this.rule.getClass());
    }

    /**
     * 懒加载指定对象
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

    private  <T> T getOrNull(Method method) {
        return null;
    }
}
