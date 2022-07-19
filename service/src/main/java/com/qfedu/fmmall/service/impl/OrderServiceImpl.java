package com.qfedu.fmmall.service.impl;

import com.qfedu.fmmall.dao.OrderItemMapper;
import com.qfedu.fmmall.dao.OrdersMapper;
import com.qfedu.fmmall.dao.ProductSkuMapper;
import com.qfedu.fmmall.dao.ShoppingCartMapper;
import com.qfedu.fmmall.entity.*;
import com.qfedu.fmmall.service.OrderService;
import com.qfedu.fmmall.utils.PageHelper;
import com.qfedu.fmmall.vo.ResultVO;
import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private ShoppingCartMapper shoppingCartMapper;

    @Resource
    private OrdersMapper ordersMapper;

    @Resource
    private ProductSkuMapper productSkuMapper;

    @Resource
    private OrderItemMapper orderItemMapper;


    private Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    /**
     * 保存订单业务
     * @param cids
     * @param order
     * @return
     * @throws SQLException
     */
    @Transactional
    public Map<String, String> addOrder(String cids, Orders order) throws SQLException {

        Map<String,String> map = new HashMap<>();

        //1.校验库存：根据cids查询当前订单中关联的购物车记录详情（包括库存）
        String[] arr = cids.split(",");
        List<Integer> cidsList = new ArrayList<>();
        for (int i = 0; i <arr.length ; i++) {
            cidsList.add(Integer.parseInt(arr[i]));
        }
        List<ShoppingCartVO> list = shoppingCartMapper.selectShopcartByCids(cidsList);

        boolean f = true;
        String untitled = "";
        for (ShoppingCartVO sc: list) {
            if(Integer.parseInt(sc.getCartNum()) > sc.getSkuStock()){
                f = false;
            }
            //获取所有商品名称，以,分割拼接成字符串
            untitled = untitled+sc.getProductName()+",";
        }

        if(f){
//            System.out.println("-----库存校验完成");
            //2.保存订单
            order.setUntitled(untitled);
            order.setCreateTime(new Date());
            order.setStatus("1");
            //生成订单编号
            String orderId = UUID.randomUUID().toString().replace("-", "");
            order.setOrderId(orderId);
            int i = ordersMapper.insert(order);

            //3.生成商品快照
            for (ShoppingCartVO sc: list) {
                int cnum = Integer.parseInt(sc.getCartNum());
                String itemId = System.currentTimeMillis()+""+ (new Random().nextInt(89999)+10000);
                OrderItem orderItem = new OrderItem(itemId, orderId, sc.getProductId(), sc.getProductName(), sc.getProductImg(), sc.getSkuId(), sc.getSkuName(), new BigDecimal(sc.getSellPrice()), cnum, new BigDecimal(sc.getSellPrice() * cnum), new Date(), new Date(), 0);
                orderItemMapper.insert(orderItem);
                //增加商品销量
            }

            //4.扣减库存：根据套餐ID修改套餐库存量
            for (ShoppingCartVO sc: list) {
                String skuId = sc.getSkuId();
                int newStock = sc.getSkuStock()- Integer.parseInt(sc.getCartNum());

                ProductSku productSku = new ProductSku();
                productSku.setSkuId(skuId);
                productSku.setStock(newStock);
                productSkuMapper.updateByPrimaryKeySelective(productSku);
            }

            //5.删除购物车：当购物车中的记录购买成功之后，购物车中对应做删除操作
            for (int cid: cidsList) {
                shoppingCartMapper.deleteByPrimaryKey(cid);
            }
            map.put("orderId",orderId);
            map.put("productNames",untitled);
            return map;
        }else{
            //表示库存不足
            return null;
        }
//        //处理cids
//        String[] strings = cids.split(",");
//        List<Integer> cidsInt = new ArrayList<>();
//        for (String string : strings) {
//            cidsInt.add(Integer.parseInt(string));
//        }
//        // 查询与当前订单相关联的购物车记录
//        List<ShoppingCartVO> shopcartList = shoppingCartMapper.selectShopcartByCids(cidsInt);
//
//        // 判断商品库存是否充足
//        boolean flag = true;
//        String untitle = "";  // 保存所有商品的名称 最后保存到订单快照中
//        for (ShoppingCartVO cartVO : shopcartList) {
//            if(Integer.parseInt(cartVO.getCartNum()) > cartVO.getSkuStock()){
//                flag = false;
//            }
//            untitle = untitle + cartVO.getProductName() + ",";
//        }
//
//
//        if(flag){
//            // 库存充足 则保存订单
//            // userId
//            // untitle
//            // 支付时间
//            // 收货人的信息
//            // 总价格
//            // 支付方式（1）
//            // 支付状态（待支付）
//            order.setUntitled(untitle);
//            order.setCancelTime(new Date());
//            order.setStatus("1");
//            String orderId = UUID.randomUUID().toString().replace("-", "");
//            order.setOrderId(orderId);
//            int i = ordersMapper.insert(order);
//
//            // 生成商品快照
//            for (ShoppingCartVO sc: shopcartList) {
//                int cnum = Integer.parseInt(sc.getCartNum());
//                String itemId = System.currentTimeMillis()+""+ (new Random().nextInt(89999)+10000);
//                OrderItem orderItem = new OrderItem(itemId, orderId, sc.getProductId(), sc.getProductName(), sc.getProductImg(), sc.getSkuId(), sc.getSkuName(), new BigDecimal(sc.getSellPrice()), cnum, new BigDecimal(sc.getSellPrice() * cnum), new Date(), new Date(), 0);
//                orderItemMapper.insert(orderItem);
//                //增加商品销量
//            }
//
//            // 扣减库存
//            // 使用当前库存减去商品数量
//            for (ShoppingCartVO cartVO : shopcartList) {
//                String skuId = cartVO.getSkuId();
//                int newStock = cartVO.getSkuStock() - Integer.parseInt(cartVO.getCartNum());
//
//                Example example = new Example(ProductSku.class);
//                Example.Criteria criteria = example.createCriteria();
//                criteria.andEqualTo("skuId", skuId);
////                ProductSku productSku = productSkuMapper.selectByPrimaryKey(skuId);
////                productSku.setStock(newStock);
////                int k = productSkuMapper.updateByExample(productSku, example);
//                ProductSku productSku = new ProductSku();
//                productSku.setStock(newStock);
//                productSku.setSkuId(skuId);
//                int k = productSkuMapper.updateByPrimaryKeySelective(productSku);
//            }
//            //购买完成后 删除对应的购物车数据
//            for (Integer cid : cidsInt) {
//                shoppingCartMapper.deleteByPrimaryKey(cid);
//            }
//
//        }else {
////            return ResultVO.failed("商品库存不足，请重新选择！");
//        }
//
//
//        return null;
    }

    @Override
    public int updateOrderStatus(String orderId, String status) {
        Orders orders = new Orders();
        orders.setStatus(status);
        orders.setOrderId(orderId);
        int i = ordersMapper.updateByPrimaryKeySelective(orders);
        return i;
    }

    @Override
    public ResultVO getOrderById(String orderId) {
        Orders orders = ordersMapper.selectByPrimaryKey(orderId);
        return ResultVO.success(orders.getStatus());
    }

    @Transactional(isolation = Isolation.SERIALIZABLE) //  隔离级别是 串行化
    public void closeOrder(String orderId) {
        synchronized (this){
            Orders orders = ordersMapper.selectByPrimaryKey(orderId);
            orders.setStatus("6");
            orders.setCloseType(1); // 失败原因未支付
            // 将订单状态改为支付失败
            ordersMapper.updateByPrimaryKeySelective(orders);
            // 根据订单id查询商品快照
            Example example = new Example(OrderItem.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("orderId", orderId);
            List<OrderItem> orderItems = orderItemMapper.selectByExample(example);

            for (OrderItem orderItem : orderItems) {
                String skuId = orderItem.getSkuId();
                ProductSku productSku = productSkuMapper.selectByPrimaryKey(skuId);
                productSku.setStock(productSku.getStock() + orderItem.getBuyCounts());
                productSkuMapper.updateByPrimaryKeySelective(productSku);
            }
        }
    }

    @Override
    public ResultVO listOrders(String userId, String status, int pageNum, int limit) {
        Example example = new Example(Orders.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andLike("userId", userId);
        if(status != null && "".equals(status)){
            criteria.andEqualTo("status",status);
        }
        int count = ordersMapper.selectCountByExample(example);

        //2.计算总页数（必须确定每页显示多少条  pageSize = limit）
        int pageCount = count%limit==0? count/limit : count/limit+1;


        int start = (pageNum-1)*limit;
        List<OrdersVO> ordersVOS = ordersMapper.selectOrders(userId, status, start, limit);
        PageHelper<OrdersVO> ordersVOPageHelper = new PageHelper<>(count, pageCount, ordersVOS);

        return ResultVO.success(ordersVOPageHelper);
    }
}
