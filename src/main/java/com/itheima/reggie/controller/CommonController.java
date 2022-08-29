package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    /**
     * 上传文件
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        //file是一个临时文件，需要转存到指定位置，否则请求结束就会被删除

        //使用UUID生成一个不会重复的文件名
        String rename = UUID.randomUUID().toString();
        //拿到原始文件名的后缀suffix
        String oriname = file.getOriginalFilename();
        String suffix = oriname.substring(oriname.lastIndexOf("."));
        String filename = rename + suffix;
        //创建一个目录对象
        File dir = new File(basePath);
        //判断当前目录是否存在
        if(!dir.exists()){
            //不存在就创建
            dir.mkdirs();
        }
        try {
            //转存临时文件
            file.transferTo(new File(basePath + filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //文件名给页面，页面后面会用到
        return R.success(filename);
    }

    /**
     * 文件下载
     * @param name
     * @param response 通过这个获得输入输出流
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response)  {

        try {
            //设置文件格式
            response.setContentType("image/jpeg");
            //通过输入流和文件名读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));
            //通过servlet输出流将文件写回浏览器
            ServletOutputStream outputStream = response.getOutputStream();
            //循环读数据直到读完
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1 ){
                //不断写进流
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }
            outputStream.close();
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
