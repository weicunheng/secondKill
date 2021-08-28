package com.weiheng.secondkill.controller;

import com.weiheng.secondkill.entity.ItemKill;
import com.weiheng.secondkill.enums.StatusCode;
import com.weiheng.secondkill.response.BaseResponse;
import com.weiheng.secondkill.service.IItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/item")
public class ItemController {

    @Autowired
    private IItemService itemService;

    @ResponseBody
    @RequestMapping(value = "/list/", method = RequestMethod.GET)
    public BaseResponse<List<ItemKill>> getItemKillList() {

        BaseResponse<List<ItemKill>> response = new BaseResponse<List<ItemKill>>(StatusCode.SUCCESS);
        response.setData(itemService.getItemList());
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/detail/{id}/", method = RequestMethod.GET)
    public BaseResponse<ItemKill> detail(@PathVariable Integer id) {
        ItemKill itemDetail = null;
        BaseResponse<ItemKill> response = null;
        try {
            itemDetail = itemService.getItemDetail(id);
            response = new BaseResponse<>(StatusCode.SUCCESS);
            response.setData(itemDetail);
        } catch (Exception e) {
            response = new BaseResponse<>(StatusCode.FAIL);
            response.setMsg("获取秒杀详情-待秒杀商品记录不存在");
        }
        return response;
    }

}
