package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee") //网页的登陆请求的url：http://localhost:8080/employee/login，那么这个类的地址就是employee
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登陆
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login") //employee中的方法地址就对应login
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){

        //1 将页面提交的password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes()); //使用DigestUtils工具

        //2 根据页面提交的username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<Employee>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername()); //第二个参数拿到当前用户名，然后第一个参数挨个比对数据库
        Employee emp = employeeService.getOne(queryWrapper); //emp是当前要登陆的用户，数据库的用户名有唯一约束

        //3 查询无结果，返回登陆失败
        if (emp == null)
            return R.error("用户名不存在！");

        //4 有结果，比对密码，不一致返回登陆失败
        if (!emp.getPassword().equals(password))
            return R.error("密码错误！");

        //5 一致，查看员工状态，已禁用员工则返回对应结果（status字段）
        if (emp.getStatus() == 0)
            return R.error("账号以禁用");

        //6 登陆成功，将员工id存入session并返回登陆成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    /**
     * 退出登陆方法
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清理session中当前保存的员工的id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * @param request
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee){
        log.info("新增员工，员工信息：{}",employee.toString());
        //为新账号设置初始密码，同样md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        //设置账户创建时间,获取当前系统时间
        //employee.setCreateTime(LocalDateTime.now());
        //设置账户更新时间
        //mployee.setUpdateTime(LocalDateTime.now());
        //设置创建人、更新人(当前登陆用户)
        //Long empID = (Long) request.getSession().getAttribute("employee");
        //employee.setCreateUser(empID);
        //employee.setUpdateUser(empID);

        //调用save方法
        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page={},pageSize={},name={}",page,pageSize,name);

        //构造分页构造器,查第几页，查几条
        Page pageInfo = new Page<>(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //添加一个过滤条件,like模糊查询
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //添加一个排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询
        employeeService.page(pageInfo,queryWrapper);
        //service的page方法已经将查询结果封装给了pageInfo
        return R.success(pageInfo);
    }

    /**
     * 根据id修改员工信息
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        //设置更新人、更新时间
        //employee.setUpdateUser((Long) request.getSession().getAttribute("employee"));
        //employee.setUpdateTime(LocalDateTime.now());
        employeeService.updateById(employee);

        return R.success("员工信息修改成功");
    }

    /**
     * 编辑页面回显员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息");
        Employee employee = employeeService.getById(id);
        return R.success(employee);
    }
}
