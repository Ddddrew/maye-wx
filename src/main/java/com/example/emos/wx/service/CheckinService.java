package com.example.emos.wx.service;

import java.util.ArrayList;
import java.util.HashMap;

public interface CheckinService {
    public String validCanCheckIn(int userId,String date);
    public void checkin(HashMap param);
    public HashMap searchTodayCheckin(int userId);
    public Long searchCheckinDays(int userId);
    public ArrayList<HashMap> searchWeekCheckin(HashMap param);
   // public void createFaceModel(int userId,String path);
    public int haveCheckIn(int userId,String date);
    public String searchDateByUserId(int userId);
    public ArrayList<HashMap> searchMonthCheckin(HashMap param);
}
