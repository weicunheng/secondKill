package com.weiheng.secondkill.kill.mapper;

import com.weiheng.secondkill.entity.ItemKill;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

public interface ItemKillMapper {
    /*
     * 秒杀商品列表
     * */
    List<ItemKill> selectAll();

    /*
     * 获取单个秒杀商品
     * */
    ItemKill selectOne(int id);


    /*
     * 减库存
     * */
    int updateKillItem(int killId);

    ItemKill selectOneV2(Integer killId);

    int updateKillItemV2(Integer killId);
}
