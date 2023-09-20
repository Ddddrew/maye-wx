package com.example.emos.wx.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateRange;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.example.emos.wx.config.SystemConstants;
import com.example.emos.wx.db.dao.TbCheckinDao;
import com.example.emos.wx.db.dao.TbFaceModelDao;
import com.example.emos.wx.db.dao.TbHolidaysDao;
import com.example.emos.wx.db.dao.TbWorkdayDao;
import com.example.emos.wx.db.pojo.TbCheckin;
import com.example.emos.wx.db.pojo.TbFaceModel;
import com.example.emos.wx.db.pojo.TbHolidays;
import com.example.emos.wx.db.pojo.TbWorkday;
import com.example.emos.wx.exception.EmosException;
import com.example.emos.wx.service.CheckinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
@Scope("prototype")//多例
@Slf4j
public class CheckinServiceImpl implements CheckinService {

    @Autowired
    private SystemConstants constants;
    @Autowired
    private TbHolidaysDao holidaysDao;
    @Autowired
    private TbWorkdayDao workdayDao;
    @Autowired
    private TbCheckinDao checkinDao;

//    @Autowired
//    private TbFaceModelDao faceModelDao;

    /*@Value("${emo.face.createFaceModelUrl}")
    private String createFaceModelUrl;
    @Value("${emo.face.checkinUrl}")
    private String checkinUrl;
    @Value("${emos.code}")
    private String code;*/

    @Override
    public String validCanCheckIn(int userId, String date) {
        boolean bool_1=holidaysDao.searchTodayIsHolidays()!=null?true:false;
        boolean bool_2=workdayDao.searchTodayIsWorkdays()!=null?true:false;
        String type="工作日";
        if (DateUtil.date().isWeekend()){
            type="节假日";
        }
        if (bool_1){
            type="节假日";
        }
        else if (bool_2){
            type="工作日";
        }

        if (type.equals("节假日")){
            return "节假日无需考勤";
        }
        else {
            DateTime now = DateUtil.date();
            String start = DateUtil.today()+" "+constants.attendanceStartTime;
            String end = DateUtil.today()+" "+constants.attendanceEndTime;
            DateTime attendanceStart = DateUtil.parse(start);
            DateTime attendanceEnd = DateUtil.parse(end);
            if (now.isBefore(attendanceStart)){
                return "还未到考勤时间";
            }
            else if(now.after(attendanceEnd)){
                return "考勤时间已经结束";
            }
            else {
                HashMap map =new HashMap();
                map.put("userId",userId);
                map.put("date",date);
                map.put("start",start);
                map.put("end",end);
                boolean bool=checkinDao.haveCheckin(map)!=null?true:false;
                return bool?"今日已考勤，无需重复考勤":"可以考勤";
            }
        }
    }

    @Override
    public void checkin(HashMap param) {
        DateTime d1 = DateUtil.date();
        DateTime d2 = DateUtil.parse(DateUtil.today() + " " + constants.attendanceTime);
        DateTime d3 = DateUtil.parse(DateUtil.today() + " " + constants.attendanceEndTime);
        int status=1;
        if (d1.compareTo(d2)<=0){
            status=1;
        }
        else if (d1.compareTo(d2)>0&&d1.compareTo(d3)<0){
            status=2;
        }
        int userId=(Integer) param.get("userId");
        int risk=1;
        String city= (String) param.get("city");
        String district= (String) param.get("district");
        String address= (String) param.get("address");
        String country= (String) param.get("country");
        String province= (String) param.get("province");
        TbCheckin entity=new TbCheckin();
        entity.setUserId(userId);
        entity.setAddress(address);
        entity.setCountry(country);
        entity.setProvince(province);
        entity.setCity(city);
        entity.setDistrict(district);
        entity.setStatus((byte) status);
        entity.setRisk(risk);
        entity.setDate(DateUtil.today());
        entity.setCreateTime(d1);
        checkinDao.insert(entity);
    }

    @Override
    public HashMap searchTodayCheckin(int userId) {
        HashMap map=checkinDao.searchTodayCheckin(userId);
        return map;
    }

    @Override
    public Long searchCheckinDays(int userId) {
        Long days=checkinDao.searchCheckinDays(userId);
        return days;
    }

