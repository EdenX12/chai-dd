package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.SOrderDetail;
import cc.mrbird.febs.api.service.ISOrderDetailService;
import cc.mrbird.febs.common.domain.FebsResponse;
import cc.mrbird.febs.common.utils.HttpUtil;
import cc.mrbird.febs.common.utils.MD5;
import com.alibaba.fastjson.JSONObject;
import freemarker.template.utility.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
@RestController
@RequestMapping("/api/express")
public class ExpressController {

    @Autowired
    private ISOrderDetailService orderDetailService;

    @GetMapping("/queryExpressInfo")
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
        String customer ="4C890F45F67888047E8B10C29DA539A5";
        String key = "aTZePbkA8749";
        String sign = MD5.encode(JSONObject.toJSONString(param)+key+customer);

        try {
            String respData = HttpUtil.sendPost("http://poll.kuaidi100.com/poll/query.do",
                    "param="+JSONObject.toJSONString(param)+"&sign="+sign+"&customer="+customer);
            if(StringUtils.isNotBlank(respData)){
                response.data((JSONObject.parseObject(respData)).get("data"));
                response.put("code", 0);
                return response;
            }else{
                response.data((JSONObject.parseObject(respData)).get("data"));
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
