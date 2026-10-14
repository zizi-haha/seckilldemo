package com.yr.seckilldemo.pojo;


import lombok.*;


import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class seckillMessage implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long userId;
    private Long goodsId;
}
