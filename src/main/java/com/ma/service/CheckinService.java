package com.ma.service;


import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;


public interface CheckinService {

    public String validCanCheckIn(int userId,String date);

    public void checkin(HashMap param);

    public void createFaceModel(int userId,String path);

    public long searchAllDays(int userId);

    public ArrayList<HashMap> searchWeekCheckin(HashMap param);

    public ArrayList<HashMap> searchMonthCheckin(HashMap param);

    public  HashMap searchTodayCheckin(int userId);
}
