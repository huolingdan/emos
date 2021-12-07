package com.ma;

import cn.hutool.core.util.StrUtil;
import com.ma.config.SystemConstants;
import com.ma.db.dao.SysConfigDao;
import com.ma.db.pojo.SysConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PostConstruct;
import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;

@SpringBootApplication
@ServletComponentScan
@Slf4j
@EnableAsync
public class EmosWxApiApplication {
    @Autowired
    private SysConfigDao sysConfigDao;

    @Autowired
    private SystemConstants constants;

    @Value("${emos.image-folder}")
    private String imageFolder;

    public static void main(String[] args) {
        SpringApplication.run(EmosWxApiApplication.class, args);
    }

    @PostConstruct
    public void init(){
        List<SysConfig> list = sysConfigDao.selectAllParam();

        list.forEach(one ->{
            String key = one.getParamKey();
            // 从数据库取出来的 字段名 不是驼峰命名，在这里转换一下
            key = StrUtil.toCamelCase(key);
            String value = one.getParamValue();
            try{
                Field field = constants.getClass().getDeclaredField(key);
                field.set(constants,value);
            }catch (Exception e){
                log.error("执行异常"+e);
            }
        });
        new File(imageFolder).mkdirs();
    }
}
