package com.atguigu.lease.web.app.controller.login;



import com.atguigu.lease.common.context.LoginUserContext;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.common.utils.JwtUtil;
import com.atguigu.lease.web.app.service.LoginService;
import com.atguigu.lease.web.app.vo.user.LoginVo;
import com.atguigu.lease.web.app.vo.user.UserInfoVo;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "登录管理")
@RequestMapping("/app/")
public class LoginController {
    @Autowired
    private LoginService loginService;
    @GetMapping("login/getCode")
    @Operation(summary = "获取短信验证码")
    public Result getCode(@RequestParam String phone) throws Exception {
        loginService.sendCode(phone);
        return Result.ok();
    }

    @PostMapping("login")
    @Operation(summary = "登录")
    public Result<String> login(@RequestBody LoginVo loginVo) {
        String token = loginService.login(loginVo);
        return Result.ok(token);
    }

    @GetMapping("info")
    @Operation(summary = "获取登录用户信息")
    public Result<UserInfoVo> info(@RequestHeader("access_token") String accessToken) {
        // 解析accessToken，拿到userId
        Claims claims = JwtUtil.parseToken(accessToken);
        Long userId = Long.valueOf(claims.get("userId").toString());
        // 根据userId查询用户信息，封装为UserInfoVo
        UserInfoVo info = loginService.getUserInfoId(userId);
        return Result.ok(info);
    }
}
