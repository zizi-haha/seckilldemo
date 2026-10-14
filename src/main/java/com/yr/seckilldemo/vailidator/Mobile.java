package com.yr.seckilldemo.vailidator;


import com.yr.seckilldemo.vo.isMobileValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Documented//是否生成java到doc
@Constraint(validatedBy = {isMobileValidator.class})

public @interface Mobile {
    boolean required() default true;
    String message() default "参数不合法";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
