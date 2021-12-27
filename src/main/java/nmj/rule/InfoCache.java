package nmj.rule;

import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

final class InfoCache {

    public static final Map<Class<?>, DefaultCons<?>> CONSTRUCTOR_CACHE = new ConcurrentHashMap<>(1024);

    private static final Map<Class<?>, List<Model>> MODEL_CACHE = new ConcurrentHashMap<>(1024);

    private static final Map<Class<?>, List<Rule>> RULE_CACHE = new ConcurrentHashMap<>(2048);

    // rule类 model名称 model实例
    private static final Map<Class<?>, Map<String, ModelInst>> MODEL_INST_CACHE = new ConcurrentHashMap<>(2048);

    public static <Model> Rules<Model> getTarget(Rules<Model> rule) {
        // TODO
        return rule;
    }

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
            String name = annotation.name();
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
            nmj.rule.annotations.RuleOrder orderAnnotation = method.getAnnotation(nmj.rule.annotations.RuleOrder.class);
            // @RuleOrder不填就默认为0, 友好一点, 减少不必要的理解成本
            int order = orderAnnotation == null ? 0 : orderAnnotation.value();
            rules.add(new Rule(name, order, method, pnd));
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
            for (Rule rule : getRules(current)) {
                String name = rule.getName();
                ModelInst inst = modelInst.get(name);
                if (inst == null) {
                    // 规则的名称在model中未定义
                    throw new IllegalArgumentException();
                }
                RuleInst ruleInst = new RuleInst(rule, modelInst);
                if (!Objects.equals(ruleInst.getReturnType(), inst.getType())) {
                    throw new IllegalArgumentException();
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
        private final List<Argument> args;

        public Rule(String name, long order, Method method, ParameterNameDiscoverer pnd) {
            if (!Modifier.isPrivate(method.getModifiers())) {
                throw new IllegalStateException();
            }
            method.setAccessible(true);
            this.name = name;
            this.method = method;
            this.order = order;
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
    }

    /**
     * model实例
     */
    static final class ModelInst {
        private final Model model;
        private List<RuleInst> rules;

        public ModelInst(Model model) {
            this.model = model;
            this.rules = new ArrayList<>();
        }

        public void addRule(RuleInst ruleInst) {
            rules.add(ruleInst);
        }

        public Class<?> getType() {
            return model.getType();
        }

        public List<RuleInst> getRules() {
            return rules;
        }

        public String getName() {
            return model.getName();
        }

        public Object get(Object md) {
            return model.get(md);
        }
    }

    /**
     * rule实例
     */
    static final class RuleInst {
        private final Rule rule;
        private final List<ModelInst> args;

        public RuleInst(Rule rule, Map<String, ModelInst> modelInst) {
            this.rule = rule;
            this.args = initArgs(rule, modelInst);
        }

        public Class<?> getReturnType() {
            return rule.getReturnType();
        }

        public List<ModelInst> getArgs() {
            return args;
        }

        public <Model> Object getValue(Rules<Model> rules) {
            return rule.getValue(rules);
        }

        public <Model> Object getValue(Rules<Model> rules, Object[] arguments) {
            return rule.getValue(rules, arguments);
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
                    throw new IllegalArgumentException();
                }
                args.add(inst);
            }
            return args;
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

        public void setValue(Object value) {
            if (isComplete()) {
                throw new IllegalStateException();
            }
            this.value = value;
        }

        public <T> T getValue() {
            if (isComplete()) {
                return (T) value;
            }
            throw new IllegalStateException();
        }

        public boolean isComplete() {
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
