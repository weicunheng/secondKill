package com.weiheng.secondkill.service;

import com.weiheng.secondkill.kill.dto.KillInfoDto;

public interface RabbitPublisherService {

    /*
    *  秒杀成功异步发送邮件通知消息
    * */

    public void SecKillSuccessEmailNotify(String orderNo);


    /*
    * 秒杀成功后生成抢购订单-发送信息入死信队列，等待着一定时间失效超时未支付的订单
    * */
    public void sendKillSuccessOrderExpireMsg(String orderNo);

    /*
    * 秒杀时异步发送Mq消息
    * */

    public void sendKillExecuteMqMsg(KillInfoDto killInfoDto);
}
