package com.weiheng.secondkill.auth.cache;

import com.weiheng.secondkill.auth.domain.UserDetail;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/*
 * 存入user token,可以引用缓存系统，存入到缓存。
 * */
@Component
public class UserRepository {
    private static final Map<String, UserDetail> userMap = new HashMap<>();

    public UserDetail findByUsername(final String username) {
        return userMap.get(username);
    }

    public UserDetail insert(UserDetail user) {
        userMap.put(user.getUsername(), user);
        return user;
    }

    public void remove(String username) {
        userMap.remove(username);
    }
}
