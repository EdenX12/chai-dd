package cc.mrbird.febs.api.controller;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.AlgorithmParameterSpec;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

import cc.mrbird.febs.api.entity.*;
import cc.mrbird.febs.api.service.*;
import cc.mrbird.febs.common.controller.BaseController;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdurmont.emoji.EmojiParser;

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
import cc.mrbird.febs.common.utils.HttpRequestWechatUtil;
import cc.mrbird.febs.common.utils.IPUtil;
import cc.mrbird.febs.common.utils.MD5Util;
import cc.mrbird.febs.common.utils.SignUtil;

/**
 * @author MrBird
 */
@Slf4j
@RestController
@RequestMapping("/api/s-user")
public class SUserController extends BaseController {

    private String message;

    @Value("${weChat.app_id}")
    private String appId;

    @Value("${weChat.app_secret}")
    private String appSecret;

	@Autowired
    private FebsProperties properties;

	@Autowired
    private RedisService redisService;

	@Autowired
    private ObjectMapper mapper;

	@Autowired
	private ISUserService userService;

	@Autowired
	private ISUserWechatService sUserWechatService;

    @Autowired
    private ISUserLevelService userLevelService;

    @Autowired
    private ISUserMsgService userMsgService;

    @Autowired
    private ISUserRelationService userRelationService;

    @Autowired
    private ISUserCouponService userCouponService;

    @Autowired
    private ISUserBonusLogService userBonusLogService;

    @Autowired
    private ITokenService tokenService;

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

    @RequestMapping("/forshare")
    public ModelAndView forshare(String id) {
    	ModelAndView mav=new ModelAndView("redirect:https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx84e3a803bae71f3a&redirect_uri=http%3a%2f%2fwww.person-info.com%2fweb%2fapi%2fs-user%2fcustomer%3ftId%3d"+id+"&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect");
		return mav;
    }

