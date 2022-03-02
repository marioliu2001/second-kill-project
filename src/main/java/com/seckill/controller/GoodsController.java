package com.seckill.controller;

import com.seckill.common.Constants;
import com.seckill.common.ReturnObject;
import com.seckill.model.Goods;
import com.seckill.service.GoodsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * @Description TODO
 * @Date 2022/2/28 9:42
 * @Version 1.0
 */
@Controller
public class GoodsController {

    @Resource
    private GoodsService goodsService;

    /**
     * 商品列表
     * @param model
     * @return
     */
    @RequestMapping("/")
    public String goodsList(Model model) {

        List<Goods> goodsList = goodsService.getGoodsList();

        System.out.println(goodsList);

        model.addAttribute("goodsList", goodsList);

        return "index";
    }

    /**
     * 商品详情页面
     * @param id
     * @param model
     * @return
     */
    @RequestMapping("/showGoodsInfo")
    public String showGoodsInfo(Integer id, Model model) {

        Goods goods = goodsService.getGoodsInfo(id);

        model.addAttribute("goods", goods);

        return "goodsInfo";
    }

    /**
     * 获取服务器时间
     * @return
     */
    @RequestMapping("/getSysTime")
    @ResponseBody
    public Object getSysTime(){
        return new ReturnObject<Long>(Constants.OK,"获取时间成功",System.currentTimeMillis());
    }

    /**
     * 获取随机名
     * @param goodsId
     * @return
     */
    @RequestMapping("getRandomName")
    @ResponseBody
    public Object getRandomName(Integer goodsId){
        Goods goods = goodsService.getGoodsInfo(goodsId);

        //根据主键获取数据
        if (goods==null){
            return new ReturnObject<String>(Constants.ERROR,"商品信息异常",null);
        }
        long sysTime = System.currentTimeMillis();
        //活动还没有开始，原因是用户可能手动拼接的请求
        if (sysTime<goods.getStartTime().getTime()){
            return new ReturnObject<String>(Constants.ERROR,"活动没有没开始",null);
        }
        //活动已经结束，原因是用户手动拼接请求，或者是在抢购页面停留时间过久
        if (sysTime>goods.getEndTime().getTime()){
            return new ReturnObject<String>(Constants.ERROR,"活动已经结束",null);
        }

        return new ReturnObject<String>(Constants.OK,"获取随即名成功",goods.getRandomName());

    }

    @RequestMapping("/secKill")
    @ResponseBody
    public Object secKill(Integer goodsId, String randomName, BigDecimal price) {
        //当前登录用户id
        Integer uid = 1;
        //调用秒杀的业务逻辑控制完成下单，返回下单结果，0表示下单成功
        int result = goodsService.secKill(goodsId, randomName, price,uid);

        switch (result){
            case 0:
                return new ReturnObject<String>(Constants.OK, "下单成功", null);
            case 1:
                return new ReturnObject<String>(Constants.ERROR, "商品信息异常", null);
            case 2:
                return new ReturnObject<String>(Constants.ERROR, "商品已被抢光", null);
            case 3:
                return new ReturnObject<String>(Constants.ERROR, "不能重复购买，请到我的订单中查看", null);
            default:
                return new ReturnObject<String>(Constants.ERROR, "下单失败", null);
        }

    }
}
