package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.SOfferPrice;
import cc.mrbird.febs.api.entity.STaskOrder;
import cc.mrbird.febs.api.entity.SUser;
import cc.mrbird.febs.api.service.ISOfferPriceService;
import cc.mrbird.febs.api.service.ISTaskOrderService;
import cc.mrbird.febs.common.annotation.Limit;
import cc.mrbird.febs.common.annotation.Log;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.domain.FebsResponse;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.utils.FebsUtil;
import cc.mrbird.febs.common.utils.WeChatPayUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author MrBird
 */
@Slf4j
@RestController
@RequestMapping("/api/s-offer-price")
public class SOfferPriceController extends BaseController {

    private String message;

    @Autowired
    private ISOfferPriceService offerPriceService;

    @Autowired
    private ISTaskOrderService taskOrderService;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    /**
     * 新增任务报价
     */
    @Log("新增任务报价")
    @PostMapping("/addOfferPrice")
    public FebsResponse addOfferPrice(HttpServletRequest request, @Valid SOfferPrice offerPrice) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {
            // 输入金额必须要大于所有报价中最大报价金额
            // 待完成

            // 先更新出局
            this.offerPriceService.updateOfferPriceOut(offerPrice);

            // 再新建一个新报价
            SUser user = FebsUtil.getCurrentUser();
            offerPrice.setUserId(user.getId());
            offerPrice.setCreateTime(new Date());
            offerPrice.setUpdateTime(new Date());
            offerPrice.setPayStatus(0);
            offerPrice.setStatus(1);

            int offerPriceId = this.offerPriceService.createOfferPrice(offerPrice);

            // 调起微信支付
            JSONObject jsonObject = weChatPayUtil.weChatPay(String.valueOf(offerPriceId),
                    offerPrice.getAmount().toString(),
                    user.getOpenId(),
                    request.getRemoteAddr(),
                    "3",
                    "转让任务报价");

            response.data(jsonObject);

        } catch (Exception e) {
            message = "新增任务报价失败";
            response.put("code", 1);
            response.message(message);
            log.error(message, e);
        }

        return response;
    }

    /**
     * 任务报价成交
     */
    @Log("任务报价成交")
    @PostMapping("/updateOfferPrice")
    public FebsResponse updateOfferPrice(@Valid SOfferPrice offerPrice) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {

            // 修改成交状态
            this.offerPriceService.updateOfferPriceOn(offerPrice);

            // 转让任务状态更新
            STaskOrder taskOrder = this.taskOrderService.getById(offerPrice.getTaskOrderId());
            taskOrder.setStatus(1);
            this.taskOrderService.updateById(taskOrder);

            // 支付金额退还


        } catch (Exception e) {
            message = "任务报价成交失败";
            response.put("code", 1);
            response.message(message);
            log.error(message, e);
        }

        return response;
    }

    /**
     * 根据转让任务ID取得转让任务报价信息
     * @return List<SOfferPrice>
     */
    @PostMapping("/getOfferPriceList")
    @Limit(key = "getOfferPriceList", period = 60, count = 20, name = "检索转让任务报价接口", prefix = "limit")
    public FebsResponse getOfferPriceList(SOfferPrice offerPrice) {

        FebsResponse response = new FebsResponse();

        List<SOfferPrice> offerPriceList = offerPriceService.findOfferPriceList(offerPrice);

        response.put("code", 0);
        response.data(offerPriceList);

        return response;
    }

    /**
     * 根据用户ID和转让任务ID取得用户最后一次对此转让任务报价信息
     * @return SProduct
     */
    @PostMapping("/getOfferPriceDetail")
    @Limit(key = "getOfferPriceDetail", period = 60, count = 20, name = "检索转让任务用户最后报价详情接口", prefix = "limit")
    public FebsResponse getOfferPriceDetail(SOfferPrice offerPrice) {

        FebsResponse response = new FebsResponse();

        SUser user = FebsUtil.getCurrentUser();
        offerPrice.setUserId(user.getId());

        SOfferPrice offerPriceDetail = offerPriceService.findOfferPriceDetail(offerPrice);

        response.put("code", 0);
        response.data(offerPriceDetail);

        return response;
    }

    /**
     * 取得我的报价列表信息
     * @return List<Map>
     */
    @PostMapping("/getMyOfferPriceList")
    @Limit(key = "getMyOfferPriceList", period = 60, count = 20, name = "检索我的任务接口", prefix = "limit")
    public FebsResponse getMyOfferPriceList(QueryRequest queryRequest, SOfferPrice offerPrice) {

        FebsResponse response = new FebsResponse();

        Map<String, Object> offerPricePageList = getDataTable(offerPriceService.findOfferPriceList(offerPrice, queryRequest));

        response.put("code", 0);
        response.data(offerPricePageList);

        return response;
    }

}
