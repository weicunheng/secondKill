package com.weiheng.secondkill.auth.service.impl;


import com.weiheng.secondkill.entity.User;
import com.weiheng.secondkill.auth.mapper.UserMapper;
import com.weiheng.secondkill.auth.domain.UserDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 从数据库中查询出用户实体对象
        User user = userMapper.selectByUsername(username);
        // 若没查询到一定要抛出该异常，这样才能被Spring Security的错误处理器处理
        if (user == null) {
            throw new UsernameNotFoundException("没有找到该用户");
        }

        // 构建UserDetail对象
        // UserDetail对象包含 当前用户和权限

        return new UserDetail(user, Collections.emptyList());
    }
}
