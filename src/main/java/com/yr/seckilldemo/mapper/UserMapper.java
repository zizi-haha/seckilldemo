package com.yr.seckilldemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yr.seckilldemo.pojo.User;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhoubin
 * @since 2026-09-21
 */
public interface UserMapper extends BaseMapper<User> {



    @Update("update t_user set password=#{password} ,salt=#{salt} where id=#{u.id}")
    void updatePassword(User u,String password,String salt);



    @Update("update  t_user set id=#{id} where id=#{u.id}")
    void updateId(User u,Long id);


    @Update("update t_order set user_id=#{id} where user_id=#{u.id}")
    void updateOrderid(User u,Long id);

    @Update("update t_seckill_order set user_id=#{id} where user_id=#{u.id}")
    void updateSorderId(User u,Long id);
}
