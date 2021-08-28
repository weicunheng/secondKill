package com.weiheng.secondkill.service.impl;

import com.weiheng.secondkill.auth.mapper.UserMapper;
import com.weiheng.secondkill.kill.domain.KillSuccessUserInfo;
import com.weiheng.secondkill.kill.dto.KillInfoDto;
import com.weiheng.secondkill.kill.mapper.ItemKillSuccessMapper;
import com.weiheng.secondkill.service.RabbitPublisherService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.AbstractJavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Primary
@Service
public class RabbitPublisherServiceImpl implements RabbitPublisherService {
    public static final Logger log = LoggerFactory.getLogger(RabbitPublisherServiceImpl.class);

    @Autowired
    private Environment env;

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    /*
     * 秒杀成功异步发送邮件通知消息
     * */
    @Override
    public void SecKillSuccessEmailNotify(String orderNo) {
        if (StringUtils.isNotBlank(orderNo)) {
            // 1. 根据orderNo查询用户信息、秒杀商品信息
            KillSuccessUserInfo info = itemKillSuccessMapper.selectKillSuccessInfo(orderNo);
            if (info != null) {

                // 设置消息转换器, 在发送消息的时候，正常情况下消息体为二进制的数据
                rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());

                // 2. 选择路由key和交换机
                rabbitTemplate.setExchange(env.getProperty("mq.kill.success.mail.exchange"));
                rabbitTemplate.setRoutingKey(env.getProperty("mq.kill.success.mail.routing.key"));

                // 3. 发送消息
                rabbitTemplate.convertAndSend(info, message -> {
                    MessageProperties processor = message.getMessageProperties();
                    processor.setDeliveryMode(MessageDeliveryMode.PERSISTENT); // 将消息持久化
                    processor.setHeader(AbstractJavaTypeMapper.DEFAULT_CONTENT_CLASSID_FIELD_NAME, KillSuccessUserInfo.class);
                    return message;
                });
            }

        }
    }

    /*
     * 秒杀成功，把生成的秒杀单发送到死信队列
     * */
    @Override
    public void sendKillSuccessOrderExpireMsg(String orderNo) {
        if (StringUtils.isNotBlank(orderNo)) {
            KillSuccessUserInfo info = itemKillSuccessMapper.selectKillSuccessInfo(orderNo);
            if (info != null) {

                // 设置消息转换器
                rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());

                // 2. 选择路由key和交换机
                rabbitTemplate.setExchange(env.getProperty("mq.kill.wait_pay.exchange"));
                rabbitTemplate.setRoutingKey(env.getProperty("mq.kill.wait_pay.routing.key"));

                // 3. 发送消息
                System.out.println("订单来了... info:" + info);
                rabbitTemplate.convertAndSend(info, message -> {
                    MessageProperties processor = message.getMessageProperties();
                    processor.setDeliveryMode(MessageDeliveryMode.PERSISTENT); // 将消息持久化
                    processor.setHeader(AbstractJavaTypeMapper.DEFAULT_CONTENT_CLASSID_FIELD_NAME, KillSuccessUserInfo.class);
                    processor.setExpiration("10000");
                    return message;
                });
            }
        }
    }

    @Override
    public void sendKillExecuteMqMsg(final KillInfoDto killInfoDto) {
        if (killInfoDto != null) {
            //1.设置消息转换器
            rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
            //2.指定交换机
            rabbitTemplate.setExchange(env.getProperty("mq.kill.limit.exchange"));
            //3.指定routingkey
            rabbitTemplate.setRoutingKey("mq.kill.limit.routing.key");
            //4.发送消息
            rabbitTemplate.convertAndSend(killInfoDto, new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    MessageProperties prop = message.getMessageProperties();
                    prop.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    prop.setHeader(AbstractJavaTypeMapper.DEFAULT_CONTENT_CLASSID_FIELD_NAME, KillInfoDto.class);
                    return message;
                }
            });
        } else {
            throw new RuntimeException("秒杀信息获取失败，请重新执行秒杀操作");
        }


    }
}
