package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 新增菜品，并插入菜品对应的口味数据，要操作两张表：dish/dish_flavor
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品基本信息到dish表
        this.save(dishDto);
        //取id
        Long dishId = dishDto.getId();
        //遍历flavors集合，拿到每个flavor，把里面的菜品id赋值
        List<DishFlavor> flavors = dishDto.getFlavors();
        //stream取到每个元素，加工一下放回去，再转回list
        flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        //保存flavors
        dishFlavorService.saveBatch(dishDto.getFlavors());
    }

    /**
     * 查询dish并携带flavorlist
     * @param id
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //先查到当前基本的dish，并准备好dto
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
        //把dish的数据拷贝给dto
        BeanUtils.copyProperties(dish,dishDto);
        //根据id查flavor表中对应的口味
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId,id);
        List<DishFlavor> flavorList = dishFlavorService.list(lambdaQueryWrapper);
        //赋给dto
        dishDto.setFlavors(flavorList);
        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表基本信息(直接传个dto没有问题）
        this.updateById(dishDto);
        //清理当前菜品对应口味数据
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        //添加当前提交过来的口味数据
        List<DishFlavor> flavors = dishDto.getFlavors();
        //手动把dishId放到flavor里
        flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        //批量保存
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public void changeStatusWithSetmeal(int flag, List<Long> ids) {
        //先正常修改菜品状态
        List<Dish> list = this.listByIds(ids);
        List<Dish> newList = list.stream().map((item) -> {
            int status = item.getStatus();
            //判断是启售还是停售
            if (flag == 0){
                if (status != 1)
                    throw new CustomException("不能停售已经停售的商品！");
                item.setStatus(0);
            }else {
                if (status != 0)
                    throw new CustomException("不能启售已经启售的商品！");
                item.setStatus(1);
            }
            return item;
        }).collect(Collectors.toList());
        this.updateBatchById(newList);

        //再修改相关套餐,如果是启售操作就跳过这一步
        if (flag == 0) {
            //查询到菜品关联的套餐菜品
            LambdaQueryWrapper<SetmealDish> dishQueryWrapper = new LambdaQueryWrapper<>();
            dishQueryWrapper.in(SetmealDish::getDishId, ids);
            List<SetmealDish> setmealDishes = setmealDishService.list(dishQueryWrapper);

            //根据套餐菜品取到套餐id，disinct方法用来去重
            List<Long> setmealIds = setmealDishes.stream().map((item) -> {
                return item.getSetmealId();
            }).distinct().collect(Collectors.toList());
            log.info(setmealIds.toString());

            //根据套餐id停售套餐
            LambdaQueryWrapper<Setmeal> setmealqueryWrapper = new LambdaQueryWrapper<>();
            setmealqueryWrapper.in(Setmeal::getId, setmealIds);
            List<Setmeal> setmeals = setmealService.list(setmealqueryWrapper);
            List<Setmeal> newSetmeals = setmeals.stream().map((item) -> {
                //如果正在出售，就停售
                if (item.getStatus() == 1)
                    item.setStatus(0);
                return item;
            }).collect(Collectors.toList());
            setmealService.updateBatchById(newSetmeals);
        }
    }
}
