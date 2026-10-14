package com.yr.seckilldemo.config;

import com.yr.seckilldemo.pojo.User;
import com.yr.seckilldemo.service.UserService;
import com.yr.seckilldemo.utils.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component

public class ArguementResovers implements HandlerMethodArgumentResolver {

    @Autowired
    private UserService userService;
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> clazz=parameter.getParameterType();
        return  clazz== User.class;
    }

    @Override
    public @Nullable Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer, NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {
        HttpServletResponse response=webRequest.getNativeResponse(HttpServletResponse.class);
        HttpServletRequest request=webRequest.getNativeRequest(HttpServletRequest.class);
        String token= CookieUtils.getCookieValue(request,"userTicket");
        if(StringUtils.isEmpty(token)){
            return null;
        }
        return userService.getUserByToken(token,request,response);
    }
}
