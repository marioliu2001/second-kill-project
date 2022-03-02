/**
 * 初始化秒杀函数，主要控制抢购按钮的逻辑
 * @param goodsId 商品ID
 * @param startTime 活动开始时间
 * @param endTime 活动结束时间
 * @param price 商品价格
 */
function initSecKill(goodsId, startTime, endTime, price) {
    //拿到服务端时间，不能单纯的拿到客户端时间（容易被改时间）
    //var nowDate = new Date().getTime();
    //alert(goods+"  "+startTime+"  "+endTime+"  "+price);
    $.get("/getSysTime",
        null,
        function (data) {
            console.log(data);
            //统一错误逻辑控制器
            if (data.code != 0) {
                alert(data.msg);
            }
            var  sysTime = data.result;
            if (sysTime<startTime) {
                //alert("活动还没有开始");
                secKillCountdown(goodsId,startTime,price);
            } 
            if (sysTime>endTime){
                //alert("活动已经结束");
                $("#secKillTip").html("<span style='color:red;'>活动已经结束！</span>");
                return;
            }
            doSecKill(goodsId,price);
            //alert("活动进行中...");
        },
        "json")
}

/**
 * 秒杀倒计时函数
 * @param goodId
 * @param startTime
 * @param price
 */
function secKillCountdown(goodsId,startTime,price) {
    //使用jquery的倒计时插件实现倒计时
    /* + 1000 防止时间偏移 这个没有太多意义，因为我们并不知道客户端和服务器的时间偏移
    这个插件简单了解，实际项目不会以客户端时间作为倒计时的，所以我们在服务器端还需要验证*/
    var killTime = new Date(startTime + 1000);
    //使用任意jquery对象调用倒计时函数
    //参数1 为倒计时的目标时间
    //参数2 为倒计时的回调函数，这个函数每秒钟被调用一次，用于更新页面效果
    $("#seckillTip").countdown(killTime, function (event) {
        //时间格式
        var format = event.strftime('距秒杀开始还有: %D天 %H时 %M分 %S秒');
        $("#secKillTip").html("<span style='color:red;'>"+format+"</span>");
    }).on('finish.countdown', function () {
        //倒计时结束后回调事件，已经开始秒杀，用户可以进行秒杀了，有两种方式：
        //1、刷新当前页面 不能刷新页面，这样一定会出现高并发问题
        //location.reload();
        //或者2、调用秒杀开始的函数
        doSecKill(goodsId,price);
    });
}

/**
 *  准备开始秒杀，主要为按钮绑定点击事件
 * @param goodId
 * @param price
 */
function doSecKill(goodsId, price) {
    $("#seckillTip").html("");
    $("#secKillBtn").attr("disabled",false);
    $("#secKillBtn").bind("click",function () {
        //设置按钮不可用，防止用户重复提交购买请求,但是这里不能百分之百拦截所有请求
        $("#secKillBtn").attr("disabled",true);
        $.get("/getRandomName",
            {
                goodsId:goodsId
            },
            function (data) {
                console.log(data);
                //统一错误逻辑控制器
                if (data.code != 0) {
                    alert(data.msg);
                    return;
                }
                secKill(goodsId,data.result,price);
            },
            "json")
    })
}

/**
 * 发送秒杀请求
 * @param goodsId
 * @param randomName 随机名
 * @param price
 */
function secKill(goodsId,randomName,price) {

    $.get("/secKill",
        {
            goodsId: goodsId,
            randomName: randomName,
            price: price
        },
        function (data) {
            console.log(data);
            //统一错误逻辑控制器
            if (data.code != 0) {
                alert(data.msg);
                return;
            }
            //alert(data.code+"  "+data.msg+"  "+data.result)
            getOrderResult(goodsId);
        },
        "json");
}

/**
 * 获取订单结果，现实支付信息
 * @param goodId
 */
function getOrderResult(goodsId) {
    $.get("/getOrderResult",
        {
            goodsId: goodsId
        },
        function (data) {
            console.log(data);
            //进入if表示暂时没有订单，需要延迟指定时间后再次尝试获取
            if (data.code != 0) {
                //每间隔某个时间调用某个方法，比较适合无限制的调用，子线程循环调用
                //window.setInterval();
                //递归调用,延迟3s调用再次获取订单数据
                window.setTimeout("getOrderResult("+goodsId+")",3000);
                return;
            }
            var orderId = data.result.id;
            var orderMoney = data.result.orderMoney;
            $("#secKillTip").html("<span style='color:red;'>下单成功,共计"+orderMoney+"元&nbsp;&nbsp;<a href='/toPay?orderId="+orderId+"'>立即支付</a></span>")
        },
        "json");
}