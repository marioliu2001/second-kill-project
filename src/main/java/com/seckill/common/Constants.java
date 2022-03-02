package com.seckill.common;

/**
 * @Description TODO 常量类，用于定义系统中的静态常量
 * @Date 2022/2/28 14:45
 * @Version 1.0
 */
public class Constants {
    public static final int OK = 0;//请求成功
    public static final int ERROR = 1;//请求失败

    //商品库存的同一Key前缀
    public static final String GOODS_STORE = "GOODS_STORE";
    //定义用户购买记录的统一Key前缀，防止重复购买
    public static final String BUY_RECODE = "BUY_RECODE";
    //订单备份数据在redis中的key，用于防止掉单
    public static final String ORDERS = "ORDERS";
    //订单结果在redis中的统一key前缀
    public static final String ORDERS_RESULT = "ORDERS_RESULT";
}
