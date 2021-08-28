package com.weiheng.secondkill.auth.model.response;

import lombok.Data;

@Data
public class UserLoginVO {
    private String username;
    private String token;
}
