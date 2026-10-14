package com.yr.seckilldemo.controller;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.application.vo.ImageCaptchaVO;
import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.common.response.ApiResponse;
import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    @Autowired
    private ImageCaptchaApplication captchaApplication;

    @PostMapping("/gen")
    public ApiResponse<ImageCaptchaVO> generate() {
        return captchaApplication.generateCaptcha(CaptchaTypeConstant.SLIDER);
    }

    @PostMapping("/check")
    public ApiResponse<?> check(
            @RequestBody CaptchaCheckRequest request,
            HttpSession session) {

        ApiResponse<?> result = captchaApplication.matching(
                request.getId(),
                request.getData()
        );

        if (result.isSuccess()) {
            session.setAttribute("login:captcha:passed", true);
        }

        return result;
    }
    @Data
    public static class CaptchaCheckRequest {
        private String id;
        private ImageCaptchaTrack data;
    }
}
