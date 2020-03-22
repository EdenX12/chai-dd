package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.*;
import cc.mrbird.febs.api.service.*;
import cc.mrbird.febs.common.annotation.Limit;
import cc.mrbird.febs.common.annotation.Log;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.domain.FebsResponse;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.utils.FebsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping("/api/s-task-order")
public class STaskOrderController extends BaseController {

    private String message;

    @Autowired
    private ISTaskOrderService taskOrderService;

    @Autowired
    private ISUserTaskService userTaskService;

    @Autowired
    private ISOfferPriceService offerPriceService;

    @Autowired
    private ISUserService userService;

    @Autowired
    private ISUserLevelService userLevelService;

    @Autowired
    private ISUserBeanLogService userBeanLogService;

    @Autowired
    private ISUserAmountLogService userAmountLogService;

    /**
     * 新增任务转让
     */
    @Log("新增任务转让")
    @Transactional
    @PostMapping("/addTaskOrder")
    public FebsResponse addTaskOrder(@Valid STaskOrder taskOrder) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {

            // 用户任务检索
            SUserTask userTask = userTaskService.getById(taskOrder.getTaskId());

            if (taskOrder.getTaskNumber() <=0) {
                message = "转让份数必须大于0";
                response.put("code", 1);
                response.message(message);
                return response;
            }

            if (userTask.getPayStatus() != 1) {
                message = "您的任务还没有领取！";
                response.put("code", 1);
                response.message(message);
                return response;
            }

            if (taskOrder.getTaskNumber() > userTask.getTaskNumber()) {
                message = "转让份数不能超过已有任务份数！";
                response.put("code", 1);
                response.message(message);
                return response;
            }

            if (userTask.getStatus() != 0) {
                message = "您现在的任务不能转让！";
                response.put("code", 1);
                response.message(message);
                return response;
            }

            // 转让份数小于任务份数的情况下，在原有的任务上更新转让数量 变为转让中  追加一条剩余转让数量的任务
            // 转让份数等于任务份数的情况下 直接修改为转让中
            if (taskOrder.getTaskNumber() < userTask.getTaskNumber()) {

                // 更新原任务的任务数量
                userTask.setTaskNumber(taskOrder.getTaskNumber());
                userTask.setPayAmount(userTask.getPayAmount().multiply( BigDecimal.valueOf(taskOrder.getTaskNumber() / userTask.getTaskNumber())));
                userTask.setStatus(1);
                this.userTaskService.updateUserTask(userTask);

                // 追加新的用户任务
                userTask.setTaskNumber(userTask.getTaskNumber() - taskOrder.getTaskNumber());
                userTask.setPayAmount(userTask.getPayAmount().multiply( BigDecimal.valueOf((userTask.getTaskNumber() - taskOrder.getTaskNumber()) / userTask.getTaskNumber())));
                userTask.setStatus(0);
                this.userTaskService.createUserTask(userTask);

            } else {

                userTask.setStatus(1);
                this.userTaskService.updateUserTask(userTask);
            }

            SUser user = FebsUtil.getCurrentUser();
            taskOrder.setUserId(user.getId());

            taskOrder.setCreateTime(new Date());
            taskOrder = this.taskOrderService.createTaskOrder(taskOrder);

            // 每参与一次任务转出 猎豆追加 10颗  * 猎人等级倍数
            SUserLevel userLevel = this.userLevelService.getById(user.getUserLevelId());
            user.setCanuseBean(user.getCanuseBean() + userLevel.getBeanRate().multiply(BigDecimal.valueOf(10)).intValue());
            this.userService.updateById(user);

            // 猎豆流水插入
            SUserBeanLog userBeanLog = new SUserBeanLog();
            userBeanLog.setUserId(user.getId());
            userBeanLog.setChangeType(5);
            userBeanLog.setChangeAmount(userLevel.getBeanRate().multiply(BigDecimal.valueOf(10)).intValue());
            userBeanLog.setChangeTime(new Date());
            userBeanLog.setRelationId(taskOrder.getId());
            userBeanLog.setRemark("关联任务转出ID");
            userBeanLog.setOldAmount(user.getCanuseBean());
            this.userBeanLogService.save(userBeanLog);

        } catch (Exception e) {
            message = "新增任务转让失败";
            response.put("code", 1);
            response.message(message);
            log.error(message, e);
        }

