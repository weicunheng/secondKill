package com.weiheng.secondkill.auth.security.config;

import com.weiheng.secondkill.auth.security.filter.JWTFilter;
import com.weiheng.secondkill.auth.service.impl.UserDetailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/*
 * Spring Security 自定义配置类
 * 1. 自定义身份验证管理器
 * 2. 配置共有URL 私有URL 授权等
 * */
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    /*
     * 认证管理器
     * */
    @Autowired
    private UserDetailServiceImpl userDetailService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 指定获取用户信息的service
        auth.userDetailsService(userDetailService).passwordEncoder(passwordEncoder());
    }

    /*
     * web security
     * */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 禁掉session
        http.addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class).authorizeRequests()
                // 放行所有OPTIONS请求
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                // 放行登录方法
                .antMatchers("/auth/login/").permitAll()
                // 其他请求都需要认证后才能访问
                .anyRequest().authenticated()
                // 使用自定义的 accessDecisionManager
//                .accessDecisionManager(accessDecisionManager())
                .and()
                // 添加未登录与权限不足异常处理器
                .exceptionHandling()
//                .accessDeniedHandler(restfulAccessDeniedHandler())
//                .authenticationEntryPoint(restAuthenticationEntryPoint())
                .and()
                // 将自定义的JWT过滤器放到过滤链中
//                .addFilterBefore(jwtAuthenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class)
                // 打开Spring Security的跨域
                .cors()
                .and()
                // 关闭CSRF
                .csrf().disable()
                // 关闭Session机制
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        // 指定密码加密规则和校验规则
        return new BCryptPasswordEncoder();
    }

    @Override
    @Bean
    protected AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    @Bean
    public JWTFilter jwtFilter() {
        return new JWTFilter();
    }

}
