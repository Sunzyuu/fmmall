package com.qfedu.fmmall.controller;

import com.qfedu.fmmall.entity.Users;
import com.qfedu.fmmall.service.UserAddrService;
import com.qfedu.fmmall.service.UserService;
import com.qfedu.fmmall.vo.ResStatus;
import com.qfedu.fmmall.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/useraddr")
@Api(value = "用户地址接口",tags = "用户地址管理")
@CrossOrigin
public class UserAddrController {

    @Autowired
    private UserAddrService userAddrService;


    @GetMapping("/list")
    @ApiImplicitParam(dataType = "int",name = "userId", value = "用户ID",required = true)
    public ResultVO list(Integer userId,@RequestHeader("token")String token){
        ResultVO resultVO = userAddrService.listAddrsByUid(userId);
        return resultVO;
    }

}
