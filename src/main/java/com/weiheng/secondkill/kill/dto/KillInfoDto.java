package com.weiheng.secondkill.kill.dto;

import lombok.Data;
import org.springframework.boot.test.autoconfigure.data.ldap.AutoConfigureDataLdap;

@AutoConfigureDataLdap
@Data
public class KillInfoDto {
    /*
     * killItemId
     * userId
     * */
    private Integer killItemId;
    private Integer userId;
}
