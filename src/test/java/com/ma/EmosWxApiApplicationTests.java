package com.ma;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.ma.db.pojo.TbMeeting;
import com.ma.service.MeetingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EmosWxApiApplicationTests {

    @Autowired
    private MeetingService meetingService;

    @Test
    void contextLoads() {

    }

    @Test
    void createMeetingData(){
        for (int i=1;i<=100;i++){
            TbMeeting meeting=new TbMeeting();
            meeting.setId((long)i);
            meeting.setUuid(IdUtil.simpleUUID());
            meeting.setTitle("测试会议"+i);
            meeting.setCreatorId(15L); //ROOT用户ID
            meeting.setDate(DateUtil.today());
            meeting.setPlace("线上会议室");
            meeting.setStart("08:30");
            meeting.setEnd("10:30");
            meeting.setType((short) 1);
            meeting.setMembers("[15,16]");
            meeting.setDesc("会议研讨Emos项目上线测试");
            meeting.setInstanceId(IdUtil.simpleUUID());
            meeting.setStatus((short)3);
            meetingService.insertMeeting(meeting);
        }
    }
}
