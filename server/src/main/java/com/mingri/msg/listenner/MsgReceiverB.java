package com.mingri.msg.listenner;

import com.mingri.config.RabbitMqConfig;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: mingri31164
 * @CreateTime: 2025/2/5 23:20
 * @ClassName: MsgReceiverB
 * @Version: 1.0
 */

@Component
@RabbitListener(queues = RabbitMqConfig.QUEUE_B)
public class MsgReceiverB {

    @RabbitHandler
    public void processB(List<String> content) {
        String o = (String)content.get(0);
        String n=o.substring(2);
        System.out.println(n);
        System.out.println("接收处理队列B当中的消息HASH： " + content);
    }

}
