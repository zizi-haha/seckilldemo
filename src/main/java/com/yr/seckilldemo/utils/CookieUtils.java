package com.yr.seckilldemo.utils;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;




    public class CookieUtils {

        /**
         * 设置 Cookie
         * @param response 响应对象
         * @param name     Cookie 名字
         * @param value    Cookie 值
         * @param maxAge   有效期（秒）
         */
        public static void setCookie(HttpServletResponse response, HttpServletRequest request,String name, String value, Integer maxAge) {
            Cookie cookie = new Cookie(name, value);
            cookie.setPath("/");          // 整个项目都能访问
            cookie.setMaxAge(maxAge);     // 有效期
            cookie.setHttpOnly(true);     // 防止 JS 读取，更安全
            response.addCookie(cookie);
        }


        /**
         * 获取 Cookie 的值
         * @param request 请求对象
         * @param name    Cookie 名字
         * @return Cookie 值，不存在返回 null
         */
        public static String getCookieValue(HttpServletRequest request, String name) {
            Cookie[] cookies = request.getCookies();
            if (cookies == null) {
                return null;
            }
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
            return null;
        }

        /**
         * 删除 Cookie
         */
        public static void deleteCookie(HttpServletResponse response, String name) {
            Cookie cookie = new Cookie(name, null);
            cookie.setPath("/");
            cookie.setMaxAge(0);   // 0 表示立即删除
            response.addCookie(cookie);
        }
    }

