package com.atguigu.lease.web.admin.custom.interceptor;

import com.atguigu.lease.common.context.LoginUser;
import com.atguigu.lease.common.context.LoginUserContext;
import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("access_token");

        if (token == null) {
            throw new LeaseException(ResultCodeEnum.ADMIN_LOGIN_AUTH);
        } else {
            Claims claims = JwtUtil.parseToken(token);
            Long userId = claims.get("userId", Long.class);
            String username = claims.get("username", String.class);
            LoginUserContext.setLoginUser(new LoginUser(userId, username));
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        LoginUserContext.clear();
    }
}