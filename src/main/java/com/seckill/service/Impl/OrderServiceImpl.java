package com.seckill.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.seckill.common.Constants;
import com.seckill.mapper.OrdersMapper;
import com.seckill.model.Orders;
import com.seckill.service.OrderService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Date;

/**
 * @Description TODO
 * @Date 2022/2/28 20:34
 * @Version 1.0
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrdersMapper ordersMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public int addSecKill(Orders orders) {
        try{
            System.out.println("我执行了吗???????");
            orders.setBuyNum(1);
            orders.setCreateTime(new Date());
            orders.setStatus(1);
            orders.setOrderMoney(orders.getBuyPrice().multiply(new BigDecimal(orders.getBuyNum())));
            //插入是数据到数据库中，这里可能会抛出异常DuplicateKeyException,表示违反违约唯一约束
            ordersMapper.insert(orders);
            /**
             * 使用key作为key
             * 使用订单数据作为value（必须携带主键）
             * 必须指定超时时间
             */
            String key = Constants.ORDERS_RESULT + orders.getGoodsId() + ":" + orders.getUid();
            Duration timeout = Duration.ofSeconds(60*5);
            stringRedisTemplate.opsForValue().set(key, JSONObject.toJSONString(orders),timeout);

        }
        catch (DuplicateKeyException e){
            //程序执行到这里，表示违反唯一约束，证明从mq中获取的消息是重复的，直接出队即可
            return 0;
        }
        return 0;
    }

    @Override
    public Orders getOrderResult(Integer goodsId, Integer uid) {
        String key = Constants.ORDERS_RESULT + goodsId+ ":" + uid;
        String str = stringRedisTemplate.opsForValue().get(key);
        if (str==null){
            return null;
        }
        return JSONObject.parseObject(str,Orders.class);
    }

}
