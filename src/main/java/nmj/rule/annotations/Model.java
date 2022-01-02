package nmj.rule.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 1. 标注在字段上面, 表明为一个模型字段
 * 2. 标注在规则方法的参数上, 指定参数对应的模型字段
 * 3. 标注在模型的get方法上, 指定get方法对应的模型名称
 * 3. 模型不允许为基本类型
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface Model {

    /**
     * 必填, 模型名称不能有重复
     *
     * @return
     */
    String value();

    /**
     * 此模型的描述信息
     *
     * @return
     */
    String comment() default "";

}
