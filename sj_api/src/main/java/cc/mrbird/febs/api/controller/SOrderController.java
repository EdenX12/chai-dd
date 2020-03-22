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
import java.util.ArrayList;
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
    private ISUserLevelService userLevelService;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private ISUserAmountLogService userAmountLogService;

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

            // 判断此用户是否领取过任务
            boolean taskGotUser = false;

            // 其他人总的领取份数
            int totalTaskNumberOther = 0;

            for (SUserTask userTasked : userTaskList) {
                if (order.getUserId().equals(userTasked.getUserId())) {
                    taskGotUser = true;
                } else {
                    totalTaskNumberOther = totalTaskNumberOther + userTasked.getTaskNumber();
                }
            }

            // 独赢收益计算 (如领取过任务 40%  50%由其他领取该任务者均分 未领取过任务 5%  80%被其他领取该任务者均分)
            BigDecimal everyReward = new BigDecimal(0);
            if (taskGotUser) {
                SUser user1 = this.userService.getById(userTask.getUserId());

                // 金额流水插入
                SUserAmountLog userAmountLog = new SUserAmountLog();
                userAmountLog.setUserId(user1.getId());
                userAmountLog.setChangeType(3);
                userAmountLog.setChangeAmount(product.getTotalReward().multiply(BigDecimal.valueOf(0.4)));
                userAmountLog.setChangeTime(new Date());
                userAmountLog.setRelationId(userTask.getId());
                userAmountLog.setRemark("关联任务ID");
                userAmountLog.setOldAmount(user.getTotalAmount());
                this.userAmountLogService.save(userAmountLog);

                user1.setTotalAmount(user1.getTotalAmount().add(product.getTotalReward().multiply(BigDecimal.valueOf(0.4))));
                this.userService.updateById(user1);

                // 每份任务的躺赢收益
                everyReward = product.getTotalReward().multiply(BigDecimal.valueOf(0.5)).divide(BigDecimal.valueOf(totalTaskNumberOther));
            } else {
                SUser user1 = this.userService.getById(userTask.getUserId());

                // 金额流水插入
                SUserAmountLog userAmountLog = new SUserAmountLog();
                userAmountLog.setUserId(user1.getId());
                userAmountLog.setChangeType(3);
                userAmountLog.setChangeAmount(product.getTotalReward().multiply(BigDecimal.valueOf(0.05)));
                userAmountLog.setChangeTime(new Date());
                userAmountLog.setRelationId(userTask.getId());
                userAmountLog.setRemark("关联任务ID");
                userAmountLog.setOldAmount(user.getTotalAmount());
                this.userAmountLogService.save(userAmountLog);

                user1.setTotalAmount(user1.getTotalAmount().add(product.getTotalReward().multiply(BigDecimal.valueOf(0.05))));
                this.userService.updateById(user1);

                // 每份任务的躺赢收益
                everyReward = product.getTotalReward().multiply(BigDecimal.valueOf(0.8)).divide(BigDecimal.valueOf(totalTaskNumberOther));
            }

            for (SUserTask userTasked : userTaskList) {

                // 躺赢收益计算  任务金退还（冻结 - > 余额）
                SUser user1 = this.userService.getById(userTasked.getUserId());

                // 任务金退还（冻结 - > 余额）
                // 冻结金额-
                user1.setLockAmount(user1.getLockAmount().subtract(product.getTaskPrice().multiply(BigDecimal.valueOf(userTasked.getTaskNumber()))));
                // 余额+
                user1.setTotalAmount(user1.getTotalAmount().add(product.getTaskPrice().multiply(BigDecimal.valueOf(userTasked.getTaskNumber()))));

                // 任务解冻金额流水插入
                SUserAmountLog userAmountLog = new SUserAmountLog();
                userAmountLog.setUserId(user1.getId());
                userAmountLog.setChangeType(9);
                userAmountLog.setChangeAmount(product.getTaskPrice().multiply(BigDecimal.valueOf(userTasked.getTaskNumber())));
                userAmountLog.setChangeTime(new Date());
                userAmountLog.setRelationId(userTasked.getId());
                userAmountLog.setRemark("关联任务ID");
                userAmountLog.setOldAmount(user.getTotalAmount());
                this.userAmountLogService.save(userAmountLog);

                // 躺赢收益计算
                user1.setTotalAmount(user1.getTotalAmount().add(everyReward.multiply(BigDecimal.valueOf(userTasked.getTaskNumber()))));
                this.userService.updateById(user1);

                // 躺赢金额流水插入
                userAmountLog = new SUserAmountLog();
                userAmountLog.setUserId(user1.getId());
                userAmountLog.setChangeType(4);
                userAmountLog.setChangeAmount(everyReward.multiply(BigDecimal.valueOf(userTasked.getTaskNumber())));
                userAmountLog.setChangeTime(new Date());
                userAmountLog.setRelationId(userTasked.getId());
                userAmountLog.setRemark("关联任务ID");
                userAmountLog.setOldAmount(user.getTotalAmount());
                this.userAmountLogService.save(userAmountLog);

                // 佣金已入账 状态更新
                userTasked.setUpdateTime(new Date());
                userTasked.setStatus(4);
                userTaskService.updateById(userTasked);
            }

            // 下级贡献收益计算 往上推四级
            List<SUser> userList = new ArrayList();
            if (user.getParentId() != null) {
                // 第一级
                SUser user1 = userService.getById(user.getParentId());
                userList.add(user1);
                if (user1.getParentId() != null) {
                    // 第二级
                    SUser user2 = userService.getById(user1.getParentId());
                    userList.add(user2);
                    if (user2.getParentId() != null) {
                        // 第三级
                        SUser user3 = userService.getById(user2.getParentId());
                        userList.add(user3);
                        if (user3.getParentId() != null) {
                            // 第四级
                            SUser user4 = userService.getById(user3.getParentId());
                            userList.add(user4);
                        }
                    }
                }
            }

            // （如是见习猎人分0.5%;如是初级猎手分1%，如遇中级猎人分2%，如遇高级猎人分3%）
            for (SUser user0 : userList) {
                SUserLevel userLevel = userLevelService.getById(user0.getUserLevelId());

                // 金额流水插入
                SUserAmountLog userAmountLog = new SUserAmountLog();
                userAmountLog.setUserId(user0.getId());
                userAmountLog.setChangeType(5);
                userAmountLog.setChangeAmount(product.getTotalReward().multiply(userLevel.getIncomeRate()));
                userAmountLog.setChangeTime(new Date());
                userAmountLog.setRelationId(order.getId());
                userAmountLog.setRemark("关联订单ID");
                userAmountLog.setOldAmount(user.getTotalAmount());
                this.userAmountLogService.save(userAmountLog);

                user0.setTotalAmount(user0.getTotalAmount().add(
                        product.getTotalReward().multiply(userLevel.getIncomeRate())));
                this.userService.updateById(user0);
            }

        } catch (Exception e) {
            message = "更新用户购买订单状态失败";
            log.error(message, e);
            throw new FebsException(message);
        }
    }
}
