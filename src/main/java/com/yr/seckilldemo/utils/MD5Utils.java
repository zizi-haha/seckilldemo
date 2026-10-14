package com.yr.seckilldemo.utils;

import org.apache.commons.codec.digest.DigestUtils;

import static org.apache.commons.codec.digest.DigestUtils.md5;

public class MD5Utils {

    private  static final  String salt = "123456";
    public static String md5(String src){
        return DigestUtils.md5Hex(src);
    }

    public static String inputPass(String pass){
        String str=salt.charAt(0)+pass+salt.charAt(2)+pass+salt.charAt(4);
        return md5(str);
    }

    public static String formpass(String pass,String salt){
        String str=salt.charAt(0)+pass+salt.charAt(2)+pass+salt.charAt(4);
        return md5(str);
    }
    public static String endpass(String pass,String salt){
        String str1=inputPass(pass);
        String str2=formpass(str1,salt);
        return str2;

    }

//    public static void main(String[] args) {
//        System.out.println(inputPass("123456"));
//        System.out.println(formpass("1cca396d3410aeecadf7c5cd61383e8e",salt));
//        System.out.println(endpass("123456",salt));
//    }
    }

