package com.weiheng.secondkill.service;

import com.weiheng.secondkill.entity.ItemKillSuccess;
import com.weiheng.secondkill.kill.mapper.ItemKillSuccessMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleService {

    private static final Logger log = LoggerFactory.getLogger(ScheduleService.class);

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    @Scheduled(cron = "0 */1 * * * ?")
    public void scheduleExpireOrder() {
        System.out.println("定时任务已经开启.......");
        List<ItemKillSuccess> itemKillSuccesses = itemKillSuccessMapper.selectExpireOrders();
        if (itemKillSuccesses != null && !itemKillSuccesses.isEmpty()) {
            itemKillSuccesses.stream().forEach(i -> {
                if (i != null && i.getDiffTime() > 10) {
                    itemKillSuccessMapper.expireOrder(i.getCode());
                }
            });
        }
        System.out.println("定时任务执行结束开启.......");
    }
}
