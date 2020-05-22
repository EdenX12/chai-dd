package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.*;
import cc.mrbird.febs.api.service.*;
import cc.mrbird.febs.common.annotation.Limit;
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
    private ISUserTaskLineService userTaskLineService;

    @Autowired
    private ISOrderService orderService;

    @Autowired
    private ISOrderDetailService orderDetailService;

    @Autowired
    private ISUserService userService;

    @Autowired
    private ISUserBeanLogService userBeanLogService;

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
    private ISProductSpecService productSpecService;

    @Autowired
    private ISProductService productService;

    /**
     * 新增用户支付
     */
    @Log("新增用户支付")
    @Transactional
    @PostMapping("/addUserPay")
    @Limit(key = "addUserPay", period = 60, count = 2000, name = "新增用户支付接口", prefix = "limit")
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
            log.error( e.getMessage(),e);
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
        StringBuffer sb = new StringBuffer();
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
                SUserTaskLine userTaskLine = new SUserTaskLine();
                userTaskLine.setTaskId(relationId);
                List<SUserTaskLine> userTaskLineList = this.userTaskLineService.findUserTaskLineList(userTaskLine);
                for (SUserTaskLine userTaskLineSuccess : userTaskLineList) {
                    this.userTaskService.updateTaskLineSuccessBatch(userTaskLineSuccess.getTaskLineId());
                }

                // 修改优惠券状态(已使用) 及 流水记录(s_user_coupon_log)追加
                if (StringUtils.isNotBlank(userTask.getUserCouponId())) {
                    SUserCoupon userCoupon = this.userCouponService.getById(userTask.getUserCouponId());
                    if (userCoupon != null) {
                        userCoupon.setCouponStatus(1);
                        userCoupon.setUpdateTime(new Date());
                        this.userCouponService.updateById(userCoupon);

                        SUserCouponLog couponLog = new SUserCouponLog();

                        couponLog.setUserId(user.getId());
                        couponLog.setCouponId(userCoupon.getCouponId());
                        couponLog.setUserCouponId(userCoupon.getId());
                        // 券类型 0-任务金 1-商铺券
                        couponLog.setCouponType(0);
                        couponLog.setUsedQuantity(1);
                        couponLog.setCreateTime(new Date());
                        couponLog.setUpdateTime(new Date());
                        this.userCouponLogService.save(couponLog);
                    }
                }

                // 拆豆奖励 及 拆豆流水记录追加 SUserBeanLog
                Integer orderBeanCnt = 0;
                String value = this.paramsService.queryBykeyForOne("order_bean_cnt");
                if (value != null) {
                    orderBeanCnt = Integer.valueOf(value);
                }
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

                            //赠送豆
                            String paramsBean = paramsService.queryBykeyForOne("children_bean_cnt");
                            Integer beanCnt = Integer.parseInt(paramsBean);
                            if (paramsBean != null && beanCnt > 0) {
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

            if ("O".equals(strPayType)) {

                // 支付购买订单成功
                SOrder order = this.orderService.getById(relationId);
                userPay.setUserId(order.getUserId());
                this.userPayService.save(userPay);

                SUser user = new SUser();
                user = this.userService.getById(order.getUserId());

                // 付款状态:  状态 0 锁定（支付中） 1 已支付； 2-待支付； 3 不支付（取消或过期）  9:已结算到冻结
                order.setPaymentState(1);
                order.setPaymentTime(new Date());
                order.setPayAmount(total);
                this.orderService.updateOrder(order);

                SOrderDetail orderDetail = new SOrderDetail();
                orderDetail.setOrderId(order.getId());
                List<SOrderDetail> orderDetailPaySuccessList = this.orderDetailService.findOrderDetailList(orderDetail);

                List<String>  settleTaskLineIds = Lists.newArrayList();
                for (SOrderDetail orderDetailPaySuccess : orderDetailPaySuccessList) {

                    // 变更订单明细状态 （1:已付款）
                    orderDetailPaySuccess.setPaymentState(1);
                    orderDetailPaySuccess.setOrderStatus(1);
                    orderDetailPaySuccess.setPaymentTime(new Date());
                    this.orderDetailService.updateById(orderDetailPaySuccess);

                    // 根据订单明细中的商品 然后从任务线表(s_task_line)中按顺序选中 结算状态未完成 作为结算任务线 修改为 结算中
                    List<SOrderProduct> orderProductList = this.orderProductService.findOrderProductList(orderDetailPaySuccess.getId());

                    int totalProductNumber = 0;

                    for (SOrderProduct orderProduct : orderProductList) {

                        totalProductNumber = totalProductNumber + orderProduct.getProductNumber();

                        for(int i=0; i<orderProduct.getProductNumber(); i++){

                            STaskLine taskLine = this.taskLineService.findTaskLineForSettle(orderProduct.getProductId());

                            // 结算中
                            taskLine.setSettleStatus(1);
                            taskLine.setOrderProductId(orderProduct.getId());
                            taskLine.setUpdateTime(new Date());
                            this.taskLineService.updateById(taskLine);

                            // 用户任务线待更新结算状态
                            settleTaskLineIds.add(taskLine.getId());
                        }

                        // 更新商品规格库存数量
                        SProductSpec productSpec = this.productSpecService.findProductSpec(orderProduct.getProductSpecId());
                        productSpec.setStockNumber(productSpec.getStockNumber() - orderProduct.getProductNumber());
                        this.productSpecService.updateById(productSpec);

                        // 更新商品库存数量
                        SProduct product = this.productService.getById(orderProduct.getProductId());
                        product.setStockNumber(product.getStockNumber() - orderProduct.getProductNumber());
                        this.productService.updateById(product);
                    }

                    // 如有使用优惠券的话 修改优惠券状态(已使用) 及 流水记录(s_user_coupon_log)追加
                    if (orderDetailPaySuccess.getUserCouponId() != null) {

                        // 修改优惠券状态(已使用)
                        SUserCoupon userCoupon = this.userCouponService.getById(orderDetailPaySuccess.getUserCouponId());
                        userCoupon.setCouponStatus(1);
                        userCoupon.setUpdateTime(new Date());
                        this.userCouponService.updateById(userCoupon);

                        // 流水记录(s_user_coupon_log)追加
                        SUserCouponLog couponLog = new SUserCouponLog();
                        couponLog.setUserId(user.getId());
                        couponLog.setCouponId(userCoupon.getCouponId());
                        couponLog.setUserCouponId(userCoupon.getId());
                        // 券类型 0-任务金 1-商铺券
                        couponLog.setCouponType(1);
                        couponLog.setUsedQuantity(1);
                        couponLog.setUsedAmount(orderDetailPaySuccess.getCouponAmount());
                        couponLog.setCreateTime(new Date());
                        couponLog.setUpdateTime(new Date());
                        this.userCouponLogService.save(couponLog);
                    }

                    // 拆豆奖励 及 拆豆流水记录追加 SUserBeanLog
//                    Integer productBeanCnt = 0;
//                    SParams params = this.paramsService.queryBykeyForOne("product_bean_cnt");
//                    if (params != null) {
//                        productBeanCnt = Integer.valueOf(params.getPValue());
//                    }
//                    productBeanCnt = productBeanCnt * totalProductNumber;

                    // 暂时变更为商品价格*10
                    Integer productBeanCnt = total.multiply(new BigDecimal(10)).intValue();

                    if (productBeanCnt != null && productBeanCnt > 0) {
                        SUserBeanLog userBeanLog = new SUserBeanLog();
                        userBeanLog.setUserId(user.getId());
                        userBeanLog.setChangeType(8);
                        userBeanLog.setChangeAmount(productBeanCnt);
                        userBeanLog.setChangeTime(new Date());
                        userBeanLog.setRelationId(orderDetailPaySuccess.getId());
                        userBeanLog.setRemark("购买商品订单明细ID");
                        userBeanLog.setOldAmount(user.getRewardBean());
                        this.userBeanLogService.save(userBeanLog);

                        user.setRewardBean(user.getRewardBean() + productBeanCnt);
                        this.userService.updateById(user);
                    }
                }

                // 同时 批量更新 s_user_task_line表中的状态为 3 佣金计算中（条件 taskLineId）
                this.taskLineService.updateUserTaskLineForSettle(settleTaskLineIds);
            }

//            if ("P".equals(strPayType)) {
//
//                // 转让任务报价成功
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
//            }
        }

        ResponseWriteUtil.responseWriteClient(request, response, "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>");
    }
}
