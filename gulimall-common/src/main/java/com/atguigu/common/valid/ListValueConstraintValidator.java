package com.atguigu.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * 实现了ListValue约束验证器，用于验证一个整数是否在一个预定义的整数集合中。
 */
public class ListValueConstraintValidator implements ConstraintValidator<ListValue,Integer> {

    private Set<Integer> set = new HashSet<>();
    /**
     * 初始化方法，根据注解提供的值填充整数集合。
     * 该方法会读取注解 {@code ListValue} 中的值数组，并将这些值添加到一个集合中。
     *
     * @param constraintAnnotation 注解对象，包含需要填充到集合中的整数值。
     *                             该参数不应为 {@code null}。
     */
    @Override
    public void initialize(ListValue constraintAnnotation) {

        // 从注解中获取值数组
        int[] vals = constraintAnnotation.vals();
        for (int val : vals) {
            set.add(val); // 将每个值添加到集合中
        }

    }

    // 判断给定的整数是否在集合中
    /**
     * 验证指定的值是否有效。
     * 这个方法是ConstraintValidator接口的实现，用于验证给定的整数值是否在一个预定义的集合中。
     *
     * @param value 需要验证的整数值。这是要检查是否存在于集合中的数值。
     * @param context 验证上下文，提供关于验证过程的环境信息。用于在验证过程中获取或设置验证相关的上下文信息。
     * @return 布尔值，如果指定值在集合中，则返回true；否则返回false。根据集合中是否包含给定的整数值来决定验证的结果。
     */
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {

        return set.contains(value); // 检查集合是否包含指定的值
    }

}

