package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.*;
import cc.mrbird.febs.api.service.*;
import cc.mrbird.febs.common.annotation.Limit;
import cc.mrbird.febs.common.annotation.Log;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.domain.FebsResponse;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.exception.FebsException;
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
import java.util.List;
import java.util.Map;

/**
 * @author MrBird
 */
@Slf4j
@RestController
@RequestMapping("/api/s-order")
public class SOrderController extends BaseController {

    private String message;

    @Autowired
    private ISOrderService orderService;

    @Autowired
    private ISProductService productService;

    @Autowired
    private ISUserTaskService userTaskService;

    @Autowired
    private ISUserService userService;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private ISUserAmountLogService userAmountLogService;

    @Autowired
    private ISUserBonusLogService userBonusLogService;

    /**
     * 新增用户购买订单
     */
    @Log("新增用户购买订单")
    @Transactional
    @PostMapping("/addOrder")
    public FebsResponse addOrder(HttpServletRequest request, @Valid SOrder order) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {

            SUser user = FebsUtil.getCurrentUser();
            order.setUserId(user.getId());

            SProduct product = productService.getById(order.getProductId());

            order.setOrderStatus(0);
            order.setCreateTime(new Date());
            order.setPaymentType(1);
            order.setPaymentState(0);
            order.setOrderAmount(product.getProductPrice().multiply(BigDecimal.valueOf(order.getProductNumber())));
            order.setPayAmount(product.getProductPrice().multiply(BigDecimal.valueOf(order.getProductNumber())));

            order = this.orderService.addOrder(order);

            // 调起微信支付
            JSONObject jsonObject = weChatPayUtil.weChatPay(String.valueOf(order.getId()),
                    product.getProductPrice().multiply(BigDecimal.valueOf(order.getProductNumber().longValue())).toString(),
                    user.getOpenId(),
                    request.getRemoteAddr(),
                    "2",
                    "购买商品");

            response.data(jsonObject);

        } catch (Exception e) {
            message = "购买订单失败";
            response.put("code", 1);
            response.message(message);
            log.error(message, e);
        }

