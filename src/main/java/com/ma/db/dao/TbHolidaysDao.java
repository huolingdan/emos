package com.ma.db.dao;

import com.ma.db.pojo.TbHolidays;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@Mapper
public interface TbHolidaysDao {

    public Integer searchTodayIsHoliday();

    public ArrayList<String> searchHolidaysInRange(HashMap params);
}