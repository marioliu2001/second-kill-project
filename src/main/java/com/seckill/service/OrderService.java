package com.seckill.service;

import com.seckill.model.Orders;

public interface OrderService {
    int addSecKill(Orders orders);

    Orders getOrderResult(Integer goodsId, Integer uid);
}
