package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.SProduct;
import cc.mrbird.febs.api.entity.SUser;
import cc.mrbird.febs.api.entity.SUserLevel;
import cc.mrbird.febs.api.entity.SUserTask;
import cc.mrbird.febs.api.service.ISProductService;
import cc.mrbird.febs.api.service.ISUserLevelService;
import cc.mrbird.febs.api.service.ISUserService;
import cc.mrbird.febs.api.service.ISUserTaskService;
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
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author MrBird
 */
@Slf4j
@RestController
@RequestMapping("/api/s-user-task")
public class SUserTaskController extends BaseController {

    private String message;

    @Autowired
    private ISUserTaskService userTaskService;

    @Autowired
    private ISUserLevelService userLevelService;

    @Autowired
    private ISProductService productService;

    @Autowired
    private ISUserService userService;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    /**
     * 新增用户任务
     */
    @Log("新增用户任务")
    @PostMapping("/addUserTask")
    public FebsResponse addUserTask(HttpServletRequest request, @Valid SUserTask userTask) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {

            SUser user = FebsUtil.getCurrentUser();

            // 最大购买份数判断
            SUserLevel userLevel = userLevelService.getById(user.getUserLevelId());
            //上限不仅是这次的领取份数要加上之前的领取份数----代码需要完善
            if (userTask.getTaskNumber() > userLevel.getBuyNumber()) {
                response.put("code", 1);
                response.message("超过领取任务上限" + userLevel.getBuyNumber() + "份！");
                return response;
            }

            // 老手不能购买新手标
            SProduct product = productService.getById(userTask.getProductId());

            if (userLevel.getLevelType() > 1 && product.getProductType() == 1) {

                response.put("code", 1);
                response.message("抱歉！您已不能领取新手任务！");
                return response;
            }

            // 用户ID
            userTask.setUserId(user.getId());

            // 已接任务
            userTask.setStatus(0);

            int intTaskId = 0;
            SUserTask userTaskOne = userTaskService.findUserTask(userTask);
            if (userTaskOne == null) {
                userTask.setCreateTime(new Date());
                userTask.setUpdateTime(new Date());
                intTaskId = this.userTaskService.createUserTask(userTask);
            } else {
                userTask.setUpdateTime(new Date());
                intTaskId = this.userTaskService.updateUserTask(userTask);
            }

            // 调起微信支付
            JSONObject jsonObject = weChatPayUtil.weChatPay(String.valueOf(intTaskId),
                    product.getTaskPrice().multiply(BigDecimal.valueOf(userTask.getTaskNumber().longValue())).toString(),
                    user.getOpenId(),
                    request.getRemoteAddr(),
                    "1",
                    "任务金");

            response.data(jsonObject);

        } catch (Exception e) {
            message = "领取任务失败！";
            response.put("code", 1);
            response.message(message);
            log.error(message, e);
        }

        return response;
    }

    /**
     * 根据任务ID获取个人信息及商品详情（被分享转发页面读取）
     * @return List<Map>
     */
    @PostMapping("/getProductByTaskId")
    @Limit(key = "getProductByTaskId", period = 60, count = 20, name = "根据任务ID检索商品详情接口", prefix = "limit")
    public FebsResponse getProductByTaskId(Long userTaskId) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        SUser user = FebsUtil.getCurrentUser();

        // 任务信息
        SUserTask userTask = userTaskService.getById(userTaskId);

        // 转发任务的用户信息
        SUser taskUser = userService.getById(userTask.getUserId());

        // 商品信息
        SProduct product = productService.getById(userTask.getProductId());

        Map<String, Object> returnMap = new HashMap<>();

        returnMap.put("userName", taskUser.getUserName());
        returnMap.put("userPhone", taskUser.getUserPhone());
        returnMap.put("productId", product.getId());
        returnMap.put("productType", product.getProductType());
        returnMap.put("productName", product.getProductName());
        returnMap.put("productDes", product.getProductDes());
        returnMap.put("productDetail", product.getProductDetail());
        returnMap.put("productImg", product.getProductImg());
        returnMap.put("productPrice", product.getProductPrice());
        returnMap.put("priceUnit", product.getPriceUnit());
        returnMap.put("totalReward", product.getTotalReward());
        returnMap.put("successReward", product.getSuccessReward());
        returnMap.put("everyReward", product.getEveryReward());
        returnMap.put("taskNumber", product.getTaskNumber());
        returnMap.put("taskPrice", product.getTaskPrice());
        returnMap.put("productId", product.getId());
        // 辛苦费 见习猎人分0.5%; 初级猎手分1% 中级猎人分2% 高级猎人分3%

        SUserLevel userLevel = userLevelService.getById(user.getUserLevelId());
        returnMap.put("commissionFee", userLevel.getIncomeRate().multiply(product.getTotalReward()));

        response.data(returnMap);

        // 第一次打开的话，生成新任务（未支付  未分享）
        SUserTask searchUserTask = new SUserTask();
        searchUserTask.setUserId(user.getId());
        searchUserTask.setProductId(userTask.getProductId());
        SUserTask userTaskOne = userTaskService.findUserTask(searchUserTask);
        if (userTaskOne == null) {
            searchUserTask.setParentId(userTaskId);
            searchUserTask.setPayStatus(2);
            searchUserTask.setTaskNumber(1);
            searchUserTask.setStatus(0);
            searchUserTask.setShareFlag(0);
            searchUserTask.setCreateTime(new Date());
            searchUserTask.setUpdateTime(new Date());
            userTaskService.createUserTask(searchUserTask);
        }

        return response;
    }

    /**
     * 取得我的任务列表信息
     * @return List<Map>
     */
    @PostMapping("/getUserTaskList")
    @Limit(key = "getUserTaskList", period = 60, count = 20, name = "检索我的任务接口", prefix = "limit")
    public FebsResponse getTaskOrderList(QueryRequest queryRequest, SUserTask userTask) {

        FebsResponse response = new FebsResponse();

        Map<String, Object> userTaskPageList = getDataTable(userTaskService.findUserTaskList(userTask, queryRequest));

        response.put("code", 0);
        response.data(userTaskPageList);

        return response;
    }

}
