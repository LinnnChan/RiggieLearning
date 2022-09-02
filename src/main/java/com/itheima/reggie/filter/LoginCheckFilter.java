package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebFault;
import java.io.IOException;

/**
 * 检查用户是否已经完成登陆
 */
@Slf4j
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher(); //路径匹配器，把请求的uri和String类型的路径相匹配
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest; //转换类型
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        //1 获取本次请求uri
        String requestURI = request.getRequestURI();
        log.info("拦截到请求：{}",requestURI);
        //2 判断是否需要处理
        String[] urls = new String[]{ //定义不需要处理的请求路径
                "/employee/login",
                "/employee/logout",
                "/backend/**", //静态资源不拦截（包括index页面），只拦截需要查数据库的动态资源
                "/front/**",
                "/common/**",
                "/user/sendMsg", //移动端发送短信
                "/user/login" //移动端登陆
        };
        boolean check = check(urls,requestURI);
        // 如果不需要，直接放行
        if (check == true) {
            log.info("本次请求不需要处理");
            filterChain.doFilter(request, response);
            return;
        }
        //4-1 判断登陆状态，如果已登陆，放行
        if (request.getSession().getAttribute("employee") != null) {
            log.info("已登陆，用户id为{}", request.getSession().getAttribute("employee"));

            //无法通过session拿到当前用户id，从线程角度解决，方便自动填充
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request, response);
            return;
            //5 否则返回未登录状态,通过输出流的方式向客户端响应数据
        }
        //4-2 判断登陆状态，如果已登陆，放行
        if (request.getSession().getAttribute("user") != null) {
            log.info("已登陆，用户id为{}", request.getSession().getAttribute("user"));

            //无法通过session拿到当前用户id，从线程角度解决，方便自动填充
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request, response);
            return;

        }

        //5 否则返回未登录状态,通过输出流的方式向客户端响应数据
        log.info("未登陆，禁止访问");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;


    }

    /**
     * 路径匹配，检查请求是否可以放行
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls, String requestURI){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match == true )
                return true;
        }
        return false;
    }

}
