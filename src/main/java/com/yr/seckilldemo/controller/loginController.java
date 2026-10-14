package com.yr.seckilldemo.controller;

import com.yr.seckilldemo.service.LoginService;
import com.yr.seckilldemo.vo.Result;
import com.yr.seckilldemo.vo.ResultEnum;
import com.yr.seckilldemo.vo.logVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
public class loginController {

    @Autowired
    private LoginService loginService;

    @GetMapping("/tologin")
    public String tologin() {
        return "login";
    }

    @PostMapping({"/api/login", "/dologin"})
    @ResponseBody
    public Result login(
            @Valid logVO logvo,
            HttpServletRequest request,
            HttpServletResponse response,
            HttpSession session) {
        Object captchaPassed = session.getAttribute("login:captcha:passed");

        if (!Boolean.TRUE.equals(captchaPassed)) {
            return Result.error(ResultEnum.CAPTCHA_ERROR);
        }

        // 验证码结果只能用一次
        session.removeAttribute("login:captcha:passed");

        log.info("User login: {}", logvo.getId());
        return loginService.login(logvo, request, response);
    }
}
