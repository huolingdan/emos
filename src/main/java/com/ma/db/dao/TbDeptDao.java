package com.ma.db.dao;

import com.ma.db.pojo.TbDept;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
public interface TbDeptDao {

    public HashMap searchNameAndDept(int userId);

    public ArrayList<HashMap> searchDeptMembers(String keyword);
}