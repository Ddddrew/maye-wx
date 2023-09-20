package com.example.emos.wx.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.SystemConstants;
import com.example.emos.wx.config.shiro.JwtUtil;
import com.example.emos.wx.controller.form.CheckinForm;
import com.example.emos.wx.controller.form.SearchMonthCheckinForm;
import com.example.emos.wx.db.dao.TbFaceModelDao;
import com.example.emos.wx.db.pojo.TbCheckin;
import com.example.emos.wx.exception.EmosException;
import com.example.emos.wx.service.CheckinService;
import com.example.emos.wx.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

@RequestMapping("/checkin")
@RestController
@Slf4j
@Api("签到模块web接口")
public class CheckinController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CheckinService checkinService;

    @Autowired
    private UserService userService;

    @Autowired
    private SystemConstants constants;


    private TbCheckin tbCheckin;

    @Autowired
    private TbFaceModelDao tbFaceModelDao;

    @Value("${emos.image-folder}")
    private String imageFolder;

    @GetMapping("/validCanCheckIn")
    @ApiOperation("查看用户今天是否可以签到")
    public R validCanCheckIn(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        String result = checkinService.validCanCheckIn(userId, DateUtil.today());
        return R.ok(result);
    }

    @PostMapping("/checkin")
    @ApiOperation("签到")
    public R checkin(@Valid CheckinForm form, @RequestParam("photo") MultipartFile file, @RequestHeader("token") String token) {
        if (file == null) {
            return R.error("没有上传文件");
        }
        int userId = jwtUtil.getUserId(token);
        String fileName = file.getOriginalFilename().toLowerCase();
        if (!fileName.endsWith(".jpg")) {
            return R.error("必须提交JPG格式图片");
        }
        String date = checkinService.searchDateByUserId(userId);
        int i = checkinService.haveCheckIn(userId, date);
        if (i == 1) {
            throw new EmosException("您已经签到过了");
        } else {
            String path = imageFolder + "/" + fileName;
            try {
                file.transferTo(Paths.get(path));
                HashMap param = new HashMap();
                param.put("userId", userId);
                param.put("path", path);
                param.put("city", form.getCity());
                param.put("district", form.getDistrict());
                param.put("address", form.getAddress());
                param.put("country", form.getCountry());
                param.put("province", form.getProvince());
                checkinService.checkin(param);
                // FileUtil.file(path);

                /*HashMap abc = new HashMap();
                abc.put("userId", userId);
                abc.put("path", path);
                tbFaceModelDao.insertUrl(abc);*/
                return R.ok("签到成功");
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new EmosException("图片保存错误");
            } finally {
                 FileUtil.del(path);
            }

        }
    }

    @GetMapping("/searchTodayCheckin")
    @ApiOperation("查询用户当日签到数据")
    public R searchTodayCheckin(@RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        HashMap map = checkinService.searchTodayCheckin(userId);
        map.put("attendanceTime", constants.attendanceTime);
        map.put("closingTime", constants.closingTime);
        long days = checkinService.searchCheckinDays(userId);
        map.put("checkinDays", days);

        DateTime hiredate = DateUtil.parse(userService.searchUserHireDate(userId));
        DateTime startDate = DateUtil.beginOfWeek(DateUtil.date());
        if (startDate.isBefore(hiredate)) {
            startDate = hiredate;
        }
        DateTime endDate = DateUtil.endOfWeek(DateUtil.date());
        HashMap param = new HashMap();
        param.put("startDate", startDate.toString());
        param.put("endDate", endDate.toString());
        param.put("userId", userId);
        ArrayList<HashMap> list = checkinService.searchWeekCheckin(param);
        map.put("weekCheckin", list);
        return R.ok().put("result", map);
    }

    @PostMapping("/searchMonthCheckin")
    @ApiOperation("查询用户某月签到数据")
    public R searchMonthCheckin(@RequestBody @Valid SearchMonthCheckinForm form, @RequestHeader("token") String token) {
        int userId = jwtUtil.getUserId(token);
        DateTime hiredate = DateUtil.parse(userService.searchUserHireDate(userId));
        String month = form.getMonth() < 10 ? "0" + form.getMonth() : form.getMonth().toString();
        DateTime startDate = DateUtil.parse(form.getYear() + "-" + form.getMonth() + "-01");
        if (startDate.isBefore(DateUtil.beginOfMonth(hiredate))) {
            throw new EmosException("只能查询考勤之后日期的数据");
        }
        if (startDate.isBefore(hiredate)) {
            startDate = hiredate;
        }//查询的月份和入职的月份相同，查询的对象自动变为入职的那一天
        DateTime endDate = DateUtil.endOfMonth(startDate);
        HashMap param = new HashMap();
        param.put("userId", userId);
        param.put("startDate", startDate.toString());
        param.put("endDate", endDate.toString());
        ArrayList<HashMap> list = checkinService.searchMonthCheckin(param);
        int sum_1 = 0, sum_2 = 0, sum_3 = 0;
        for (HashMap<String, String> one : list) {
            String type = one.get("type");
            String status = one.get("status");
            if ("工作日".equals(type)) {
                if ("正常".equals(status)) {
                    sum_1++;
                } else if ("迟到".equals(status)) {
                    sum_2++;
                } else if ("缺勤".equals(status)) {
                    sum_3++;
                }
            }
        }
        return R.ok().put("list", list).put("sum_1", sum_1).put("sum_2", sum_2).put("sum_3", sum_3);
    }

   /* @PostMapping("/createFaceModel")
    @ApiOperation("创建人脸模型")
    public R createFaceModel(@RequestParam("photo") MultipartFile file, @RequestHeader("token") String token) {
        if (file == null) {
            return R.error("没有上传文件");
        }
        int userId = jwtUtil.getUserId(token);
        String fileName = file.getOriginalFilename().toLowerCase();
        if (!fileName.endsWith(".jpg")) {
            return R.error("必须提交JPG格式图片");
        } else {
            String path = imageFolder + "/" + fileName;
            try {
                file.transferTo(Paths.get(path));
                HashMap abc = new HashMap();
                abc.put("userId", userId);
                abc.put("path", path);
                tbFaceModelDao.insertUrl(abc);
                return R.ok("人脸建模成功");
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new EmosException("图片保存错误");
            } finally {

            }

        }
    }*/

