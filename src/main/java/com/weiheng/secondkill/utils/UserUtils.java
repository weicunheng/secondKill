package com.weiheng.secondkill.utils;


import com.weiheng.secondkill.auth.domain.CurrentUserDetail;
import com.weiheng.secondkill.auth.domain.UserDetail;
import com.weiheng.secondkill.auth.jwt.properties.JwtSecurityProperties;
import com.weiheng.secondkill.auth.jwt.utils.JwtTokenUtil;
import com.weiheng.secondkill.common.constant.SecurityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/*
 * 当前登录用户相关工具类
 * 从登录的jwt令牌中得到用户信息。存储的到当前线程中
 * */
public class UserUtils {

    @Autowired
    private static JwtSecurityProperties jwtSecurityProperties;

    @Autowired
    private static JwtTokenUtil jwtUtil;


    public static CurrentUserDetail getCurrentUser() {
        CurrentUserDetail user = new CurrentUserDetail();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetail userDetail = (UserDetail) authentication.getPrincipal();
            user.setUsername(userDetail.getUser().getUserName());
            user.setId(userDetail.getUser().getId());
        } catch (Exception exception) {
            // 持有上下文的Request容器
            ServletRequestAttributes requestAttr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = Objects.requireNonNull(requestAttr).getRequest();
            String jwtToken = request.getHeader(jwtSecurityProperties.getHeader());
            jwtToken = jwtToken.substring(SecurityConstant.TOKEN_SPLIT.length());
            user.setId(Integer.parseInt(jwtUtil.getUserId(jwtToken)));
            user.setUsername(jwtUtil.getUsername(jwtToken));

        }
        return user;
    }
}
