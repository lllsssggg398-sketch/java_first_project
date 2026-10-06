package com.atguigu.lease.web.app.service.impl;


import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.common.utils.JwtUtil;
import com.atguigu.lease.model.entity.UserInfo;
import com.atguigu.lease.model.enums.BaseStatus;
import com.atguigu.lease.web.app.service.LoginService;
import com.atguigu.lease.web.app.service.UserInfoService;
import com.atguigu.lease.web.app.vo.user.LoginVo;
import com.atguigu.lease.web.app.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private Client client;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private UserInfoService userInfoService;
    @Override
    public void sendCode(String phone) throws Exception {

        //1.检查收集是否为null,抛出异常
        if (StringUtils.isBlank(phone)) {
            throw  new LeaseException(ResultCodeEnum.APP_LOGIN_PHONE_EMPTY);
        }
        //2.计算短信发送的间隔是否超出指定的时间  1分钟
        //2.1 redis的参数配置好
        //2.2 key约定好 [手机号]
        String key = RedisConstant.APP_LOGIN_PREFIX + phone;
        //public static final Integer APP_LOGIN_CODE_RESEND_TIME_SEC = 60;//约定短信发送间隔时间 60
        //public static final Integer APP_LOGIN_CODE_TTL_SEC = 60 * 10; //数据库的短信验证码的有效时间 600
        Boolean hasKey = redisTemplate.hasKey(key);

        //检查的前提 key有效的!
        //检查发送的间隔时间是否大于APP_LOGIN_CODE_RESEND_TIME_SEC ||  500  540  550
        // 有效的总时间(600)  -  获取剩余的有效时间  < 间隔时间显示 60  抛异常
        if (hasKey){
            //剩余时间
            Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (RedisConstant.APP_LOGIN_CODE_TTL_SEC - expire < RedisConstant.APP_LOGIN_CODE_RESEND_TIME_SEC){
                throw  new LeaseException(ResultCodeEnum.APP_SEND_SMS_TOO_OFTEN);
            }
        }

        //3.生成一个短信验证码(四位的随机数字)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            Random random = new Random();
            int number = random.nextInt(10);
            sb.append(number);
        }

        //4.发送短信client
        SendSmsRequest smsRequest = new SendSmsRequest();
        smsRequest.setPhoneNumbers(phone);
        smsRequest.setSignName("阿里云短信测试");
        smsRequest.setTemplateCode("SMS_154950909");
        smsRequest.setTemplateParam("{\"code\":\"" + sb.toString() + "\"}\n");


        //5.存储到redis
        redisTemplate.opsForValue().set(key,sb.toString(),RedisConstant.APP_LOGIN_CODE_TTL_SEC,TimeUnit.SECONDS);

        client.sendSms(smsRequest);
    }

    @Override
    public String login(LoginVo loginVo) {

        //1.判断手机号码和验证码是否为空
        if (!StringUtils.isEmpty(loginVo.getPhone())) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_PHONE_EMPTY);
        }

        if (!StringUtils.isEmpty(loginVo.getCode())) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EMPTY);
        }

        //2.校验验证码
        String key = RedisConstant.APP_LOGIN_PREFIX + loginVo.getPhone();
        String code = redisTemplate.opsForValue().get(key).toString();
        if (code == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EXPIRED);
        }

        if (!code.equals(loginVo.getCode())) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_ERROR);
        }

        //3.判断用户是否存在,不存在则注册（创建用户）
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getPhone, loginVo.getPhone());
        UserInfo userInfo = userInfoService.getOne(queryWrapper);
        if (userInfo == null) {
            userInfo = new UserInfo();
            userInfo.setPhone(loginVo.getPhone());
            userInfo.setStatus(BaseStatus.ENABLE);
            userInfo.setNickname("佚名"+loginVo.getPhone().substring(5));
            userInfoService.save(userInfo);
        }

        //4.判断用户是否被禁
        if (userInfo.getStatus().equals(BaseStatus.DISABLE)) {
            throw new LeaseException(ResultCodeEnum.APP_ACCOUNT_DISABLED_ERROR);
        }

        //5.创建并返回TOKEN
        return JwtUtil.createToken(userInfo.getId(), loginVo.getPhone());
    }

    @Override
    public UserInfoVo getUserInfoId(Long id) {
        UserInfo userInfo = userInfoService.getById(id);
        return new UserInfoVo(userInfo.getNickname(), userInfo.getAvatarUrl());
    }
}
