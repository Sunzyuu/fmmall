package com.qfedu.fmmall.controller;

import com.github.wxpay.sdk.WXPay;
import com.qfedu.fmmall.config.MyPayConfig;
import com.qfedu.fmmall.entity.Orders;
import com.qfedu.fmmall.service.OrderService;
import com.qfedu.fmmall.service.UserAddrService;
import com.qfedu.fmmall.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/order")
@Api(value = "订单接口",tags = "订单接口")
@CrossOrigin
public class OrderController {

    @Autowired
    private OrderService orderService;


    @PostMapping("/add")
//    @ApiImplicitParam(dataType = "String",name = "cids", value = "购物车id",required = true)
    public ResultVO add(String cids, @RequestBody Orders order){
        System.out.println("##################");
        System.out.println(cids);
        if(cids.contains("#")){
            cids.replace("#", "");
        }
        System.out.println(cids);
        try {
            Map<String, String> orderInfo = orderService.addOrder(cids, order);
            String orderId = orderInfo.get("orderId");

            if(orderId !=null){
                Map<String , String> data = new HashMap<>();
                data.put("body",orderInfo.get("productNames")); //商品描述
                data.put("out_trade_no",orderId); //使⽤当前⽤户订单的编号作为当前⽀付交易的交易号
                data.put("fee_type","CNY"); //⽀付币种
                data.put("total_fee", "1"); //⽀付⾦额 一分钱
//                data.put("total_fee", order.getActualAmount()*100 + "" ); //⽀付⾦额
                data.put("trade_type","NATIVE"); //交易类型
                data.put("notify_url","http://81.68.252.36:8080/pay/callable"); //设置⽀付完成时的回调⽅法
                WXPay wxPay = new WXPay(new MyPayConfig());
                Map<String, String> resp = wxPay.unifiedOrder(data); //发送请求
                System.out.println(resp);
                String code_url = resp.get("code_url");
                orderInfo.put("code_url", code_url);
                return ResultVO.success(orderInfo);
            }else {
                return ResultVO.failed("订单为空！");
            }
        } catch (SQLException throwables) {
            return ResultVO.failed("添加订单失败！");
        } catch (Exception e) {
            e.printStackTrace();
            return ResultVO.failed("添加订单失败！");
        }
    }

    @GetMapping("/status/{oid}")
    public ResultVO getStatus(@PathVariable("oid") String oid, @RequestHeader("token") String token){
        return orderService.getOrderById(oid);
    }


    // list?userId=15&pageNum=1&limit=5
    @GetMapping("/list")
    public ResultVO getList(String userId,
                            String status,
                            int pageNum,
                            int limit){
        return orderService.listOrders(userId, status, pageNum,limit);

    }


}
