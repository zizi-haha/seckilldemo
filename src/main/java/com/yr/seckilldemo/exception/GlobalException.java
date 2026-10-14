package com.yr.seckilldemo.exception;


import com.yr.seckilldemo.vo.ResultEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlobalException extends RuntimeException {
    private ResultEnum resultEnum;
}
