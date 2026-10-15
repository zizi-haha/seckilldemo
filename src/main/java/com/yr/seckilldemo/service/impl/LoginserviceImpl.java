package com.yr.seckilldemo.service.impl;

import com.yr.seckilldemo.mapper.LoginMapper;
import com.yr.seckilldemo.pojo.User;
import com.yr.seckilldemo.service.LoginService;
import com.yr.seckilldemo.utils.CookieUtils;
import com.yr.seckilldemo.utils.MD5Utils;
import com.yr.seckilldemo.utils.UUidUtils;
import com.yr.seckilldemo.vo.Result;
import com.yr.seckilldemo.vo.ResultEnum;
import com.yr.seckilldemo.vo.logVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class LoginserviceImpl implements LoginService {

    @Autowired
    private LoginMapper loginMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Result login(logVO logvo, HttpServletRequest request, HttpServletResponse response) {
        String password = logvo.getPassword();
        Long mobile = logvo.getId();

        User user = loginMapper.selectBymobile(mobile);
        if (user == null) {
            log.info("User does not exist: {}", mobile);
            return Result.error(ResultEnum.Loging_ERROR);
        }

        if (!user.getPassword().equals(MD5Utils.endpass(password, user.getSalt()))) {
            return Result.error(ResultEnum.Loging_ERROR);
        }

        String token = UUidUtils.uuid();
        redisTemplate.opsForValue().set("user:" + token, user, 120, TimeUnit.MINUTES);
        CookieUtils.setCookie(response, request, "userTicket", token, 30 * 60);
        return Result.success(user);
    }
}
