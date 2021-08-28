package com.weiheng.secondkill.service;

import com.weiheng.secondkill.entity.Item;
import com.weiheng.secondkill.entity.ItemKill;

import java.util.List;

public interface IItemService {
    List<ItemKill> getItemList();
    ItemKill getItemDetail(Integer id) throws Exception;
}
