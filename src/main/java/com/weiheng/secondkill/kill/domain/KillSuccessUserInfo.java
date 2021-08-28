package com.weiheng.secondkill.kill.domain;

import com.weiheng.secondkill.entity.ItemKillSuccess;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;


@ToString
public class KillSuccessUserInfo extends ItemKillSuccess implements Serializable {
    private String username;
    private String goodsName;
    private String email;
    private String phone;

}
