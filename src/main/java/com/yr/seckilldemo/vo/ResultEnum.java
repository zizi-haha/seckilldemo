package com.yr.seckilldemo.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ResultEnum {
    SUCCESS(200, "成功"),
    ERROR(500, "失败"),
    Loging_ERROR(500100, "用户名或密码错误"),
    Mobile_ERROR(500101, "手机号格式错误"),
    BIND_ERROR(500102, "参数校验异常"),
    SECKILL_FAIL(500200, "秒杀失败"),
    USER_ERROR(500202,"用户不存在"),
    EMPLY_ERROR(500201,"库存为空"),
    CAPTCHA_ERROR(500203,"验证码为空"),
    OLD_PASSWORD_ERROR(500103,"原密码错误"),;




    private final Integer code;
    private final String msg;
}
