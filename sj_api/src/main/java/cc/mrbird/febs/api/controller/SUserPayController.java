package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.*;
import cc.mrbird.febs.api.service.*;
import cc.mrbird.febs.common.annotation.Log;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.exception.FebsException;
import cc.mrbird.febs.common.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private ISTaskOrderService taskOrderService;

    @Autowired
    private ISUserTaskService userTaskService;

    @Autowired
    private ISOrderService orderService;

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

    /**
     * 新增用户支付
     */
    @Log("新增用户支付")
    @PostMapping("/addUserPay")
    public void addUserPay(@Valid SUserPay userPay) throws FebsException {

        try {

            userPay.setCreateTime(new Date());

            SUser user = FebsUtil.getCurrentUser();
            userPay.setUserId(user.getId());

            this.userPayService.save(userPay);

        } catch (Exception e) {
            message = "新增用户支付失败";
            log.error(message, e);
            throw new FebsException(message);
        }
    }

    /**
     * 微信支付成功回调
     */
    @Log("微信支付成功回调")
    @PostMapping("/paySuccess")
    public void paySuccess(HttpServletRequest request, HttpServletResponse response) throws FebsException {

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
        BigDecimal total = new BigDecimal(total_fee).divide(new BigDecimal("100"));

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
            userPay.setRelationId(Long.parseLong(relationId));
            userPay.setTransSn(transSn);
            userPay.setPayType(1);

            if ("T".equals(strPayType)) {

                // 领取任务支付成功
                SUserTask userTask = this.userTaskService.getById(relationId);

                userPay.setUserId(userTask.getUserId());

                this.userPayService.save(userPay);

                // 变更用户任务的支付状态
                userTask.setPayStatus(1);
                userTask.setPayAmount(total);
                userTask.setUpdateTime(new Date());
                this.userTaskService.updateUserTask(userTask);

                // 用户冻结金额追加  猎豆追加  领取任务的人（20颗）  * 猎人等级倍数
                SUser user = this.userService.getById(userTask.getUserId());
                SUserLevel userLevel = this.userLevelService.getById(user.getUserLevelId());
                user.setLockAmount(user.getLockAmount().add(total));
                user.setCanuseBean(user.getCanuseBean() + userLevel.getBeanRate().multiply(BigDecimal.valueOf(20)).intValue());
                // 领取任务次数 + 1
                int taskCount = user.getTaskCount() + 1;
                user.setTaskCount(taskCount);
                // 变更个人等级
                List<SUserLevel> userLevelList = userLevelService.list();
                Long userLevelId = null;
                for (SUserLevel userLevelAll : userLevelList) {
                    if (taskCount>= userLevelAll.getMinNumber() && taskCount <= userLevelAll.getMaxNumber()) {
                        userLevelId = userLevelAll.getId();
                    }
                }
                user.setUserLevelId(userLevelId);
                this.userService.updateById(user);

                // 猎豆流水插入
                SUserBeanLog userBeanLog = new SUserBeanLog();
                userBeanLog.setUserId(user.getId());
                userBeanLog.setChangeType(1);
                userBeanLog.setChangeAmount(userLevel.getBeanRate().multiply(BigDecimal.valueOf(20)).intValue());
                userBeanLog.setChangeTime(new Date());
                userBeanLog.setRelationId(userTask.getId());
                userBeanLog.setRemark("领取任务ID");
                userBeanLog.setOldAmount(user.getCanuseBean());
                this.userBeanLogService.save(userBeanLog);

            } else if ("O".equals(strPayType)) {

                // 支付购买订单成功
                SOrder order = this.orderService.getById(relationId);

                userPay.setUserId(order.getUserId());

                this.userPayService.save(userPay);

                // 变更订单状态 已付款
                order.setPaymentState(1);
                order.setOrderStatus(1);
                order.setPaymentTime(new Date());
                this.orderService.updateOrder(order);

                // 产品下的所有 转让中的任务终止动作：
                SUserTask userTask = new SUserTask();
                userTask.setProductId(order.getProductId());
                userTask.setPayStatus(1);
                userTask.setStatus(1);
                List<SUserTask> userTaskList = this.userTaskService.findUserTaskList(userTask);

                for (SUserTask userTasking : userTaskList) {

                    // 2.转让任务表状态更新 （转让中 - > 未成交流标）
                    STaskOrder taskOrder = new STaskOrder();
                    taskOrder.setTaskId(userTasking.getId());
                    taskOrder.setStatus(0);
                    List<STaskOrder> taskOrderList = taskOrderService.findTaskOrderList(taskOrder);

                    for (STaskOrder taskOrdering : taskOrderList) {

                        // 1.任务报价表 全部竞标中修改已出局状态  所有出局者支付金额退还
                        SOfferPrice offerPrice = new SOfferPrice();
                        offerPrice.setTaskOrderId(taskOrdering.getId());
                        this.offerPriceService.updateOfferPriceOut(offerPrice);

                        // 所有出局者
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
                            userAmountLog.setRemark("关联任务报价ID");
                            userAmountLog.setOldAmount(user.getTotalAmount());
                            this.userAmountLogService.save(userAmountLog);

                            // 冻结金额-
                            user.setLockAmount(user.getLockAmount().subtract(offerPriceOut.getAmount()));
                            // 余额+
                            user.setTotalAmount(user.getTotalAmount().add(offerPriceOut.getAmount()));

                            this.userService.updateById(user);
                        }

                        taskOrdering.setStatus(2);
                        this.taskOrderService.updateById(taskOrdering);
                    }

                    // 3.用户任务表状态更新（转让中 -> 任务完结）
                    userTasking.setStatus(3);
                    this.userTaskService.updateById(userTasking);
                }

                // 4.用户任务表状态更新（已接任务 -> 任务完结）
                userTask.setProductId(order.getProductId());
                userTask.setPayStatus(1);
                userTask.setStatus(0);
                userTaskList = this.userTaskService.findUserTaskList(userTask);
                for (SUserTask userTask0 : userTaskList) {
                    userTask0.setStatus(3);
                    this.userTaskService.updateById(userTask0);
                }

            } else if ("P".equals(strPayType)) {

                // 转让任务报价成功
                SOfferPrice offerPrice = this.offerPriceService.getById(relationId);

                userPay.setUserId(offerPrice.getUserId());

                this.userPayService.save(userPay);

                // 变更订单状态 已付款
                offerPrice.setPayStatus(1);
                offerPrice.setUpdateTime(new Date());
                this.offerPriceService.updateById(offerPrice);

                // 用户冻结金额追加   每参与一次任务报价 （10颗） * 猎人等级倍数
                SUser user = this.userService.getById(offerPrice.getUserId());
                SUserLevel userLevel = this.userLevelService.getById(user.getUserLevelId());
                user.setLockAmount(user.getLockAmount().add(total));
                user.setCanuseBean(user.getCanuseBean() + userLevel.getBeanRate().multiply(BigDecimal.valueOf(10)).intValue());
                this.userService.updateById(user);

                // 猎豆流水插入
                SUserBeanLog userBeanLog = new SUserBeanLog();
                userBeanLog.setUserId(user.getId());
                userBeanLog.setChangeType(6);
                userBeanLog.setChangeAmount(userLevel.getBeanRate().multiply(BigDecimal.valueOf(10)).intValue());
                userBeanLog.setChangeTime(new Date());
                userBeanLog.setRelationId(offerPrice.getId());
                userBeanLog.setRemark("转让任务报价ID");
                userBeanLog.setOldAmount(user.getCanuseBean());
                this.userBeanLogService.save(userBeanLog);
            }

        }

        ResponseWriteUtil.responseWriteClient(request, response, "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>");
    }
}
