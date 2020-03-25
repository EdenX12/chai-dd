package cc.mrbird.febs.api.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import cc.mrbird.febs.api.entity.SUserLevel;
import cc.mrbird.febs.api.entity.SUserMsg;
import cc.mrbird.febs.api.service.ISUserLevelService;
import cc.mrbird.febs.api.service.ISUserMsgService;
import cc.mrbird.febs.api.service.ISUserTaskService;
import cc.mrbird.febs.common.controller.BaseController;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
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
import cc.mrbird.febs.common.utils.ContentUtil;
import cc.mrbird.febs.common.utils.DateUtil;
import cc.mrbird.febs.common.utils.FebsUtil;
import cc.mrbird.febs.common.utils.HttpRequest;
import cc.mrbird.febs.common.utils.HttpRequestWechatUtil;
import cc.mrbird.febs.common.utils.IPUtil;
import cc.mrbird.febs.common.utils.MD5Util;

/**
 * @author MrBird
 */
@Slf4j
@RestController
@RequestMapping("/api/s-user")
public class SUserController extends BaseController {

    private String message;

	@Autowired
    private FebsProperties properties;

	@Autowired
    private RedisService redisService;

	@Autowired
    private ObjectMapper mapper;

	@Autowired
	private ISUserService userService;

    @Autowired
    private ISUserLevelService userLevelService;

    @Autowired
    private ISUserMsgService userMsgService;

    @Autowired
    private ISUserTaskService userTaskService;
    @Value("${weChat.app_id}")
    private String appId;

    @Value("${weChat.app_secret}")
    private String appSecret;
    /**
     * 临时用一下 因为我的前端访问链接里带# 微信处理这种链接会出错
     * @param code
     * @return
     */
    @RequestMapping("/index")
    public ModelAndView indexUtil(String code) {
    	ModelAndView mav=new ModelAndView("redirect:http://www.person-info.com/#/index?code="+code);
		return mav;
    	
    }
    @RequestMapping("/customer")
    public ModelAndView indexUtil(String code,String tId) {
    	ModelAndView mav=new ModelAndView("redirect:http://www.person-info.com/#/taskForCustomer/"+tId+"?code="+code);
		return mav;
    	
    }

    /**
     * 用户登录
     * @return SUser
     * @throws Exception 
     */
	@PostMapping("/login")
    @Limit(key = "login", period = 60, count = 20, name = "登录接口", prefix = "limit")
    public FebsResponse login(HttpServletRequest request, String code) throws Exception {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);
        Map<String, String> params=new HashMap<String, String>();
		params.put("appid",appId);
    	params.put("secret",appSecret);
    	params.put("grant_type", "authorization_code");
    	params.put("code", code);
		//String jsonStr=HttpRequestWechatUtil.postData("https://api.weixin.qq.com/sns/oauth2/access_token", params, "utf-8");
		//System.out.println(jsonStr);
		//JSONObject object = JSONObject.parseObject(jsonStr);
		//String openId=object.getString("openid");
    	JSONObject object=null;
    	String openId="ojuKPv844Q3LBwRTCh_3mxFfLehM";
		String password = MD5Util.encrypt(openId, "123456");
        String token = FebsUtil.encryptToken(JWTUtil.sign(openId, password));
        LocalDateTime expireTime = LocalDateTime.now().plusSeconds(properties.getShiro().getJwtTimeOut());
        String expireTimeStr = DateUtil.formatFullTime(expireTime);
        JWTToken jwtToken = new JWTToken(token, expireTimeStr);

