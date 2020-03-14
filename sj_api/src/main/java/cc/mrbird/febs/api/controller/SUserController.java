package cc.mrbird.febs.api.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import cc.mrbird.febs.common.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.fasterxml.jackson.databind.ObjectMapper;

import cc.mrbird.febs.api.entity.SUser;
import cc.mrbird.febs.api.service.ISUserService;
import cc.mrbird.febs.common.annotation.Limit;
import cc.mrbird.febs.common.authentication.JWTToken;
import cc.mrbird.febs.common.authentication.JWTUtil;
import cc.mrbird.febs.common.domain.ActiveUser;
import cc.mrbird.febs.common.domain.FebsConstant;
import cc.mrbird.febs.common.domain.FebsResponse;
import cc.mrbird.febs.common.properties.FebsProperties;
import cc.mrbird.febs.common.service.RedisService;
import cc.mrbird.febs.common.utils.AddressUtil;
import cc.mrbird.febs.common.utils.DateUtil;
import cc.mrbird.febs.common.utils.FebsUtil;
import cc.mrbird.febs.common.utils.IPUtil;
import cc.mrbird.febs.common.utils.MD5Util;

/**
 * @author MrBird
 */
@RestController
@RequestMapping("/api/s-user")
public class SUserController extends BaseController {

	@Autowired
    private FebsProperties properties;

	@Autowired
    private RedisService redisService;

	@Autowired
    private ObjectMapper mapper;

	@Autowired
	private ISUserService userService;

	@PostMapping("/login")
    @Limit(key = "login", period = 60, count = 20, name = "登录接口", prefix = "limit")
    public FebsResponse login(HttpServletRequest request, String openId) throws Exception {
      
		String password = MD5Util.encrypt(openId, "123456");
        String token = FebsUtil.encryptToken(JWTUtil.sign(openId, password));
        LocalDateTime expireTime = LocalDateTime.now().plusSeconds(properties.getShiro().getJwtTimeOut());
        String expireTimeStr = DateUtil.formatFullTime(expireTime);
        JWTToken jwtToken = new JWTToken(token, expireTimeStr);

        SUser su = userService.findByOpenId(openId);

        if (su == null) {

            // 创建用户
            su = new SUser();
            su.setOpenId(openId);
            su.setUserPassword(password);

            this.userService.createUser(su);
            su = userService.findByOpenId(openId);
        }
        	
        this.saveTokenToRedis(su, jwtToken, request);

        Map<String, Object> userInfo = this.generateUserInfo(jwtToken, su);
        return new FebsResponse().message("认证成功").data(userInfo);
    }

	@PostMapping("/test")
    @Limit(key = "test", period = 60, count = 20, name = "测试接口", prefix = "limit")
    public FebsResponse test( HttpServletRequest request) throws Exception {

        SUser user = FebsUtil.getCurrentUser();

        return new FebsResponse().message("获取信息成功").data(user);
    }

    private String saveTokenToRedis(SUser user, JWTToken token, HttpServletRequest request) throws Exception {
        String ip = IPUtil.getIpAddr(request);

        // 构建在线用户
        ActiveUser activeUser = new ActiveUser();
        activeUser.setUsername(user.getUserName());
        activeUser.setIp(ip);
        activeUser.setToken(token.getToken());
        activeUser.setLoginAddress(AddressUtil.getCityInfo(ip));

        // zset 存储登录用户，score 为过期时间戳
        this.redisService.zadd(FebsConstant.ACTIVE_USERS_ZSET_PREFIX, Double.valueOf(token.getExipreAt()), mapper.writeValueAsString(activeUser));

        // redis 中存储这个加密 token，key = 前缀 + 加密 token + .ip
        this.redisService.set(FebsConstant.TOKEN_CACHE_PREFIX + token.getToken() + StringPool.DOT + ip, token.getToken(), properties.getShiro().getJwtTimeOut() * 1000);

        return activeUser.getId();
    }

    /**
     * 生成前端需要的用户信息，包括：
     * 1. token
     * 2. 前端系统个性化配置信息
     *
     * @param token token
     * @param user  用户信息
     * @return UserInfo
     */
    private Map<String, Object> generateUserInfo(JWTToken token, SUser user) {

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("token", token.getToken());
        userInfo.put("exipreTime", token.getExipreAt());

        userInfo.put("user", user);
        return userInfo;
    }
}
