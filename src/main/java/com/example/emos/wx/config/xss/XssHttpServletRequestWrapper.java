package com.example.emos.wx.config.xss;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.json.JSONUtil;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {
    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }//定义一个构造器去接收传入的对象

    @Override
    public String getParameter(String name) {
        String value= super.getParameter(name);//定义一个value保存从请求里面获得的原始数据
        if(!StrUtil.hasEmpty(value)){
            value=HtmlUtil.filter(value);//转译完的数据再返回给value
        }//判断字符串是否等于空
        return value;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values= super.getParameterValues(name);//定义一个数组
        if(values!=null){
            for (int i=0;i<values.length;i++){
                String value=values[i];
                if(!StrUtil.hasEmpty(value)){
                    value=HtmlUtil.filter(value);
                }
                values[i]=value;
            }
        }//遍历数组，判断是否为空，进行同上操作
        return values;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> parameters = super.getParameterMap();//声明一个map
        LinkedHashMap<String, String[]> map=new LinkedHashMap();//保持插入顺序
        if(parameters!=null){
            for (String key:parameters.keySet()){
                String[] values=parameters.get(key);
                for (int i = 0; i < values.length; i++) {
                    String value = values[i];
                    if (!StrUtil.hasEmpty(value)) {
                        value = HtmlUtil.filter(value);
                    }
                    values[i] = value;
                }
                map.put(key,values);
            }
        }
        return map;
    }

    @Override
    public String getHeader(String name) {
        String value= super.getHeader(name);
        if (!StrUtil.hasEmpty(value)) {
            value = HtmlUtil.filter(value);
        }
        return value;
    }//请求头转译

    @Override
    public ServletInputStream getInputStream() throws IOException {
        InputStream in= super.getInputStream(); //从请求里读数据的IO流
        InputStreamReader reader=new InputStreamReader(in, Charset.forName("UTF-8"));//从请求流读数据的字符流
        BufferedReader buffer=new BufferedReader(reader);//配置一个缓冲流
        StringBuffer body=new StringBuffer();
        String line=buffer.readLine();//从缓冲流里读第一行数据
        while(line!=null){
            body.append(line);//把line的内容拼接到字符串里
            line=buffer.readLine();//读取下一行的数据
        }//判断第一行数据是否有效
        buffer.close();
        reader.close();
        in.close();
        Map<String,Object> map=JSONUtil.parseObj(body.toString());//数据类型转换，json字符串转换成map对象
        Map<String,Object> result=new LinkedHashMap<>();//定义一个新的map
        for(String key:map.keySet()){
            Object val=map.get(key);
            if(val instanceof String){//判断是否是字符串格式数据
                if(!StrUtil.hasEmpty(val.toString())){
                    result.put(key,HtmlUtil.filter(val.toString()));
                }
            }
            else {
                result.put(key,val);
            }
        }
        String json=JSONUtil.toJsonStr(result);//转成json格式
        ByteArrayInputStream bain=new ByteArrayInputStream(json.getBytes());//创建io流，从字符串里读数据
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return bain.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }
        };
    }
}
