//package com.example.emos.wx.utils;
//
//
//import com.alibaba.fastjson.JSONObject;
//import com.aliyuncs.CommonRequest;
//import com.aliyuncs.CommonResponse;
//import com.aliyuncs.DefaultAcsClient;
//import com.aliyuncs.IAcsClient;
//import com.aliyuncs.exceptions.ClientException;
//import com.aliyuncs.exceptions.ServerException;
//import com.aliyuncs.facebody.model.v20191230.*;
//import com.aliyuncs.http.MethodType;
//import com.aliyuncs.profile.DefaultProfile;
//import com.google.gson.Gson;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.UUID;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.text.SimpleDateFormat;
//
//import sun.misc.BASE64Decoder;
//import sun.misc.BASE64Encoder;
//
//
//public class FaceRecognitionUtil {
//
//
//    //DefaultProfile.getProfile的参数分别是地域，access_key_id, access_key_secret
//    public static DefaultProfile profile = DefaultProfile.getProfile("cn-shanghai", "xxxxxxxxxxxxxxxxxxx", "ooooooooooooooooooooooooooooo");
//    public static  IAcsClient client = new DefaultAcsClient(profile);
//
//    // 人脸对比
//    /**
//     * VerifyFace API 人脸比对
//     *
//     * @param imageUrl_login 对比人脸图片1
//     * @param imageUrl_register 对比人脸图片2
//     */
//    public static String VerifyFace(String imageUrl_login, String imageUrl_register) {
//
//        CompareFaceRequest request = new CompareFaceRequest();
//        request.setRegionId("cn-shanghai");
//        request.setImageURLA(imageUrl_login);
//        request.setImageURLB(imageUrl_register);
//
//        try {
//            CompareFaceResponse response = client.getAcsResponse(request);
//            System.out.println("人脸对比："+new Gson().toJson(response));
//
//            JSONObject jsonObject = JSONObject.parseObject(new Gson().toJson(response));
//            jsonObject=JSONObject.parseObject((jsonObject.get("data").toString()));
//            String confidence = jsonObject.get("confidence").toString();
//            Float confidenceFloat = Float.parseFloat(confidence);
//
//            if (confidenceFloat >= 0.61) {
//                return "Yes";
//            } else {
//                return "No";
//            }
//
//        } catch (ServerException e) {
//            e.printStackTrace();
//        } catch (ClientException e) {
//            System.out.println("ErrCode:" + e.getErrCode());
//            System.out.println("ErrMsg:" + e.getErrMsg());
//            System.out.println("RequestId:" + e.getRequestId());
//        }
//
//
//        return null;
//    }
//    /**
//     * RecognizeFace API 人脸检测
//     *
//     * @param imageUrl 人脸图片
//     */
//    public static String RecognizeFace(String imageUrl) {
//
//        RecognizeFaceRequest request = new RecognizeFaceRequest();
//        request.setRegionId("cn-shanghai");
//        request.setImageURL(imageUrl);
//
//        try {
//            RecognizeFaceResponse response = client.getAcsResponse(request);
//            System.out.println("人脸检测："+new Gson().toJson(response));
//
//            JSONObject jsonObject = JSONObject.parseObject(new Gson().toJson(response));
//            jsonObject=JSONObject.parseObject((jsonObject.get("data").toString()));
//            String faceCounts = jsonObject.get("faceCount").toString();
//            if (Integer.parseInt(faceCounts)==0) {
//                return "No";
//            }
//            String likeDoubles = jsonObject.get("faceProbabilityList").toString();
//            likeDoubles = likeDoubles.substring(1,likeDoubles.length() - 1);
//            if ("".equals(likeDoubles)) {
//                return "No";
//            }
//            String[] likeList = likeDoubles.split(",");
//            if (likeList.length>0) {
//                String num_str=likeList[0].toString();
//                Double num_double=Double.parseDouble(num_str);
//                if (num_double>0.6) {
//                    return "Yes";
//                }else {
//                    return "No";
//                }
//            }
//            return "No";
//        } catch (ServerException e) {
//            e.printStackTrace();
//        } catch (ClientException e) {
//            System.out.println("ErrCode:" + e.getErrCode());
//            System.out.println("ErrMsg:" + e.getErrMsg());
//            System.out.println("RequestId:" + e.getRequestId());
//        }
//
//        return null;
//    }
//    /**
//     * DetectLivingFace API 人脸活体识别
//     *
//     * @param imageUrl_login  人脸活体识别
//     */
//    public static String DetectLivingFace(String imageUrl_login) {
//
//        DetectLivingFaceRequest request = new DetectLivingFaceRequest();
//        request.setRegionId("cn-shanghai");
//
//        List<DetectLivingFaceRequest.Tasks> tasksList = new ArrayList<DetectLivingFaceRequest.Tasks>();
//
//        DetectLivingFaceRequest.Tasks tasks1 = new DetectLivingFaceRequest.Tasks();
//        tasks1.setImageURL(imageUrl_login);
//        tasksList.add(tasks1);
//        request.setTaskss(tasksList);
//
//        try {
//            DetectLivingFaceResponse response = client.getAcsResponse(request);
//            System.out.println("人脸活体识别："+new Gson().toJson(response));
//            String str = new Gson().toJson(response).toString();
//            if (str!=null){
//                // 此处引入的是 com.alibaba.fastjson.JSONObject; 对象
//                JSONObject jsonObject = JSONObject.parseObject(str);
//                jsonObject=JSONObject.parseObject((jsonObject.get("data").toString()));
//                //result_pass为-1时，人脸活体识别不通过pass
//                int result_pass = jsonObject.toString().indexOf("pass");
//                int result_block = jsonObject.toString().indexOf("block");
//                //判断是否通过活体
//                if (result_pass != -1 && result_block == -1){
//                    return "Yes";
//                }
//                return  "No";
//            }
//
//
//        } catch (ServerException e) {
//            e.printStackTrace();
//        } catch (ClientException e) {
//            System.out.println("ErrCode:" + e.getErrCode());
//            System.out.println("ErrMsg:" + e.getErrMsg());
//            System.out.println("RequestId:" + e.getRequestId());
//        }
//        return  null;
//    }
//
//
//    // 本地上传sdk无需网络url
//    public static void testUploadFile() throws ClientException, IOException {
//        String accessKey = "xxxxxxxxxxxxxxxxx";
//        String accessKeySecret = "ooooooooooooooo";
//        String file1 = "D:\\2020-11-06\\timg.jpg"; // 或者本地上传
//        String file2 = "D:\\2020-11-06\\0.jpg"; // 或者本地上传
//        // String file =
//        // "https://fuss10.elemecdn.com/5/32/c17416d77817f2507d7fbdf15ef22jpeg.jpeg";
//        FileUtils fileUtils = FileUtils.getInstance(accessKey, accessKeySecret);
//        String ossurl1 = fileUtils.upload(file1);
//        String ossurl2 = fileUtils.upload(file2);
//        System.out.println(ossurl2);
//        //人脸属性检测
//        RecognizeFace(ossurl2);
//        //GetFaceAttribute(ossurl2);
//        //人脸活体检测
//        DetectLivingFace(ossurl2);
//        //人脸对比
//        VerifyFace(ossurl1, ossurl2);
//    }
//}
//
