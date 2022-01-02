package nmj.rule;

import nmj.rule.annotations.OrderedRule;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import java.beans.Introspector;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

final class InfoCache {

    public static final Map<Class<?>, DefaultCons<?>> CONSTRUCTOR_CACHE = new ConcurrentHashMap<>(1024);

    private static final Map<Class<?>, List<Model>> MODEL_CACHE = new ConcurrentHashMap<>(1024);

    private static final Map<Class<?>, List<Rule>> RULE_CACHE = new ConcurrentHashMap<>(2048);

    private static final Map<Method, String> METHOD_CACHE = new ConcurrentHashMap<>(8192);

    private static final Map<Class<?>, Map<String, ModelInst>> MODEL_INST_CACHE = new ConcurrentHashMap<>(2048);

    public static <Model> DefaultCons<Model> getConstructor(Class<?> ruleClass) {
        DefaultCons<Model> defaultCons = (DefaultCons<Model>) CONSTRUCTOR_CACHE.get(ruleClass);
        if (defaultCons != null) {
            return defaultCons;
        }
        defaultCons = (DefaultCons<Model>) getConstructorInternal(ruleClass);
        CONSTRUCTOR_CACHE.put(ruleClass, defaultCons);
        return defaultCons;
    }

    public static List<Model> getModels(Class<?> modelClass) {
        // 为了不用computeIfAbsent? 因为发现这个方法第一次调用时, 需要80ms左右的消耗, 第二次就好了
        // return MODEL_CACHE.computeIfAbsent(modelClass, InfoCache::getModels);
        List<Model> models = MODEL_CACHE.get(modelClass);
        if (models != null) {
            return models;
        }
        models = getModelsInternal(modelClass);
        MODEL_CACHE.put(modelClass, models);
        return models;
    }

    public static List<Rule> getRules(Class<?> ruleClass) {
        List<Rule> rules = RULE_CACHE.get(ruleClass);
        if (rules != null) {
            return rules;
        }
        rules = getRulesInternal(ruleClass);
        RULE_CACHE.put(ruleClass, rules);
        return rules;
    }

    public static Map<String, ModelInst> getModelInst(Class<?> ruleClass) {
        Map<String, ModelInst> modelInstMap = MODEL_INST_CACHE.get(ruleClass);
        if (modelInstMap != null) {
            return modelInstMap;
        }
        modelInstMap = getModelInstInternal(ruleClass);
        MODEL_INST_CACHE.put(ruleClass, modelInstMap);
        return modelInstMap;
    }

    public static String getModelName(Method method) {
        final String methodName = method.getName();
        if (!methodName.startsWith("get") && !methodName.startsWith("is")) {
            return null;
        }
        String model = METHOD_CACHE.get(method);
        if (model != null) {
            return model;
        }
        nmj.rule.annotations.Model annotation = method.getAnnotation(nmj.rule.annotations.Model.class);
        if (annotation != null) {
            model = annotation.value();
            METHOD_CACHE.put(method, model);
            return model;
        }
        if (methodName.startsWith("get")) {
            model = Introspector.decapitalize(methodName.substring(3));
        } else if (methodName.startsWith("is")) {
            model = Introspector.decapitalize(methodName.substring(3));
        } else {
            throw new IllegalStateException();
        }
        METHOD_CACHE.put(method, model);
        return model;
    }

    private static <Model> DefaultCons<Model> getConstructorInternal(Class<Model> targetRuleClass) {
        for (Class<?> current = targetRuleClass;
             !Objects.equals(current, Object.class);
             current = current.getSuperclass()) {
            for (Type genericInterface : current.getGenericInterfaces()) {
                String typeName = genericInterface.getTypeName();
                if (typeName.contains(Rules.class.getName())) {
                    Class<Model> modelClass = (Class<Model>) ((ParameterizedType) genericInterface).getActualTypeArguments()[0];
                    return new DefaultCons<>(modelClass);
                }
            }
        }
        throw new IllegalArgumentException();
    }

