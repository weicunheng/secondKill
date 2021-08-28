package com.weiheng.secondkill.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.HashMap;

@Configuration
public class RabbitConfig {

    @Autowired
    private CachingConnectionFactory cachingConnectionFactory;

    @Autowired
    private Environment env;


    private final static Logger log = LoggerFactory.getLogger(RabbitConfig.class);

    //单一消费者
    @Bean(name = "singleListenerContainer")
    public SimpleRabbitListenerContainerFactory listenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(cachingConnectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);
        factory.setPrefetchCount(1);
        return factory;
    }

    //多个消费者
    @Bean(name = "multiListenerContainer")
    public SimpleRabbitListenerContainerFactory multiListenerContainer() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(cachingConnectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.NONE); // 确认消息模式-NONE
        factory.setConcurrentConsumers(5);
        factory.setMaxConcurrentConsumers(5);
        factory.setPrefetchCount(5);
        return factory;
    }

    // rabbitmq Template
    @Bean
    public RabbitTemplate rabbitTemplate() {
        // 消息确认
        cachingConnectionFactory.setPublisherConfirms(true);
        cachingConnectionFactory.setPublisherReturns(true);
        RabbitTemplate template = new RabbitTemplate(cachingConnectionFactory);
        // 发送消息时设置强制标志
        template.setMandatory(true);
        template.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                log.info("消息发送成功:correlationData({}),ack({}),cause({})", correlationData, ack, cause);
            }
        });
        template.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                log.warn("消息丢失:exchange({}),route({}),replyCode({}),replyText({}),message:{}", exchange, routingKey, replyCode, replyText, message);
            }
        });
        return template;
    }

    // 指定队列
    @Bean
    public Queue successEmailQueue() {
        return new Queue(env.getProperty("mq.kill.success.mail.queue"));
    }

    // 指定交换机
    @Bean
    public DirectExchange successEmailExchange() {

        return new DirectExchange(env.getProperty("mq.kill.success.mail.exchange"));
    }

    // 绑定交换机
    @Bean
    public Binding successEmailBinding() {
        return BindingBuilder.bind(successEmailQueue()).to(successEmailExchange()).with(env.getProperty("mq.kill.success.mail.routing.key"));
    }


    @Bean
    public Queue killSuccessWaitPayQueue() {
        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", env.getProperty("mq.kill.close.exchange"));
        arguments.put("x-dead-letter-routing-key", env.getProperty("mq.kill.close.routing.key"));
        return new Queue(env.getProperty("mq.kill.wait_pay.queue"), true, false, false, arguments);
    }

    // 指定死信队列
    @Bean
    public DirectExchange killSuccessWaitPayExchange() {
        return new DirectExchange(env.getProperty("mq.kill.wait_pay.exchange"), true, false);
    }

    // 死信队列和交换机绑定
    @Bean
    public Binding killSuccessWaitPayBinding() {
        return BindingBuilder.bind(killSuccessWaitPayQueue()).to(killSuccessWaitPayExchange()).with(env.getProperty("mq.kill.wait_pay.routing.key"));
    }


    /*
     *  超时未支付、待关闭的订单
     * */
    @Bean
    public Queue waitPayQueue() {
        return new Queue(env.getProperty("mq.kill.close.queue"), true);
    }

    @Bean
    public DirectExchange waitPayExchange() {
        return new DirectExchange(env.getProperty("mq.kill.close.exchange"), true, false);
    }

    @Bean
    public Binding waitPayBinding() {
        return BindingBuilder.bind(waitPayQueue()).to(waitPayExchange()).with(env.getProperty("mq.kill.close.routing.key"));
    }


}
