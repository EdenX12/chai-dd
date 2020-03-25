package cc.mrbird.febs.api.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import cc.mrbird.febs.api.service.ITokenService;
import cc.mrbird.febs.common.service.RedisService;
import cc.mrbird.febs.common.utils.HttpRequestWechatUtil;

@Service
public class STokenServiceImpl implements ITokenService {
	@Autowired
	private RedisService redisService;
	@Value("${weChat.app_id}")
    private String appId;

    @Value("${weChat.app_secret}")
    private String appSecret;
	@Override
	public String getAccessToken() throws Exception {
		
		if(redisService.exists("access_token")) {
			return redisService.get("access_token");
		}else {
			//请求accesstoken
			//请求token
			Map<String, String> params=new HashMap<String, String>();
	    	params.put("grant_type", "client_credential");
	    	params.put("appid",appId);
	    	params.put("secret",appSecret);
			String aa=HttpRequestWechatUtil.postData("https://api.weixin.qq.com/cgi-bin/token", params, "utf-8");
			JSONObject object = JSONObject.parseObject(aa);
			if(object.get("access_token")!=null){
				redisService.set("access_token", object.get("access_token").toString(), 3600000L);
				return object.get("access_token").toString();
			}else{
				return null;
			}
		}
		
	}

	@Override
	public String getJsToken() throws Exception{
		if(redisService.exists("js_token")) {
			return redisService.get("js_token");
		}else {
			String param="access_token="+this.getAccessToken()+"&type=jsapi";
			String jsonStr2 =HttpRequestWechatUtil.postData("https://api.weixin.qq.com/cgi-bin/ticket/getticket?"+param, "utf-8");
			JSONObject object2 = JSONObject.parseObject(jsonStr2);
			if (object2.get("ticket") != null) {
				redisService.set("js_token", object2.getString("ticket"), 1200000L);
				return object2.getString("ticket");
			}else{
				return null;
			}
			
		}
	}
	

}
