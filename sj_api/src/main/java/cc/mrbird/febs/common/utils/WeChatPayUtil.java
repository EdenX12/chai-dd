package cc.mrbird.febs.common.utils;

import cc.mrbird.febs.common.annotation.Log;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class WeChatPayUtil {

    @Value("${weChat.app_id}")
    private String appId;

    @Value("${weChat.mch_id}")
    private String mchId;

    @Value("${weChat.app_key}")
    private String appKey;

    @Value("${weChat.nonce_str}")
    private String nonceStr;

    /**
     * 微信支付请求
     */
    @Log("微信支付请求")
    public JSONObject weChatPay(String relationId, String total, String openid, String ip, String payType, String productName) {

        Map<String, Object> mm1 = new HashMap<String, Object>();
        mm1.put("appid", appId);
        mm1.put("mch_id", mchId);
        mm1.put("nonce_str", nonceStr);
        try {
            mm1.put("body",  new String(productName.getBytes(),"utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        mm1.put("attach", relationId);

        String orderSn = "" + System.currentTimeMillis();

        // 1：领取任务支付  2：购买订单支付 3：转让任务报价
        if ("1".equals(payType)) {
            orderSn = "T" + orderSn;
        } else if ("2".equals(payType)) {
            orderSn = "O" + orderSn;
        } else if ("3".equals(payType)) {
            orderSn = "P" + orderSn;
        } else {

        }

        mm1.put("out_trade_no", orderSn);

        mm1.put("total_fee", String.valueOf(Integer.parseInt(total)));

        mm1.put("spbill_create_ip", ip);

        mm1.put("notify_url","http://hycw-api.hellokeeper.com/api/s-user-pay/paySuccess");
        mm1.put("trade_type", "JSAPI");

        mm1.put("openid", openid);

        mm1.put("device_info", "WEB");
        mm1.put("sign", SignUtil.findSignForPay(mm1, appKey).toUpperCase());

        String aa1 = XmlUtil.maptoXml(mm1);

        String aa = HttpRequest.sendPost("https://api.mch.weixin.qq.com/pay/unifiedorder", aa1);

        Map<String, Object> map2 = XmlUtil.xmltoMap(aa);

        Calendar c1 = Calendar.getInstance();
        map2.put("timestamp", (c1.getTimeInMillis() + "").substring(0, 10));

        // 获取一遍
        Map<String, Object> newMap = new HashMap<String, Object>();
        newMap.put("appId", map2.get("appid"));
        newMap.put("timeStamp", map2.get("timestamp"));
        newMap.put("nonceStr", "e-rongque");
        newMap.put("signType", "MD5");
        newMap.put("package", "prepay_id=" + map2.get("prepay_id"));

        map2.put("paySign", SignUtil.findSignForPay(newMap, appKey).toUpperCase());

        JSONObject jop = new JSONObject();

        jop.putAll(map2);
        jop.put("msg", 0);
        jop.put("relationId", relationId);

        return jop;
    }

}