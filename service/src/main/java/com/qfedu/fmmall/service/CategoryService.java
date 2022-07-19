package com.qfedu.fmmall.service;

import com.qfedu.fmmall.entity.CategoryVO;
import com.qfedu.fmmall.vo.ResultVO;

import java.util.List;

public interface CategoryService {

    public ResultVO listCategories();

    public ResultVO listFirstLevelCategories();

    public List<CategoryVO> getCategoryList();

}
