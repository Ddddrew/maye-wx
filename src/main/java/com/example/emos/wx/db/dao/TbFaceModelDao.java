package com.example.emos.wx.db.dao;

import com.example.emos.wx.db.pojo.TbFaceModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;

@Mapper
public interface TbFaceModelDao {
    public String searchFaceModel(int userId);

    public void insert(TbFaceModel faceModel);

    public int deleteFaceModel(int userId);


    /*public int insertUrl(HashMap map);

    public String searchUrl(int userId);*/
}