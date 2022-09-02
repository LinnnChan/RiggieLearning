package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.common.SMSUtils;
import com.itheima.reggie.common.ValidateCodeUtils;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 发送手机短信验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String phone = user.getPhone();
        if (phone != null) {
            //生成随机验证码
            String code = ValidateCodeUtils.generateValidateCode4String(4);
            log.info(code);
            //调用api发送短信
            try {
                SMSUtils.sendMessage("阿里云短信测试","SMS_154950909",phone,code);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //保存验证码到Session
            session.setAttribute(phone,code);
        }

        return R.error("短信发送失败");
    }

    /**
     * 移动端用户登陆
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){ //直接使用user作参数，user中没有code字段，所以使用map结构
        //获取手机号
        String phone = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();
        //从session中获取保存的验证码
        Object codeInSession = session.getAttribute(phone);
        //进行验证码的比对
        //如果能够比对成功，说明登陆成功
        if (codeInSession != null && codeInSession.equals(code)) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User one = userService.getOne(queryWrapper);
            //如果是新用户，就需要自动注册
            if (one == null){
                one = new User();
                one.setPhone(phone);
                one.setStatus(1);
                userService.save(one);
            }
            session.setAttribute("user",one.getId());
            return R.success(one);
        }

        return R.error("登陆失败");
    }
}