    @Override
    public ArrayList<HashMap> searchWeekCheckin(HashMap param) {
        ArrayList<HashMap> checkinList=checkinDao.searchWeekCheckin(param);
        ArrayList holidaysList=holidaysDao.searchHolidaysInRange(param);
        ArrayList workdayList=workdayDao.searchWorkdayInRange(param);
        DateTime startDate=DateUtil.parseDate(param.get("startDate").toString());
        DateTime endDate=DateUtil.parseDate(param.get("endDate").toString());
        DateRange range=DateUtil.range(startDate,endDate, DateField.DAY_OF_MONTH);
        ArrayList<HashMap> list=new ArrayList<>();
        range.forEach(one->{
            String date=one.toString("yyyy-MM-dd");
            String type="工作日";
            if(one.isWeekend()){
                type="节假日";
            }
            if(holidaysList!=null&&holidaysList.contains(date)){
                type="节假日";
            }
            else if(workdayList!=null&&workdayList.contains(date)){
                type="工作日";
            }
            String status="";
            if(type.equals("工作日")&&DateUtil.compare(one,DateUtil.date())<=0){
                status="缺勤";
                boolean flag=false;
                for (HashMap<String,String> map:checkinList){
                    if(map.containsValue(date)){
                        status=map.get("status");
                        flag=true;
                        break;
                    }
                }
                DateTime endTime=DateUtil.parse(DateUtil.today()+" "+constants.attendanceEndTime);
                String today=DateUtil.today();
                if(date.equals(today)&&DateUtil.date().isBefore(endTime)&&flag==false){
                    status="";
                }
            }
            HashMap map=new HashMap();
            map.put("date",date);
            map.put("status",status);
            map.put("type",type);
            map.put("day",one.dayOfWeekEnum().toChinese("周"));
            list.add(map);
        });
        return list;
    }

    @Override
    public int haveCheckIn(int userId, String date) {
        DateTime now = DateUtil.date();
        String start = DateUtil.today()+" "+constants.attendanceStartTime;
        String end = DateUtil.today()+" "+constants.attendanceEndTime;
        HashMap map =new HashMap();
        map.put("userId",userId);
        map.put("date",date);
        map.put("start",start);
        map.put("end",end);
        System.out.println(checkinDao.haveCheckin(map));
        Integer i = checkinDao.haveCheckin(map)==null?0:1;
        return i;
    }

    @Override
    public String searchDateByUserId(int userId) {
        return checkinDao.searchDateByUserId(userId);
    }

    @Override
    public ArrayList<HashMap> searchMonthCheckin(HashMap param) {
        return this.searchWeekCheckin(param);
    }


/*
    @Override
    public void checkin(HashMap param) {
        DateTime d1 = DateUtil.date();
        DateTime d2 = DateUtil.parse(DateUtil.today() + "" + constants.attendanceTime);
        DateTime d3 = DateUtil.parse(DateUtil.today() + "" + constants.attendanceEndTime);
        int status=1;
        if (d1.compareTo(d2)<=0){
            status=1;
        }
        else if (d1.compareTo(d2)>0&&d1.compareTo(d3)<0){
            status=2;
        }
        int userId=(Integer) param.get("userId");
        String faceModel = faceModelDao.searchFaceModel(userId);
        if (faceModel==null){
            throw new EmosException("不存在人脸模型");
        }
        else {
            String path = (String) param.get("path");
            HttpRequest request= HttpUtil.createPost(checkinUrl);
            request.form("photo", FileUtil.file(path),"targetModel",faceModel);
            request.form("code",code);
            HttpResponse response = request.execute();
            if (response.getStatus()!=200){
                log.error("人脸识别异常");
                throw new EmosException("人脸识别异常");
            }
            String body=response.body();
            if ("无法识别出人脸".equals(body)||"照片中存在多张人脸".equals(body)){
                throw new EmosException(body);
            }
            else if ("False".equals(body)){
                throw new EmosException("签到无效，非本人签到");
            }
            else if ("True".equals(body)){
                int risk=1;
                String city= (String) param.get("city");
                String district= (String) param.get("district");
                String address= (String) param.get("address");
                String country= (String) param.get("country");
                String province= (String) param.get("province");
                TbCheckin entity=new TbCheckin();
                entity.setUserId(userId);
                entity.setAddress(address);
                entity.setCountry(country);
                entity.setProvince(province);
                entity.setCity(city);
                entity.setDistrict(district);
                entity.setStatus((byte) status);
                entity.setRisk(risk);
                entity.setDate(DateUtil.today());
                entity.setCreateTime(d1);
                checkinDao.insert(entity);
            }
        }
    }

    @Override
    public void createFaceModel(int userId, String path) {
        HttpRequest request = HttpUtil.createPost(createFaceModelUrl);
        request.form("photo",FileUtil.file(path));
        request.form("code",code);
        HttpResponse response = request.execute();
        String body = response.body();
        if ("无法识别出人脸".equals(body)||"照片中存在多张人脸".equals(body)){
            throw new EmosException(body);
        }
        else {
            TbFaceModel entity=new TbFaceModel();
            entity.setUserId(userId);
            entity.setFaceModel(body);
            faceModelDao.insert(entity);
        }
    }

*/
}
