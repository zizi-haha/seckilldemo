package com.yr.seckilldemo.mapper;

import com.yr.seckilldemo.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LoginMapper {

    @Select("select id, nickname as username, password, salt, " +
            "regist_date as registDate, last_login_date as lastLoginDate, " +
            "login_count as loginCount from t_user where id = #{mobile}")
    User selectBymobile(Long mobile);
}
