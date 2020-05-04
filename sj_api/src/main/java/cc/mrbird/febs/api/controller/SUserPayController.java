package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.*;
import cc.mrbird.febs.api.service.*;
import cc.mrbird.febs.common.annotation.Log;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.domain.FebsResponse;
import cc.mrbird.febs.common.utils.*;
import com.google.common.collect.Lists;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author MrBird
 */
@Slf4j
@RestController
@RequestMapping("/api/s-user-pay")
public class SUserPayController extends BaseController {

    private String message;

    @Autowired
    private ISUserPayService userPayService;

    @Autowired
    private ISUserTaskService userTaskService;

    @Autowired
    private ISOrderService orderService;

    @Autowired
    private ISOrderDetailService orderDetailService;

    @Autowired
    private ISProductService productService;

    private ISUserService userService;

    @Autowired
    private ISUserLevelService userLevelService;

    @Autowired
    private ISUserAmountLogService userAmountLogService;

    @Autowired
    private ISUserBeanLogService userBeanLogService;

    @Autowired
    private ISUserBonusLogService userBonusLogService;

    @Autowired
    private ISUserCouponService userCouponService;

    @Autowired
    private ISUserCouponLogService userCouponLogService;

    @Autowired
    private ISParamsService paramsService;

    @Autowired
    private ISUserShareService userShareService;

    @Autowired
    private ISUserBrowserService userBrowserService;

    @Autowired
    private ISUserRelationService userRelationService;

    @Autowired
    private ISTaskLineService taskLineService;

    @Autowired
    private ISOrderProductService orderProductService;

    @Autowired
    private ISUserTaskLineService userTaskLineService;


    /**
     * 新增用户支付
     */
    @Log("新增用户支付")
    @Transactional
    @PostMapping("/addUserPay")
    public FebsResponse addUserPay(@Valid SUserPay userPay) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {

            userPay.setCreateTime(new Date());

            SUser user = FebsUtil.getCurrentUser();
            userPay.setUserId(user.getId());

            this.userPayService.save(userPay);

        } catch (Exception e) {
            message = "新增用户支付失败";
            response.put("code", 1);
            response.message(message);
            log.error(message, e);
        }

