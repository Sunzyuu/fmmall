package com.qfedu.fmmall.controller;

import com.qfedu.fmmall.service.ProductCommontsService;
import com.qfedu.fmmall.service.ProductService;
import com.qfedu.fmmall.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/product")
@Api(value = "提供商品信息相关的接口",tags = "商品管理")
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private ProductCommontsService productCommontsService;

    @ApiOperation("商品基本信息查询接口")
    @GetMapping("/detail-info/{pid}")
    public ResultVO getProductBasicInfo(@PathVariable("pid") String pid){
        return productService.getProductBasicInfo(pid);
    }

    @ApiOperation("商品参数信息查询接口")
    @GetMapping("/detail-params/{pid}")
    public ResultVO getProductParams(@PathVariable("pid") String pid){
        return productService.getProductParamsById(pid);
    }


    @ApiOperation("商品总体评价信息查询接口")
    @GetMapping("/detail-commontscount/{pid}")
    public ResultVO getProductCommontscount(@PathVariable("pid") String pid){
        return productCommontsService.getCommentsCountByProductId(pid);
    }


    @ApiOperation("商品评价分页查询接口")
    @GetMapping("/detail-commonts/{pid}")
    public ResultVO getProductCommonts(@PathVariable("pid") String pid,
                                       @RequestParam("pageNum") int pageNum,
                                       @RequestParam("limit") int limit){
        return productCommontsService.listCommontsByProductId(pid, pageNum, limit);
    }


    @ApiOperation("商品品牌分类查询接口")
    @GetMapping("/listbrands/{cid}")
    public ResultVO getListBrands(@PathVariable("cid") int cid){
        return productService.listBrands(cid);
    }

    @ApiOperation("商品品牌分页查询接口")
    @GetMapping("/listbycid/{cid}")
    public ResultVO getProductsByCategoryId(@PathVariable("cid") String cid,
                                            @RequestParam("pageNum") int pageNum,
                                            @RequestParam("limit") int limit){
        return productService.getProductsByCategoryId(Integer.parseInt(cid), pageNum, limit);
    }
}

