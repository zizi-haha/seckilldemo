package com.yr.seckilldemo.service;

import com.yr.seckilldemo.vo.Result;
import com.yr.seckilldemo.vo.logVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public interface LoginService {
    Result login(logVO logvo, HttpServletRequest request, HttpServletResponse response);
}
