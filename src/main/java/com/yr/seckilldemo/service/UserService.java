package com.yr.seckilldemo.service;

import com.yr.seckilldemo.pojo.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;


public interface UserService {
    User getUserByToken(String token, HttpServletRequest request, HttpServletResponse response);

    void updatePassword(User u,String password);

    void updateId(User u,Long id);
}
