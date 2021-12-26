package nmj.rule.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在字段上面, 表明为一个模型字段
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Model {

    /**
     * 必填, 模型名称不能有重复
     * @return
     */
    String name();

    /**
     * 必填, 此模型的描述信息
     * @return
     */
    String comment();

}
