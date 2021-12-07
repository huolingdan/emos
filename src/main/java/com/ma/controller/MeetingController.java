package com.ma.controller;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.json.JSONUtil;
import com.ma.common.util.R;
import com.ma.config.shiro.JwtUtil;
import com.ma.controller.form.*;
import com.ma.db.pojo.TbMeeting;
import com.ma.exception.EmosException;
import com.ma.service.MeetingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@Slf4j
@Api("会议Web模块")
@RequestMapping("/meeting")
public class MeetingController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MeetingService meetingService;

    @PostMapping("/searchMyMeetingByPage")
    @ApiOperation("分页查询会议")
    public R searchMyMeetingListByPage(@Valid @RequestBody SearchMyMeetingListByPageForm form, @RequestHeader("token")String token){


        int userId = jwtUtil.getUserId(token);

        int page = form.getPage();
        int length = form.getLength();
        long start = (page-1)*length;
        HashMap map = new HashMap();
        map.put("userId",userId);
        map.put("start",start);
        map.put("length",length);

        ArrayList list = meetingService.searchMeetingListByPage(map);

        return  R.ok().put("result",list);
    }

    @PostMapping("/insertMeeting")
    @ApiOperation("添加会议")
    @RequiresPermissions(value = {"ROOT","MEETING:INSERT"},logical = Logical.OR)
    public R insertMeeting(@Valid @RequestBody InsertMeetingForm form,@RequestHeader("token") String token){
        if(form.getType() == 2 && (form.getPlace()) == null || form.getPlace()==""){
            throw new EmosException("线下会议地点不能为空");
        }
        DateTime d1 = DateUtil.parse(form.getDate() + "" + form.getStart());
        DateTime d2 = DateUtil.parse(form.getDate() + "" + form.getEnd());
        if(d2.isBeforeOrEquals(d1)){
            throw new EmosException("结束时间必须大于开始时间");
        }
        if(!JSONUtil.isJsonArray(form.getMembers())){
            throw new EmosException("members不是JSON类型数据");
        }
        TbMeeting entity=new TbMeeting();
        entity.setUuid(UUID.randomUUID().toString(true));
        entity.setTitle(form.getTitle());
        entity.setCreatorId((long)jwtUtil.getUserId(token));
        entity.setDate(form.getDate());
        entity.setPlace(form.getPlace());
        entity.setStart(form.getStart() + ":00");
        entity.setEnd(form.getEnd() + ":00");
        entity.setType((short)form.getType());
        entity.setMembers(form.getMembers());
        entity.setDesc(form.getDesc());
        entity.setStatus((short)1);
        meetingService.insertMeeting(entity);

        return R.ok().put("result","success");
    }

    @PostMapping("/searchMeetingById")
    @ApiOperation("根据ID查询会议")
    @RequiresPermissions(value = {"ROOT", "MEETING:SELECT"}, logical = Logical.OR)
    public R searchMeetingById(@Valid @RequestBody SearchMeetingByIdForm form){
        HashMap map=meetingService.searchMeetingById(form.getId());
        return R.ok().put("result",map);
    }

    @PostMapping("/updateMeetingInfo")
    @ApiOperation("更新会议")
    @RequiresPermissions(value = {"ROOT", "MEETING:UPDATE"}, logical = Logical.OR)
    public R updateMeetingInfo(@Valid @RequestBody UpdateMeetingInfoForm form){
        if(form.getType()==2&&(form.getPlace()==null||form.getPlace().length()==0)){
            throw new EmosException("线下会议地点不能为空");
        }
        DateTime d1= DateUtil.parse(form.getDate()+" "+form.getStart()+":00");
        DateTime d2= DateUtil.parse(form.getDate()+" "+form.getEnd()+":00");
        if(d2.isBeforeOrEquals(d1)){
            throw new EmosException("结束时间必须大于开始时间");
        }
        if(!JSONUtil.isJsonArray(form.getMembers())){
            throw new EmosException("members不是JSON数组");
        }
        HashMap param=new HashMap();
        param.put("title", form.getTitle());
        param.put("date", form.getDate());
        param.put("place", form.getPlace());
        param.put("start", form.getStart() + ":00");
        param.put("end", form.getEnd() + ":00");
        param.put("type", form.getType());
        param.put("members", form.getMembers());
        param.put("desc", form.getDesc());
        param.put("id", form.getId());
        param.put("instanceId", form.getInstanceId());
        param.put("status", 1);
        meetingService.updateMeetingInfo(param);
        return R.ok().put("result","success");
    }


    @PostMapping("/deleteMeetingById")
    @ApiOperation("根据ID删除会议")
    @RequiresPermissions(value = {"ROOT", "MEETING:DELETE"}, logical = Logical.OR)
    public R deleteMeetingById(@Valid @RequestBody DeleteMeetingByIdForm form){
        meetingService.deleteMeetingById(form.getId());
        return R.ok().put("result","success");
    }


    @PostMapping("/recieveNotify")
    @ApiOperation("接收工作流通知")
    public R recieveNotify(@Valid @RequestBody RecieveNotifyForm form){

        if(form.getResult().equals("同意")){
            log.debug(form.getUuid()+"的会议审批通过");
        }else {
            log.debug(form.getUuid()+"的会议审批不通过");
        }

        return R.ok();
    }
}

