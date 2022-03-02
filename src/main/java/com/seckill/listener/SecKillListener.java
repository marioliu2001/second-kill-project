package com.seckill.listener;

import com.alibaba.fastjson.JSONObject;
import com.seckill.common.Constants;
import com.seckill.model.Orders;
import com.seckill.service.OrderService;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Description TODO
 * @Date 2022/2/28 20:35
 * @Version 1.0
 */
@Component
public class SecKillListener {
    @Resource
    private OrderService ordersService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @RabbitListener(bindings = {@QueueBinding(exchange = @Exchange(name = "secKillExchange",type = "fanout"),value = @Queue(name = "secKillQueue"))})
    public void onSecKillMessage(String message){
        System.out.println(message);
        Orders orders= JSONObject.parseObject(message,Orders.class);
        int result=ordersService.addSecKill(orders);
        //进入if表示数据已经存入数据库
        if(result==0){
            //将订单备份数据从Redis中删除
            stringRedisTemplate.opsForZSet().remove(Constants.ORDERS,message);
        }
    }
}
