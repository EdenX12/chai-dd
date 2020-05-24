package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.SOrderDetail;
import cc.mrbird.febs.api.service.ISOrderDetailService;
import cc.mrbird.febs.common.annotation.Limit;
import cc.mrbird.febs.common.domain.FebsResponse;
import cc.mrbird.febs.common.utils.HttpUtil;
import cc.mrbird.febs.common.utils.MD5;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import freemarker.template.utility.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
@RestController
@RequestMapping("/api/express")
public class SExpressController {

    @Autowired
    private ISOrderDetailService orderDetailService;

    @GetMapping("/queryExpressInfo")
    @Limit(key = "queryExpressInfo", period = 60, count = 2000, name = "物流查询接口", prefix = "limit")
    public FebsResponse queryExpressInfo(String orderDetailId) {
        FebsResponse response = new FebsResponse();

        if(StringUtils.isBlank(orderDetailId)){
            response.put("code", 1);
            response.message("单号不可为空");
            return response;
        }
        SOrderDetail orderDetail = orderDetailService.getById(orderDetailId);
        if(orderDetail == null || StringUtils.isBlank(orderDetail.getShippingCode())){
            response.put("code", 1);
            response.message("物流单不存在");
            return response;
        }

        HashMap param = new HashMap();
        param.put("com",orderDetail.getShippingExpressCode());
        param.put("num",orderDetail.getShippingCode());
        String customer ="38E2F9F4D77D8A3B03C7562B06049549";
        String key = "TRjsfeJT8443";

        //自动发货得单子可能没有物流公司code，需要根据单号请求物流公司code
        if(StringUtils.isBlank(orderDetail.getShippingExpressCode())){
            try {
                String respData = HttpUtil.sendGet("http://www.kuaidi100.com/autonumber/auto?num="+orderDetail.getShippingCode()+"&key="+key);
                if(StringUtils.isNotBlank(respData)){
                    JSONArray jarry = JSONArray.parseArray(respData);
                    if(jarry != null && jarry.size() >0){
                        String shippingExpressCode = jarry.getJSONObject(0).getString("comCode");
                        orderDetail.setShippingExpressCode(shippingExpressCode);
                        orderDetailService.updateById(orderDetail);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //授权KEY：TRjsfeJT8443
        //customer：38E2F9F4D77D8A3B03C7562B06049549
        //secret：6f733036acd8463b9dc1a5b54f5956f7
        //userid：df293828f26645ab9c0bdde9139d7201
        //智能判断：TRjsfeJT8443
        String sign = MD5.encode(JSONObject.toJSONString(param)+key+customer);

        try {
            String respData = HttpUtil.sendPost("http://poll.kuaidi100.com/poll/query.do",
                    "param="+JSONObject.toJSONString(param)+"&sign="+sign+"&customer="+customer);
            if(StringUtils.isNotBlank(respData)){
                //1： 已发货   0 7：运输中  5：派件中  3：已签收
                response.data((JSONObject.parseObject(respData)));
                response.put("code", 0);
                return response;
            }else{
                response.put("code", 1);
                response.message("没有查询到物流信息");
                return response;
            }
        } catch (Exception e) {
            response.put("code", 1);
            response.message("没有查询到物流信息");
            return response;
        }

    }
}
