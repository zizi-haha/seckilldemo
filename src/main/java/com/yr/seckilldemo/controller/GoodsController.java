package com.yr.seckilldemo.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.yr.seckilldemo.pojo.User;
import com.yr.seckilldemo.service.IGoodsService;
import com.yr.seckilldemo.vo.GoodsVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.Thymeleaf;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

import java.util.concurrent.TimeUnit;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;

    @GetMapping(value = "/toList/{goodsid}",produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toList(Model model, User user, @PathVariable Long goodsid, HttpServletRequest request, HttpServletResponse response) {
        //先从缓存里尝试读取页面
        //能读到就直接返回
        ValueOperations valueOperations=redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodsList:" + goodsid);
        if(!StringUtils.isEmpty(html)){
            return html;
        }
        //读不到就手动渲染
        GoodsVO goods = goodsService.findGoodsByid(goodsid);
        model.addAttribute("goodsDetail", goods);
        JakartaServletWebApplication jakartaServletWebApplication=JakartaServletWebApplication.buildApplication(request.getServletContext());
        IWebExchange exchange=jakartaServletWebApplication.buildExchange(request,response);
        WebContext context= new WebContext(exchange, request.getLocale(),model.asMap());

        html=thymeleafViewResolver.getTemplateEngine().process("goodsList",context);;
        if(!StringUtils.isEmpty(html)){
            valueOperations.set("goodsList:"+goodsid,html,60, TimeUnit.MINUTES);
        }
        return html;
    }


    //windows  1317   57143
    //linux  1254.9
    @GetMapping(value = "/toDetail",produces = "text/html;charset=utf-8")
    @ResponseBody
    public String findGoods(Model model, User user,HttpServletResponse response,HttpServletRequest request,Long goodsid) {
        ValueOperations valueOperations=redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodsDetails"+goodsid);
        if(!StringUtils.isEmpty(html)){
            return html;
        }
        model.addAttribute("goodList", goodsService.findGoods());

        JakartaServletWebApplication jakartaServletWebApplication=JakartaServletWebApplication.buildApplication(request.getServletContext());
        IWebExchange exchange=jakartaServletWebApplication.buildExchange(request,response);
        WebContext context= new WebContext(exchange, request.getLocale(),model.asMap());

        html=thymeleafViewResolver.getTemplateEngine().process("goodsDList",context);
        return html;
    }
}
