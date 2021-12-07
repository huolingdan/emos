package com.ma.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.ma.common.util.R;
import com.ma.config.SystemConstants;
import com.ma.config.shiro.JwtUtil;
import com.ma.controller.form.CheckinForm;
import com.ma.controller.form.SearchMonthCheckinForm;
import com.ma.exception.EmosException;
import com.ma.service.CheckinService;
import com.ma.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@RequestMapping("checkin")
@RestController
@Slf4j
@Api("签到Web接口")
public class CheckinController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CheckinService checkinService;

    @Autowired
    private UserService userService;

    @Autowired
    private SystemConstants constants;

    @Value("${emos.image-folder}")
    private String imageFolder;

    @ApiOperation("查看用户今天是否可以签到")
    @GetMapping("validCanCheckIn")
    public R validCanCheckIn(@RequestHeader("token") String token){

        int userId = jwtUtil.getUserId(token);
        String result = checkinService.validCanCheckIn(userId, DateUtil.today());

        return R.ok(result);
    }


    @PostMapping("/checkin")
    @ApiOperation("签到")
    public R checkin(@Valid CheckinForm form,@RequestParam("photo") MultipartFile file,@RequestHeader("token") String token){



        if(file == null){
            return  R.error("没有上传文件");
        }
        int userId = jwtUtil.getUserId(token);
        String fileName = file.getOriginalFilename();
        if(fileName.endsWith(".jpg")){
            return R.error("必须提交JPG格式图片");
        }else {
            String path = imageFolder+"/"+fileName;

            try{
                file.transferTo(Paths.get(path));
                HashMap map = new HashMap();
                map.put("userId",userId);
                map.put("path",path);
                map.put("country",form.getCountry());
                map.put("city",form.getCity());
                map.put("province",form.getProvince());
                map.put("district",form.getDistrict());
                map.put("address",form.getAddress());

                checkinService.checkin(map);
            }catch (IOException e){
                log.error(e.getMessage(),e);
                throw new EmosException("图片保存错误");
            }finally {
                FileUtil.del(path);
            }
        }

        return R.ok("签到成功");

    }

    @PostMapping("/createFaceModel")
    @ApiOperation("创建人脸模型")
    public R createFaceModel(@RequestParam("photo")MultipartFile file,@RequestHeader("token")String token){
        int userId = jwtUtil.getUserId(token);
        if(file == null){
            return R.error("没有上传文件");
        }
        String fileName = file.getOriginalFilename();
        String path = imageFolder + "/"+fileName;
        if(!fileName.endsWith(".jpg")){
            return R.error("必须提交JPG格式图片");
        }else {
            try{
                //保存文件
                file.transferTo(Paths.get(path));
                checkinService.createFaceModel(userId,path);
                return R.ok("人脸建模成功");
            }catch (IOException e){
                log.error(e.getMessage());
                throw new EmosException("保存图片错误");
            }finally {
                FileUtil.del(path);
            }
        }






    }


    @GetMapping("/searchTodayCheckin")
    @ApiOperation("查询用户当日签到数据")
    public R searchTodayCheckin(@RequestHeader("token") String token){
        int userId = jwtUtil.getUserId(token);
        HashMap map = checkinService.searchTodayCheckin(userId);
        map.put("attendanceTime",constants.attendanceStartTime);
        map.put("closingTime",constants.closingEndTime);
        long days = checkinService.searchAllDays(userId);
        map.put("checkinDays",days);
        DateTime hiredate = DateUtil.parse(userService.searchUserHiredate(userId));
        DateTime startDate = DateUtil.beginOfWeek(DateUtil.date());
        if(startDate.isBefore(hiredate)){
            startDate=hiredate;
        }
        DateTime endDate = DateUtil.endOfWeek(DateUtil.date());
        HashMap param = new HashMap();
        param.put("startDate",startDate.toString());
        param.put("endDate",endDate.toString());
        param.put("userId",userId);
        ArrayList<HashMap> list = checkinService.searchWeekCheckin(param);
        map.put("weekCheckin",list);

        return R.ok().put("result",map);
    }
    @PostMapping("/searchMonthCheckin")
    @ApiOperation("本月考勤详情")
    public R searchMonthCheckin(@Valid @RequestBody SearchMonthCheckinForm form,@RequestHeader("token")String token){
        int userId = jwtUtil.getUserId(token);

        // 查询入职日期
        DateTime hiredate = DateUtil.parse(userService.searchUserHiredate(userId));
        // 把月份处理成撒双数字
        String month = form.getMonth() <10 ?"0"+ form.getMonth(): ""+form.getMonth();
        // 某年某月的起始日期
        DateTime startDate = DateUtil.parse(form.getYear()+"-"+month+"-01");
        // 如果查询的月份早于员工入职日期的月份就抛出异常
        if(startDate.isBefore(DateUtil.beginOfMonth(hiredate))){
            throw new EmosException("只能查询入职之后的日期数据");
        }
        // 如果入职日期是本月
        if(startDate.isBefore(hiredate)){
            startDate = hiredate;
        }

        DateTime endDate = DateUtil.endOfMonth(startDate);

        HashMap param = new HashMap();
        param.put("userId",userId);
        param.put("startDate",startDate.toString());
        param.put("endDate",endDate.toString());
        ArrayList<HashMap> list = checkinService.searchMonthCheckin(param);
        int sum_1=0;
        int sum_2=0;
        int sum_3=0;

        for(HashMap<String,String> one:list){
            String type= one.get("type");
            String status=one.get("status");
            if(type.equals("工作日")){
                if("正常".equals(status)){
                    sum_1++;
                }else if("迟到".equals(status)){
                    sum_2++;
                }else if("缺勤".equals(status)){
                    sum_3++;
                }
            }
        }
        return R.ok().put("list",list).put("sum_1",sum_1).put("sum_2",sum_2).put("sum_3",sum_3);
    }









































































































}
