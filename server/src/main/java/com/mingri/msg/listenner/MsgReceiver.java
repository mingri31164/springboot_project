package com.mingri.msg.listenner;

import com.mingri.config.RabbitMqConfig;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: mingri31164
 * @CreateTime: 2025/2/5 23:20
 * @ClassName: MsgReceiver
 * @Version: 1.0
 */

@Component
@RabbitListener(queues = RabbitMqConfig.QUEUE_A)
public class MsgReceiver {
    @RabbitHandler
    public void process(List<String> content) {
        System.out.println("接收处理队列A当中的消息HASH： " + content);
    }

}