        return response;
    }

    /**
     * 根据用户ID取得我的购买订单列表信息
     * @return List<SOrder>
     */
    @PostMapping("/getOrderList")
    @Limit(key = "getOrderList", period = 60, count = 20, name = "检索我的购买订单接口", prefix = "limit")
    public FebsResponse getOrderList(QueryRequest queryRequest, SOrder order) {

        FebsResponse response = new FebsResponse();

        SUser user = FebsUtil.getCurrentUser();
        order.setUserId(user.getId());

        Map<String, Object> orderPageList = getDataTable(orderService.findOrderList(order, queryRequest));

        response.put("code", 0);
        response.data(orderPageList);

        return response;
    }

    /**
     * 根据用户ID取得我的购买订单详情
     * @return SOrder
     */
    @PostMapping("/getOrderDetail")
    @Limit(key = "getOrderDetail", period = 60, count = 20, name = "检索用户购买订单详情接口", prefix = "limit")
    public FebsResponse getOrderDetail(SOrder order) {

        FebsResponse response = new FebsResponse();

        SUser user = FebsUtil.getCurrentUser();
        order.setUserId(user.getId());

        SOrder orderDetail = orderService.findOrderDetail(order);

        response.put("code", 0);
        response.data(orderDetail);

        return response;
    }

    /**
     * 确认收货
     * 用户ID 订单ID 状态
     */
    @Log("用户确认收货")
    @Transactional
    @PostMapping("/confirmOrder")
    public void confirmOrder(@Valid SOrder order) throws FebsException {

        try {

            SUser user = FebsUtil.getCurrentUser();
            order.setUserId(user.getId());

            order.setOrderStatus(3);
            order = this.orderService.updateOrder(order);

            SProduct product = productService.getById(order.getProductId());

            // 产品下所有已完结 任务金退还（冻结 - > 余额）  并且修改状态为佣金已入账
            SUserTask userTask = new SUserTask();
            userTask.setProductId(order.getProductId());
            userTask.setPayStatus(1);
            userTask.setStatus(3);
            List<SUserTask> userTaskList = userTaskService.findUserTaskList(userTask);
            for (SUserTask userTasked : userTaskList) {

                // 佣金已入账 状态更新
                userTasked.setUpdateTime(new Date());
                userTasked.setStatus(4);
                this.userTaskService.updateById(userTasked);
            }

            SUserBonusLog userBonusLog = new SUserBonusLog();

            userBonusLog.setOrderId(order.getId());

            List<SUserBonusLog> userBonusLogList = userBonusLogService.findUserBonusList(userBonusLog);

            for (SUserBonusLog userBonus : userBonusLogList) {

                if (userBonus.getBonusType() == 3) {

                    // 独赢奖励 （余额+）
                    SUser user1 = this.userService.getById(userBonus.getUserId());

                    // 金额流水插入
                    SUserAmountLog userAmountLog = new SUserAmountLog();
                    userAmountLog.setUserId(user1.getId());
                    userAmountLog.setChangeType(3);
                    userAmountLog.setChangeAmount(userBonus.getBonusAmount());
                    userAmountLog.setChangeTime(new Date());
                    userAmountLog.setRelationId(userBonus.getTaskId());
                    userAmountLog.setRemark("关联任务ID");
                    userAmountLog.setOldAmount(user1.getTotalAmount());
                    this.userAmountLogService.save(userAmountLog);

                    user1.setTotalAmount(user1.getTotalAmount().add(userBonus.getBonusAmount()));
                    this.userService.updateById(user1);

                } else if (userBonus.getBonusType() == 4) {

                    // 躺赢奖励（余额+）
                    SUser user1 = this.userService.getById(userBonus.getUserId());

                    // 躺赢金额流水插入
                    SUserAmountLog userAmountLog = new SUserAmountLog();
                    userAmountLog = new SUserAmountLog();
                    userAmountLog.setUserId(user1.getId());
                    userAmountLog.setChangeType(4);
                    userAmountLog.setChangeAmount(userBonus.getBonusAmount().multiply(BigDecimal.valueOf(userBonus.getTaskNumber())));
                    userAmountLog.setChangeTime(new Date());
                    userAmountLog.setRelationId(userBonus.getTaskId());
                    userAmountLog.setRemark("关联任务ID");
                    userAmountLog.setOldAmount(user1.getTotalAmount());
                    this.userAmountLogService.save(userAmountLog);

                    user1.setTotalAmount(user1.getTotalAmount().add(userBonus.getBonusAmount().multiply(BigDecimal.valueOf(userBonus.getTaskNumber()))));
                    this.userService.updateById(user1);

                } else if (userBonus.getBonusType() == 5) {

                    // 下级奖励（余额+）
                    SUser user1 = this.userService.getById(userBonus.getUserId());

                    // 躺赢金额流水插入
                    SUserAmountLog userAmountLog = new SUserAmountLog();
                    userAmountLog = new SUserAmountLog();
                    userAmountLog.setUserId(user1.getId());
                    userAmountLog.setChangeType(5);
                    userAmountLog.setChangeAmount(userBonus.getBonusAmount());
                    userAmountLog.setChangeTime(new Date());
                    userAmountLog.setRelationId(userBonus.getTaskId());
                    userAmountLog.setRemark("关联任务ID");
                    userAmountLog.setOldAmount(user1.getTotalAmount());
                    this.userAmountLogService.save(userAmountLog);

                    user1.setTotalAmount(user1.getTotalAmount().add(userBonus.getBonusAmount()));
                    this.userService.updateById(user1);

                } else if (userBonus.getBonusType() == 9) {

                    // 任务金奖励（余额+ 冻结-）
                    SUser user1 = this.userService.getById(userBonus.getUserId());

                    // 冻结-
                    user1.setLockAmount(user1.getLockAmount().subtract(userBonus.getBonusAmount().multiply(BigDecimal.valueOf(userBonus.getTaskNumber()))));
                    // 余额+
                    user1.setTotalAmount(user1.getTotalAmount().add(userBonus.getBonusAmount().multiply(BigDecimal.valueOf(userBonus.getTaskNumber()))));

                    this.userService.updateById(user1);

                    // 任务解冻金额流水插入
                    SUserAmountLog userAmountLog = new SUserAmountLog();
                    userAmountLog.setUserId(user1.getId());
                    userAmountLog.setChangeType(9);
                    userAmountLog.setChangeAmount(userBonus.getBonusAmount().multiply(BigDecimal.valueOf(userBonus.getTaskNumber())));
                    userAmountLog.setChangeTime(new Date());
                    userAmountLog.setRelationId(userBonus.getTaskId());
                    userAmountLog.setRemark("关联任务ID");
                    userAmountLog.setOldAmount(user1.getTotalAmount());
                    this.userAmountLogService.save(userAmountLog);
                }
            }

        } catch (Exception e) {
            message = "更新用户购买订单状态失败";
            log.error(message, e);
            throw new FebsException(message);
        }
    }
}
