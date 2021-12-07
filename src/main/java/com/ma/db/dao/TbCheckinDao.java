package com.ma.db.dao;

import com.ma.db.pojo.TbCheckin;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
public interface TbCheckinDao {

    public Integer haveCheckin(HashMap params);

    public void insert(TbCheckin checkin);

    public HashMap searchTodayCheckin(int userId);

    public long searchCheckinDays(int userId);

    public ArrayList<HashMap> searchWeekCheckin(HashMap params);

}