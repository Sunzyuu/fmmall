package com.qfedu.fmmall.service.impl;

import com.qfedu.fmmall.dao.ProductCommentsMapper;
import com.qfedu.fmmall.entity.ProductComments;
import com.qfedu.fmmall.entity.ProductCommentsVO;
import com.qfedu.fmmall.service.ProductCommontsService;
import com.qfedu.fmmall.utils.PageHelper;
import com.qfedu.fmmall.vo.ResStatus;
import com.qfedu.fmmall.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;

@Service
public class ProductCommontsServiceImpl implements ProductCommontsService {

    @Autowired
    private ProductCommentsMapper productCommentsMapper;

    @Override
    public ResultVO listCommontsByProductId(String productId,int pageNum,int limit) {
        //1.根据商品id查询总记录数
        Example example = new Example(ProductComments.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("productId",productId);
        int count = productCommentsMapper.selectCountByExample(example);

        //2.计算总页数（必须确定每页显示多少条  pageSize = limit）
        int pageCount = count%limit==0? count/limit : count/limit+1;

        //3.查询当前页的数据（因为评论中需要用户信息，因此需要连表查询---自定义）
        int start = (pageNum-1)*limit;
        List<ProductCommentsVO> list = productCommentsMapper.selectCommontsByProductId(productId, start, limit);

        return ResultVO.success(list);
    }

    @Override
    public ResultVO getCommentsCountByProductId(String productId) {
       // 查询商品总数
        Example example = new Example(ProductComments.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("productId",productId);
        int total = productCommentsMapper.selectCountByExample(example);

        // 查询好评数
        Example example1 = new Example(ProductComments.class);
        Example.Criteria criteria1 = example1.createCriteria();
        criteria1.andEqualTo("productId",productId);
        criteria1.andEqualTo("commType", 1);
        int goodTotal = productCommentsMapper.selectCountByExample(example1);

        // 查询中评数
        Example example2 = new Example(ProductComments.class);
        Example.Criteria criteria2 = example1.createCriteria();
        criteria2.andEqualTo("productId",productId);
        criteria2.andEqualTo("commType", 0);
        int midTotal = productCommentsMapper.selectCountByExample(example2);

        // 查询差评数
        Example example3 = new Example(ProductComments.class);
        Example.Criteria criteria3 = example1.createCriteria();
        criteria3.andEqualTo("productId",productId);
        criteria3.andEqualTo("commType", -1);
        int badTotal = productCommentsMapper.selectCountByExample(example3);

        // 好评率
        double percent = (Double.parseDouble(goodTotal+"")/Double.parseDouble(total + "")) * 100;
        String percentValue = (percent+"").substring(0,(percent+"").lastIndexOf(".")+3);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("total", total);
        hashMap.put("goodTotal", goodTotal);
        hashMap.put("badTotal", badTotal);
        hashMap.put("midTotal", midTotal);
        hashMap.put("percent", percentValue);

        return ResultVO.success(hashMap);

    }


}
