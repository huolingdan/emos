package com.ma.db.dao;

import com.ma.db.pojo.TbFaceModel;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TbFaceModelDao {

    public void insert(TbFaceModel faceModel);

    String searchFaceModel(int userId);

    int delete(int userId);

}