package com.ma.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateRange;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.ma.config.SystemConstants;
import com.ma.db.dao.*;
import com.ma.db.pojo.TbCheckin;
import com.ma.db.pojo.TbFaceModel;
import com.ma.exception.EmosException;
import com.ma.service.CheckinService;
import com.ma.task.EmailTask;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@Service
@Slf4j
public class CheckinServiceImpl implements CheckinService {


    @Autowired
    private TbFaceModelDao faceModelDao;

    @Autowired
    private TbHolidaysDao holidaysDao;

    @Autowired
    private TbWorkdayDao workdayDao;

    @Autowired
    private TbCheckinDao checkinDao;

    @Autowired
    private TbDeptDao deptDao;

    @Autowired
    private SystemConstants constants;

    @Autowired
    private TbCityDao cityDao;

    @Autowired
    private EmailTask emailTask;

    @Value("${emos.email.hr}")
    private String hrEmail;

    @Value("${emos.face.checkinUrl}")
    private String checkinUrl;
    @Value("${emos.face.createFaceModelUrl}")
    private String createFaceModelUrl;

    @Value("${emos.code}")
    private String code;

    @Override
    public String validCanCheckIn(int userId, String date) {


        boolean bool1 = holidaysDao.searchTodayIsHoliday()!=null ? true : false;
        boolean bool2 = workdayDao.searchTodayIsWorkday()!=null ? true : false;
        String type = "工作日";
        if(DateUtil.date().isWeekend()){
            type="节假日";
        }

        if(bool1){
            type = "节假日";
        }else if(bool2){
            type = "工作日";
        }


        if(type.equals("工作日")){

            DateTime now = DateUtil.date();
            String start = DateUtil.today()+" "+constants.attendanceEndTime;
            String end = DateUtil.today()+" "+constants.attendanceEndTime;
            DateTime attendanceStart = DateUtil.parse(start);
            DateTime attendanceEnd = DateUtil.parse(end);

            if(now.isBefore(attendanceStart)){
                return "没到考勤时间";
            }else if(now.isAfter(attendanceEnd)){
                return "超过考勤时间";
            }else {

                HashMap map = new HashMap();
                map.put("userId", userId);
                map.put("date",date);
                map.put("start", start);
                map.put("end", end);
                boolean bool = checkinDao.haveCheckin(map) == null ? true : false;


                return bool ? "可以考勤" : "今日已经考勤，不用重复考勤";

            }
        }else {
            return "节假日不需要考勤";
        }

    }