    /**
     * 小程序登录
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/wxLogin")
    @Limit(key = "wxLogin", period = 60, count = 20, name = "登录接口", prefix = "limit")
    public FebsResponse wxLogin(HttpServletRequest request, String wxcode,String nickName,String avatarUrl,String sex) throws Exception {
    	System.out.println(wxcode);
    	System.out.println(appId);
    	System.out.println(appSecret);
    	String info=HttpRequestWechatUtil.postData("https://api.weixin.qq.com/sns/jscode2session?appid="+appId+"&secret="+appSecret+"&js_code="+wxcode+"&grant_type=authorization_code" , "utf-8");
		System.out.println(info);
    	JSONObject userInfo=JSONObject.parseObject(info);
		String unionid="123456";
		String openId=userInfo.getString("openid");
		String sessionKey=userInfo.getString("session_key");
		if(userInfo.containsKey("unionid")) {
			//按理说应该都有 因为暂时没绑定 先临时加个判断
		 unionid=userInfo.getString("unionid");
		}
		LambdaQueryWrapper<SUser> queryWrapper=new LambdaQueryWrapper<SUser>();
		//先根据unionid判断用户是不是存在
		queryWrapper.eq(SUser::getUnionId, unionid);
		queryWrapper.last("limit 1");
		SUser suer=userService.getOne(queryWrapper);
		if(suer==null) {
			//判断wechat里有没有数据
			LambdaQueryWrapper<SUserWechat> quser=new LambdaQueryWrapper<>();
			quser.eq(SUserWechat::getUnionId, unionid);
			quser.last("limit 1");
			SUserWechat suerWechat=sUserWechatService.getOne(quser);
			if(suerWechat==null) {
				//保存
				suerWechat=new SUserWechat();
				suerWechat.setOpenId(openId);
				suerWechat.setUnionId(unionid);
				suerWechat.setCreateTime(new Date());
				suerWechat.setNickName(nickName);
				suerWechat.setUserImg(avatarUrl);
				suerWechat.setLastLogin(new Date());
				sUserWechatService.save(suerWechat);		
			}
			System.out.println(sessionKey);
			suerWechat.setSessionKey(sessionKey);
			//union保存好了 但是没有执行登录 期待补充手机号码
			return new FebsResponse().warn("用户没有手机号码").data(suerWechat);
			
		}else {
			//做登录操作
			String password = MD5Util.encrypt(openId, "123456");
	        String token = FebsUtil.encryptToken(JWTUtil.sign(openId, password));
	        LocalDateTime expireTime = LocalDateTime.now().plusSeconds(properties.getShiro().getJwtTimeOut());
	        String expireTimeStr = DateUtil.formatFullTime(expireTime);
	        JWTToken jwtToken = new JWTToken(token, expireTimeStr);
	        this.saveTokenToRedis(suer, jwtToken, request);

            Map<String, Object> returnInfo = this.generateUserInfo(jwtToken, suer);
            returnInfo.put("sessionKey", sessionKey);
            return new FebsResponse().message("操作成功").data(returnInfo);

		}
    }
    /**
     * 解析手机号码
     * @param encryptedData
     * @param iv
     * @param sessionKey
     * @return
     * @throws Exception
     */
    @RequestMapping("/updateTelePhone")
    @ResponseBody
    public FebsResponse updateTelePhone(HttpServletRequest request,String unionid,String encryptedData, String iv,String sessionKey )throws Exception{
    	String info=this.decryptWeChatRunInfo(sessionKey, encryptedData, iv);
    	JSONObject jo=JSONObject.parseObject(info);
    	String phone=jo.getString("purePhoneNumber");
    	//先再次判断下用户是不是有user信息了
    	LambdaQueryWrapper<SUser> queryWrapper=new LambdaQueryWrapper<SUser>();
		//先根据unionid判断用户是不是存在
    	//从微信信息表里拿数据出来 插入到user表里
    	//登录 返回token
		queryWrapper.eq(SUser::getUnionId, unionid);
		queryWrapper.last("limit 1");
		SUser suer=userService.getOne(queryWrapper);
		if(suer==null) {
			LambdaQueryWrapper<SUserWechat> quser=new LambdaQueryWrapper<>();
			quser.eq(SUserWechat::getUnionId, unionid);
			quser.last("limit 1");
			SUserWechat suerWechat=sUserWechatService.getOne(quser);
			//新增userInfo信息
			suer=new SUser();
			suer.setCanuseBean(0);
			suer.setCreateTime(new Date());
			suer.setLastLogin(new Date());
			suer.setLockAmount(BigDecimal.ZERO);
			suer.setNickName(suerWechat.getNickName());
			suer.setOpenId(suerWechat.getOpenId());
			suer.setRewardBean(0);
			suer.setTaskCount(0);
			suer.setTotalAmount(BigDecimal.ZERO);
			suer.setUnionId(unionid);
			suer.setUserImg(suerWechat.getUserImg());
			suer.setUserLevelType(0);
			suer.setUserName(phone);
			suer.setUserPhone(phone);
			suer.setUserStatus(0);
			suer.setUserType(1);
			userService.save(suer);
			//更新下微信信息里的手机号
			suerWechat.setUserPhone(phone);
			sUserWechatService.updateById(suerWechat);
		}
    	//模拟登录 
		//返回token
		String password = MD5Util.encrypt(suer.getOpenId(), "123456");
        String token = FebsUtil.encryptToken(JWTUtil.sign(suer.getOpenId(), password));
        LocalDateTime expireTime = LocalDateTime.now().plusSeconds(properties.getShiro().getJwtTimeOut());
        String expireTimeStr = DateUtil.formatFullTime(expireTime);
        JWTToken jwtToken = new JWTToken(token, expireTimeStr);
        this.saveTokenToRedis(suer, jwtToken, request);

        Map<String, Object> returnInfo = this.generateUserInfo(jwtToken, suer);
        return new FebsResponse().message("操作成功").data(returnInfo);
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
		String jsonStr=HttpRequestWechatUtil.postData("https://api.weixin.qq.com/sns/oauth2/access_token", params, "utf-8");
		//System.out.println(jsonStr);
		//JSONObject object = JSONObject.parseObject(jsonStr);
		//String openId=object.getString("openid");
    	JSONObject object=null;
    	String openId="ojuKPv_-zqTPidaHx4V_OSx7HYrA";
		String password = MD5Util.encrypt(openId, "123456");
        String token = FebsUtil.encryptToken(JWTUtil.sign(openId, password));
        LocalDateTime expireTime = LocalDateTime.now().plusSeconds(properties.getShiro().getJwtTimeOut());
        String expireTimeStr = DateUtil.formatFullTime(expireTime);
        JWTToken jwtToken = new JWTToken(token, expireTimeStr);

        try {

            SUser su = userService.findByOpenId(openId);

            if (su == null) {

            	// 查询用户信息
            	String newToke=object.getString("access_token");
    				
                // 请求微信头像
                Map<String, String> params1=new HashMap<String, String>();
                params1.put("access_token",newToke);
                params1.put("openid",object.getString("openid"));
                params1.put("lang","zh_CN");
                String 	aa = HttpRequestWechatUtil.postData("https://api.weixin.qq.com/sns/userinfo", params1, "utf-8");
                JSONObject object1 = JSONObject.parseObject(aa);
                String nick=object1.getString("nickname");
                String pic=object1.getString("headimgurl");
                String sex=object1.getString("sex");
                String unionid=object1.getString("unionid");
                nick=EmojiParser.removeAllEmojis(nick);

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
                su.setUserLevelType(0);
                su.setUnionId(unionid);
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
            log.error( e.getMessage());
        }

        return response;
    }

    @PostMapping("/getJsInfo")
    @Limit(key = "getJsInfo", period = 60, count = 20, name = "获取jssdk加密信息", prefix = "limit")
	public FebsResponse getJsInfo(String url) throws Exception {

		 // 先url解码
		 url=URLDecoder.decode(url);
		 Map<String,Object> map=new HashMap<String,Object>();
		 map.put("noncestr","e-rongque" );
		 map.put("jsapi_ticket",tokenService.getJsToken() );
		 System.out.println(tokenService.getJsToken());
		 Calendar c1 = Calendar.getInstance();
		 map.put("timestamp", (c1.getTimeInMillis() + "").substring(0, 10));
		 map.put("url",url );
		 map.put("signature", SignUtil.findSignature(map));
		 map.put("appId", appId);
		 FebsResponse response = new FebsResponse();
		 response.put("code", 0);
		 response.put("data", map);
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
        // 用户昵称
        returnMap.put("nickName", user.getNickName());
        // 用户电话
        returnMap.put("userPhone", user.getUserPhone());
        // 用户头像
        returnMap.put("userImg", user.getUserImg());
        // 拆豆数量
        returnMap.put("rewardBean", user.getRewardBean());
        // 可用余额
        returnMap.put("totalAmount", user.getTotalAmount());
        // 冻结金额
        returnMap.put("lockAmount", user.getLockAmount());
        // 等级名称
        SUserLevel userLevel = this.userLevelService.findByLevelType(user.getUserLevelType());
        returnMap.put("levelName", userLevel.getLevelName());
        // 每件商品最高任务线数
        returnMap.put("buyNumber", userLevel.getBuyNumber());
        // 最多并行商品件数
        returnMap.put("productNumber", userLevel.getProductNumber());

        // 未读消息数
        SUserMsg userMsg = new SUserMsg();
        userMsg.setUserId(user.getId());
        int notReadMsgCount = this.userMsgService.findUserMsgNotReadCount(userMsg);
        returnMap.put("notReadMsgCount", notReadMsgCount);

        // 优惠券数量
        int userCouponCnt = 0;
        List<SUserCoupon> userCouponList = userCouponService.findUserCouponList(
                user.getId(), null, 0, null);
        if (userCouponList != null) {
            userCouponCnt = userCouponList.size();
        }
        returnMap.put("userCouponCnt", userCouponCnt);

        // 战队人数
        int userRelationCnt = this.userRelationService.findUserRelationCnt(user.getId());
        returnMap.put("relationCount", userRelationCnt);

        // 今日新增人数
        int userRelationTodayCnt = this.userRelationService.findUserRelationTodayCnt(user.getId());
        returnMap.put("userRelationTodayCnt", userRelationTodayCnt);

        // 战队贡献累计收益 （横向+纵向 躺赢收益）
        BigDecimal totalBonus = this.userBonusLogService.findUserBonusOrgRewardSum(user.getId(), null);

        // 战队贡献今日收益
        BigDecimal todayBonus = this.userBonusLogService.findUserBonusOrgRewardTodaySum(user.getId());

        returnMap.put("totalBonus", totalBonus);
        returnMap.put("todayBonus", todayBonus);

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
    /**
     * 微信解密运动步数
     *
     * @param sessionKey
     * @param encryptedData
     * @param iv
     * @return
     * @throws NoSuchPaddingException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidAlgorithmParameterException 
     * @throws InvalidKeyException 
     * @throws BadPaddingException 
     * @throws IllegalBlockSizeException 
     * @throws NoSuchProviderException 
     */
    public static String decryptWeChatRunInfo(String sessionKey, String encryptedData, String iv) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException {
        String result = null;
        byte[] encrypData = Base64.decodeBase64(encryptedData);
        byte[] ivData = Base64.decodeBase64(iv);
        byte[] sessionKeyB = Base64.decodeBase64(sessionKey);
        try {
            AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivData);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(sessionKeyB, "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] doFinal = cipher.doFinal(encrypData);
            result = new String(doFinal);
        }catch (Exception e){
        	 AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivData);
             Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding","BC");
             SecretKeySpec keySpec = new SecretKeySpec(sessionKeyB, "AES");
             cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
             byte[] doFinal = cipher.doFinal(encrypData);
             result = new String(doFinal);
        }
        return result;
    }
}
