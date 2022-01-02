package nmj.rule.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在实现了Rules接口的类的private方法上, 表明其为一条普通规则
 * 普通规则和有序规则的区别在于, 普通规则的执行无固定顺序, 而有序规则总是按照优先级顺序执行
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Rule {

    /**
     * 此规则作用的模型名称
     * @return
     */
    String value();
}
