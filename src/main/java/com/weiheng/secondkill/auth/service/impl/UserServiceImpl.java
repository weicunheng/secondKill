package com.weiheng.secondkill.auth.service.impl;

import com.weiheng.secondkill.entity.User;
import com.weiheng.secondkill.auth.mapper.UserMapper;
import com.weiheng.secondkill.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserMapper userMapper;

    @Override
    public User selectByUsername(String username) {
        return userMapper.selectByUsername(username);
    }
}
