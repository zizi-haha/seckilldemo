package com.yr.seckilldemo.utils;

import java.util.regex.Pattern;

public class MobileUtils {
    public  static boolean isMobile(Long mobile){
        if(mobile==null){
            return false;
        }
        String s=String.valueOf(mobile);
        boolean b=Pattern.matches("^1[3-9]\\d{9}$",s);
        return b;
        }
    }

