package com.weiheng.secondkill.service.impl;

import com.weiheng.secondkill.kill.domain.KillSuccessUserInfo;
import com.weiheng.secondkill.kill.dto.KillInfoDto;
import com.weiheng.secondkill.kill.mapper.ItemKillSuccessMapper;
import com.weiheng.secondkill.kill.service.IKillService;
import com.weiheng.secondkill.service.RabbitReceiverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
public class RabbitReceiverServiceImpl {
    public static final Logger log = LoggerFactory.getLogger(RabbitReceiverServiceImpl.class);

    @Autowired
    private ItemKillSuccessMapper killSuccessMapper;

    @Autowired
    private IKillService iKillService;

    /*
     * 秒杀异步邮件通知-接收消息
     */
    @RabbitListener(queues = {"${mq.kill.success.mail.queue}"}, containerFactory = "singleListenerContainer")
    public void consumerEmailMsg(KillSuccessUserInfo info) {
        log.info("秒杀成功邮件通知{}", info);
        // TODO:邮件发送，暂不实现
        System.out.println("秒杀成功，邮件发送成功！！！");
    }

    /*
     * 用户秒杀超时未支付
     * */
    @RabbitListener(queues = {"${mq.kill.close.queue}"}, containerFactory = "singleListenerContainer")
    public void consumerExpireOrder(KillSuccessUserInfo info) {
        log.info("超时未支付:{}", info);
        System.out.println("超时未支付，订单关闭:" + info.getCode());
        KillSuccessUserInfo killBean = killSuccessMapper.selectKillSuccessInfo(info.getCode());

        if (killBean != null) {
            int isSuccess = killSuccessMapper.expireOrder(info.getCode());
            if (isSuccess != 1) {
                log.info("订单过期失败！订单信息：{}", info);
            }
        }
    }

    /*
     * 监听队列中的请求消息，进行消息处理
     * */
    @RabbitListener(queues = {"${mq.kill.limit.queue}"}, containerFactory = "multiListenerContainer")
    public void consumeKillExecuteMqMsg(KillInfoDto killInfoDto) {
        if (killInfoDto != null) {
            try {
                iKillService.killItemV4(killInfoDto.getKillItemId(), killInfoDto.getUserId());
            } catch (Exception e) {
                String errMsg = String.format("用户秒杀请求异步处理失败！killItemId:{0}, userId:{1}, 失败原因:{2}", killInfoDto.getKillItemId(), killInfoDto.getUserId(), e.fillInStackTrace());
                log.error(errMsg);
            }
        } else {
            log.error("秒杀请求消息消费异常，异常信息killInfoDto为null");
            throw new RuntimeException("消息消费异常");
        }
    }
}

