package com.weiheng.secondkill.service.impl;

import com.weiheng.secondkill.entity.ItemKill;
import com.weiheng.secondkill.kill.mapper.ItemKillMapper;
import com.weiheng.secondkill.service.IItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;


@Primary
@Service
public class ItemServiceImpl implements IItemService {

    @Autowired
    private ItemKillMapper itemKillMapper;

    @Override
    public List<ItemKill> getItemList() {
        return itemKillMapper.selectAll();
    }

    @Override
    public ItemKill getItemDetail(Integer id) throws Exception {
        ItemKill itemKillDetail = itemKillMapper.selectOne(id);
        if(itemKillDetail == null){
            throw new Exception("获取秒杀详情-待秒杀商品记录不存在");
        }
        return itemKillDetail;
    }
}
