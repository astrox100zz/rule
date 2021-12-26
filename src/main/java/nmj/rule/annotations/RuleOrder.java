package nmj.rule.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指定规则的优先级, 为什么不放到@Rule中设置? 为了方便查找rule, 只要项目搜索@Rule("字段名")就能很容易找到规则定义
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RuleOrder {

    /**
     * 规则的执行优先级, 数字越小, 执行优先级越高, Integer.MIN_VALUE为最高优先级
     * @return
     */
    int value();
}
