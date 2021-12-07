package com.ma.config.xss;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.support.json.JSONUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {
    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request The request to wrap
     * @throws IllegalArgumentException if the request is null
     */
    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }


    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        if(!StrUtil.hasEmpty(value)){
            value = HtmlUtil.filter(value);
        }
        return value;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if(values!=null) {
            for (int i = 0; i < values.length; i++) {
                String value = values[i];
                if (!StrUtil.hasEmpty(value)) {
                    value = HtmlUtil.filter(value);
                }
                values[i] = value;
            }
        }
        return values;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String,String[]> map = super.getParameterMap();
        LinkedHashMap<String, String[]> linkedHashMap  = new LinkedHashMap();
        if(map!= null) {
            for (String key : map.keySet()) {
                String[] values = map.get(key);
                for (int i = 0; i < values.length; i++) {
                    String value = values[i];
                    if (!StrUtil.hasEmpty(value)) {
                        value = HtmlUtil.filter(value);
                    }
                    values[i] = value;
                }
                linkedHashMap.put(key, values);
            }
        }
        return linkedHashMap;
    }

    @Override
    public String getHeader(String name) {

        String header = super.getHeader(name);
        if(!StrUtil.hasEmpty(header)){
            header = HtmlUtil.filter(header);
        }
        return header;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        InputStream inputStream = super.getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
        BufferedReader buffer  = new BufferedReader(reader);
        StringBuffer stringBuffer = new StringBuffer();
        String s = buffer.readLine();
        while(s!=null){
            stringBuffer.append(s);
            s = buffer.readLine();
        }

        buffer.close();
        reader.close();
        inputStream.close();

        Map<String,Object> map= JSONUtil.parseObj(stringBuffer.toString());
        HashMap<String,Object> result = new HashMap<>();
        for (String key:map.keySet()) {
            Object value = map.get(key);
            if(value instanceof String){
                if(!StrUtil.hasEmpty(value.toString())){
                    result.put(key,HtmlUtil.filter(value.toString()));
                }
            }else {
                result.put(key,value);
            }
        }

        String json = JSONUtil.toJsonStr(result);
        ByteArrayInputStream brin = new ByteArrayInputStream(json.getBytes());

        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener listener) {

            }

            @Override
            public int read() throws IOException {
                return brin.read();
            }
        };
    }
}
