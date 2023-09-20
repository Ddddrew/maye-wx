package com.example.emos.wx.db.dao;

import com.example.emos.wx.db.pojo.TbCheckin;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
public interface TbCheckinDao {
    public Integer haveCheckin(HashMap param);

    public void insert(TbCheckin checkin);

    public String searchDateByUserId(int userId);

    public HashMap searchTodayCheckin(int userId);

    public Long searchCheckinDays(int userId);

    public ArrayList<HashMap> searchWeekCheckin(HashMap param);
}