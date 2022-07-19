package com.qfedu.fmmall.service.impl;

import com.qfedu.fmmall.dao.ProductImgMapper;
import com.qfedu.fmmall.dao.ProductMapper;
import com.qfedu.fmmall.dao.ProductParamsMapper;
import com.qfedu.fmmall.dao.ProductSkuMapper;
import com.qfedu.fmmall.entity.*;
import com.qfedu.fmmall.service.ProductService;
import com.qfedu.fmmall.utils.PageHelper;
import com.qfedu.fmmall.vo.ResStatus;
import com.qfedu.fmmall.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ProductImgMapper productImgMapper;
    @Autowired
    private ProductSkuMapper productSkuMapper;
    @Autowired
    private ProductParamsMapper productParamsMapper;

    public ResultVO listRecommendProducts() {
        List<ProductVO> productVOS = productMapper.selectRecommendProducts();
        ResultVO resultVO = new ResultVO(ResStatus.OK, "success", productVOS);
        return resultVO;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public ResultVO getProductBasicInfo(String productId) {
        //1.商品基本信息
        Example example = new Example(Product.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("productId",productId);
        criteria.andEqualTo("productStatus",1);//状态为1表示上架商品
        List<Product> products = productMapper.selectByExample(example);
        if(products.size() > 0){
            Example exampleImg = new Example(ProductImg.class);
            Example.Criteria imgCriteria = exampleImg.createCriteria();
            imgCriteria.andEqualTo("itemId",productId);
            List<ProductImg> productImgs = productImgMapper.selectByExample(exampleImg);

            Example exampleSku = new Example(ProductSku.class);
            Example.Criteria skuCriteria = exampleSku.createCriteria();
            skuCriteria.andEqualTo("productId",productId);
            List<ProductSku> productSkus = productSkuMapper.selectByExample(exampleSku);

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("product", products.get(0));
            hashMap.put("productImgs", productImgs);
            hashMap.put("productSkus", productSkus);
            return ResultVO.success(hashMap);
        }
        return ResultVO.failed("查询的商品不存在");
    }

    @Override
    public ResultVO getProductParamsById(String productId) {
        //1.商品基本信息
        Example example = new Example(Product.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("productId",productId);
        criteria.andEqualTo("productStatus",1);//状态为1表示上架商品
        List<Product> products = productMapper.selectByExample(example);
        if(products.size() > 0){
            Example exampleParams = new Example(ProductParams.class);
            Example.Criteria criteria1 = exampleParams.createCriteria();
            criteria1.andEqualTo("productId", productId);
            List<ProductParams> params = productParamsMapper.selectByExample(exampleParams);
            if(params.size() > 0){
                return ResultVO.success(params.get(0));
            }else {
                return ResultVO.failed("此产品为三无产品！");
            }
        }else {
            return ResultVO.failed("查询的商品不存在");
        }
    }

    @Override
    public ResultVO getProductsByCategoryId(int categoryId, int pageNum, int limit) {
        //1.查询分页数据
        int start = (pageNum-1)*limit;
        List<ProductVO> productVOS = productMapper.selectProductByCategoryId(categoryId, start, limit);
        //2.查询当前类别下的商品的总记录数
        Example example = new Example(Product.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("categoryId",categoryId);
        int count = productMapper.selectCountByExample(example);
        //3.计算总页数
        int pageCount = count%limit==0? count/limit : count/limit+1;
        //4.封装返回数据
        PageHelper<ProductVO> pageHelper = new PageHelper<>(count, pageCount, productVOS);
        return new ResultVO(ResStatus.OK,"SUCCESS",pageHelper);
    }

    @Override
    public ResultVO listBrands(int categoryId) {
        List<String> brands = productMapper.selectBrandByCategoryId(categoryId);
        return new ResultVO(ResStatus.OK,"success",brands);
    }

    @Override
    public ResultVO searchProduct(String kw, int pageNum, int limit) {
        //1.查询搜索结果
        kw = "%"+kw+"%";
        int start = (pageNum-1)*limit;
        List<ProductVO> productVOS = productMapper.selectProductByKeyword(kw, start, limit);
        
        //2.查询总记录数
        Example example = new Example(Product.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andLike("productName",kw);
        int count = productMapper.selectCountByExample(example);

        //3.计算总页数
        int pageCount = count%limit==0? count/limit:count/limit+1;

        //4.封装，返回数据
        PageHelper<ProductVO> pageHelper = new PageHelper<>(count, pageCount, productVOS);
        ResultVO resultVO = new ResultVO(ResStatus.OK, "SUCCESS", pageHelper);
        return resultVO;
    }

    @Override
    public ResultVO listBrands(String kw) {
        kw = "%"+kw+"%";
        List<String> brands = productMapper.selectBrandByKeyword(kw);
        return new ResultVO(ResStatus.OK,"SUCCESS",brands);
    }
}
