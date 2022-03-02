package com.seckill.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description TODO
 * @Date 2022/2/28 17:54
 * @Version 1.0
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue secKillQueue(){
        return new Queue("secKillQueue");
    }

    @Bean
    public FanoutExchange secKillExchange(){
        return new FanoutExchange("secKillExchange");
}

    @Bean
    public Binding secKillBind(){
        return new Binding("secKillQueue",Binding.DestinationType.QUEUE,"secKillExchange","",null);
    }
}
