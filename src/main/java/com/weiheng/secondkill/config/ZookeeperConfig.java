package com.weiheng.secondkill.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;


/*
 * Zookeeper组件自定义配置
 * */
@Configuration
public class ZookeeperConfig {
    @Autowired
    private Environment env;

    /*
     * 自定义注入zookeeper客户端实例
     * */

    @Bean
    public CuratorFramework curatorFramework() {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
                .connectString(env.getProperty("zk.host"))
                .namespace("zk.namespace")
                .retryPolicy(new RetryNTimes(5, 1000)).build();

        curatorFramework.start();
        return curatorFramework;
    }

}