//    1 拍照完成之后 根据当前用户id判断 数据库中是否存在该用户
//        不存在 根据照片进行人脸建模
//        存在 直接调用工具类进行人脸识别对比
//            去到签到详情页面


    /*@PostMapping("/createFaceModel")
    @ApiOperation("创建人脸模型")
    public R createFaceModel(@RequestParam("photo") MultipartFile file,@RequestHeader("token") String token){
        if(file==null){
            return R.error("没有上传文件");
        }
        int userId=jwtUtil.getUserId(token);
        String fileName=file.getOriginalFilename().toLowerCase();
        if(!fileName.endsWith(".jpg")){
            return R.error("必须提交JPG格式图片");
        }
        else{
            String path=imageFolder+"/"+fileName;
            try{
                file.transferTo(Paths.get(path));
                checkinService.createFaceModel(userId,path);
                return R.ok("人脸建模成功");
            }catch (IOException e){
                log.error(e.getMessage(),e);
                throw new EmosException("图片保存错误");
            }
            finally {
                FileUtil.del(path);
            }

        }
    }*/


    /*@RequestMapping("/")
    public void createFace() throws Exception {
        // 创建AccessKey ID和AccessKey Secret，请参考https://help.aliyun.com/document_detail/175144.html
        // 如果您使用的是RAM用户的AccessKey，还需要为子账号授予权限AliyunVIAPIFullAccess，请参考https://help.aliyun.com/document_detail/145025.html
        // 从环境变量读取配置的AccessKey ID和AccessKey Secret。运行代码示例前必须先配置环境变量。
        String accessKeyId = System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID");
        String accessKeySecret = System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET");
        com.aliyun.facebody20191230.Client client = AddFaceEntity.createClient(accessKeyId, accessKeySecret);
        com.aliyun.facebody20191230.models.AddFaceEntityRequest addFaceEntityRequest = new com.aliyun.facebody20191230.models.AddFaceEntityRequest()
                .setDbName("Face1")
                .setEntityId("Entity_id");
        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
        try {
            // 复制代码运行请自行打印 API 的返回值
            AddFaceEntityResponse addFaceEntityResponse = client.addFaceEntityWithOptions(addFaceEntityRequest, runtime);
            // 获取整体结果
            System.out.println(com.aliyun.teautil.Common.toJSONString(TeaModel.buildMap(addFaceEntityResponse)));
        } catch (TeaException teaException) {
            // 获取整体报错信息
            System.out.println(com.aliyun.teautil.Common.toJSONString(teaException));
            // 获取单个字段
            System.out.println(teaException.getCode());
        }
    }

    @RequestMapping("/")
    public void compareFace(String path) throws Exception {


        // 创建AccessKey ID和AccessKey Secret，请参考https://help.aliyun.com/document_detail/175144.html
        // 如果您使用的是RAM用户的AccessKey，还需要为子账号授予权限AliyunVIAPIFullAccess，请参考https://help.aliyun.com/document_detail/145025.html
        // 从环境变量读取配置的AccessKey ID和AccessKey Secret。运行代码示例前必须先配置环境变量。
        String accessKeyId = System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID");
        String accessKeySecret = System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET");
        com.aliyun.facebody20191230.Client client = SearchFace.createClient(accessKeyId, accessKeySecret);
        // 场景一，使用本地文件
        InputStream inputStream = new FileInputStream(new File(path));
        // 场景二，使用任意可访问的url
//        URL url = new URL("https://viapi-test-bj.oss-cn-beijing.aliyuncs.com/viapi-3.0domepic/facebody/SearchFace1.png");
//        InputStream inputStream = url.openConnection().getInputStream();
        com.aliyun.facebody20191230.models.SearchFaceAdvanceRequest searchFaceAdvanceRequest = new com.aliyun.facebody20191230.models.SearchFaceAdvanceRequest()
                .setDbName("Face1")
                .setLimit(2)
                .setImageUrlObject(inputStream);
        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
        try {
            // 复制代码运行请自行打印 API 的返回值
            SearchFaceResponse searchFaceResponse = client.searchFaceAdvance(searchFaceAdvanceRequest, runtime);
            // 获取整体结果
//            System.out.println(com.aliyun.teautil.Common.toJSONString(TeaModel.buildMap(searchFaceResponse)));
            // 获取单个字段：Confidence转换后的置信度
            Float num = searchFaceResponse.getBody().getData().getMatchList().iterator().next().getFaceItems().iterator().next().getConfidence();

            if (num >= 60.0){
                //
            }else{

            }
        } catch (TeaException teaException) {
            // 获取整体报错信息
            System.out.println(com.aliyun.teautil.Common.toJSONString(teaException));
            // 获取单个字段
            System.out.println(teaException.getCode());

        }
    }*/
}
