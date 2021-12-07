package com.ma.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ma.db.dao.TbDeptDao;
import com.ma.db.dao.TbUserDao;
import com.ma.db.pojo.MessageEntity;
import com.ma.db.pojo.TbUser;
import com.ma.exception.EmosException;
import com.ma.service.UserService;
import com.ma.task.MessageTask;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.patterns.HasMemberTypePattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@Scope("prototype")
public class UserServiceImpl implements UserService {

    @Value("${wx.app-id}")
    private String appId;

    @Value("${wx.app-secret}")
    private String appSecret;

    @Autowired
    private TbUserDao tbUserDao;

    @Autowired
    private MessageTask messageTask;

    @Autowired
    private TbDeptDao deptDao;

    private String getOpenId(String code){

        String url = "https://api.weixin.qq.com/sns/jscode2session";
        HashMap map = new HashMap();
        map.put("appid",appId);
        map.put("secret",appSecret);
        map.put("js_code",code);
        map.put("grant_type","authorization_code");

        String resp = HttpUtil.post(url, map);
        log.info("从微信平台获取响应"+resp);
        JSONObject jsonObject = JSONUtil.parseObj(resp);
        log.info("解析成JSON的响应"+jsonObject);
        String openId = jsonObject.getStr("openid");
        log.info("获取到的openId"+openId);
        if(openId == null || openId.length()==0){
            throw new RuntimeException("临时登录凭证错误");
        }

        return openId;
    }

    @Override
    public int registerUser(String registerCode, String code, String nickname, String photo) {

        if(registerCode.equals("000000")){
            if(!tbUserDao.haveRootUser()){
                log.info("前端传过来的临时授权"+code);
                String openId = getOpenId(code);
                HashMap param = new HashMap();
                param.put("openId",openId);
                param.put("nickname",nickname);
                param.put("photo",photo);
                param.put("role","[0]");
                param.put("status",1);
                param.put("createTime",new Date());
                param.put("root",true);
                tbUserDao.insert(param);
                int id = tbUserDao.searchIdByOpenId(openId);



//                MessageEntity entity = new MessageEntity();
//                entity.setSenderId(0);
//                entity.setSenderName("系统消息");
//                entity.setUuid(IdUtil.simpleUUID());
//                entity.setMsg("欢迎您注册成为超级管理员，请及时更新你的员工个人信息。");
//                entity.setSendTime(new Date());
//                messageTask.sendAsync(id+"",entity);



                return id;
            }else {

                throw new EmosException("无法绑定超级管理员账号");
            }
        }else {

        }
        return 0;
    }


    public Set<String> searchUserPermissions(int userId){
        return tbUserDao.searchUserPermissions(userId);
    }

    @Override
    public Integer login(String code) {

        String openId = getOpenId(code);
        Integer id = tbUserDao.searchIdByOpenId(openId);
        if(id==null){
            throw new EmosException("账户不存在");
        }

        //TODO 从消息队列中接收消息
//        messageTask.receiveAsync(id+"");

        return id;
    }

    @Override
    public TbUser searchById(int userId) {
        return  tbUserDao.searchById(userId);
    }

    @Override
    public String searchUserHiredate(int userId) {
       String hiredate = tbUserDao.searchUserHiredate(userId);
       return hiredate;
    }

    @Override
    public HashMap searchUserSummary(int userId) {
        HashMap map = tbUserDao.searchUserSummary(userId);
        return map;
    }

    @Override
    public ArrayList<HashMap> searchUserGroupByDept(String keyword) {
        ArrayList<HashMap> list_1 = deptDao.searchDeptMembers(keyword);
        ArrayList<HashMap> list_2 = tbUserDao.searchUserGroupByDept(keyword);

        for (HashMap map1:list_1){
            long deptId = (Long) map1.get("id");
            ArrayList members = new ArrayList();

            for (HashMap map2:list_2){
                long id = (Long) map2.get("deptId");
                if(deptId==id){
                    members.add(map2);
                }
            }
            map1.put("members",members);
        }


        return list_1;
    }

    @Override
    public ArrayList<HashMap> searchMembers(List param) {
        ArrayList<HashMap> list = tbUserDao.searchMembers(param);
        return list;
    }

    @Override
    public List<HashMap> selectUserPhotoAndName(List param) {
        List<HashMap> list = tbUserDao.selectUserPhotoAndName(param);
        return list;
    }

    @Override
    public String searchMemberEmail(int id) {
        String email=tbUserDao.searchMemberEmail(id);
        return email;
    }
}
