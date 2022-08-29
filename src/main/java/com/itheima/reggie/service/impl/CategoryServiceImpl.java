package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id删除，删除前需要判断
     * @param id
     */
    @Override
    public void remove(Long id) {
        //条件构造器
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //设置查询条件，分类id
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);

        int count1 = dishService.count(dishLambdaQueryWrapper);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);
        //查询当前分了是否关联菜品，如果有则抛出业务异常
        if (count1 > 0){
            //已关联菜品，抛出业务异常
            throw new CustomException("当前分类下关联了菜品，无法删除");
        }
        //查询当前分了是否关联套餐，如果有则抛出业务异常
        if (count2 > 0){
            //已关联菜单，抛出业务异常
            throw new CustomException("当前分类下关联了套餐，无法删除");
        }
        //删除分类
        super.removeById(id);
    }
}
