package com.qfedu.fmmall.service.impl;

import com.qfedu.fmmall.dao.CategoryMapper;
import com.qfedu.fmmall.entity.Category;
import com.qfedu.fmmall.entity.CategoryVO;
import com.qfedu.fmmall.service.CategoryService;
import com.qfedu.fmmall.vo.ResStatus;
import com.qfedu.fmmall.vo.ResultVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 查询分类列表（包含三个级别的分类）
     * @return
     */
    public ResultVO listCategories() {
        List<CategoryVO> categoryVOS = categoryMapper.selectAllCategories();
        ResultVO resultVO = new ResultVO(ResStatus.OK, "success", categoryVOS);
        return resultVO;
    }

    /**
     * 查询所有一级分类，同时查询当前一级分类下销量最高的6个商品
     * @return
     */
    public ResultVO listFirstLevelCategories() {
        List<CategoryVO> categoryVOS = categoryMapper.selectFirstLevelCategories();
        ResultVO resultVO = new ResultVO(ResStatus.OK, "success", categoryVOS);
        return resultVO;
    }

    @Override
    public List<CategoryVO> getCategoryList() {
        List<Category> categories = categoryMapper.getCategoryList();
        List<CategoryVO> categoryVOS = new ArrayList<>();
        // 获取所有的一级标题
        for (Category category : categories) {
            CategoryVO categoryVO = new CategoryVO();
            if(category.getCategoryLevel() == 1){
                BeanUtils.copyProperties(category, categoryVO);
                categoryVOS.add(categoryVO);
            }
        }
        // 将一级标题下的二级标题添加到setCategoryVOList属性中
        for (CategoryVO categoryVO : categoryVOS) {
            List<CategoryVO> category2List = new ArrayList<>();
            for (Category category : categories) {
                // 筛选条件为 二级标题且父标题与一级标题一致
                if(category.getCategoryLevel() == 2 && category.getParentId().equals(categoryVO.getCategoryId())){
                    CategoryVO categoryvo = new CategoryVO();
                    BeanUtils.copyProperties(category, categoryvo);
                    category2List.add(categoryvo);
                }
            }
            categoryVO.setCategories(category2List);
//            System.out.println(categoryVO);
        }
        for (CategoryVO categoryVO : categoryVOS) {

            for(CategoryVO categoryVO2: categoryVO.getCategories()){  //遍历所有二级标题
                List<CategoryVO> category3List = new ArrayList<>(); //保存三级标题的数组
                for (Category category : categories) {
                    // 筛选符合条件的三级标题 存放到二级标题的 categoryVOList中
                    if(category.getCategoryLevel() == 3 && category.getParentId().equals(categoryVO2.getCategoryId())){
                        CategoryVO categoryvo = new CategoryVO();
                        BeanUtils.copyProperties(category, categoryvo);
                        category3List.add(categoryvo);
                    }
                }
                categoryVO2.setCategories(category3List);
            }
            System.out.println(categoryVO);
        }
        return categoryVOS;
    }

}
