package com.yr.seckilldemo.service.impl;

import com.yr.seckilldemo.mapper.UserMapper;
import com.yr.seckilldemo.pojo.User;
import com.yr.seckilldemo.service.UserService;
import com.yr.seckilldemo.utils.CookieUtils;
import com.yr.seckilldemo.utils.MD5Utils;
import com.yr.seckilldemo.utils.UUidUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private UserMapper userMapper;

    @Override
    public User getUserByToken(String token, HttpServletRequest request, HttpServletResponse response) {
        if (token == null) {
            return null;
        }

        User user = (User) redisTemplate.opsForValue().get("user:" + token);
        if (user != null) {

                // 续期，让服务端 ticket 和浏览器 cookie 的过期时间保持一致
                redisTemplate.expire("user:" + token, 120, TimeUnit.MINUTES);
                CookieUtils.setCookie(response, request, "userTicket", token, 120 * 60);

           // CookieUtils.setCookie(response, request, "userTicket", token, 30 * 60);
        }
        return user;
    }

    public void updatePassword(User u,String password){
        String newSalt= UUidUtils.uuid().substring(0,10);
        String newPw= MD5Utils.endpass(password,newSalt);

        userMapper.updatePassword(u, newPw,newSalt);

    }

    @Transactional(rollbackFor = Exception.class)
    public void updateId(User u,Long id){
        userMapper.updateId(u,id);
        userMapper.updateOrderid(u,id);
        userMapper.updateSorderId(u,id);
    }
}
