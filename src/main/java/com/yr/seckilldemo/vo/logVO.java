package com.yr.seckilldemo.vo;


import com.yr.seckilldemo.vailidator.Mobile;
import lombok.Data;
import lombok.NonNull;
import org.hibernate.validator.constraints.Length;

@Data

public class logVO {
    @NonNull
    @Mobile
    private Long id;//手机号码
    @NonNull
    @Length()
    private String password;
   // private String role;
}
