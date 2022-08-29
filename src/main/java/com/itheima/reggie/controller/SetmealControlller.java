package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理controller
 */
@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealControlller {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 提交表单，保存套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        setmealService.saveWithDishes(setmealDto);
        return R.success("新增套餐成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        Page<SetmealDto> dtoPage = new Page<>();
        //先进行基本的分页查询
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Setmeal::getName, name);
        queryWrapper.eq(Setmeal::getIsDeleted, 0);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //获得基本分页查询结果，拷贝给dtoPage，注意records不要拷贝，因为两个page泛型不一样，要手动设置
        setmealService.page(setmealPage,queryWrapper);
        BeanUtils.copyProperties(setmealPage,dtoPage,"records");
        //遍历结果集，手动设置分类名称
        List<Setmeal> records= setmealPage.getRecords();
        List<SetmealDto> newRecords = records.stream().map((item) ->{
            //获取分类id，根据id获取分类名称
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            //创建dto对象，对象拷贝使其与当前套餐对象一致
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item,setmealDto);
            //手动设置分类名称
            setmealDto.setCategoryName(category.getName());
            return setmealDto;
        }).collect(Collectors.toList());
        //手动设置结果集
        dtoPage.setRecords(newRecords);
        return R.success(dtoPage);
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        setmealService.removeWithDishes(ids);
        return R.success("删除成功");
    }

    /**
     * 停售/启售商品
     * @param ids
     * @return
     */
    @PostMapping("/status/{flag}")
    public R<String> status(@PathVariable int flag,@RequestParam List<Long> ids){
        List<Setmeal> list = setmealService.listByIds(ids);
        List<Setmeal> newList = list.stream().map((item) -> {
            int status = item.getStatus();
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
        setmealService.updateBatchById(newList);
        return R.success("状态修改成功");
    }
}
