package com.qfedu.fmmall.service.job;

import com.github.wxpay.sdk.WXPay;
import com.qfedu.fmmall.dao.OrderItemMapper;
import com.qfedu.fmmall.dao.OrdersMapper;
import com.qfedu.fmmall.dao.ProductSkuMapper;
import com.qfedu.fmmall.entity.OrderItem;
import com.qfedu.fmmall.entity.Orders;
import com.qfedu.fmmall.entity.ProductSku;
import com.qfedu.fmmall.service.OrderService;
import com.qfedu.fmmall.service.impl.OrderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OrderTimeoutCheckJob {

    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private OrderService orderService;

    private WXPay wxPay = new WXPay(new MyPayConfig());



    @Scheduled(cron = "0/60 * * * * ?")
    public void checkAndCloseOrder() {
        // 首先查询已失效的未支付订单
        // 订单的有效支付时间是半小时，因此判断订单是否失效，就是从当前时间下，向前推半个小时，
        // 如果订单的创建时间不在此范围内则属于失效的订单
        // 需要注意的是，在数据库查询到是 未支付 状态的不一定就一定是未支付，因为支付平台支付成功对服务器响应过程中可能
        // 出现意想不到的问题，所以在修改订单状态之前一定要向支付平台确认此订单的状态，若仍然是 未支付 则取消订单
        // 取消订单后需要修改订单状态 为支付失败(6)，并向支付平台通知取消支付链接
        // 取消订单后，需要恢复商品的库存，就是ProductSku中的stock + OrderItem(订单快照)中的buy_conuts
        // 这里需要考虑到数据库的并发问题，需要加锁和事务管理
        // 查询超过三十分钟未支付订单

        try{
            System.out.println("1——————————————————————----1");
            Example example = new Example(Orders.class);
            Example.Criteria criteria = example.createCriteria();
            Date time = new Date(System.currentTimeMillis() - 30 * 60 * 1000);
            criteria.andLessThan("createTime", time);
            List<Orders> orders = ordersMapper.selectByExample(example);


            //2.访问微信平台接口，确认当前订单最终的支付状态
            for (Orders order : orders) {
                HashMap<String, String> params = new HashMap<>();
                params.put("out_trade_no", order.getOrderId());
                // 使用微信支付提供发接口查询订单的支付状态
                Map<String, String> resp = wxPay.orderQuery(params);
                System.out.println(resp);
                if ("SUCCESS".equalsIgnoreCase(resp.get("trade_state"))) {
                    //2.1 如果订单已经支付，则修改订单状态为"代发货/已支付"  status = 2
                    Orders updateOrder = new Orders();
                    updateOrder.setOrderId(order.getOrderId());
                    updateOrder.setStatus("2");
                    ordersMapper.updateByPrimaryKeySelective(updateOrder);
                } else if ("NOTPAY".equalsIgnoreCase(resp.get("trade_state"))) {

                    //2.2 如果确实未支付 则取消订单：
                    //  a.向微信支付平台发送请求，关闭当前订单的支付链接
                    Map<String, String> map = wxPay.closeOrder(params);
                    System.out.println(map);
                    // b.关闭订单
                    orderService.closeOrder(order.getOrderId());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }


//    @Scheduled(cron = "0/60 * * * * ?")
//    public void checkAndCloseOrder() {
//        System.out.println("-------------------11");
//        try {
//            //1.查询超过30min订单状态依然为待支付状态的订单
//            Example example = new Example(Orders.class);
//            Example.Criteria criteria = example.createCriteria();
//            criteria.andEqualTo("status", "1");
//            Date time = new Date(System.currentTimeMillis() - 30 * 60 * 1000);
//            criteria.andLessThan("createTime", time);
//            List<Orders> orders = ordersMapper.selectByExample(example);
//
//            //2.访问微信平台接口，确认当前订单最终的支付状态
//            for (int i = 0; i < orders.size(); i++) {
//                Orders order = orders.get(i);
//                HashMap<String, String> params = new HashMap<>();
//                params.put("out_trade_no", order.getOrderId());
//
//                Map<String, String> resp = wxPay.orderQuery(params);
//
//                if("SUCCESS".equalsIgnoreCase(resp.get("trade_state"))){
//                    //2.1 如果订单已经支付，则修改订单状态为"代发货/已支付"  status = 2
//                    Orders updateOrder = new Orders();
//                    updateOrder.setOrderId(order.getOrderId());
//                    updateOrder.setStatus("2");
//                    ordersMapper.updateByPrimaryKeySelective(updateOrder);
//                }else if("NOTPAY".equalsIgnoreCase(resp.get("trade_state"))){
//                    //2.2 如果确实未支付 则取消订单：
//                    //  a.向微信支付平台发送请求，关闭当前订单的支付链接
//                    Map<String, String> map = wxPay.closeOrder(params);
//                    System.out.println(map);
//
//                    // b.关闭订单
////                    orderService.closeOrder(order.getOrderId());
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


}