        try {

            SUser su = userService.findByOpenId(openId);

            if (su == null) {
            	//查询用户信息
            	String newToke=object.getString("access_token");
    				
    	    		//请求微信头像
    	    		Map<String, String> params1=new HashMap<String, String>();
    	        	params1.put("access_token",newToke);
    	        	params1.put("openid",object.getString("openid"));
    	        	params1.put("lang","zh_CN");
    	    		String 	aa = HttpRequestWechatUtil.postData("https://api.weixin.qq.com/sns/userinfo", params1, "utf-8");
    	    		JSONObject object1 = JSONObject.parseObject(aa);
    	    		String nick=object1.getString("nickname");
    	    		String pic=object1.getString("headimgurl");
    	    		String sex=object1.getString("sex");
    	    		Object unionid=object1.get("unionid");
    	    		nick=ContentUtil.getString(nick);
                // 创建用户
                su = new SUser();
                su.setOpenId(openId);
                su.setUserPassword(password);
                su.setLockAmount(BigDecimal.ZERO);
                su.setCanuseBean(0);
                su.setNickName(nick);
                su.setRewardBean(0);
                su.setTaskCount(0);
                su.setTotalAmount(BigDecimal.ZERO);
                su.setUserImg(pic);
                su.setUserLevelId(1L);
                su.setUserName(nick);
                this.userService.createUser(su);
                su = userService.findByOpenId(openId);
            }

            this.saveTokenToRedis(su, jwtToken, request);

            Map<String, Object> userInfo = this.generateUserInfo(jwtToken, su);

            response.message("认证成功");
            response.data(userInfo);

        } catch (Exception e) {
            message = "用户登录失败！";
            response.put("code", 1);
            response.message(message);
            log.error(message, e);
        }

        return response;
    }

    /**
     * 取得我的个人信息
     * @return SUser
     */
    @PostMapping("/getUser")
    @Limit(key = "getUser", period = 60, count = 20, name = "检索个人信息接口", prefix = "limit")
    public FebsResponse getUser() {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        Map<String, Object> returnMap = new HashMap<>();

        SUser user = FebsUtil.getCurrentUser();

        // 用户名称
        returnMap.put("userName", user.getUserName());
        // 用户电话
        returnMap.put("userPhone", user.getUserPhone());
        // 用户头像
        returnMap.put("userImg", user.getUserImg());
        // 猎豆数量
        returnMap.put("rewardBean", user.getRewardBean());
        // 可用余额
        returnMap.put("totalAmount", user.getTotalAmount());
        // 冻结金额
        returnMap.put("lockAmount", user.getLockAmount());
        // 等级名称
        SUserLevel userLevel = userLevelService.getById(user.getUserLevelId());
        returnMap.put("levelName", userLevel.getLevelName());
        // 未读消息数
        SUserMsg userMsg = new SUserMsg();
        userMsg.setUserId(user.getId());
        int notReadMsgCount = userMsgService.findUserMsgNotReadCount(userMsg);
        returnMap.put("notReadMsgCount", notReadMsgCount);

        // 一级组织人数
        List parentIds1 = new ArrayList();
        parentIds1.add(user.getId());
        List<SUser> userList1 = userService.findByParentId(parentIds1);
        returnMap.put("levelCount1", userList1.size());

        // 二级组织人数
        List parentIds2 = new ArrayList();
        for (SUser user1 : userList1) {
            parentIds2.add(user1.getId());
        }
        List<SUser> userList2 = userService.findByParentId(parentIds2);
        returnMap.put("levelCount2", userList2.size());

        // 三级组织人数
        List parentIds3 = new ArrayList();
        for (SUser user2 : userList2) {
            parentIds3.add(user2.getId());
        }
        List<SUser> userList3 = userService.findByParentId(parentIds3);
        returnMap.put("levelCount3", userList3.size());

        // 四级组织人数
        List parentIds4 = new ArrayList();
        for (SUser user3 : userList3) {
            parentIds4.add(user3.getId());
        }
        List<SUser> userList4 = userService.findByParentId(parentIds4);
        returnMap.put("levelCount4", userList4.size());

        // 预备队人数
        List<Long> userIds = userTaskService.findUserIdsByParent(user.getId());
        returnMap.put("reserveCount", userIds.size());

        response.data(returnMap);

        return response;
    }

    /**
     * 缓存存储信息
     * @param token token
     * @param user  用户信息
     * @return String
     */
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
