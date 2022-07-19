package com.qfedu.fmmall.controller;

import com.github.wxpay.sdk.WXPayUtil;
import com.qfedu.fmmall.entity.Orders;
import com.qfedu.fmmall.service.OrderService;
import com.qfedu.fmmall.vo.ResultVO;
import com.qfedu.fmmall.websocket.WebSocketServer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.websocket.Session;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
@Api(value = "用户地址接口", tags = "用户地址管理")
@CrossOrigin
public class PayController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/callable")
    public String success(HttpServletRequest request) throws Exception {
        ServletInputStream is = request.getInputStream();
        byte[] bytes = new byte[1024];
        int len = -1;
        StringBuilder builder = new StringBuilder();
        while ((len = is.read(bytes)) != -1) {
            builder.append(new String(bytes, 0, len));
        }
        String s = builder.toString();
        // 使用wxpay的工具类讲xml的响应结果 转换成map
        Map<String, String> map = WXPayUtil.xmlToMap(s);
        if (map != null && "success".equalsIgnoreCase(map.get("result_code"))) {
            // 支付成功
            // 修改订单状态为代发货/已支付
            String orderId = map.get("out_trade_no");
            int i = orderService.updateOrderStatus(orderId, "2");
            // 通过websocket向前端发送消息
            WebSocketServer.sendMsg(orderId,"1");

            if (i > 0) {
                // 响应微信平台
                HashMap<String, String> resp = new HashMap<>();
                resp.put("return_code", "success");
                resp.put("return_msg", "OK");
                resp.put("appid", map.get("appid"));
                resp.put("result_code", "success");
                return WXPayUtil.mapToXml(resp);
            }
        }
        return null;
    }

}
