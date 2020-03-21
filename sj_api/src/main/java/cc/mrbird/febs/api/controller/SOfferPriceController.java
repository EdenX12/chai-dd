package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.*;
import cc.mrbird.febs.api.service.*;
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
import org.springframework.transaction.annotation.Transactional;
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
    private ISUserTaskService userTaskService;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private ISUserService userService;

    @Autowired
    private ISUserAmountLogService userAmountLogService;

    /**
     * 新增任务报价
     */
    @Log("新增任务报价")
    @Transactional
    @PostMapping("/addOfferPrice")
    public FebsResponse addOfferPrice(HttpServletRequest request, @Valid SOfferPrice offerPrice) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {

            // TODO 输入金额必须要大于所有报价中最大报价金额

            // 先更新出局
            this.offerPriceService.updateOfferPriceOut(offerPrice);

            // 再新建一个新报价
            SUser user = FebsUtil.getCurrentUser();
            offerPrice.setUserId(user.getId());
            offerPrice.setCreateTime(new Date());
            offerPrice.setUpdateTime(new Date());
            offerPrice.setPayStatus(0);
            offerPrice.setStatus(1);

            Long offerPriceId = this.offerPriceService.createOfferPrice(offerPrice);

            // 调起微信支付
            JSONObject jsonObject = this.weChatPayUtil.weChatPay(String.valueOf(offerPriceId),
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
    @Transactional
    @PostMapping("/updateOfferPrice")
    public FebsResponse updateOfferPrice(@Valid SOfferPrice offerPrice) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {

            // 修改成交状态 （竞标中 - > 已成交）
            this.offerPriceService.updateOfferPriceOn(offerPrice);

            // 转让任务状态更新 （转让中 - > 已成交）
            STaskOrder taskOrder = this.taskOrderService.getById(offerPrice.getTaskOrderId());
            taskOrder.setStatus(1);
            this.taskOrderService.updateById(taskOrder);

            // 用户任务表状态更新（转让中 -> 转让成功）
            SUserTask userTaskOld = this.userTaskService.getById(taskOrder.getTaskId());
            userTaskOld.setStatus(2);
            userTaskOld.setUpdateTime(new Date());
            this.userTaskService.updateById(userTaskOld);

            // 增加新用户的任务（已接任务）
            SUserTask userTaskNew = new SUserTask();
            userTaskNew.setUserId(offerPrice.getUserId());
            userTaskNew.setProductId(userTaskOld.getProductId());
            userTaskNew.setParentId(userTaskOld.getParentId());
            userTaskNew.setPayStatus(userTaskOld.getPayStatus());
            userTaskNew.setTaskNumber(userTaskOld.getTaskNumber());
            userTaskNew.setStatus(0);
            userTaskNew.setShareFlag(0);
            userTaskNew.setCreateTime(new Date());
            userTaskNew.setUpdateTime(new Date());
            this.userTaskService.createUserTask(userTaskNew);

            // 出局者支付金额退还（冻结 -> 余额）
            List<SOfferPrice> offerPriceOutList = this.offerPriceService.findOfferPriceOutList(offerPrice);
            for (SOfferPrice offerPriceOut : offerPriceOutList) {

                SUser user = this.userService.getById(offerPriceOut.getUserId());

                // 金额流水插入
                SUserAmountLog userAmountLog = new SUserAmountLog();
                userAmountLog.setUserId(user.getId());
                userAmountLog.setChangeType(8);
                userAmountLog.setChangeAmount(offerPriceOut.getAmount());
                userAmountLog.setChangeTime(new Date());
                userAmountLog.setRelationId(offerPriceOut.getId());
                userAmountLog.setRemark("关联报价ID");
                userAmountLog.setOldAmount(user.getTotalAmount());
                this.userAmountLogService.save(userAmountLog);

                // 冻结金额-
                user.setLockAmount(user.getLockAmount().subtract(offerPriceOut.getAmount()));
                // 余额+
                user.setTotalAmount(user.getTotalAmount().add(offerPriceOut.getAmount()));

                this.userService.updateById(user);
            }

            // 转让任务的人 领取任务或者报价任务成交的金额解冻   成交金额到余额
            SUser user = this.userService.getById(userTaskOld.getUserId());

            // 金额流水插入
            SUserAmountLog userAmountLog = new SUserAmountLog();
            userAmountLog.setUserId(user.getId());
            userAmountLog.setChangeType(7);
            userAmountLog.setChangeAmount(offerPrice.getAmount());
            userAmountLog.setChangeTime(new Date());
            userAmountLog.setRelationId(userTaskOld.getId());
            userAmountLog.setRemark("关联任务ID");
            userAmountLog.setOldAmount(user.getTotalAmount());
            this.userAmountLogService.save(userAmountLog);

            // 冻结金额-原来支付金额
            user.setLockAmount(user.getLockAmount().subtract(userTaskOld.getPayAmount()));
            // 余额+
            user.setTotalAmount(user.getTotalAmount().add(offerPrice.getAmount()));
            this.userService.updateById(user);

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

        List<SOfferPrice> offerPriceList = this.offerPriceService.findOfferPriceList(offerPrice);

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

        SOfferPrice offerPriceDetail = this.offerPriceService.findOfferPriceDetail(offerPrice);

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

        Map<String, Object> offerPricePageList = getDataTable(this.offerPriceService.findOfferPriceList(offerPrice, queryRequest));

        response.put("code", 0);
        response.data(offerPricePageList);

        return response;
    }

}
