package com.qfedu.fmmall.service.impl;

import com.qfedu.fmmall.dao.ShoppingCartMapper;
import com.qfedu.fmmall.entity.ShoppingCart;
import com.qfedu.fmmall.entity.ShoppingCartVO;
import com.qfedu.fmmall.service.ShoppingCartService;
import com.qfedu.fmmall.vo.ResStatus;
import com.qfedu.fmmall.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    @Override
    public ResultVO addShoppingCart(ShoppingCart cart) {
        cart.setCartTime(sdf.format(new Date()));
        int i = shoppingCartMapper.insert(cart);
        if(i>0){
            return new ResultVO(ResStatus.OK,"success",null);
        }else{
            return new ResultVO(ResStatus.NO,"fail",null);
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public ResultVO listShoppingCartsByUserId(int userId) {
        List<ShoppingCartVO> list = shoppingCartMapper.selectShopcartByUserId(userId);
        ResultVO resultVO = new ResultVO(ResStatus.OK, "success", list);
        return resultVO;
    }

    @Override
    public ResultVO updateCartNum(int cartId, int cartNum) {
        int i = shoppingCartMapper.updateCartnumByCartid(cartId, cartNum);
        if(i>0){
            return new ResultVO(ResStatus.OK,"update success",null);
        }else{
            return new ResultVO(ResStatus.NO,"update fail",null);
        }
    }

    @Override
    public ResultVO listShoppingCartsByCids(String cids) {
        // 将前端请求携带的字符串转换成int数组
        List<Integer> cartIds = new ArrayList<>();
        String[] strings = cids.split(",");
        for (String string : strings) {
            cartIds.add(Integer.parseInt(string));
        }
        List<ShoppingCartVO> list = shoppingCartMapper.selectShopcartByCids(cartIds);
        return ResultVO.success(list);
    }

    @Override
    public ResultVO deleteShoppingCartsByCids(String cids) {
        // 将前端请求携带的字符串转换成int数组
        List<Integer> cartIds = new ArrayList<>();
        String[] strings = cids.split(",");
        for (String string : strings) {
            cartIds.add(Integer.parseInt(string));
        }
        int i = shoppingCartMapper.deleteShopcartByCids(cartIds);
        if(i > 0){
            return ResultVO.success();
        }else {
            return ResultVO.failed("删除失败！");
        }
    }



}

