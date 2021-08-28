package com.weiheng.secondkill.common.component;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisDistributeLock {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    /*
     * 获取分布式锁
     * */
    public boolean getLock(String lockId, long expireSecond) {
        // 1. set lockId values ex 5 nx
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockId, "lock", expireSecond, TimeUnit.SECONDS);
        //TODO:设置失败，为了防止死锁，删掉已存在的锁
        return locked != null && locked;
    }

    /*
     * 释放分布式锁
     * */
    public boolean releaseLock(String lockId) {
        Boolean deleteOk = stringRedisTemplate.delete(lockId);
        return deleteOk != null && deleteOk;
    }

}
