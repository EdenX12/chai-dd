package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.*;
import cc.mrbird.febs.api.service.*;
import cc.mrbird.febs.common.annotation.Log;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.domain.FebsResponse;
import cc.mrbird.febs.common.utils.*;
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
import java.util.ArrayList;
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
    private ISTaskOrderService taskOrderService;

    @Autowired
    private ISUserTaskService userTaskService;

    @Autowired
    private ISOrderService orderService;

    @Autowired
    private ISOrderDetailService orderDetailService;

    @Autowired
    private ISProductService productService;

    @Autowired
    private ISOfferPriceService offerPriceService;

    @Autowired
    private ISUserService userService;

    @Autowired
    private ISUserLevelService userLevelService;

    @Autowired
    private ISUserAmountLogService userAmountLogService;

    @Autowired
    private ISUserBeanLogService userBeanLogService;

    @Autowired
    private ISUserBonusLogService userBonusLogService;

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

                // 修改s_user_task支付状态 已支付
                userTask.setPayStatus(1);
                userTask.setPayAmount(total);
                userTask.setUpdateTime(new Date());
                userTask.setPayTime(new Date());
                this.userTaskService.updateUserTask(userTask);

                // 修改s_user_task_line 支付状态 已支付

                // 修改s_task_line 锁定任务数量-1  已领取任务数量+1

                // 修改优惠券状态(已使用) 及 流水记录(s_user_coupon_log)追加


                // 拆豆奖励（10） s_user (reward_bean+10) 及 拆豆流水记录追加 SUserBeanLog
//                SUserBeanLog userBeanLog = new SUserBeanLog();
//                userBeanLog.setUserId(user.getId());
//                userBeanLog.setChangeType(1);
//                userBeanLog.setChangeAmount(userLevel.getBeanRate().multiply(BigDecimal.valueOf(20)).intValue());
//                userBeanLog.setChangeTime(new Date());
//                userBeanLog.setRelationId(userTask.getId());
//                userBeanLog.setRemark("领取任务ID");
//                userBeanLog.setOldAmount(user.getCanuseBean());
//                this.userBeanLogService.save(userBeanLog);

                // 此时若还没有上级，形成正式上下级绑定关系 找到他的上级
                //if (user.getParentId() == null) {
                    // 根据user_id、productId 到s_user_browser表中 找到shareId

                    // 根据shareId 到 s_user_share 表中 找到user_id 作为他的上级ID 更新到s_user中的parentId

                //}

            }


            if ("O".equals(strPayType)) {

                // 支付购买订单成功
                SOrder order = this.orderService.getById(relationId);
                userPay.setUserId(order.getUserId());
                this.userPayService.save(userPay);

                // 变更批量订单状态 已付款
                order.setPaymentState(1);
                order.setPaymentState(1);
                order.setPaymentTime(new Date());
                this.orderService.updateOrder(order);

                // 变更订单明细状态 已付款
                SOrderDetail orderDetail = new SOrderDetail();
                orderDetail.setUserId(order.getUserId());
                orderDetail.setOrderId(order.getId());
                orderDetail.setOrderStatus(1);
                orderDetail.setPaymentState(1);
                orderDetail.setPaymentTime(new Date());

                this.orderDetailService.updateOrderDetail(orderDetail);


                // 根据订单明细中的商品 然后从任务线表(s_task_line)中按顺序选中 结算状态未完成 作为结算任务线 修改为 结算中 并把orderDetailId 更新进去

                // 上面任务线表中的所有 转让中的任务终止动作 （暂不做）
                // 1.转让任务表状态更新 （转让中 - > 未成交流标）
                // 2.任务报价表 全部竞标中修改已出局状态  所有出局者支付金额退还 (金额流水插入 冻结-余额）
                // 3.用户任务表状态更新（转让中 -> 已接任务）


                // 然后选中的结算任务线 对应的 用户任务线表(s_user_task_line) 状态修改为 佣金结算中

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
