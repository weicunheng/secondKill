package com.weiheng.secondkill.kill.mapper;

import com.weiheng.secondkill.entity.ItemKill;
import com.weiheng.secondkill.kill.domain.KillSuccessUserInfo;
import com.weiheng.secondkill.entity.ItemKillSuccess;

import java.util.List;


public interface ItemKillSuccessMapper {

    // 查询用户抢购数量
    int countByKillAndUid(int killId, int userId);

    // 插入秒杀成功订单
    int insertKillSuccess(ItemKillSuccess itemKillSuccess);

    // 返回秒杀成功订单信息
    KillSuccessUserInfo selectKillSuccessInfo(String orderNo);

    // 订单过期失效
    int expireOrder(String orderNo);

    List<ItemKillSuccess> selectExpireOrders();

    KillSuccessUserInfo selectKillOrder(int killId, int userId);
}
