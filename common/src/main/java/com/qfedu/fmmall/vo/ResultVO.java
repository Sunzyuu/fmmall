package com.qfedu.fmmall.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Resource;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "ResultVO对象",description = "封装接口返回给前端的数据")
public class ResultVO {

    //响应给前端的状态码
    @ApiModelProperty(value = "响应状态码",dataType = "int")
    private int code;

    //响应给前端的提示信息
    @ApiModelProperty("响应提示信息")
    private String msg;

    //响应给前端的数据
    @ApiModelProperty("响应数据")
    private Object data;

    public static ResultVO success(){
        return new ResultVO(ResStatus.OK, "success", null);
    }

    public static ResultVO success(String msg){
        return new ResultVO(ResStatus.OK, msg, null);
    }

    public static ResultVO success(Object obj){
        return new ResultVO(ResStatus.OK, "success", obj);
    }

    public static ResultVO failed(){
        return new ResultVO(ResStatus.NO, "failed", null);
    }

    public static ResultVO failed(String msg){
        return new ResultVO(ResStatus.NO, msg, null);
    }

    public static ResultVO success(String msg, String obj) {
        return new ResultVO(ResStatus.OK, msg, obj);
    }
}
