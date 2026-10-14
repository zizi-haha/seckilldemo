package com.yr.seckilldemo.controller;

import com.rabbitmq.tools.json.JSONUtil;
import com.yr.seckilldemo.pojo.User;
import com.yr.seckilldemo.rabbitemq.QueuemqSender;
import com.yr.seckilldemo.service.UserService;


import com.yr.seckilldemo.utils.CookieUtils;
import com.yr.seckilldemo.utils.MD5Utils;
import com.yr.seckilldemo.vo.Result;
import com.yr.seckilldemo.vo.ResultEnum;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zhoubin
 * @since 2026-09-21
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private QueuemqSender queuemqSender;


    @RequestMapping("/info")
    public Result info(User u) {
        return Result.success(u);
    }

    //商品页做整体缓存，缓存内容不含用户信息，登录态由这个接口单独返回
    @GetMapping("/current")
    @ResponseBody
    public Result current(User user) {
        if (user == null) {
            return Result.success(null);
        }
        return Result.success(user.getUsername());
    }

    //用户修改密码
    @GetMapping("/settings")
    public String settings(Model model, User user) {
        if (user == null) {
            return "redirect:/tologin";
        }

        model.addAttribute("user", user);
        return "userSettings";
    }

    @PostMapping("/updatePassword")
    @ResponseBody//@RestController 或者mapping+resopodbody
    public Result updatePassword(User u,String oldPassword ,String password, HttpServletRequest request) {
        if (u == null) {
            return Result.error(ResultEnum.Loging_ERROR);
        }
        //校验原密码
        if (oldPassword == null || oldPassword.isBlank()||!MD5Utils.endpass(oldPassword,u.getSalt()).equals(u.getPassword())){
            return Result.error(ResultEnum.OLD_PASSWORD_ERROR);
        }

        //校验密码格式
        if (password == null || password.isBlank()
                || password.length() < 6
                || password.length() > 20) {
            return Result.error(ResultEnum.BIND_ERROR);
        }
        userService.updatePassword(u,password);
        String token= CookieUtils.getCookieValue(request,"userTicket");
        String userredisKey= "user:"+token;
        redisTemplate.delete(userredisKey);
        return Result.success();

    }


    //用户修改id
    @PostMapping("/updateId")
    @ResponseBody
    public Result updateId(User u,Long id,HttpServletRequest request) {
        if (u == null) {
            return Result.error(ResultEnum.Loging_ERROR);
        }
        userService.updateId(u,id);
        String token= CookieUtils.getCookieValue(request,"userTicket");
        String userredisKey= "user:"+token;
        redisTemplate.delete(userredisKey);
        return Result.success();
    }



//    @GetMapping("/mq")
//    @ResponseBody
//    public Result mq(){
//        queuemqSender.send("hello");
//
//        queuemqSender.senddericte1("hello-direct1");
//        queuemqSender.senddericte2("hello-direct2");
//        return Result.success();
//    }
}