    @Override
    public void checkin(HashMap param) {

        // 判断签到
        Date d1 = DateUtil.date(); // 当前时间
        Date d2 = DateUtil.parse(DateUtil.today()+" "+constants.attendanceStartTime);
        Date d3 = DateUtil.parse(DateUtil.today()+" "+constants.attendanceEndTime);
        int status = 1;

        if(d1.compareTo(d2) <= 0){
            status = 1; // 正常签到
        }else if(d1.compareTo(d2) > 0 & d1.compareTo(d3) < 0){
            status = 2; // 迟到
        }
        //查询签到人的人脸数据模型
        int userId = (int) param.get("UserId");
        String faceModel = faceModelDao.searchFaceModel(userId);
        if(faceModel == null){
            throw new EmosException("不存在人脸模型");
        }else {
            String path =(String) param.get("path");
            HttpRequest request = HttpUtil.createPost(checkinUrl);
            request.form("photo", FileUtil.file(path),"targetModel",faceModel);
            request.form("code",code);
            HttpResponse resp = request.execute();
            if(resp.getStatus() != 200){
                log.error("人脸识别服务异常");
                throw new EmosException("人脸识别服务异常");
            }
            String body = resp.body();
            if("无法识别出人脸".equals(body) || "照片中存在多张人脸".equals(body)){
                throw new EmosException(body);
            }else if("False".equals(body)){
                throw new EmosException("签到无效,非本人签到");
            }else if("True".equals(body)){

                // TODO 查询疫情风险等级
                int risk = 1;

                // 查询城市简称
                String city = (String) param.get("city");
                String district = (String) param.get("district");
                String address = (String) param.get("address");
                String country = (String) param.get("country");
                String province = (String) param.get("province");
                if(!StrUtil.isBlank(city) && !StrUtil.isBlank(district)){
                    String code = cityDao.searchCode(city);
                    // 查询地区风险
                    try {
                        String url = "http://m." + code + ".bendibao.com/news/yqdengji/?qu=" + district;
                        Document document = Jsoup.connect(url).get();
                        Elements elements = document.getElementsByClass("list-content");
                        if(elements.size()>0){
                            Element element = elements.get(0);
                            String result = element.select("p:last-child").text();
                            if(result.equals("高风险")){
                                risk = 3;
                                //TODO 发送告警邮件
                                HashMap map = deptDao.searchNameAndDept(userId);
                                String name = (String) map.get("name");
                                String deptName = (String) map.get("dept_name");
                                deptName = deptName==null ? "" : deptName;
                                SimpleMailMessage message = new SimpleMailMessage();
                                message.setTo(hrEmail);
                                message.setSubject("员工"+name+"身处高风险地区警告");
                                message.setText(deptName + "员工" + name + "，" + DateUtil.format(new Date(), "yyyy年MM月dd日") + "处于" + address + "，属于新冠疫情高风险地区，请及时与该员工联系，核实情况！");
                                emailTask.sendAsync(message);
                            }else if("中风险".equals(result)){
                                risk = 2;
                            }
                        }
                    }catch (Exception e){
                        log.error("执行异常",e);
                        throw new EmosException("获取风险等级失败");
                    }

                }

                // 保存签到记录
                TbCheckin entity = new TbCheckin();
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
        if("无法识别出人脸".equals(body) || "照片中存在多种人脸".equals(body)){
            throw new EmosException(body);
        }else {
            TbFaceModel entity = new TbFaceModel();
            entity.setUserId(userId);
            entity.setFaceModel(body);
            faceModelDao.insert(entity);
        }
    }

    // 查询总考勤天数
    @Override
    public long searchAllDays(int userId){

       return checkinDao.searchCheckinDays(userId);
    }

    // 查询本周考勤
    @Override
    public ArrayList<HashMap> searchWeekCheckin(HashMap param){


        // 查询本周考勤记录
        ArrayList<HashMap> checkinList = checkinDao.searchWeekCheckin(param);
        // 查询特殊节假日
        ArrayList<String> holidays = holidaysDao.searchHolidaysInRange(param);
        // 查询特殊工作日
        ArrayList<String> workdays = workdayDao.searchWorkdayInRange(param);

        DateTime startDate = DateUtil.parseDate(param.get("startDate").toString());
        DateTime endDate = DateUtil.parseDate(param.get("endDate").toString());

        DateRange range = DateUtil.range(startDate, endDate, DateField.DAY_OF_MONTH);
        ArrayList<HashMap> results = new ArrayList<>();

        range.forEach(one ->{
            String date = one.toString("yyyy-MM-dd");
            String type ="工作日";
            if(one.isWeekend()){
                type="节假日";
            }
            if(holidays != null && holidays.contains(one)){
                type="节假日";
            }else if(workdays!= null && workdays.contains(one)){
                type="工作日";
            }
            String status = "";
            if(type.equals("工作日") && DateUtil.compare(one,DateUtil.date())<=0){
                status="缺勤";
                boolean flag = false;


                for (HashMap<String,String> map:checkinList){
                    if(map.containsValue(date)){
                        status = map.get("status");
                        flag = true;
                        break;
                    }
                }

                DateTime endTime = DateUtil.parse(DateUtil.today()+" "+constants.attendanceEndTime);
                String today = DateUtil.today();

                if(date.equals(today)&& DateUtil.date().isBefore(endTime)&& flag==false){
                    status="";
                }
            }


            HashMap map = new HashMap();
            map.put("date",date);
            map.put("status",status);
            map.put("type",type);
            map.put("day",one.dayOfWeekEnum().toChinese("周"));

            results.add(map);

        });

        return results;
    }

    // 查询本月考勤
    @Override
    public ArrayList<HashMap> searchMonthCheckin(HashMap param) {
        return this.searchWeekCheckin(param);
    }

    @Override
    public HashMap searchTodayCheckin(int userId) {
        return checkinDao.searchTodayCheckin(userId);
    }
}
