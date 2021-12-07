package com.ma.service;

import com.ma.db.pojo.TbMeeting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface MeetingService {
    public void insertMeeting(TbMeeting entity);

    public ArrayList<HashMap> searchMeetingListByPage(HashMap param);

    public HashMap searchMeetingById(int id);

    public void updateMeetingInfo(HashMap param);

    public void deleteMeetingById(int id);

    public Long searchRoomIdByUUID(String uuid);

    public List<String> searchUserMeetingInMonth(HashMap param);
}
