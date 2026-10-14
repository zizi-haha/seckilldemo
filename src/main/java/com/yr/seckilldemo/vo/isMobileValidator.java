package com.yr.seckilldemo.vo;

import com.yr.seckilldemo.utils.MobileUtils;
import com.yr.seckilldemo.vailidator.Mobile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component

public class isMobileValidator implements ConstraintValidator<Mobile,Long> {

    private boolean required = false;

    @Override
    public void initialize(Mobile constraintAnnotation) {
        required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(Long s, ConstraintValidatorContext constraintValidatorContext) {
        if (required) {//        如果是必填项
            return MobileUtils.isMobile(s);
        } else {
            if (s == null) {
                return true;
            } else {
                return MobileUtils.isMobile(s);
            }
        }

    }
}