        return response;
    }

    /**
     * 微信支付成功回调
     */
    @Log("微信支付成功回调")
    @Transactional
    @PostMapping("/paySuccess")
    public void paySuccess(HttpServletRequest request, HttpServletResponse response) {

        // 获取返回数据
        StringBuffer sb   =new StringBuffer();
        InputStream is = null;
        try {
            is = request.getInputStream();
            byte[] b =new byte[2048];
            for (int n;(n=is.read(b))!=-1;){
                sb.append(new String(b,0,n));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 返回值转xml
        Map<String,Object> map = XmlUtil.xmltoMap(sb.toString());

        // 订单号
        String paySn = map.get("out_trade_no").toString();

        // 支付流水号
        String transSn = map.get("transaction_id").toString();

        // 关联ID
        String relationId = map.get("attach").toString();

        // 支付金额
        String total_fee = map.get("total_fee").toString();
        BigDecimal total = new BigDecimal(total_fee).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);

        // openId
        String openid = map.get("openid").toString();

        // 支付成功时更新订单状态
        if (map.get("return_code").equals("SUCCESS")) {

            String strPayType = paySn.substring(0, 1);

            // 用户支付表插入
            SUserPay userPay = new SUserPay();
            userPay.setCreateTime(new Date());
            userPay.setPayAmount(total);
            userPay.setTotalAmount(total);
            userPay.setPaySn(paySn);
            userPay.setPayStatus(1);
            userPay.setPayTime(new Date());
            userPay.setRelationId(relationId);
            userPay.setTransSn(transSn);
            userPay.setPayType(1);

            if ("T".equals(strPayType)) {

                // 领取任务支付成功
                SUserTask userTask = this.userTaskService.getById(relationId);
                userPay.setUserCouponId(userTask.getUserCouponId());
                userPay.setUserId(userTask.getUserId());
                this.userPayService.save(userPay);

                SUser user = new SUser();
                user = this.userService.getById(userTask.getUserId());

                // 修改s_user_task支付状态 已支付
                userTask.setPayStatus(1);
                userTask.setPayAmount(total);
                userTask.setUpdateTime(new Date());
                userTask.setPayTime(new Date());
                this.userTaskService.updateUserTask(userTask);

                // 修改s_user_task_line 支付状态 已支付
                this.userTaskService.updateUserTaskLineSuccessBatch(relationId);

                // 修改s_task_line 锁定任务数量-1  已领取任务数量+1
                this.userTaskService.updateTaskLineSuccessBatch(relationId);

                // 修改优惠券状态(已使用) 及 流水记录(s_user_coupon_log)追加
                if (StringUtils.isNotBlank(userTask.getUserCouponId())) {
                    SUserCoupon userCoupon = this.userCouponService.getById(userTask.getUserCouponId());
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
                        couponLog.setUserId(user.getId());
                        this.userCouponLogService.save(couponLog);
                    }
                }

                // 拆豆奖励 及 拆豆流水记录追加 SUserBeanLog
                Integer orderBeanCnt = 0;
                SParams params = this.paramsService.queryBykeyForOne("order_bean_cnt");
                if (params != null) {
                    orderBeanCnt = Integer.valueOf(params.getPValue());
                }
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
                    userBrowser.setUserId(user.getId());
                    userBrowser.setProductId(userTask.getProductId());
                    userBrowser = this.userBrowserService.findUserBrowser(userBrowser);
                    // 根据shareId 到 s_user_share 表中 找到user_id 作为他的上级ID 更新到s_user中的parentId
                    if(userBrowser != null && userBrowser.getShareId() != null){
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
                        }
                    }
                }
            }

            if ("O".equals(strPayType)) {


                // 支付购买订单成功
                SOrder order = this.orderService.getById(relationId);
                userPay.setUserId(order.getUserId());
                this.userPayService.save(userPay);

                String userId =order.getUserId();

                        // 变更批量订单状态 已付款
                order.setPaymentState(1);
                order.setPaymentState(1);
                order.setPaymentTime(new Date());
                this.orderService.updateOrder(order);

                // 变更订单明细状态 已付款
                SOrderDetail orderDetail = new SOrderDetail();
                orderDetail.setUserId(userId);
                orderDetail.setOrderId(order.getId());
                orderDetail.setOrderStatus(1);
                orderDetail.setPaymentState(1);
                orderDetail.setPaymentTime(new Date());

                this.orderDetailService.updateOrderDetail(orderDetail);
                List<Map<String,Object>> productIds = orderDetailService.queryProductByOrder(order.getId());

                // 根据订单明细中的商品 然后从任务线表(s_task_line)中按顺序选中 结算状态未完成 作为结算任务线 修改为 结算中
                // 然后选中的结算任务线 对应的 用户任务线表(s_user_task_line) 状态修改为 佣金结算中

                //1.  根据s_order_detail 这个表中的id 检索s_order_product  这儿可能会有多条数据 如有多条数据做循环处理
                //2.  针对上记1中每一条数据，做以下处理：
                //a . 根据s_order_product中的 商品ID（ product_id） 到s_task_line表中检索  条件 结算状态未完成（settle_status=0）
                // line_order（asc升序） 取得N（商品数量）条 然后把这些数据中 结算中（1） order_product_id 更新
                // 同时 批量更新 s_user_task_line表中的状态为结算中3（条件 taskLineId）

                if(productIds != null && productIds.size()>0){
                    List<String>  settleTaskLineIds = Lists.newArrayList();
                    for(Map<String,Object> obj  : productIds){
                        String productId = obj.get("productId").toString();
                        String orderProductId = obj.get("orderProductId").toString();
                        String settleTaskLineId = taskLineService.queryForSettle(productId);
                        STaskLine taskLine = taskLineService.getById(settleTaskLineId);
                        taskLine.setOrderProductId(orderProductId);
                        // 结算中
                        taskLine.setSettleStatus(1);
                        taskLineService.updateById(taskLine);
                        settleTaskLineIds.add(settleTaskLineId);
                    }
                    taskLineService.updateUserTaskLineForSettle(settleTaskLineIds);
                }
                // 修改优惠券状态(已使用) 及 流水记录(s_user_coupon_log)追加
                List<SOrderDetail> orderDetails = orderDetailService.findOrderDetailList(orderDetail );
                if (orderDetails != null && orderDetails.size() > 0) {
                    for(SOrderDetail od : orderDetails){
                        SUserCoupon userCoupon = this.userCouponService.getById(od.getUserCouponId());
                        if (userCoupon != null) {
                            userCoupon.setCouponStatus(1);
                            userCoupon.setUpdateTime(new Date());
                            this.userCouponService.updateById(userCoupon);

                            SUserCouponLog couponLog = new SUserCouponLog();
                            couponLog.setCreateTime(new Date());
                            couponLog.setUpdateTime(new Date());
                            // 券类型 0-任务金 1-商铺券
                            couponLog.setCouponType(1);
                            couponLog.setCouponId(userCoupon.getCouponId());
                            couponLog.setUserId(userId);
                            this.userCouponLogService.save(couponLog);
                        }
                    }
                }

                // 拆豆奖励 及 拆豆流水记录追加 SUserBeanLog todo  ???确定这个地方是否也需要送拆豆？？？
                Integer orderBeanCnt = 0;
                SParams params = this.paramsService.queryBykeyForOne("order_bean_cnt");
                if (params != null) {
                    orderBeanCnt = Integer.valueOf(params.getPValue());
                }
                SUser user = userService.getById(userId);
                if (orderBeanCnt != null && orderBeanCnt > 0) {
                    SUserBeanLog userBeanLog = new SUserBeanLog();
                    userBeanLog.setUserId(userId);
                    userBeanLog.setChangeType(1);
                    userBeanLog.setChangeAmount(orderBeanCnt);
                    userBeanLog.setChangeTime(new Date());
                    userBeanLog.setRelationId(order.getId());//TODO 待定，确定是否是orderID
                    userBeanLog.setRemark("订单orderID");
                    userBeanLog.setOldAmount(user.getCanuseBean());
                    this.userBeanLogService.save(userBeanLog);

                    user.setRewardBean(user.getRewardBean() + orderBeanCnt);
                    this.userService.updateById(user);
                }

            }


            if ("P".equals(strPayType)) {

                // 转让任务报价成功
//                SOfferPrice offerPrice = this.offerPriceService.getById(relationId);
//
//                userPay.setUserId(offerPrice.getUserId());
//
//                this.userPayService.save(userPay);
//
//                // 先更新出局
//                this.offerPriceService.updateOfferPriceOut(offerPrice);
//
//                // 变更订单状态 已付款
//                offerPrice.setPayStatus(1);
//                offerPrice.setUpdateTime(new Date());
//                this.offerPriceService.updateById(offerPrice);
//
//                // 用户冻结金额追加   每参与一次任务报价 （10颗） * 猎人等级倍数
//                SUser user = this.userService.getById(offerPrice.getUserId());
//                SUserLevel userLevel = userLevelService.findByLevelType(user.getUserLevelType());
//                user.setLockAmount(user.getLockAmount().add(total));
//                user.setCanuseBean(user.getCanuseBean() + userLevel.getBeanRate().multiply(BigDecimal.valueOf(10)).intValue());
//                this.userService.updateById(user);
//
//                // 猎豆流水插入
//                SUserBeanLog userBeanLog = new SUserBeanLog();
//                userBeanLog.setUserId(user.getId());
//                userBeanLog.setChangeType(6);
//                userBeanLog.setChangeAmount(userLevel.getBeanRate().multiply(BigDecimal.valueOf(10)).intValue());
//                userBeanLog.setChangeTime(new Date());
//                userBeanLog.setRelationId(offerPrice.getId());
//                userBeanLog.setRemark("转让任务报价ID");
//                userBeanLog.setOldAmount(user.getCanuseBean());
//                this.userBeanLogService.save(userBeanLog);
            }

        }



        ResponseWriteUtil.responseWriteClient(request, response, "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>");
    }
}
