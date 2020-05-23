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
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import io.micrometer.core.instrument.util.StringUtils;
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
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;

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

    @Autowired
    private ISUserRelationService userRelationService;

    /**
     * 确认拆单
     */
    @Log("确认拆单")
    @PostMapping("/confirmUserTask")
    @Transactional
    @Limit(key = "confirmUserTask", period = 60, count = 2000, name = "确认拆单数量", prefix = "limit")
    public FebsResponse confirmUserTask(HttpServletRequest request,
                                        @NotNull(message="商品ID不可空") String productId,
                                        @NotNull(message="商品ID不可空")Integer taskNumber,
                                        String userCouponId) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        Map<String, Object> resultData = new HashMap();

        try {

            SUser user = FebsUtil.getCurrentUser();

            // 检查任务数量、优惠券
            String errorMessage = this.validateTask(user, productId, taskNumber, userCouponId);
            if (StringUtils.isNotBlank(errorMessage)) {
                response.put("code", 1);
                response.message(errorMessage);
                return response;
            }

            // 商品信息取得
            Map<String, Object> productInfo = this.productService.findProductDetail(productId, user);
            resultData.put("productInfo", productInfo);

            // 计算总任务金、优惠金额、实付金额等
            this.cluTaskAmt(productId,
                    taskNumber,
                    userCouponId,
                    resultData,
                    new BigDecimal (productInfo.get("taskPrice").toString()));

            response.data(resultData);

        } catch(Exception e) {
            message = "确认拆单失败！";
            response.put("code", 1);
            response.message(message);
            log.error( e.getMessage(),e);
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
        String value  = this.paramsService.queryBykeyForOne("order_bean_cnt");
        if (value != null) {
            orderBeanCnt = Integer.valueOf(value);
        }
        resultData.put("orderBeanCnt", orderBeanCnt);
    }

    /**
     * 确认支付领取任务
     */
    @Log("确认支付领取任务")
    @Transactional
    @PostMapping("/payUserTask")
    @Limit(key = "payUserTask", period = 60, count = 2000, name = "确认支付领取任务接口", prefix = "limit")
    public FebsResponse payUserTask(HttpServletRequest request,
                                    String productId,
                                    Integer taskNumber,
                                    String userCouponId) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        Map<String, Object> resultData = new HashMap();

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
            BigDecimal needPayAmt = new BigDecimal(taskAmtMap.get("needPayAmt").toString());

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
            userTask.setTaskNumber(taskNumber);
            userTask.setOrderSn("T"+System.currentTimeMillis());
            userTask.setProductId(productId);
            userTask = userTaskService.createUserTask(userTask);

            // 根据商品ID、任务数从s_task_line表中分配N条任务线
            // 并且在s_user_task_line表上生成N条数据
            Integer minLineOrder = this.taskLineService.queryMinLineOrder(productId);

            List<STaskLine> updateTaskLineList = Lists.newArrayList();
            List<SUserTaskLine> userTaskLineList = Lists.newArrayList();

            int i = 0;
            int x = 0;
            while (i < taskNumber) {

                QueryWrapper<SUserTaskLine> queryWrapper = new QueryWrapper<SUserTaskLine>();
                String taskLineId = this.taskLineService.queryIdByLineOrder(productId,minLineOrder + x);
                x++;

                // 到最后，退出
                if (taskLineId == null) {
                    break;
                }

                queryWrapper.eq("user_id", userId);
                queryWrapper.eq("task_line_id", taskLineId);
                queryWrapper.eq("pay_status",1);
                // 首先判断登录人是不是在这个任务线中
                int count = this.userTaskLineService.count(queryWrapper);
                if (count > 0) {
                    continue;
                }
                i++;

                STaskLine taskLine = this.taskLineService.getById(taskLineId);

                if (needPayAmt.compareTo(BigDecimal.ZERO) > 0) {
                    // 锁定任务数+1
                    taskLine.setLockTask(taskLine.getLockTask() + 1);
                    log.info("taskLine="+taskLine.getId()+"新增一个锁定");
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
                    userBeanLog.setOldAmount(user.getRewardBean());
                    this.userBeanLogService.save(userBeanLog);

                    user.setRewardBean(user.getRewardBean() + orderBeanCnt);
                    this.userService.updateById(user);
                }

                // 此时若还没有上级，形成正式上下级绑定关系 找到他的上级
                // 根据user_id、productId 到s_user_browser表中 找到shareId
                if (user.getParentId() == null) {

                    SUserBrowser userBrowser = new SUserBrowser();

                    // 阅读表中可能暂时没有userId
                    userBrowser.setUnionId(user.getUnionId());
                    userBrowser.setProductId(productId);
                    userBrowser = this.userBrowserService.findUserBrowser(userBrowser);

                    // 根据shareId 到 s_user_share 表中 找到user_id 作为他的上级ID 更新到s_user中的parentId
                    if (userBrowser != null && userBrowser.getShareId() != null) {
                        SUserShare userShare = this.userShareService.getById(userBrowser.getShareId());
                        if (userShare != null) {
                            user.setParentId(userShare.getUserId());
                            this.userService.updateById(user);

                            SUserRelation userRelation = new SUserRelation();
                            userRelation.setUnionId(user.getUnionId());
                            userRelation.setParentId(userShare.getUserId());
                            SUserRelation userRelationOne = this.userRelationService.findUserRelation(userRelation);
                            // 由预备队修改为禁卫军
                            userRelationOne.setRelationType(1);
                            this.userRelationService.updateById(userRelationOne);

                            //赠送豆
                            String value = this.paramsService.queryBykeyForOne("children_bean_cnt");
                            Integer beanCnt = Integer.parseInt(value);
                            if (beanCnt != null && beanCnt > 0) {
                                SUser parentUser = this.userService.getById(userShare.getUserId());
                                SUserBeanLog userBeanLog = new SUserBeanLog();
                                userBeanLog.setUserId(parentUser.getId());
                                userBeanLog.setChangeType(9);
                                userBeanLog.setChangeAmount(beanCnt);
                                userBeanLog.setChangeTime(new Date());
                                userBeanLog.setRelationId(userRelationOne.getId());
                                userBeanLog.setRemark("新增禁卫军");
                                userBeanLog.setOldAmount(parentUser.getRewardBean());
                                this.userBeanLogService.save(userBeanLog);

                                parentUser.setRewardBean(parentUser.getRewardBean() + beanCnt);
                                this.userService.updateById(parentUser);
                            }
                        }
                    }
                }
            }

            // 调起微信支付
            if (needPayAmt.compareTo(BigDecimal.ZERO) > 0) {
                JSONObject jsonObject = this.weChatPayUtil.weChatPay(String.valueOf(userTask.getId()),
                        needPayAmt.toString(),
                        user.getOpenId(),
                        request.getRemoteAddr(),
                        "1",
                        "任务金");

                resultData.put("jsonObject", jsonObject);
            }

            // 用户任务ID
            resultData.put("userTaskId", userTask.getId());

            response.data(resultData);

        } catch (Exception e) {
            response.put("code", 1);
            response.message("确认支付领取任务!");
            log.error( e.getMessage(),e);
        }

        return response;
    }

    /**
     * 支付领取任务成功页面
     */
    @Log("支付领取任务成功页面")
    @PostMapping("/payUserTaskSuccess")
    @Limit(key = "payUserTaskSuccess", period = 60, count = 2000, name = "支付领取任务成功页面接口", prefix = "limit")
    public FebsResponse payUserTaskSuccess(HttpServletRequest request,
                                           String userTaskId) {

        Map<String, Object> taskSuccessMap = new HashMap();

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        // 根据userTaskId取得支付金额、商品ID
        SUserTask userTask = this.userTaskService.getById(userTaskId);

        // 支付金额
        BigDecimal payAmount = userTask.getPayAmount();

        // 商品ID
        String productId = userTask.getProductId();

        // 根据userTaskId取得user_task_line条数
        SUserTaskLine userTaskLine = new SUserTaskLine();
        userTaskLine.setTaskId(userTaskId);
        List<SUserTaskLine> userTaskLineList = this.userTaskLineService.findUserTaskLineList(userTaskLine);
        // 领取任务数量
        int successTaskNumber = userTaskLineList.size();

        // 根据商品ID 取得 奖励佣金 任务总额度 计算 预计躺赢
        SProduct product = this.productService.getById(productId);

        // 同组任务躺赢佣金比例
        String value =  this.paramsService.queryBykeyForOne("same_group_rate");
        BigDecimal sameGroupRate = BigDecimal.valueOf(Double.parseDouble(value));

        // 赠送拆豆取得
        value = this.paramsService.queryBykeyForOne("order_bean_cnt");
        Integer orderBeanCnt = Integer.valueOf(value);

        // 总佣金
        BigDecimal totalReward = product.getTotalReward();

        // 本次预计躺赢
        BigDecimal bonusAmount = totalReward.multiply(sameGroupRate).multiply(new BigDecimal(successTaskNumber)).divide(
                new BigDecimal(product.getTaskNumber()), 2, BigDecimal.ROUND_HALF_UP);

        // 本次支付金额
        taskSuccessMap.put("payAmount", payAmount);
        // 本次预计躺赢
        taskSuccessMap.put("bonusAmount", bonusAmount);
        // 拆单奖励个数
        taskSuccessMap.put("orderBeanCnt", orderBeanCnt);

        response.data(taskSuccessMap);

        return response;
    }

    /**
     * 任务支付失败时 锁定去除 修改状态为 3-不支付[取消或超期] 并且修改任务线 锁定任务数
     * 2分钟执行一次 (支付失败时间超过5分钟的任务处理)
     */
    @Scheduled(cron = "0 */2 * * * ?")
    public void unLockPayFailTask() {

        // 同时把s_user_task_line中的相关数据也同样修改为 3-不支付[取消或超期]
        userTaskService.updateUserTaskLineFailBatch();

        // 抽取s_user_task中 支付状态（锁定） 支付时间大于5分钟的 数据 修改状态为 3-不支付[取消或超期]
        this.userTaskService.updateTaskForUnLock();

        // 再根据s_user_task_line中的task_line_id到 表s_task_line 修改 冻结任务数量-1
        Integer count = userTaskService.updateTaskLineFailBatch();
        log.info("定时任务本次解冻了"+count+"个任务线");
    }

    /**
     * 取得我的任务【进行中】列表信息
     * @return List<Map>
     */
    @PostMapping("/getUserTaskingList")
    @Limit(key = "getUserTaskingList", period = 60, count = 2000, name = "检索我的任务【进行中】接口", prefix = "limit")
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
                        userTask.get("productId").toString(), user);

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
    @Limit(key = "getUserTaskFollowList", period = 60, count = 2000, name = "检索我的任务【已关注】接口", prefix = "limit")
    public FebsResponse getUserTaskFollowList(QueryRequest queryRequest) {

        FebsResponse response = new FebsResponse();

        SUser user = FebsUtil.getCurrentUser();

        // 我的关注中任务
        IPage<Map> result = this.userTaskService.findUserTaskFollowList(
                queryRequest, user.getId());

        if (result != null) {
            List<Map> userTaskList = result.getRecords();
            for (Map userTask : userTaskList) {
                Map<String, Object> productDetail = this.productService.findProductDetail(
                        userTask.get("productId").toString(), user);

                // 关注中 默认数量为1
                userTask.put("taskingCnt", 1);

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
    @Limit(key = "getUserTaskEndList", period = 60, count = 2000, name = "检索我的任务【已完成】接口", prefix = "limit")
    public FebsResponse getUserTaskEndList(QueryRequest queryRequest) {

        FebsResponse response = new FebsResponse();

        SUser user = FebsUtil.getCurrentUser();

        // 我的完成中任务
        IPage<Map> result = this.userTaskService.findTaskDetailByStatus(
                queryRequest, user.getId(),5);

        if (result != null) {
            List<Map> userTaskList = result.getRecords();
            for (Map userTask : userTaskList) {
                Map<String, Object> productDetail = this.productService.findProductDetail(
                        userTask.get("productId").toString(), user);

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
    @Limit(key = "getTaskSettlementList", period = 60, count = 2000, name = "检索我的任务【结算中】接口", prefix = "limit")
    public FebsResponse getTaskSettlementList(QueryRequest queryRequest) {

        FebsResponse response = new FebsResponse();

        SUser user = FebsUtil.getCurrentUser();

        // 我的结算中任务
        IPage<Map> result = this.userTaskService.findTaskDetailByStatus(
                queryRequest, user.getId(),4);

        if (result != null) {
            List<Map> userTaskList = result.getRecords();
            for (Map userTask : userTaskList) {
                Map<String, Object> productDetail = this.productService.findProductDetail(
                        userTask.get("productId").toString(), user);

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
    @Limit(key = "getUserTaskOutingList", period = 60, count = 2000, name = "检索我的任务【转出中】接口", prefix = "limit")
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
     * 我的任务列表
     * @param queryRequest
     * @param type
     * @return
     */
    @PostMapping("/getUserTaskList")
    @Limit(key = "getUserTaskList", period = 60, count = 2000, name = "检索我的任务【转出中】接口", prefix = "limit")
    public FebsResponse getUserTaskList(QueryRequest queryRequest, String type) {

        FebsResponse response = new FebsResponse();
        SUser user = FebsUtil.getCurrentUser();
        Map<String, Object> userTaskPageList = null;
        IPage<Map> result = null;
        switch (type)
        {
            //进行中
            case "1" :
                result = this.userTaskService.findTaskDetailByStatus(
                        queryRequest, user.getId(),0);break;
            //已完成
            case "2" :
                result = this.userTaskService.findTaskDetailByStatus(
                        queryRequest, user.getId(),5);break;
            //已关注
            case "3" : result = this.userTaskService.findUserTaskFollowList(
                    queryRequest, user.getId());break;
            //结算中
            case "4" :
                result = this.userTaskService.findTaskDetailByStatus(
                        queryRequest, user.getId(),4);break;
            //转让中
            case "5" :
                SUserTask userTask = new SUserTask();
                userTask.setUserId(user.getId());
                result = this.userTaskService.findUserTaskOutList(userTask, queryRequest);break;

        }
        if (result != null) {
            List<Map> userTaskList = result.getRecords();
            for (Map userTask : userTaskList) {
                Map<String, Object> productDetail = this.productService.findProductDetail(
                        userTask.get("productId").toString(), user);

                userTask.put("productDetail", productDetail);
            }

            result.setRecords(userTaskList);
        }

        userTaskPageList =  getDataTable(result);
        response.put("code", 0);
        response.data(userTaskPageList);

        return response;
    }

    /**
     * 取得我的任务【收购中】列表信息
     * @return List<Map>
     */
    @PostMapping("/getUserTaskOfferingList")
    @Limit(key = "getUserTaskOfferingList", period = 60, count = 2000, name = "检索我的任务【收购中】接口", prefix = "limit")
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

    @PostMapping("/getTaskDetail")
    @Limit(key = "getTaskDetail", period = 60, count = 2000, name = "查询拆单详情", prefix = "limit")
    public FebsResponse getTaskDetail(@NotEmpty(message = "任务ID不可为空") String userTaskId) {
        FebsResponse response = new FebsResponse();

        SUser user = FebsUtil.getCurrentUser();

        //商品详情
        //任务金 合计
        //优惠券
        //任务躺赢
        //赠送拆豆
        //订单编号

        response.put("code", 0);

        return response;
    }


    @PostMapping("/getTotalTaskCount")
    @Limit(key = "getTotalTaskCount", period = 60, count = 2000, name = "查询拆单总数", prefix = "limit")
    public FebsResponse getTotalTaskCount() {
        //03  进行中 4结算中 5 完成
        FebsResponse response = new FebsResponse();

        SUser user = FebsUtil.getCurrentUser();
        response.put("code", 0);
        try{
            List<Map<String,Object>> resultList = this.userTaskService.queryTotalCount(user.getId());
            response.data(resultList);
        }catch (Exception e){
            response.put("code",1);
            response.message("查询失败，稍后重试。");
            log.error(e.getMessage(),e);
        }
        return response;
    }
    /**
     * 任务数量、优惠券检查
     * @return message String
     */
    private String validateTask(SUser user,
                                String productId,
                                Integer taskNumber,
                                String userCouponId) {

        // 先判断有没有自己锁定的任务
        QueryWrapper<SUserTask> queryWrapper = new QueryWrapper<SUserTask>();

        queryWrapper.eq("user_id", user.getId());
        queryWrapper.eq("pay_status", 0);

        List<SUserTask> userTaskList = this.userTaskService.list(queryWrapper);

        for (SUserTask userTask : userTaskList) {

            // 抽取s_user_task中 支付状态（锁定） 的（自己的） 数据 修改状态为 3-不支付[取消或超期]
            userTask.setPayStatus(3);
            this.userTaskService.updateById(userTask);

            // 再根据s_user_task_line中的task_line_id到 表s_task_line 修改 冻结任务数量-1
            QueryWrapper<SUserTaskLine> queryWrapper1 = new QueryWrapper<SUserTaskLine>();
            queryWrapper1.eq("task_id", userTask.getId());

            List<SUserTaskLine> userTaskLineList = this.userTaskLineService.list(queryWrapper1);

            for(SUserTaskLine userTaskLine : userTaskLineList) {

                // 同时把s_user_task_line中的相关数据也同样修改为 3-不支付[取消或超期]
                userTaskLine.setPayStatus(3);
                this.userTaskLineService.updateById(userTaskLine);

                UpdateWrapper<STaskLine> updateWrapper = new UpdateWrapper<STaskLine>();
                STaskLine taskLine = this.taskLineService.getById(userTaskLine.getTaskLineId());

                updateWrapper.set("lock_task", taskLine.getLockTask() - 1);
                updateWrapper.eq("id", userTaskLine.getTaskLineId());

                this.taskLineService.update(updateWrapper);
                log.info("LockTask更新之前数量 "+taskLine.getLockTask() +"|||taskLineId="+userTaskLine.getTaskLineId()+"userTaskLineId="+userTaskLine.getId());
            }
        }

        // 判断任务数量（输入数量必须大于0）
        if(taskNumber == null || taskNumber <= 0){
            return "请重新选择数量！";
        }

        // 每件商品最高领取任务线判断
        SUserLevel userLevel = this.userLevelService.findByLevelType(user.getUserLevelType());
        if (taskNumber > userLevel.getBuyNumber()) {
            return "您已超过此商品领取任务上限!";
        }

        // 老手只能购买非新手区商品
        SProduct product = this.productService.getById(productId);
        if (StringUtils.isBlank(productId) || product == null || product.getProductStatus() != 1) {
            return "该商品状态不可购买，请重新选择商品！";
        }
        if (userLevel.getLevelType() > 0 && product.getProductType() == 1) {
            return "您已经不能再次领取新手任务！";
        }

        // 判断任务线上是否有足够任务 （抽出数据count(*) 必须大于等于任务数量）
        // 根据商品ID && 未满 && 结算未完成 && 冻结任务数+已领任务数<总任务数 抽取s_task_line表  并且不包含自己已经领取任务
        Integer taskLineCount = this.taskLineService.queryTaskLineCount(productId, user.getId());
        if (taskLineCount < taskNumber) {
            return "您只能领取" + taskLineCount + "个任务，请修改数量！";
        }

        // 已领取任务数
        Integer userTaskLineCount = this.userTaskLineService.queryCountByUserIdAndProductId(user.getId(), productId);
        if (userTaskLineCount + taskNumber > userLevel.getBuyNumber()) {
            return "您只能领取此商品" + String.valueOf(userLevel.getBuyNumber() -  userTaskLineCount) + "个任务，请修改数量！";
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