    private static List<Model> getModelsInternal(Class<?> modelClass) {
        Field[] fields = modelClass.getDeclaredFields();
        List<InfoCache.Model> models = new ArrayList<>(fields.length);
        for (Field field : fields) {
            nmj.rule.annotations.Model annotation = field.getAnnotation(nmj.rule.annotations.Model.class);
            if (annotation == null) {
                continue;
            }
            if (field.getType().isPrimitive()) {
                // 模型定义必须是非基本类型(也就是可以为null)
                throw new IllegalArgumentException("字段 " + field.toGenericString() + " 不得为基本类型 " + field.getType().getName());
            }
            String name = annotation.value();
            if (name.isEmpty()) {
                throw new IllegalArgumentException();
            }
            models.add(new InfoCache.Model(name, field));
        }
        return models;
    }

    private static List<Rule> getRulesInternal(Class<?> ruleClass) {
        Method[] methods = ruleClass.getDeclaredMethods();
        List<Rule> rules = new ArrayList<>(methods.length);
        ParameterNameDiscoverer pnd = new LocalVariableTableParameterNameDiscoverer();
        for (Method method : methods) {
            nmj.rule.annotations.Rule ruleAnnotation = method.getAnnotation(nmj.rule.annotations.Rule.class);
            if (ruleAnnotation == null) {
                continue;
            }
            String name = ruleAnnotation.value();
            if (name.isEmpty()) {
                throw new IllegalArgumentException();
            }
            OrderedRule orderAnnotation = method.getAnnotation(nmj.rule.annotations.OrderedRule.class);
            // @RuleOrder不填就默认为0, 友好一点, 减少不必要的理解成本
            int order = orderAnnotation == null ? 0 : orderAnnotation.value();
            rules.add(new Rule(name, orderAnnotation != null, order, method, pnd));
        }
        // 排序很关键
        rules.sort(Comparator.comparingLong(Rule::getOrder));
        return rules;
    }

    private static Map<String, ModelInst> getModelInstInternal(Class<?> ruleClass) {
        Map<String, ModelInst> modelInst = new HashMap<>(1024);
        // 初始化ModelInst
        Class<Object> modelClass = getConstructor(ruleClass).getDeclaringClass();
        for (Class<?> current = modelClass;
             !Objects.equals(current, Object.class);
             current = current.getSuperclass()
        ) {
            for (Model model : getModels(current)) {
                String name = model.getName();
                if (modelInst.containsKey(name)) {
                    throw new IllegalStateException();
                }
                modelInst.put(name, new ModelInst(model));
            }
        }
        // 初始化RuleInst
        for (Class<?> current = ruleClass;
             !Objects.equals(current, Object.class);
             current = current.getSuperclass()) {
            Map<String, RuleStore> ruleStoreMap = new HashMap<>(modelInst.size() * 2);
            for (Rule rule : getRules(current)) {
                String name = rule.getName();
                ModelInst inst = modelInst.get(name);
                if (inst == null) {
                    // 规则的名称在model中未定义
                    throw new IllegalArgumentException();
                }
                RuleInst ruleInst = new RuleInst(rule, modelInst);

                if (!inst.getType().isAssignableFrom(ruleInst.getReturnType())) {
                    throw new IllegalArgumentException("规则 " + rule.toGenericString() + " 中返回类型为 " + ruleInst.getReturnType().getName() + " 与模型 " + name + " 的类型 " + inst.getType().getName() + " 不一致");
                }
                RuleStore ruleStore = ruleStoreMap.get(name);
                if (ruleStore == null) {
                    ruleStore = new RuleStore();
                    ruleStoreMap.put(name, ruleStore);
                    inst.addRuleStore(ruleStore);
                }
                inst.addRule(ruleInst);
            }
        }
        return Collections.unmodifiableMap(modelInst);
    }

    static final class Model {
        private final String name;
        private final Field field;

        public Model(String name, Field field) {
            field.setAccessible(true);
            this.name = name;
            this.field = field;
        }

        public String getName() {
            return this.name;
        }

        public Object get(Object model) {
            try {
                return field.get(model);
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }

        public Class<?> getType() {
            return field.getType();
        }
    }

    static final class Rule {
        private final String name;
        private final Method method;
        private final long order;
        private final boolean isOrderedRule;
        private final List<Argument> args;

        public Rule(String name, boolean isOrderedRule, long order, Method method, ParameterNameDiscoverer pnd) {
            // 规则必须为private方法, 这是为了防止有人去继承
            if (!Modifier.isPrivate(method.getModifiers())) {
                throw new IllegalStateException("规则 " + method.toGenericString() + " 必须为private");
            }
            method.setAccessible(true);
            this.name = name;
            this.method = method;
            this.order = order;
            this.isOrderedRule = isOrderedRule;
            this.args = initArgs(pnd, method);
        }

