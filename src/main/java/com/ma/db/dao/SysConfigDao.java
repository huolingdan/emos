package com.ma.db.dao;

import com.ma.db.pojo.SysConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface SysConfigDao {

    public List<SysConfig> selectAllParam();

    public HashMap selectTest();
}