package com.weiheng.secondkill.auth.mapper;

import com.weiheng.secondkill.entity.User;


public interface UserMapper {
    User selectByUsername(String username);

    User selectByOrderNo(String orderNo);
}
