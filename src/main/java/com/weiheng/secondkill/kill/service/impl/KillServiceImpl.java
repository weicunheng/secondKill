package com.weiheng.secondkill.kill.service.impl;

import com.weiheng.secondkill.common.component.RedisDistributeLock;
import com.weiheng.secondkill.entity.ItemKill;
import com.weiheng.secondkill.entity.ItemKillSuccess;
import com.weiheng.secondkill.kill.constants.OrderStatus;
import com.weiheng.secondkill.kill.mapper.ItemKillMapper;
import com.weiheng.secondkill.kill.mapper.ItemKillSuccessMapper;
import com.weiheng.secondkill.kill.service.IKillService;
import com.weiheng.secondkill.service.RabbitPublisherService;
import com.weiheng.secondkill.utils.SnowFlake;
import jodd.datetime.TimeUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.joda.time.DateTime;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Primary
@Service
public class KillServiceImpl implements IKillService {


    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    @Autowired
    private ItemKillMapper itemKillMapper;

    @Autowired
    private RabbitPublisherService rabbitPublisherService;

    @Autowired
    private RedisDistributeLock redisDistributeLock;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private CuratorFramework curatorFramework;

    private SnowFlake snowFlake = new SnowFlake(0, 0);

    public int createKillSuccessOrder(ItemKill itemKill, Integer userId) {
        // 创建 秒杀成功订单
        String orderNo = String.valueOf(snowFlake.nextId());
        ItemKillSuccess killSuccessOrder = new ItemKillSuccess();
        killSuccessOrder.setCode(orderNo);
        killSuccessOrder.setItemId(itemKill.getItemId());
        killSuccessOrder.setKillId(itemKill.getId());
        killSuccessOrder.setUserId(userId.toString());
        killSuccessOrder.setStatus(OrderStatus.WAIT_PAY.byteValue());
        killSuccessOrder.setCreateTime(DateTime.now().toDate());

        // 双重校验
        if (itemKillSuccessMapper.countByKillAndUid(itemKill.getId(), userId) <= 0) {
            // 1. 创建订单
            itemKillSuccessMapper.insertKillSuccess(killSuccessOrder);
            // 2. 邮件异步通知付款
            rabbitPublisherService.SecKillSuccessEmailNotify(orderNo);
            // 3. 入死信队列， 用于 “失效” 超时关闭指定时间未支付的订单
            rabbitPublisherService.sendKillSuccessOrderExpireMsg(orderNo);
        }
        return 0;
    }

    @Override
    public Boolean killItem(Integer killId, Integer userId) throws Exception {
        boolean result = false;
        // 1. 判断用户是否已抢购
        int count = itemKillSuccessMapper.countByKillAndUid(killId, userId);
        if (count > 0) {
            throw new Exception("您已经抢购过该商品了！");
        }
        // 2. 判断商品库存 及 商品是否可抢购
        ItemKill itemKill = itemKillMapper.selectOne(killId);
        if (itemKill != null && itemKill.getCanKill() == 1) {
            //TODO: 扣减库存-减1
            int ret = itemKillMapper.updateKillItem(killId);
            if (ret > 0) {
                //TODO:判断是否扣减成功了?是-生成秒杀成功的订单、同时通知用户秒杀已经成功（在一个通用的方法里面实现）
                this.createKillSuccessOrder(itemKill, userId);
                result = true;
            }
        }
        return result;
    }

    @Override
    public Boolean killItemV2(Integer killId, Integer userId) throws Exception {
        boolean result = false;

        // 1. 查询用户是否已经抢购了
        int count = itemKillSuccessMapper.countByKillAndUid(killId, userId);
        if (count > 0) {
            throw new Exception("您已经抢购过该商品了！");
        }


        // 2. 判断商品库存是否够, 增加了可秒杀商品的数量限制
        ItemKill itemKill = itemKillMapper.selectOneV2(killId);

        if (null != itemKill && 1 == itemKill.getCanKill() && itemKill.getTotal() > 0) {
            // 扣减库存-减一 除了保证证件减1之外，还要保证扣完之后的数量大于等于0
            int ret = itemKillMapper.updateKillItemV2(killId);
            if (ret > 0) {
                //TODO:判断是否扣减成功了?是-生成秒杀成功的订单、同时通知用户秒杀已经成功（在一个通用的方法里面实现）
                this.createKillSuccessOrder(itemKill, userId);
                result = true;
            }
        }
        return result;
    }

    @Override
    public Boolean killItemV3(Integer killId, Integer userId) throws Exception {
        /*
         *
         * */
        String key = String.format("{0}#{1}#lock", killId, userId);
        boolean lock = redisDistributeLock.getLock(key, 30);
        if (lock) {
            ItemKill itemKill = itemKillMapper.selectOneV2(killId);
            if (null != itemKill && 1 == itemKill.getCanKill() && itemKill.getTotal() > 0) {
                int result = itemKillMapper.updateKillItemV2(killId);
                if (result > 0) {
                    this.createKillSuccessOrder(itemKill, userId);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Boolean killItemV4(Integer killId, Integer userId) throws Exception {
        /*
         * 1. 尝试获取锁
         * 2. 获取到锁之后，cacheRes=true， 即可进入秒杀业务逻辑， 同时处理完成后需要释放锁
         * 3.基于Redisson的分布式锁解决高并发业务场景下，并发多线程对于共享资源/共享代码块的并发访问所出现的并发安全的问题的代码实战已经完毕了！
         * */
        String key = String.format("{0}#{1}#lock", killId, userId);
        RLock lock = redissonClient.getLock(key);
        try {
            boolean b = lock.tryLock(30, 10, TimeUnit.SECONDS);
            if (b) {
                ItemKill itemKill = itemKillMapper.selectOneV2(killId);
                if (null != itemKill && 1 == itemKill.getCanKill() && itemKill.getTotal() > 0) {
                    int result = itemKillMapper.updateKillItemV2(killId);
                    if (result > 0) {
                        this.createKillSuccessOrder(itemKill, userId);
                        return true;
                    }
                }
            }
        } finally {
            lock.unlock();
        }

        return false;
    }

    @Override
    public Boolean killItemV5(Integer killId, Integer userId) throws Exception {
        /*
        基于zookeeper实现分布式锁
        1. 定义获取分布式锁的操作组件
        2. 尝试获取分布式锁
        3. 释放分布式锁
         * */
        String key = String.format("{0}#{1}#lock", killId, userId);
        InterProcessMutex mutex = new InterProcessMutex(curatorFramework, key);
        try {

            if (mutex.acquire(10L, TimeUnit.SECONDS)) {
                ItemKill itemKill = itemKillMapper.selectOneV2(killId);
                if (null != itemKill && 1 == itemKill.getCanKill() && itemKill.getTotal() > 0) {
                    int result = itemKillMapper.updateKillItemV2(killId);
                    if (result > 0) {
                        this.createKillSuccessOrder(itemKill, userId);
                        return true;
                    }
                }
            }
        } finally {
            if (mutex != null) {
                mutex.release();
            }
        }

        return false;
    }

    @Override
    public Map<String, Object> checkUserKillResult(Integer killId, Integer userId) {
        itemKillSuccessMapper.selectKillOrder(killId, userId);
        return null;
    }


}
