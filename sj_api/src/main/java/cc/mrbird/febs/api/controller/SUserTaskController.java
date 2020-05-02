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

    /**
     * 确认拆单
     */
    @Log("确认拆单")
    @PostMapping("/confirmUserTask")
    public FebsResponse confirmUserTask(HttpServletRequest request, @NotNull(message="商品ID不可空") String productId,
                                        @NotNull(message="商品ID不可空")Integer taskNumber,String userCouponId) {
        Map<String, Object> resultData = new HashMap();
        FebsResponse response = new FebsResponse();
        response.put("code", 0);
        try {
            String errorMessage = validateTask(productId, taskNumber);
            if (StringUtils.isNotBlank(errorMessage)) {
                response.put("code", 1);
                response.message(errorMessage);
                return response;
            }

            SUser user = FebsUtil.getCurrentUser();
            String userId = user.getId();

            // 商品信息取得
            Map<String, Object> productInfo = productService.findProductDetail(productId);
            resultData.put("productInfo", productInfo);
            SProduct product = productService.getById(productId);
            // 用户在此商品的所有优惠券列表取得
            List<SUserCoupon> userCouponList = userCouponService.findUserCouponList(userId, productId, 0, 0);
            resultData.put("userCouponList", userCouponList);
            cluTaskAmt( productId,taskNumber,userCouponId,resultData,product.getTaskPrice());//计算各值
            response.put("code", 0);
            response.data(resultData);
            } catch(Exception e){
                message = "确认拆单失败！";
                response.put("code", 1);
                response.message(message);
                log.error(message, e);
            }

            return response;
    }

    private void  cluTaskAmt (String productId,Integer taskNumber,String userCouponId,
                                             Map<String, Object> resultData,BigDecimal taskPrice){
        //任务金合计
        BigDecimal totalAmt ;
        boolean totalAmtFlag = false;
        // 前端页面选择的优惠券
        BigDecimal couponAmt = new BigDecimal(0);
        if (userCouponId != null) {
            SUserCoupon userCoupon = userCouponService.getById(userCouponId);
            if(userCoupon != null && userCoupon.getCouponId() != null){
                STaskCoupon coupon = taskCouponService.getById(userCoupon.getCouponId());
                couponAmt = coupon.getCouponAmount();
                if("2".equals(coupon.getUseCon())){
                    totalAmtFlag = true;//超级优惠券
                }
            }
        }
        resultData.put("couponAmt", couponAmt);//优惠券
        // 任务金合计、 默认实付金额
        totalAmt = (taskPrice.multiply(new BigDecimal(taskNumber))).subtract(couponAmt);

        if(totalAmtFlag){
            totalAmt = BigDecimal.ZERO;
        }

        resultData.put("totalAmt", totalAmt);
        // 赠送拆豆取得
        Integer orderBeanCnt = 0;
        SParams params = paramsService.queryBykeyForOne("order_bean_cnt");
        if (params != null) {
            orderBeanCnt = Integer.valueOf(params.getPValue());
            resultData.put("orderBeanCnt", orderBeanCnt);
        }
    }

    /**
     * 确认支付领取任务
     */
    @Log("确认支付领取任务")
    @Transactional
    @PostMapping("/payUserTask")
    public FebsResponse payUserTask(HttpServletRequest request, String productId, Integer taskNumber, String userCouponId) {
        SUser user = FebsUtil.getCurrentUser();
        String userId = user.getId();
        Map<String, Object> taskAmtMap = new HashMap();//支付金额等详细额度
        FebsResponse response = new FebsResponse();
        response.put("code", 0);
        try {

            SProduct product = productService.getById(productId);

            // 和确认拆单一样逻辑 判断任务线上是否有足够任务
            // 根据商品ID && 未满 && 结算未完成 && 冻结任务数+已领任务数<总任务数 抽取s_task_line表
            // 上面抽出数据count(*) 必须小于等于任务数量
            String errorMessage = validateTask(productId, taskNumber);
            if (StringUtils.isNotBlank(errorMessage)) {
                response.put("code", 1);
                response.message(errorMessage);
                return response;
            }
            // 根据商品ID从商品表取得 任务金 * 任务数量 = 总任务金
            // 根据用户优惠券ID 从 s_user_coupon 得到 coupon_id
            // 再根据coupon_id 从s_task_coupon取得 use_con 和 coupon_amount
            // 前端页面选择的优惠券
            // 如果是use_con=2-超级抵扣券的情况下，实付金额==0）
            cluTaskAmt( productId,taskNumber,userCouponId,taskAmtMap ,product.getTaskPrice());

            BigDecimal totalAmt = new BigDecimal((String) taskAmtMap.get("totalAmt"));
            BigDecimal couponAmt = taskAmtMap.get("couponAmt") == null ? BigDecimal.ZERO :  new BigDecimal((String) taskAmtMap.get("couponAmt"));
            Integer orderBeanCnt = taskAmtMap.get("orderBeanCnt") == null ? 0 :  new Integer((String) taskAmtMap.get("orderBeanCnt"));
            // 生成s_user_task一条数据（支付状态：已支付）
            SUserTask userTask = new SUserTask();
            userTask.setChannel(2);//1-微信公众号,2-小程序'
            userTask.setPayTime(new Date());
            userTask.setPayAmount(totalAmt);
            if(totalAmt.compareTo(BigDecimal.ZERO) > 0){
                userTask.setPayStatus(0);//应该是锁定啊
            }else{
                userTask.setPayStatus(1);//已支付
            }
            userTask.setUserId(userId);
            userTask.setUserCouponId(userCouponId);
            userTask.setCreateTime(new Date());
            userTask.setUpdateTime(new Date());
            userTaskService.save(userTask);
            // 根据商品ID、任务数从s_task_line表中分配N条任务线
            // 根据（商品ID && 未满 && 结算未完成 && 冻结任务数+已领任务数<总任务数）此条件 取得N条数据
            // 在上记N条数据上 已领任务数+1
            // 根据上记N条数据 然后在s_user_task_line表上生成N条数据 支付状态：已支付
            Integer  minLineOrder = taskLineService.queryMinLineOrder(productId);
            minLineOrder = minLineOrder == null ? 0 : minLineOrder;

            List<STaskLine> updateTaskLineList = Lists.newArrayList();
            List<SUserTaskLine> userTaskLineList = Lists.newArrayList();
            for(int i=0;i < taskNumber;i++){
               String taskLineId =  taskLineService.queryIdByLineOrder(productId,minLineOrder+i);
               STaskLine taskLine  = taskLineService.getById(taskLineId);
               if(taskLine != null){
                   if(totalAmt.compareTo(BigDecimal.ZERO) > 0){
                       taskLine.setReceivedTask(taskLine.getReceivedTask()+1);
                   }else{
                       taskLine.setLockTask(taskLine.getLockTask()+1);
                   }
                   updateTaskLineList.add(taskLine);
               }
                SUserTaskLine utLine = new SUserTaskLine();
                utLine.setCreateTime(new Date());
                utLine.setUpdateTime(new Date());
                utLine.setPayAmount(totalAmt);
                if(totalAmt.compareTo(BigDecimal.ZERO) > 0){
                    utLine.setPayStatus(0);//应该是锁定啊
                }else{
                    utLine.setPayStatus(1);//已支付
                }
                utLine.setPayTime(new Date());
                utLine.setProductId(productId);
                //TODO 待重写save 方法，让其返回id
                // utLine.setTaskId()
                utLine.setStatus(0);
                utLine.setTaskLineId(taskLineId);
                utLine.setUserId(userId);
                userTaskLineList.add(utLine);

            }
            taskLineService.updateBatchById(updateTaskLineList);
            userTaskLineService.saveBatch(userTaskLineList);
            // 优惠券状态(已使用)修改 及 流水记录(s_user_coupon_log)追加
            if(StringUtils.isNotBlank(userCouponId)){
                SUserCoupon userCoupon = userCouponService.getById(userCouponId);
                if(userCoupon != null){
                    userCoupon.setCouponStatus(1);
                    userCoupon.setUpdateTime(new Date());
                    userCouponService.updateById(userCoupon);
                    SUserCouponLog couponLog = new SUserCouponLog();
                    couponLog.setCreateTime(new Date());
                    couponLog.setUpdateTime(new Date());
                    couponLog.setCouponType(0);//券类型 0-任务金 1-商铺券
                    couponLog.setCouponId(userCoupon.getCouponId());
                    couponLog.setUserId(userId);
                    userCouponLogService.save(couponLog);
                }
            }

            // 拆豆奖励（10） s_user (reward_bean+10) 及 拆豆流水记录追加 SUserBeanLog
            SUserLevel userLevel = this.userLevelService.findByLevelType(user.getUserLevelType());
            SUserBeanLog userBeanLog = new SUserBeanLog();
            userBeanLog.setUserId(user.getId());
            userBeanLog.setChangeType(1);
            userBeanLog.setChangeAmount(orderBeanCnt);
            userBeanLog.setChangeTime(new Date());
            userBeanLog.setRelationId(userTask.getId());
            userBeanLog.setRemark("领取任务ID");
            userBeanLog.setOldAmount(user.getCanuseBean());
            this.userBeanLogService.save(userBeanLog);

            // 此时若还没有上级，形成正式上下级绑定关系 找到他的上级
            //if (user.getParentId() == null) {
            // 根据user_id、productId 到s_user_browser表中 找到shareId
            String userShareId = userShareService.getCurrentShareId(productId,userId);
            // 根据shareId 到 s_user_share 表中 找到user_id 作为他的上级ID 更新到s_user中的parentId
            if(userShareId != null){
                SUserShare userShare = userShareService.getById(userShareId);
                if(userShare != null){
                    user.setParentId(userShare.getUserId());
                    userService.updateById(user);
                }
            }
           //调起微信支付
            if(totalAmt.compareTo(BigDecimal.ZERO) >0){
                JSONObject jsonObject = this.weChatPayUtil.weChatPay(String.valueOf(userTask.getId()),
                        product.getTaskPrice().multiply(totalAmt).toString(),
                        user.getOpenId(),
                        request.getRemoteAddr(),
                        "1",
                        "任务金");
            }
        }catch (Exception e){
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

        // 抽取s_user_task中 支付状态（锁定） 支付时间大于5分钟的 数据

            // 修改状态为 未支付

            // 同时把s_user_task_line中的相关数据也同样修改为 未支付

            // 再根据s_user_task_line中的task_line_id到 表s_task_line 修改 冻结任务数量-1

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

    private String validateTask(String productId,Integer taskNumber){

        // 判断任务数量（输入数量必须大于0）
        if(taskNumber == null || taskNumber <= 0){
            return "请重新选择数量！";
        }
        SUser user = FebsUtil.getCurrentUser();

        if(user == null){
            return  "请重新登陆！" ;
        }
        String userId  = user.getId();

        // 每件商品最高领取任务线判断
        SUserLevel userLevel = this.userLevelService.findByLevelType(user.getUserLevelType());

        if (taskNumber > userLevel.getBuyNumber()) {
            return "您已超过此商品领取任务上限!";
        }

        // 新手只能购买新手标  其他只能购买正常标
        SProduct product = this.productService.getById(productId);
        if(StringUtils.isBlank(productId) || product == null || !"1".equals(product.getProductStatus())){
            return "抱歉！该商品状态不可购买，请重新选择商品！";
        }

        if (userLevel.getLevelType() == 0 && product.getProductType() == 2) {
            return "抱歉！您现在只能在新手区领取新手任务！";
        }

        if (userLevel.getLevelType() > 0 && product.getProductType() == 1) {
            return "抱歉！您已经不能再次领取新手任务！";
        }

        // 判断任务线上是否有足够任务

        String taskLineId = taskLineService.currentTaskLine(productId);
        if(taskLineId == null){
            return "抱歉！您购买的商品任务已领完，请选择其他商品！";
        }
        STaskLine taskLine  = taskLineService.getById(taskLineId);
        if(taskLine == null){
            return "抱歉！您购买的商品任务已领完，请选择其他商品！";
        }

        //已满任务不能再买  根据商品ID && 未满 && 结算未完成 && 冻结任务数+已领任务数<总任务数 抽取s_task_line表
        // 上面抽出数据count(*) 必须小于等于任务数量
        Integer rereiveTaskCount = userTaskService.queryReCount(productId,taskLineId);
        rereiveTaskCount = rereiveTaskCount == null ? 0 : rereiveTaskCount;
        if(rereiveTaskCount >= taskLine.getTotalTask()){
            return "抱歉！您购买的商品任务已领完，请选择其他商品！";
        }

        // 根据等级判断一个用户最多可以并行在多少件商品上领取任务
        // 最多并行商品件数
        Integer productCount = this.userTaskService.queryProductCount(userId);

        if (productCount != null && productCount >= userLevel.getProductNumber()) {
            return "抱歉！您已超过领取商品件数的任务！";
        }
        //TODO 根据 couponId 判断优惠券是否可用
        return null;
    }
}