        public String getName() {
            return name;
        }

        public long getOrder() {
            return order;
        }

        public Class<?> getReturnType() {
            return method.getReturnType();
        }

        public List<Argument> getArgs() {
            return args;
        }

        public boolean isOrderedRule() {
            return isOrderedRule;
        }

        private List<Argument> initArgs(ParameterNameDiscoverer pnd, Method method) {
            Parameter[] parameters = method.getParameters();
            if (parameters == null || parameters.length == 0) {
                return Collections.emptyList();
            }
            String[] parameterNames = pnd.getParameterNames(method);
            List<Argument> args = new ArrayList<>(parameters.length);
            for (int i = 0; i < parameters.length; i++) {
                Argument argument = new Argument(parameterNames[i], parameters[i].getType());
                args.add(argument);
            }
            return args;
        }

        public <Model> Object getValue(Rules<Model> rules, Object[] arguments) {
            try {
                return method.invoke(rules, arguments);
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }

        public <Model> Object getValue(Rules<Model> rules) {
            try {
                return method.invoke(rules);
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }

        public String toGenericString() {
            return method.toGenericString();
        }
    }

    /**
     * model实例
     */
    static final class ModelInst {
        private final Model model;
        private final List<RuleStore> rules;

        public ModelInst(Model model) {
            this.model = model;
            this.rules = new ArrayList<>();
        }

        public void addRule(RuleInst ruleInst) {
            // 总是追加在最后一个ruleStore中
            rules.get(rules.size() - 1).addRule(ruleInst);
        }

        public void addRuleStore(RuleStore ruleStore) {
            rules.add(ruleStore);
        }

        public Class<?> getType() {
            return model.getType();
        }

        public List<RuleStore> getRules() {
            return rules;
        }

        public String getName() {
            return model.getName();
        }

        public Object get(Object md) {
            return model.get(md);
        }
    }

    static final class RuleStore {
        // 有序规则, 需要有序执行
        private final List<RuleInst> orderedRules;
        // 普通规则, 任意顺序执行
        private List<RuleInst> generalRules;

        public RuleStore() {
            this.orderedRules = new ArrayList<>();
            this.generalRules = new ArrayList<>();
        }

        public List<RuleInst> getOrderedRules() {
            return orderedRules;
        }

        public List<RuleInst> getGeneralRules() {
            return generalRules;
        }

        public void setGeneralRules(List<RuleInst> generalRules) {
            this.generalRules = generalRules;
        }

        public void addRule(RuleInst ruleInst) {
            if (ruleInst.isOrderedRule()) {
                orderedRules.add(ruleInst);
            } else {
                generalRules.add(ruleInst);
            }
        }
    }

    /**
     * rule实例
     */
    static final class RuleInst {
        private final Rule rule;
        private final ModelInst model;
        private final List<ModelInst> args;
        // 第一次调用的时候记录的调用耗时, 用于做性能优化时的代价评估
        private long cost;

        public RuleInst(Rule rule, Map<String, ModelInst> modelInst) {
            this.rule = rule;
            this.model = modelInst.get(rule.getName());
            this.args = initArgs(rule, modelInst);
            this.cost = -1L;
        }

        public Class<?> getReturnType() {
            return rule.getReturnType();
        }

        public List<ModelInst> getArgs() {
            return args;
        }

        public <Model> Object getValue(Rules<Model> rules) {
            if (this.cost < 0) {
                long start = System.currentTimeMillis();
                Object value = rule.getValue(rules);
                if (value != null) {
                    this.cost = System.currentTimeMillis() - start;
                    sortGeneralRuleByCost();
                }
                return value;
            } else {
                return rule.getValue(rules);
            }
        }

        public <Model> Object getValue(Rules<Model> rules, ValueMap valueMap) {
            Object[] arguments = getArguments(args, valueMap);
            if (this.cost < 0) {
                long start = System.currentTimeMillis();
                Object value = rule.getValue(rules, arguments);
                if (value != null) {
                    this.cost = System.currentTimeMillis() - start;
                    sortGeneralRuleByCost();
                }
                return value;
            } else {
                return rule.getValue(rules, arguments);
            }
        }

        public <Model> Object tryIfAllArgsReady(Rules<Model> rules, Model model, ValueMap valueMap, Set<RuleInst> visited) {
            if (visited.contains(this)) {
                return null;
            }
            int size = args.size();
            if (size == 0) {
                Object value = getValue(rules);
                if (value == null) {
                    visited.add(this);
                }
                return value;
            }
            boolean allReady = true;
            for (ModelInst arg : args) {
                if (!valueMap.get(arg.getName(), model, arg).isCompleted()) {
                    allReady = false;
                    break;
                }
            }
            if (allReady) {
                Object value = getValue(rules, valueMap);
                if (value == null) {
                    visited.add(this);
                }
                return value;
            }
            return null;
        }

        public long getCost() {
            return cost;
        }

        public boolean isOrderedRule() {
            return rule.isOrderedRule();
        }

        private List<ModelInst> initArgs(Rule rule, Map<String, ModelInst> modelInst) {
            List<Argument> arguments = rule.getArgs();
            List<ModelInst> args = new ArrayList<>(arguments.size());
            for (Argument argument : arguments) {
                String name = argument.getName();
                ModelInst inst = modelInst.get(name);
                if (inst == null) {
                    // 规则中的名称在model中未定义
                    throw new IllegalArgumentException();
                }
                if (!Objects.equals(argument.getType(), inst.getType())) {
                    // 规则类型定义不一致
                    throw new IllegalArgumentException("规则 " + this.rule.toGenericString() + " 定义的参数 " + name + " 类型为 " + argument.getType().getName() + " 与模型中定义的 " + inst.getType().getName() + " 不一致");
                }
                args.add(inst);
            }
            return args;
        }

        private void sortGeneralRuleByCost() {
            if (isOrderedRule()) {
                // 有序规则不需要排序, 执行顺序严格按照用户定义
                return;
            }
            // 对普通规则进行排序
            List<RuleStore> rulesStoreList = this.model.getRules();
            for (RuleStore rs : rulesStoreList) {
                List<RuleInst> generalRules = rs.getGeneralRules();
                for (RuleInst gr : generalRules) {
                    if (this == gr) {
                        List<RuleInst> copy = new ArrayList<>(generalRules);
                        copy.sort(Comparator.comparingLong(RuleInst::getCost));
                        rs.setGeneralRules(copy);
                        // 只会存在一个, 所以直接返回
                        return;
                    }
                }
            }
        }

        private Object[] getArguments(List<InfoCache.ModelInst> args, InfoCache.ValueMap valueMap) {
            Object[] arguments = new Object[args.size()];
            for (int i = 0; i < args.size(); i++) {
                InfoCache.ModelInst arg = args.get(i);
                final InfoCache.ValueHolder argValue = valueMap.get(arg.getName(), model, arg);
                arguments[i] = argValue.getValue();
            }
            return arguments;
        }
    }

    static final class Argument {
        private final String name;
        private final Class<?> type;

        public Argument(String name, Class<?> type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public Class<?> getType() {
            return type;
        }
    }

    /**
     * 默认构造函数的包装, 是为了去掉烦人try-catch, 让主体代码更干净
     */
    static final class DefaultCons<Model> {

        private final Constructor<Model> constructor;

        public DefaultCons(Class<Model> clazz) {
            try {
                this.constructor = clazz.getConstructor();
                this.constructor.setAccessible(true);
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }

        public Model newInstance() {
            try {
                return this.constructor.newInstance();
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }

        public Class<Model> getDeclaringClass() {
            return constructor.getDeclaringClass();
        }
    }

    static final class ValueHolder {
        private Object value;

        public ValueHolder(Object value) {
            setValue(value);
        }

        public <T> T getValue() {
            if (isCompleted()) {
                return (T) value;
            }
            throw new IllegalStateException();
        }

        public void setValue(Object value) {
            if (isCompleted()) {
                throw new IllegalStateException();
            }
            this.value = value;
        }

        public boolean isCompleted() {
            return value != null;
        }
    }

    static final class ValueMap {
        private final Map<String, ValueHolder> valueMap;

        public ValueMap(int size) {
            this.valueMap = new HashMap<>(size);
        }

        public ValueHolder get(String name, Object model, ModelInst modelInst) {
            ValueHolder valueHolder = valueMap.get(name);
            if (valueHolder == null) {
                valueHolder = new ValueHolder(modelInst.get(model));
                valueMap.put(name, valueHolder);
            }
            return valueHolder;
        }
    }
}
