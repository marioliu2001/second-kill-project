package com.seckill.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.seckill.common.Constants;
import com.seckill.mapper.GoodsMapper;
import com.seckill.model.Goods;
import com.seckill.service.GoodsService;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description TODO
 * @Date 2022/2/28 9:45
 * @Version 1.0
 */
@Service
public class GoodsServiceImpl implements GoodsService {

    @Resource
    private GoodsMapper goodsMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private AmqpTemplate amqpTemplate;

    /**
     * 实际过成功不能获取所有商品列表
     * 这个方法并发量可能较高，必要时需要用redis来提高效率
     * @return
     */
    @Override
    public List<Goods> getGoodsList() {
        return goodsMapper.selectAll();
    }

    /**
     * 商品详情页面
     * 这个方法并发量可能较高，必要时需要用redis来提高效率
     * @param goodsId
     * @return
     */
    @Override
    public Goods getGoodsInfo(Integer goodsId) {
        return goodsMapper.selectByPrimaryKey(goodsId);
    }

    /**
     * 秒杀主业务方法，主要完成减少库存下单，控制重复购买的=等逻辑，由于并发量过高
     * 不能直接操作数据库，需要利用redis和mq完成
     *
     * @param goodsId
     * @param randomName
     * @param price
     * @return
     * 下单结果
     * 0 表示下单成功
     * 1 表示商品信息异常
     * 2 表示没有库存
     * 3 表示重复购买
     */
    @Override
    public int secKill(Integer goodsId, String randomName, BigDecimal price,Integer uid) {
        //定义订单Map集合，存入mq用于通知订单完成数据库下单，以及存入到redis中防止掉单
        Map orderMap = new HashMap();
        orderMap.put("goodsId",goodsId);
        orderMap.put("uid",uid);
        orderMap.put("buyPrice",price);
        String orderJson = JSONObject.toJSONString(orderMap);
        /**
         * 减少库存，利用redis的事务+key监控解决超卖问题
         */
        for (;;){
            //使用匿名内部类重写execute方法用于使用redis事务，返回执行结果
            Object result = stringRedisTemplate.execute(new SessionCallback<Object>() {
                @Override
                public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                    //定义List集合用于存放需要监控的key，这里需要监控商品库存，以及用户购买记录
                    //用于防止超卖和重复购买
                    List keys = new ArrayList();
                    keys.add(Constants.GOODS_STORE+randomName);
                    keys.add(Constants.BUY_RECODE+goodsId+":"+uid);

                    //设置key监控
                    operations.watch(keys);

                    String store = (String) operations.opsForValue().get(Constants.GOODS_STORE + randomName);

                    if (store==null){
                        //进去if表示商品错误信息
                        operations.unwatch();
                        return 1;
                    }
                    if (Integer.valueOf(store) <= 0) {
                        //进去if表示没有库存
                        operations.unwatch();
                        return 2;
                    }

                    String buyRecode = (String) operations.opsForValue().get(Constants.BUY_RECODE + goodsId + ":" + uid);
                    //进入if表示用户有购买记录
                    if (buyRecode!=null){
                        operations.unwatch();
                        return 3;
                    }
                    //三个if都没进去,开启事务
                    operations.multi();

                    //减库存
                    operations.opsForValue().decrement((K) (Constants.GOODS_STORE + randomName));
                    //添加购买记录
                    operations.opsForValue().set((K)(Constants.BUY_RECODE+goodsId+":"+uid),(V)"1");
                    //使用这个固定的key，使用订单的Json作为value，使用系统毫秒作为分数将数据存入到redis、防止掉单
                    // 这个数据需要利用定时任务来定期扫描，如果存在掉单行为则补发消息到mq中，这个数据只有在完成数据库操作后才删除
                    long sysTime = System.currentTimeMillis();
                    operations.opsForZSet().add((K)Constants.ORDERS,(V)orderJson,sysTime);
                    //执行事务
                    return operations.exec();
                }
            });
            //进入了表示出现了逻辑错误，需要返回
            if (result instanceof Integer){
                return (int) result;
            }
            List list = (List) result;
            //表示提交事务成功，否则尝试再去减少库存
            if (!list.isEmpty()){
                break;
            }
        }
        //程序到这，减少了库存，添加了购买记录
        amqpTemplate.convertAndSend("secKillExchange","", orderJson);
        return 0;
    }
}
