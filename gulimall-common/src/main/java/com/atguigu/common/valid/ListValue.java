package com.atguigu.common.valid;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 用于校验列表值的注解。可以用于方法、字段、注解类型、构造器、参数和类型使用处。
 * 需要由ListValueConstraintValidator类进行验证。
 *
 * @Documented 标明此注解会在javadoc中显示。
 * @Constraint 指明这是一个验证注解，并指定了使用的验证器。
 * @Target 指明此注解可以应用于各种元素类型，包括方法、字段、注解类型、构造函数、参数和类型使用。
 * @Retention 指明此注解的保留策略为运行时。
 */
@Documented
@Constraint(validatedBy = { ListValueConstraintValidator.class })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
public @interface ListValue {
    /**
     * 违规时的错误消息。默认值为"{com.atguigu.common.valid.ListValue.message}"。
     * 可以通过此字段自定义校验失败时的错误信息。
     */
    String message() default "{com.atguigu.common.valid.ListValue.message}";

    /**
     * 校验组，用于分组校验。默认为空组。
     * 可以定义多个校验组，以支持不同的校验场景。
     */
    Class<?>[] groups() default { };

    /**
     * 负载信息，用于承载额外的payload信息。
     * 默认为空，可以扩展以支持更复杂的校验需求。
     */
    Class<? extends Payload>[] payload() default { };

    /**
     * 允许的值列表。默认为空数组。
     * 用于校验字段或参数值是否在允许的值列表之中。
     */
    int[] vals() default { };
}
