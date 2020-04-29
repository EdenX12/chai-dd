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
import com.baomidou.mybatisplus.core.metadata.IPage;
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
     * 确认拆单
     */
    @Log("确认拆单")
    @Transactional
    @PostMapping("/confirmUserTask")
    public FebsResponse confirmUserTask(HttpServletRequest request, String productId, int taskNumber) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {

//            SUser user = FebsUtil.getCurrentUser();
//            userTask.setUserId(user.getId());
//
//            // 每件商品最高领取任务线判断
//            SUserLevel userLevel = this.userLevelService.findByLevelType(user.getUserLevelType());
//
//            if (userTask.getTaskNumber() > userLevel.getBuyNumber()) {
//                response.put("code", 1);
//                response.message("您已超过此商品领取任务上限" + userLevel.getBuyNumber() + "份！");
//                return response;
//            }

//            // 新手只能购买新手标  其他只能购买正常标
//            SProduct product = this.productService.getById(userTask.getProductId());
//
//            if (userLevel.getLevelType() == 0 && product.getProductType() == 2) {
//                response.put("code", 1);
//                response.message("抱歉！您现在只能在新手区领取新手任务！");
//                return response;
//            }
//
//            if (userLevel.getLevelType() > 0 && product.getProductType() == 1) {
//                response.put("code", 1);
//                response.message("抱歉！您已经不能再次领取新手任务！");
//                return response;
//            }
//
//            // 最多并行商品件数
//            Integer productCount = this.userTaskService.findProductCount(userTask);
//
//            if (productCount >= userLevel.getProductNumber()) {
//                response.put("code", 1);
//                response.message("抱歉！您已超过领取商品件数的任务！");
//                return response;
//            }
//
//            if (userTask.getId() == null) {
//                userTask.setPayStatus(2);
////                userTask.setStatus(0);
////                userTask.setShareFlag(0);
//                userTask.setCreateTime(new Date());
//                userTask.setUpdateTime(new Date());
//
//                userTask = this.userTaskService.createUserTask(userTask);
//            } else {
//                userTask.setUpdateTime(new Date());
//
//                userTask = this.userTaskService.updateUserTask(userTask);
//            }
//
////            // 调起微信支付
////            JSONObject jsonObject = this.weChatPayUtil.weChatPay(String.valueOf(userTask.getId()),
////                    product.getTaskPrice().multiply(BigDecimal.valueOf(userTask.getTaskNumber().longValue())).toString(),
////                    user.getOpenId(),
////                    request.getRemoteAddr(),
////                    "1",
////                    "任务金");
//
////            response.data(jsonObject);

        } catch (Exception e) {
            message = "确认拆单失败！";
            response.put("code", 1);
            response.message(message);
            log.error(message, e);
        }

        return response;
    }

    /**
     * 确认支付领取任务
     */
    @Log("确认支付领取任务")
    @Transactional
    @PostMapping("/payUserTask")
    public FebsResponse payUserTask(HttpServletRequest request, String productId, int taskNumber, String userCouponId) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);




        return response;
    }


    /**
     * 任务分享成功之后调用
     * @return List<Map>
     */
    @Transactional
    @PostMapping("/getShareTaskSuccess")
    @Limit(key = "getShareTaskSuccess", period = 60, count = 20, name = "根据任务ID检索商品详情接口", prefix = "limit")
    public FebsResponse getShareTaskSuccess(String userTaskId) {

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

//            if (userTask.getShareFlag() == 0) {
//
//                SUserLevel userLevel = this.userLevelService.findByLevelType(user.getUserLevelType());
//                user.setCanuseBean(user.getCanuseBean() + userLevel.getBeanRate().multiply(BigDecimal.valueOf(10)).intValue());
//                this.userService.updateById(user);
//
//                // 猎豆流水插入
//                SUserBeanLog userBeanLog = new SUserBeanLog();
//                userBeanLog.setUserId(user.getId());
//                userBeanLog.setChangeType(2);
//                userBeanLog.setChangeAmount(userLevel.getBeanRate().multiply(BigDecimal.valueOf(10)).intValue());
//                userBeanLog.setChangeTime(new Date());
//                userBeanLog.setRelationId(userTask.getId());
//                userBeanLog.setRemark("关联任务ID");
//                userBeanLog.setOldAmount(user.getCanuseBean());
//                this.userBeanLogService.save(userBeanLog);
//            }
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
        returnMap.put("payStatus", userTask.getPayStatus());
        // 用户名称
        returnMap.put("userName", taskUser.getUserName());
        // 用户电话
        returnMap.put("userPhone", taskUser.getUserPhone());
        // 用户已领取任务
//        returnMap.put("userTaskNumber", userTask.getTaskNumber());
        // 商品ID
        returnMap.put("productId", product.getId());
        // 产品类型（新手标 正常标）
        returnMap.put("productType", product.getProductType());
        returnMap.put("productName", product.getProductName());
        returnMap.put("productDes", product.getProductDes());
        returnMap.put("productDetail", product.getProductDetail());
        returnMap.put("productImg", product.getProductImg());
        returnMap.put("productPrice", product.getProductPrice());
        returnMap.put("totalReward", product.getTotalReward());
//        returnMap.put("successReward", product.getSuccessReward());
//        returnMap.put("everyReward", product.getEveryReward());
//        // 总任务数
        returnMap.put("taskNumber", product.getTaskNumber());
        returnMap.put("taskPrice", product.getTaskPrice());
        returnMap.put("followCount", followCount);
        if(userFollowDetail!=null) {
        returnMap.put("followStatus", userFollowDetail.getStatus());
        }else {
        	 returnMap.put("followStatus",null);	
        }

        // 辛苦费 见习猎人分0.5%; 初级猎手分1% 中级猎人分2% 高级猎人分3%
        SUserLevel userLevel = this.userLevelService.findByLevelType(user.getUserLevelType());
//        returnMap.put("commissionFee", userLevel.getIncomeRate().multiply(product.getTotalReward()));


        // 如果是本人打开自己的分享页面时，直接返回
        if (userTask.getUserId() == user.getId()) {
            returnMap.put("taskId", userTask.getId());
        } else {

            // 第一次打开的话，生成新任务（未支付  未分享）
            SUserTask searchUserTask = new SUserTask();
            searchUserTask.setUserId(user.getId());
            searchUserTask.setProductId(userTask.getProductId());
            //searchUserTask.setParentId(userTaskId);
            List<SUserTask> userTaskOne = this.userTaskService.findUserTaskList(searchUserTask);
            Long newTaskId;
//            if (userTaskOne == null || userTaskOne.size() == 0) {
//                searchUserTask.setPayStatus(2);
//                searchUserTask.setTaskNumber(1);
//                searchUserTask.setStatus(0);
//                searchUserTask.setShareFlag(0);
//                searchUserTask.setCreateTime(new Date());
//                searchUserTask.setUpdateTime(new Date());
//                SUserTask newUserTask = this.userTaskService.createUserTask(searchUserTask);
//                // 新生成的任务ID
//                returnMap.put("taskId", newUserTask.getId());
//
//                // 有人查看或转发“我”分享的任务时，“我”获10颗
//                // 猎豆追加 本人（10颗） * 猎人等级倍数
//                SUser user2 = this.userService.getById(userTask.getUserId());
//                SUserLevel userLevel2 = this.userLevelService.findByLevelType(user2.getUserLevelType());
//                user2.setCanuseBean(user2.getCanuseBean() + userLevel2.getBeanRate().multiply(BigDecimal.valueOf(10)).intValue());
//                this.userService.updateById(user2);
//
//                // 猎豆流水插入
//                SUserBeanLog userBeanLog = new SUserBeanLog();
//                userBeanLog.setUserId(user2.getId());
//                userBeanLog.setChangeType(7);
//                userBeanLog.setChangeAmount(userLevel2.getBeanRate().multiply(BigDecimal.valueOf(10)).intValue());
//                userBeanLog.setChangeTime(new Date());
//                userBeanLog.setRelationId(userTask.getId());
//                userBeanLog.setRemark("关联任务ID");
//                userBeanLog.setOldAmount(user.getCanuseBean());
//                this.userBeanLogService.save(userBeanLog);
//            } else {
//                returnMap.put("taskId", userTaskOne.get(0).getId());
//            }
        }

        response.data(returnMap);

        return response;
    }


    /**
     * 取得我的任务【进行中】列表信息
     * @return List<Map>
     */
    @PostMapping("/getUserTaskingList")
    @Limit(key = "getUserTaskingList", period = 60, count = 20, name = "检索我的任务【进行中】接口", prefix = "limit")
    public FebsResponse getUserTaskingList(QueryRequest queryRequest) {

        FebsResponse response = new FebsResponse();

        SUser user = FebsUtil.getCurrentUser();

        // 我的进行中任务
        IPage<Map> result = this.userTaskService.findTaskDetailByStatus(
                queryRequest, user.getId(),0);

        if (result != null) {
            List<Map> userTaskList = result.getRecords();
            for (Map userTask : userTaskList) {
                Map<String, Object> productDetail = this.productService.findProductDetail(
                        userTask.get("productId").toString());

                userTask.put("productDetail", productDetail);
            }

            result.setRecords(userTaskList);
        }

        Map<String, Object> userTaskPageList = getDataTable(result);

        response.put("code", 0);
        response.data(userTaskPageList);

        return response;
    }

    /**
     * 取得我的任务【已关注】列表信息
     * @return List<Map>
     */
    @PostMapping("/getUserTaskFollowList")
    @Limit(key = "getUserTaskFollowList", period = 60, count = 20, name = "检索我的任务【已关注】接口", prefix = "limit")
    public FebsResponse getUserTaskFollowList(QueryRequest queryRequest) {

        FebsResponse response = new FebsResponse();

        SUser user = FebsUtil.getCurrentUser();

        // 我的关注中任务
        IPage<Map> result = this.userTaskService.findUserTaskFollowList(
                queryRequest, user.getId());

        if (result != null) {
            List<Map> userTaskList = result.getRecords();
            for (Map userTask : userTaskList) {
                Map<String, Object> productDetail = this.productService.findProductDetail(userTask.get("productId").toString());

                userTask.put("productDetail", productDetail);
            }

            result.setRecords(userTaskList);
        }

        Map<String, Object> userTaskPageList = getDataTable(result);

        response.put("code", 0);
        response.data(userTaskPageList);

        return response;
    }

    /**
     * 取得我的任务【已完成】列表信息
     * @return List<Map>
     */
    @PostMapping("/getUserTaskEndList")
    @Limit(key = "getUserTaskEndList", period = 60, count = 20, name = "检索我的任务【已完成】接口", prefix = "limit")
    public FebsResponse getUserTaskEndList(QueryRequest queryRequest) {

        FebsResponse response = new FebsResponse();

        SUser user = FebsUtil.getCurrentUser();

        // 我的完成中任务
        IPage<Map> result = this.userTaskService.findTaskDetailByStatus(
                queryRequest, user.getId(),4);

        if (result != null) {
            List<Map> userTaskList = result.getRecords();
            for (Map userTask : userTaskList) {
                Map<String, Object> productDetail = this.productService.findProductDetail(userTask.get("productId").toString());

                userTask.put("productDetail", productDetail);
            }

            result.setRecords(userTaskList);
        }

        Map<String, Object> userTaskPageList = getDataTable(result);

        response.put("code", 0);
        response.data(userTaskPageList);

        return response;
    }

    /**
     * 取得我的任务【结算中】列表信息
     * @return List<Map>
     */
    @PostMapping("/getTaskSettlementList")
    @Limit(key = "getTaskSettlementList", period = 60, count = 20, name = "检索我的任务【结算中】接口", prefix = "limit")
    public FebsResponse getTaskSettlementList(QueryRequest queryRequest) {

        FebsResponse response = new FebsResponse();

        SUser user = FebsUtil.getCurrentUser();

        // 我的结算中任务
        IPage<Map> result = this.userTaskService.findTaskDetailByStatus(
                queryRequest, user.getId(),3);

        if (result != null) {
            List<Map> userTaskList = result.getRecords();
            for (Map userTask : userTaskList) {
                Map<String, Object> productDetail = this.productService.findProductDetail(
                        userTask.get("productId").toString());

                userTask.put("productDetail", productDetail);
            }

            result.setRecords(userTaskList);
        }

        Map<String, Object> userTaskPageList = getDataTable(result);

        response.put("code", 0);
        response.data(userTaskPageList);

        return response;
    }

    /**
     * 取得我的任务【转出中】列表信息
     * @return List<Map>
     */
    @PostMapping("/getUserTaskOutingList")
    @Limit(key = "getUserTaskOutingList", period = 60, count = 20, name = "检索我的任务【转出中】接口", prefix = "limit")
    public FebsResponse getUserTaskOutingList(QueryRequest queryRequest, SUserTask userTask) {

        FebsResponse response = new FebsResponse();

        SUser user = FebsUtil.getCurrentUser();
        userTask.setUserId(user.getId());

        Map<String, Object> userTaskPageList = getDataTable(this.userTaskService.findUserTaskOutList(userTask, queryRequest));

        response.put("code", 0);
        response.data(userTaskPageList);

        return response;
    }

    /**
     * 取得我的任务【收购中】列表信息
     * @return List<Map>
     */
    @PostMapping("/getUserTaskOfferingList")
    @Limit(key = "getUserTaskOfferingList", period = 60, count = 20, name = "检索我的任务【收购中】接口", prefix = "limit")
    public FebsResponse getUserTaskOfferingList(QueryRequest queryRequest, SUserTask userTask) {

        FebsResponse response = new FebsResponse();

        SUser user = FebsUtil.getCurrentUser();
        userTask.setUserId(user.getId());

        Map<String, Object> userTaskPageList = getDataTable(
                this.userTaskService.findUserTaskOfferList(userTask, queryRequest));

        response.put("code", 0);
        response.data(userTaskPageList);

        return response;
    }
}
