package com.seckill.timer;

import com.seckill.common.Constants;
import com.seckill.model.Goods;
import com.seckill.service.GoodsService;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * @Description TODO
 * @Date 2022/2/28 15:43
 * @Version 1.0
 */
@EnableScheduling
@Component
public class SecKillTimer {

    @Resource
    private GoodsService goodsService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private AmqpTemplate amqpTemplate;

    /**
     * 配置定时任务，每秒钟执行一次初始化秒杀商品到redis中
     * 实际工作时，不能每秒钟一次，应该在活动开始前或每天的固定时间
     * 为了将未来即将开始活动的商品初始化redis中
     */
    @Scheduled(cron = "* * * * * *")
    public void initSecKillDataToRedis() {
        //获取所以的商品列表，实际工作是应该根据系统时间，获取即将开始的商品列表
        List<Goods> goodsList = goodsService.getGoodsList();
        goodsList.forEach(goods -> {
            //使用同一的key前缀+商品随即名作为key，使用商品库存作为value初始化商品信息到redis中
            stringRedisTemplate.opsForValue().setIfAbsent(Constants.GOODS_STORE+goods.getRandomName(),goods.getStore()+"");
        });
    }

    /**
     * 配置定时任务每秒钟执行一次，用于扫描redis中的订单数据判断是否存在掉单行为
     * 实际工作不能使5秒钟一次，应该是更长时间例如5分钟或15分钟
     */
    @Scheduled(cron = "0/5 * * * * *")
    public void diaoDan(){
        //计算当前系统时间5分钟之前的毫秒值,作为获取数据的最大分数
        long score=System.currentTimeMillis()-1000*60*5;
        Set<String> orderSet=stringRedisTemplate.opsForZSet().rangeByScore(Constants.ORDERS,0,score);
        orderSet.forEach(orderStr->{
            //将可能掉单的数据补单到MQ中
            amqpTemplate.convertAndSend("secKillExchange","",orderStr);
        });
    }
}
