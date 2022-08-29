package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import org.springframework.stereotype.Service;

import java.util.List;

public interface DishService extends IService<Dish> {
    /**
     * 新增菜品，并插入菜品对应的口味数据，要操作两张表：dish/dish_flavor
     */
    public void saveWithFlavor(DishDto dishDto);

    /**
     * 查询菜品给页面回显，携带额外的口味list
     * @param id
     */
    public DishDto getByIdWithFlavor(Long id);

    /**
     * 更新菜品，同样两张表
     * @param dishDto
     */
    public void updateWithFlavor(DishDto dishDto);

    /**
     * 启售/停售商品，相关套餐也要修改
     * @param ids
     */
    public void changeStatusWithSetmeal(int flag, List<Long> ids);
}
