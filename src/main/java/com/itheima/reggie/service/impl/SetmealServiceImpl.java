package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 根据dto，保存套餐和对应菜品
     * @param setmealDto
     */
    @Override
    public void saveWithDishes(SetmealDto setmealDto) {
        //保存套餐基本信息，操作setmeal表
        this.save(setmealDto);
        //保存套餐和菜品的相关信息，操作setmeal和setmeal_dish表
        //取到setmealDish，此时菜品和套餐未关联（套餐id=null）
        List<SetmealDish> dishes = setmealDto.getSetmealDishes();
        dishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(dishes);
    }

    /**
     * 删除套餐和菜品的关联数据
     * @param ids
     */
    @Override
    public void removeWithDishes(List<Long> ids) {
        //查询套餐状态，确定是否可以删除
        //select count(*) from setmeal where id in (1,2,3) and status = 1
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);
        //不能删除，抛出业务异常
        if (this.count(queryWrapper) > 0)
            throw new CustomException("有套餐正在售卖中，不能删除！");
        //可以删除，先删套餐数据setmeal
        this.removeByIds(ids);
        //根据setmealId查到setmealDish
        LambdaQueryWrapper<SetmealDish> dishQueryWrapper = new LambdaQueryWrapper<>();
        dishQueryWrapper.in(SetmealDish::getSetmealId, ids);
        //然后再删setmeal_dish关联数据
        setmealDishService.remove(dishQueryWrapper);
    }
}
