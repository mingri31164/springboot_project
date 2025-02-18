package com.mingri.msg.send;

import com.mingri.config.RabbitMqConfig;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

/**
 * @Author: mingri31164
 * @CreateTime: 2025/2/5 23:18
 * @ClassName: RabbitmqSend
 * @Version: 1.0
 */
public class RabbitmqSend implements RabbitTemplate.ConfirmCallback{

    //由于rabbitTemplate的scope属性设置为ConfigurableBeanFactory.SCOPE_PROTOTYPE，所以不能自动注入
    private RabbitTemplate rabbitTemplate;
    /**
     * 构造方法注入rabbitTemplate,config的
     */
    @Autowired
    public RabbitmqSend(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        rabbitTemplate.setConfirmCallback(this); //rabbitTemplate如果为单例的话，那回调就是最后设置的内容
    }

    public void sendMsg(List<String> content) {
        CorrelationData correlationId = new CorrelationData(UUID.randomUUID().toString());
        //把消息放入ROUTINGKEY_A对应的队列当中去，对应的是队列A
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_A, RabbitMqConfig.ROUTINGKEY_A, content, correlationId);
    }
    public void sendMsgB(List<String> content) {
        CorrelationData correlationId = new CorrelationData(UUID.randomUUID().toString());
        //把消息放入ROUTINGKEY_B对应的队列当中去，对应的是队列B
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_B, RabbitMqConfig.ROUTINGKEY_B, content, correlationId);
    }

    /**
     * 回调
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        System.out.println(" 回调id:" + correlationData);
        if (ack) {
            System.out.println("消息成功消费");

        } else {
            System.out.println("消息消费失败:" + cause);
        }
    }

}
