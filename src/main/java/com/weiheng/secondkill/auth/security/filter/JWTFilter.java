package com.weiheng.secondkill.auth.security.filter;

import com.weiheng.secondkill.common.constant.SecurityConstant;
import com.weiheng.secondkill.auth.cache.UserRepository;
import com.weiheng.secondkill.auth.jwt.utils.JwtTokenUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/*
 * JWT过滤器
 * */
public class JWTFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //从header中获取JWT
        String jwtToken = request.getHeader(SecurityConstant.HEADER);
        if (StringUtils.isNotBlank(jwtToken) && jwtToken.startsWith(SecurityConstant.TOKEN_SPLIT)) {
            // 1. 获取token
            jwtToken = jwtToken.substring(SecurityConstant.TOKEN_SPLIT.length());
            if (StringUtils.isNotBlank(jwtToken)) {
                // 2. 解析token，获取user信息
                String userName = jwtUtil.getUsername(jwtToken);
                if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // 3. 从已有的user缓存中 获取user信息
                    User user = userRepository.findByUsername(userName);
                    Map<String, String> resultMap = new HashMap<>();
                    if (jwtUtil.validate(jwtToken) & user != null) {
                        //创建一个标识符, 表示此时Token有效, 不需要更新
                        resultMap.put("needRefresh", "false");
                        request.setAttribute("authInfo", resultMap);
                        //创建一个UsernamePasswordAuthenticationToken
                        // 构造参数是Principal Credentials 与 Authorities
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        //设置用户登录状态 ==> 放到当前的Context中
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                    } else if (userName.equals(user.getUsername())) {
                        //如果用户名相同但是过期了, 刷新token (和缓存中的比较)
                        //TODO 将更新后的token更新到前端
                        String refreshToken = jwtUtil.refreshToken(jwtToken);
                        resultMap.put("needRefresh", "true");
                        //将更新后的Token放到request中, 我们写一个controller, 从中取出后就可以更新了
                        request.setAttribute("authInfo", resultMap);
                        //创建一个UsernamePasswordAuthenticationToken
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        //设置用户登录状态 ==> 放到当前的Context中
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        }
        //继续过滤器链的请求
        filterChain.doFilter(request, response);
    }
}
