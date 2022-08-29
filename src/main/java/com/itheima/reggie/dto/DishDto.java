package com.itheima.reggie.dto;

import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据传输对象，用于ui层和业务层之间的数据传输，封装页面提交的数据
 * 具体实现就是实体类的子类，这里的flavors就是页面提交的flavor属性List
 * 页面提交的flavor有name和value两个属性，对应DishFlavor实体中的name和value
 * 而一个菜品有多个flavor组成list，所以这里是List<DishFlavor>
 */
@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
