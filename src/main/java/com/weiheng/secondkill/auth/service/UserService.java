package com.weiheng.secondkill.auth.service;

import com.weiheng.secondkill.entity.User;

public interface UserService {
    User selectByUsername(String username);
}
