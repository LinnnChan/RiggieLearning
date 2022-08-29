package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")//网页的登陆请求的url：http://localhost:8080/category
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info(category.toString());
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize){
        log.info("page={},pageSize={}",page,pageSize);

        //构造分页构造器,查第几页，查几条
        Page<Category> pageInfo = new Page<>(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加一个排序条件,根据sort字段排序
        queryWrapper.orderByAsc(Category::getSort);
        //执行查询
        categoryService.page(pageInfo,queryWrapper);
        //service的page方法已经将查询结果封装给了pageInfo
        return R.success(pageInfo);
    }

    /**
     * 根据id删除
     * @param id
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long id){
        log.info("删除分类：{}",id);
        categoryService.remove(id);
        return R.success("删除成功");
    }

    /**
     * 根据id修改套餐信息
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Category category){

        categoryService.updateById(category);
        return R.success("员工信息修改成功");
    }

    /**
     * 获取菜品分类数据并展示到下拉框
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件，查询菜品分类或者套餐分类
        queryWrapper.eq(category.getType() != null, Category::getType,category.getType());
        //排序,先根据sort排，再根据更新时间排
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        //用service的list方法，将queryWrapper转成list
        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }
}
