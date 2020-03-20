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
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
    private ISUserFollowService userFollowService;

    @Autowired
    private ISUserBeanLogService userBeanLogService;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    /**
     * 新增用户任务
     */
    @Log("新增用户任务")
    @Transactional
    @PostMapping("/addUserTask")
    public FebsResponse addUserTask(HttpServletRequest request, @Valid SUserTask userTask) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {

            SUser user = FebsUtil.getCurrentUser();

            // 最大购买份数判断
            SUserLevel userLevel = this.userLevelService.getById(user.getUserLevelId());

            // 已购买任务  （现在 转让成功 的 也算在内  后续可能需要调整）
            SUserTask oldUserTask = new SUserTask();
            oldUserTask.setUserId(user.getId());
            oldUserTask.setPayStatus(1);
            oldUserTask.setProductId(userTask.getProductId());
            List<SUserTask> oldUserTaskList = this.userTaskService.findUserTaskList(oldUserTask);
            int oldTaskNumber = 0;
            for (SUserTask userTask1 : oldUserTaskList) {
                oldTaskNumber = oldTaskNumber + userTask1.getTaskNumber();
            }

            // 上限不仅是这次的领取份数要加上之前的领取份数
            if (oldTaskNumber + userTask.getTaskNumber() > userLevel.getBuyNumber()) {
                response.put("code", 1);
                response.message("您已超过此商品领取任务上限" + userLevel.getBuyNumber() + "份！");
                return response;
            }

            // 老手不能购买新手标
            SProduct product = this.productService.getById(userTask.getProductId());

            if (userLevel.getLevelType() > 1 && product.getProductType() == 1) {

                response.put("code", 1);
                response.message("抱歉！您已不能领取新手任务！");
                return response;
            }

            // 用户ID
            userTask.setUserId(user.getId());

            if (userTask.getId() == null) {
                userTask.setPayStatus(2);
                userTask.setStatus(0);
                userTask.setShareFlag(0);
                userTask.setCreateTime(new Date());
                userTask.setUpdateTime(new Date());

                this.userTaskService.createUserTask(userTask);
            } else {
                userTask.setUpdateTime(new Date());

                this.userTaskService.updateUserTask(userTask);
            }

            // 调起微信支付
            JSONObject jsonObject = this.weChatPayUtil.weChatPay(String.valueOf(userTask.getId()),
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
     * 任务分享成功之后调用
     * @return List<Map>
     */
    @Transactional
    @PostMapping("/getShareTaskSuccess")
    @Limit(key = "getShareTaskSuccess", period = 60, count = 20, name = "根据任务ID检索商品详情接口", prefix = "limit")
    public FebsResponse getShareTaskSuccess(Long userTaskId) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        SUser user = FebsUtil.getCurrentUser();

        // 猎豆追加 10颗  * 猎人等级倍数  分享只计算一次
        SUserTask userTask = new SUserTask();

        userTask.setId(userTaskId);
        userTask.setUserId(user.getId());

        List<SUserTask> userTaskList = this.userTaskService.findUserTaskList(userTask);

        // 未分享过的任务
        if (userTaskList != null && userTaskList.size() > 0) {
            userTask = userTaskList.get(0);

            if (userTask.getShareFlag() == 0) {

                SUserLevel userLevel = this.userLevelService.getById(user.getUserLevelId());
                user.setCanuseBean(user.getCanuseBean() + userLevel.getBeanRate().multiply(BigDecimal.valueOf(10)).intValue());
                this.userService.updateById(user);

                // 猎豆流水插入
                SUserBeanLog userBeanLog = new SUserBeanLog();
                userBeanLog.setUserId(user.getId());
                userBeanLog.setChangeType(2);
                userBeanLog.setChangeAmount(userLevel.getBeanRate().multiply(BigDecimal.valueOf(10)).intValue());
                userBeanLog.setChangeTime(new Date());
                userBeanLog.setRelationId(userTask.getId());
                userBeanLog.setRemark("关联任务ID");
                userBeanLog.setOldAmount(user.getCanuseBean());
                this.userBeanLogService.save(userBeanLog);
            }
        }

        return response;
    }

    /**
     * 根据任务ID获取个人信息及商品详情（被分享转发页面读取）
     * @return List<Map>
     */
    @Transactional
    @PostMapping("/getProductByTaskId")
    @Limit(key = "getProductByTaskId", period = 60, count = 20, name = "根据任务ID检索商品详情接口", prefix = "limit")
    public FebsResponse getProductByTaskId(Long userTaskId) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        SUser user = FebsUtil.getCurrentUser();

        // 任务信息
        SUserTask userTask = this.userTaskService.getById(userTaskId);

        // 转发任务的用户信息
        SUser taskUser = this.userService.getById(userTask.getUserId());

        // 商品信息
        SProduct product = this.productService.getById(userTask.getProductId());

        // 商品关注数量
        SUserFollow userFollowCount = new SUserFollow();
        userFollowCount.setProductId(userTask.getProductId());
        userFollowCount.setFollowType(0);
        int followCount = this.userFollowService.findUserFollowCount(userFollowCount);

        // 用户是否已关注
        SUserFollow userFollowDetail = new SUserFollow();
        userFollowDetail.setProductId(userTask.getProductId());
        userFollowDetail.setFollowType(0);
        userFollowDetail.setUserId(user.getId());
        userFollowDetail = this.userFollowService.findUserFollowDetail(userFollowDetail);

        Map<String, Object> returnMap = new HashMap<>();

        // 用户名称
        returnMap.put("userName", taskUser.getUserName());
        // 用户电话
        returnMap.put("userPhone", taskUser.getUserPhone());
        // 用户已领取任务
        returnMap.put("userTaskNumber", userTask.getTaskNumber());
        // 商品ID
        returnMap.put("productId", product.getId());
        // 产品类型（新手标 正常标）
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
        // 总任务数
        returnMap.put("taskNumber", product.getTaskNumber());
        returnMap.put("taskPrice", product.getTaskPrice());
        returnMap.put("followCount", followCount);
        returnMap.put("followStatus", userFollowDetail.getStatus());

        // 辛苦费 见习猎人分0.5%; 初级猎手分1% 中级猎人分2% 高级猎人分3%
        SUserLevel userLevel = this.userLevelService.getById(user.getUserLevelId());
        returnMap.put("commissionFee", userLevel.getIncomeRate().multiply(product.getTotalReward()));

        // 第一次打开的话，生成新任务（未支付  未分享）
        SUserTask searchUserTask = new SUserTask();
        searchUserTask.setUserId(user.getId());
        searchUserTask.setProductId(userTask.getProductId());
        searchUserTask.setParentId(userTaskId);
        List<SUserTask> userTaskOne = this.userTaskService.findUserTaskList(searchUserTask);
        Long newTaskId;
        if (userTaskOne == null) {
            searchUserTask.setPayStatus(2);
            searchUserTask.setTaskNumber(1);
            searchUserTask.setStatus(0);
            searchUserTask.setShareFlag(0);
            searchUserTask.setCreateTime(new Date());
            searchUserTask.setUpdateTime(new Date());
            newTaskId = this.userTaskService.createUserTask(searchUserTask);
            // 新生成的任务ID
            returnMap.put("taskId", newTaskId);

            // 有人查看或转发“我”分享的任务时，“我”获10颗
            // 猎豆追加 本人（10颗） * 猎人等级倍数
            SUser user2 = this.userService.getById(userTask.getUserId());
            SUserLevel userLevel2 = this.userLevelService.getById(user2.getUserLevelId());
            user2.setCanuseBean(user2.getCanuseBean() + userLevel2.getBeanRate().multiply(BigDecimal.valueOf(10)).intValue());
            this.userService.updateById(user2);

            // 猎豆流水插入
            SUserBeanLog userBeanLog = new SUserBeanLog();
            userBeanLog.setUserId(user2.getId());
            userBeanLog.setChangeType(7);
            userBeanLog.setChangeAmount(userLevel2.getBeanRate().multiply(BigDecimal.valueOf(10)).intValue());
            userBeanLog.setChangeTime(new Date());
            userBeanLog.setRelationId(userTask.getId());
            userBeanLog.setRemark("关联任务ID");
            userBeanLog.setOldAmount(user.getCanuseBean());
            this.userBeanLogService.save(userBeanLog);
        } else {
            returnMap.put("taskId", userTaskOne.get(0).getId());
        }

        response.data(returnMap);

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

        Map<String, Object> userTaskPageList = getDataTable(this.userTaskService.findUserTaskList(userTask, queryRequest));

        response.put("code", 0);
        response.data(userTaskPageList);

        return response;
    }

}
