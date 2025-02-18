package com.mingri.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.amqp.core.Queue;

/**
 * @Author: mingri31164
 * @CreateTime: 2025/2/5 23:13
 * @ClassName: RabbitMqConfig
 * @Version: 1.0
 */

@Configuration
@ConfigurationProperties(prefix = "spring.rabbit")
public class RabbitMqConfig {

    private   String host;
    private  int port;
    private  String username;
    private  String password;

    public static final String EXCHANGE_A = "my-mq-exchange_A";
    public static final String EXCHANGE_B = "my-mq-exchange_B";


    public static final String QUEUE_A = "QUEUE_A";
    public static final String QUEUE_B = "QUEUE_B";

    public static final String ROUTINGKEY_A = "spring-boot-routingKey_A";
    public static final String ROUTINGKEY_B = "spring-boot-routingKey_B";

    //创建连接工厂
    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host,port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost("/");
        connectionFactory.setPublisherConfirms(true);
        return connectionFactory;
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    //必须是prototype类型
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory());
        return template;
    }

    /**
     * * 针对消费者配置
     *      * 1. 设置交换机类型
     *      * 2. 将队列绑定到交换机
     *      FanoutExchange: 将消息分发到所有的绑定队列，无routingkey的概念
     *      HeadersExchange ：通过添加属性key-value匹配
     *      DirectExchange:按照routingkey分发到指定队列
     *      TopicExchange:多关键字匹配
     */
    //声明交换机
    @Bean
    public DirectExchange defaultExchange() {
        return new DirectExchange(EXCHANGE_A);
    }
    @Bean
    public DirectExchange defaultExchange1() {
        return new DirectExchange(EXCHANGE_B);
    }

    //声明队列
    @Bean
    public Queue queueA() {
        return new Queue(QUEUE_A, true); //队列持久：不会随着服务器重启造成丢失
    }
    @Bean
    public Queue queueB() {
        return new Queue(QUEUE_B, true); //队列持久
    }

    //队列绑定交换机，指定routingkey
    @Bean
    public Binding binding() {
        //绑定队列到交换机上通过路由
        return BindingBuilder.bind(queueA()).to(defaultExchange()).with(RabbitMqConfig.ROUTINGKEY_A);
    }

    @Bean
    public Binding bindingb() {
        return BindingBuilder.bind(queueB()).to(defaultExchange1()).with(RabbitMqConfig.ROUTINGKEY_B);
    }

}
