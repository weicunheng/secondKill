package com.weiheng.secondkill.auth.domain;

import com.weiheng.secondkill.entity.User;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class UserDetail extends org.springframework.security.core.userdetails.User {
    private User user;

    public UserDetail(User userEntity, Collection<? extends GrantedAuthority> authorities) {
        // 必须调用父类的构造方法，以初始化用户名、密码、权限
        super(userEntity.getUserName(), userEntity.getPassword(), authorities);
        this.user = userEntity;
    }

    public void setUser(User user) {
        this.user = user;
    }
    public User getUser(){
        return this.user;
    }
}