        return response;
    }

    /**
     * 取消终止任务转让
     */
    @Log("取消终止任务转让")
    @Transactional
    @PostMapping("/cancelTaskOrder")
    public FebsResponse cancelTaskOrder(@Valid STaskOrder taskOrder) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {

            SUser user = FebsUtil.getCurrentUser();
            taskOrder.setUserId(user.getId());
            taskOrder.setStatus(0);
            taskOrder = this.taskOrderService.findTaskOrder(taskOrder);

            if (taskOrder == null) {
                message = "您需要取消终止的转让任务不存在！";
                response.put("code", 1);
                response.message(message);
                return response;
            }

            // 已报价用户报价状态修改为已出局 金额退还到余额
            SOfferPrice offerPrice = new SOfferPrice();
            offerPrice.setTaskOrderId(taskOrder.getId());
            offerPrice = this.offerPriceService.updateOfferPriceOut(offerPrice);

            List<SOfferPrice> offerPriceOutList = this.offerPriceService.findOfferPriceOutList(offerPrice);

            for (SOfferPrice offerPriceOut : offerPriceOutList) {
                SUser userOut = this.userService.getById(offerPriceOut.getUserId());

                // 金额流水插入
                SUserAmountLog userAmountLog = new SUserAmountLog();
                userAmountLog.setUserId(userOut.getId());
                userAmountLog.setChangeType(8);
                userAmountLog.setChangeAmount(offerPriceOut.getAmount());
                userAmountLog.setChangeTime(new Date());
                userAmountLog.setRelationId(offerPriceOut.getId());
                userAmountLog.setRemark("关联任务报价ID");
                userAmountLog.setOldAmount(userOut.getTotalAmount());
                this.userAmountLogService.save(userAmountLog);

                // 冻结金额-
                userOut.setLockAmount(userOut.getLockAmount().subtract(offerPriceOut.getAmount()));
                // 余额+
                userOut.setTotalAmount(userOut.getTotalAmount().add(offerPriceOut.getAmount()));

                this.userService.updateById(userOut);
            }

            // 转让任务状态（转让中 -> 未成交流标）
            STaskOrder taskOrderOut = new STaskOrder();
            taskOrderOut = this.taskOrderService.getById(taskOrder.getId());
            taskOrderOut.setStatus(2);
            this.taskOrderService.updateById(taskOrderOut);

            // 用户任务状态 （转让中 -> 已接任务）
            SUserTask userTask = new SUserTask();
            userTask.setId(taskOrderOut.getTaskId());
            userTask.setStatus(0);
            this.userTaskService.updateById(userTask);

        } catch (Exception e) {
            message = "取消终止任务转让失败";
            response.put("code", 1);
            response.message(message);
            log.error(message, e);
        }

        return response;
    }

    /**
     * 取得所有转让任务信息
     * @return List<Map>
     */
    @PostMapping("/getTaskOrderList")
    @Limit(key = "getTaskOrderList", period = 60, count = 20, name = "检索全部转让任务接口", prefix = "limit")
    public FebsResponse getTaskOrderList(QueryRequest queryRequest, STaskOrder taskOrder) {

        FebsResponse response = new FebsResponse();

        SUser user = FebsUtil.getCurrentUser();
        taskOrder.setUserId(user.getId());

        Map<String, Object> taskOrderPageList = getDataTable(this.taskOrderService.findTaskOrderList(taskOrder, queryRequest));

        response.put("code", 0);
        response.data(taskOrderPageList);

        return response;
    }

    /**
     * 取得转让任务详情信息
     * @return Map
     */
    @PostMapping("/getTaskOrderDetail")
    @Limit(key = "getTaskOrderDetail", period = 60, count = 20, name = "检索转让任务详情接口", prefix = "limit")
    public FebsResponse getTaskOrderDetail(QueryRequest queryRequest, STaskOrder taskOrder) {

        FebsResponse response = new FebsResponse();

        SUser user = FebsUtil.getCurrentUser();
        taskOrder.setUserId(user.getId());

        Map<String, Object> taskOrderDetail = this.taskOrderService.findTaskOrderDetail(taskOrder);

        response.put("code", 0);
        response.data(taskOrderDetail);

        return response;
    }
}
