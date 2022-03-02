package com.seckill.controller;

import com.seckill.common.Constants;
import com.seckill.common.ReturnObject;
import com.seckill.model.Orders;
import com.seckill.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * @Description TODO
 * @Date 2022/3/2 12:06
 * @Version 1.0
 */
@Controller
public class OrdersController {

    @Resource
    private OrderService orderService;

    @RequestMapping("/getOrderResult")
    @ResponseBody
    public Object getOrderResult(Integer goodsId){
        Integer uid = 1;
        Orders orders = orderService.getOrderResult(goodsId,uid);
        if (orders==null){
            return new ReturnObject<Orders>(Constants.ERROR,"获取订单失败",null);
        }
        return new ReturnObject<Orders>(Constants.OK,"获取订单成功",orders);
    }

}
