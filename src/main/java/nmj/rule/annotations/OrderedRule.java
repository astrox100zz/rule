package nmj.rule.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在标注了@Rule的方法上, 表明其为一条有序规则, 其中value值用于指定规则的优先级
 * 在同一个规则类(实现了Rules接口的类)中, 有序规则总是优先于普通规则来执行
 * 同时, 子类规则(包含有序规则和普通规则)总是优先于父类规则来执行
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OrderedRule {

    /**
     * 规则的执行优先级, 数字越小, 执行优先级越高, Integer.MIN_VALUE为最高优先级, Integer.MAX_VALUE为最低优先级
     *
     * @return
     */
    int value() default 0;
}
