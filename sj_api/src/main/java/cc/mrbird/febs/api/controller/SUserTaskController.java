package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.dto.PayUserTaskDto;
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
import com.google.common.collect.Lists;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
@Configuration
@EnableScheduling
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

    @Autowired
    private ISParamsService paramsService;

    @Autowired
    private ISTaskLineService taskLineService;

    @Autowired
    private ISUserCouponService userCouponService;

    @Autowired
    private ISTaskCouponService taskCouponService;

    @Autowired
    private ISUserTaskLineService userTaskLineService;

    @Autowired
    private ISUserCouponLogService userCouponLogService;

    @Autowired
    private ISUserShareService userShareService;

    @Autowired
    private ISUserBrowserService userBrowserService;

    /**
     * 确认拆单
     */
    @Log("确认拆单")
    @PostMapping("/confirmUserTask")
    public FebsResponse confirmUserTask(HttpServletRequest request,
                                        @NotNull(message="商品ID不可空") String productId,
                                        @NotNull(message="商品ID不可空")Integer taskNumber,
                                        String userCouponId) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        Map<String, Object> resultData = new HashMap();

        try {

            SUser user = FebsUtil.getCurrentUser();
            String userId = user.getId();

            // 检查任务数量、优惠券
            String errorMessage = this.validateTask(user, productId, taskNumber, userCouponId);
            if (StringUtils.isNotBlank(errorMessage)) {
                response.put("code", 1);
                response.message(errorMessage);
                return response;
            }

            // 商品信息取得
            Map<String, Object> productInfo = this.productService.findProductDetail(productId);
            resultData.put("productInfo", productInfo);

            // 计算总任务金、优惠金额、实付金额等
            this.cluTaskAmt(productId,
                    taskNumber,
                    userCouponId,
                    resultData,
                    (BigDecimal) productInfo.get("taskPrice"));

            response.put("code", 0);
            response.data(resultData);

        } catch(Exception e) {
            message = "确认拆单失败！";
            response.put("code", 1);
            response.message(message);
            log.error(message, e);
        }

        return response;
    }

    /**
     * 计算总任务金、优惠金额、实付金额等
     */
    private void cluTaskAmt (String productId,
                              Integer taskNumber,
                              String userCouponId,
                              Map<String, Object> resultData,
                              BigDecimal taskPrice){
        // 任务金合计
        BigDecimal totalAmt = new BigDecimal(0);
        // 实付金额
        BigDecimal needPayAmt = new BigDecimal(0);
        // 优惠金额
        BigDecimal couponAmt = new BigDecimal(0);
        // 优惠券使用条件
        int userCon = 0;

        // 优惠金额及优惠使用条件取得
        if (userCouponId != null && !"".equals(userCouponId)) {
            SUserCoupon userCoupon = this.userCouponService.getById(userCouponId);
            if (userCoupon != null) {
                STaskCoupon coupon = this.taskCouponService.getById(userCoupon.getCouponId());
                if (coupon != null) {
                    couponAmt = coupon.getCouponAmount();
                    userCon = coupon.getUseCon();
                }
            }
        }

        // 优惠券金额
        resultData.put("couponAmt", couponAmt);

        // 任务金合计
        resultData.put("totalAmt", taskPrice.multiply(new BigDecimal(taskNumber)));

        // 实付金额
        needPayAmt = (taskPrice.multiply(new BigDecimal(taskNumber))).subtract(couponAmt);

        // 如果是【2：超级抵扣券】的情况
        if(userCon == 2){
            needPayAmt = BigDecimal.ZERO;
        }
        resultData.put("needPayAmt", needPayAmt);

        // 赠送拆豆取得
        Integer orderBeanCnt = 0;
        SParams params = this.paramsService.queryBykeyForOne("order_bean_cnt");
        if (params != null) {
            orderBeanCnt = Integer.valueOf(params.getPValue());
        }
        resultData.put("orderBeanCnt", orderBeanCnt);
    }

    /**
     * 确认支付领取任务
     */
    @Log("确认支付领取任务")
    @Transactional
    @PostMapping("/payUserTask")
    public FebsResponse payUserTask(HttpServletRequest request,
                                    String productId,
                                    Integer taskNumber,
                                    String userCouponId) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {

            // 支付金额等详细额度
            Map<String, Object> taskAmtMap = new HashMap();

            SUser user = FebsUtil.getCurrentUser();
            String userId = user.getId();

            // 检查任务数量、优惠券
            String errorMessage = this.validateTask(user, productId, taskNumber, userCouponId);
            if (StringUtils.isNotBlank(errorMessage)) {
                response.put("code", 1);
                response.message(errorMessage);
                return response;
            }

            SProduct product = this.productService.getById(productId);

            // 计算总任务金、优惠金额、实付金额等
            this.cluTaskAmt(productId,
                    taskNumber,
                    userCouponId,
                    taskAmtMap,
                    product.getTaskPrice());

            // 实付金额
            BigDecimal needPayAmt = (BigDecimal) taskAmtMap.get("needPayAmt");

            // 奖励拆豆
            Integer orderBeanCnt = (Integer) taskAmtMap.get("orderBeanCnt");

            // 生成s_user_task一条数据
            SUserTask userTask = new SUserTask();

            // 0-APP,1-微信公众号,2-小程序
            userTask.setChannel(2);
            userTask.setPayTime(new Date());
            userTask.setPayAmount(needPayAmt);
            if (needPayAmt.compareTo(BigDecimal.ZERO) > 0) {
                // 锁定
                userTask.setPayStatus(0);
            } else {
                // 已支付
                userTask.setPayStatus(1);
            }
            userTask.setUserId(userId);
            userTask.setUserCouponId(userCouponId);
            userTask.setCreateTime(new Date());
            userTask.setUpdateTime(new Date());
            userTask = userTaskService.createUserTask(userTask);

            // 根据商品ID、任务数从s_task_line表中分配N条任务线
            // 并且在s_user_task_line表上生成N条数据
            Integer minLineOrder = this.taskLineService.queryMinLineOrder(productId);

            List<STaskLine> updateTaskLineList = Lists.newArrayList();
            List<SUserTaskLine> userTaskLineList = Lists.newArrayList();

            for (int i=0; i < taskNumber; i++) {

                String taskLineId = this.taskLineService.queryIdByLineOrder(productId,minLineOrder + i);
                STaskLine taskLine = this.taskLineService.getById(taskLineId);

                if (needPayAmt.compareTo(BigDecimal.ZERO) > 0) {
                    // 锁定任务数+1
                    taskLine.setLockTask(taskLine.getLockTask() + 1);
                } else {
                    // 已领取任务数+1
                    taskLine.setReceivedTask(taskLine.getReceivedTask() + 1);
                }
                taskLine.setUpdateTime(new Date());

                updateTaskLineList.add(taskLine);

                // 在s_user_task_line表上生成N条数据
                SUserTaskLine userTaskLine = new SUserTaskLine();

                userTaskLine.setUserId(userId);
                userTaskLine.setTaskId(userTask.getId());
                userTaskLine.setProductId(productId);
                userTaskLine.setTaskLineId(taskLineId);
                if (needPayAmt.compareTo(BigDecimal.ZERO) > 0) {
                    // 锁定
                    userTaskLine.setPayStatus(0);
                } else {
                    // 已支付
                    userTaskLine.setPayStatus(1);
                }
                userTaskLine.setPayAmount(product.getTaskPrice());
                userTaskLine.setPayTime(new Date());
                userTaskLine.setStatus(0);
                userTaskLine.setCreateTime(new Date());
                userTaskLine.setUpdateTime(new Date());

                userTaskLineList.add(userTaskLine);
            }

            this.taskLineService.updateBatchById(updateTaskLineList);
            this.userTaskLineService.saveBatch(userTaskLineList);

            // 支付金额不需要的情况，不需要调用支付，此时按照已支付处理
            if (needPayAmt.compareTo(BigDecimal.ZERO) == 0) {

                // 优惠券状态(已使用)修改 及 流水记录(s_user_coupon_log)追加
                if (StringUtils.isNotBlank(userCouponId)) {
                    SUserCoupon userCoupon = this.userCouponService.getById(userCouponId);
                    if (userCoupon != null) {
                        userCoupon.setCouponStatus(1);
                        userCoupon.setUpdateTime(new Date());
                        this.userCouponService.updateById(userCoupon);

                        SUserCouponLog couponLog = new SUserCouponLog();
                        couponLog.setCreateTime(new Date());
                        couponLog.setUpdateTime(new Date());
                        // 券类型 0-任务金 1-商铺券
                        couponLog.setCouponType(0);
                        couponLog.setCouponId(userCoupon.getCouponId());
                        couponLog.setUserId(userId);
                        this.userCouponLogService.save(couponLog);
                    }
                }

                // 拆豆奖励 及 拆豆流水记录追加 SUserBeanLog
                if (orderBeanCnt != null && orderBeanCnt > 0) {
                    SUserBeanLog userBeanLog = new SUserBeanLog();
                    userBeanLog.setUserId(user.getId());
                    userBeanLog.setChangeType(1);
                    userBeanLog.setChangeAmount(orderBeanCnt);
                    userBeanLog.setChangeTime(new Date());
                    userBeanLog.setRelationId(userTask.getId());
                    userBeanLog.setRemark("领取任务ID");
                    userBeanLog.setOldAmount(user.getCanuseBean());
                    this.userBeanLogService.save(userBeanLog);

                    user.setRewardBean(user.getRewardBean() + orderBeanCnt);
                    this.userService.updateById(user);
                }

                // 此时若还没有上级，形成正式上下级绑定关系 找到他的上级
                // 根据user_id、productId 到s_user_browser表中 找到shareId
                if (user.getParentId() == null) {

                    SUserBrowser userBrowser = new SUserBrowser();
                    userBrowser.setUserId(userId);
                    userBrowser.setProductId(productId);
                    userBrowser = this.userBrowserService.findUserBrowser(userBrowser);
                    // 根据shareId 到 s_user_share 表中 找到user_id 作为他的上级ID 更新到s_user中的parentId
                    if(userBrowser != null && userBrowser.getShareId() != null){
                        SUserShare userShare = this.userShareService.getById(userBrowser.getShareId());
                        if(userShare != null){
                            user.setParentId(userShare.getUserId());
                            this.userService.updateById(user);
                        }
                    }
                }
            }

            // 调起微信支付
            if (needPayAmt.compareTo(BigDecimal.ZERO) > 0) {
                JSONObject jsonObject = this.weChatPayUtil.weChatPay(String.valueOf(userTask.getId()),
                        product.getTaskPrice().multiply(needPayAmt).toString(),
                        user.getOpenId(),
                        request.getRemoteAddr(),
                        "1",
                        "任务金");

                response.data(jsonObject);
            }

        } catch (Exception e) {
            response.put("code", 1);
            response.message("确认支付领取任务!");
            log.error(message, e);
        }

        return response;
    }

    /**
     * 任务支付失败时 锁定去除 修改状态为未支付 并且修改任务线 锁定任务数
     * 2分钟执行一次 (支付失败时间超过5分钟的任务处理)
     */
    @Scheduled(cron = "0 0/2 0 * * ?")
    public void unLockPayFailTask() {

        // 抽取s_user_task中 支付状态（锁定） 支付时间大于5分钟的 数据 修改状态为 未支付
        this.userTaskService.updateTaskForUnLock();

        // 再根据s_user_task_line中的task_line_id到 表s_task_line 修改 冻结任务数量-1
        userTaskService.updateTaskLineFailBatch();

        // 同时把s_user_task_line中的相关数据也同样修改为 未支付
        userTaskService.updateUserTaskLineFailBatch();
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

    /**
     * 任务数量、优惠券检查
     * @return message String
     */
    private String validateTask(SUser user,
                                String productId,
                                Integer taskNumber,
                                String userCouponId){

        // 判断任务数量（输入数量必须大于0）
        if(taskNumber == null || taskNumber <= 0){
            return "请重新选择数量！";
        }

        // 每件商品最高领取任务线判断
        SUserLevel userLevel = this.userLevelService.findByLevelType(user.getUserLevelType());
        if (taskNumber > userLevel.getBuyNumber()) {
            return "您已超过此商品领取任务上限!";
        }

        // 新手只能购买新手区商品  其他只能购买非新手区商品
        SProduct product = this.productService.getById(productId);
        if (StringUtils.isBlank(productId) || product == null || !"1".equals(product.getProductStatus())) {
            return "该商品状态不可购买，请重新选择商品！";
        }
        if (userLevel.getLevelType() == 0 && product.getProductType() == 2) {
            return "您现在只能在新手区领取新手任务！";
        }
        if (userLevel.getLevelType() > 0 && product.getProductType() == 1) {
            return "您已经不能再次领取新手任务！";
        }

        // 判断任务线上是否有足够任务 （抽出数据count(*) 必须大于等于任务数量）
        // 根据商品ID && 未满 && 结算未完成 && 冻结任务数+已领任务数<总任务数 抽取s_task_line表
        Integer taskLineCount = this.taskLineService.queryTaskLineCount(productId);
        if (taskLineCount < taskNumber) {
            return "现在只能领取" + taskLineCount + "个任务，请修改数量！";
        }

        // 根据等级判断一个用户最多可以并行在多少件商品上领取任务
        // 最多并行商品件数
        Integer productCount = this.userTaskService.queryProductCount(user.getId());
        if (productCount >= userLevel.getProductNumber()) {
            return "您已超过领取商品件数的任务，暂时不能领取任务！";
        }

        // 根据 couponId 判断优惠券是否可用
        if (userCouponId != null && !"".equals(userCouponId)) {
            SUserCoupon userCoupon = this.userCouponService.findUserCoupon(user.getId(), userCouponId);
            if (userCoupon == userCoupon) {
                return "您选择的优惠券不能使用！";
            }
        }

        return null;
    }
}
