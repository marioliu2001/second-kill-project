package com.seckill.service;

import com.seckill.model.Goods;

import java.math.BigDecimal;
import java.util.List;

public interface GoodsService {
    List<Goods> getGoodsList();

    Goods getGoodsInfo(Integer id);

    int secKill(Integer goodsId, String randomName, BigDecimal price,Integer uid);

}
