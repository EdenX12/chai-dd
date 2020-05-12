package cc.mrbird.febs.task.controller;

import cc.mrbird.febs.task.entity.*;
import cc.mrbird.febs.task.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author MrBird
 */
@Slf4j
@RestController
@Configuration
@EnableScheduling
public class SOrderJobTask {

    @Autowired
    private ISOrderService orderService;

    @Autowired
    private ISOrderDetailService orderDetailService;

    @Autowired
    private ISOrderProductService orderProductService;

    @Autowired
    private ISTaskLineService taskLineService;

    @Autowired
    private ISUserTaskLineService userTaskLineService;

    @Autowired
    private ISUserService userService;

    @Autowired
    private ISUserBonusLogService userBonusLogService;

    @Autowired
    private ISParamsService paramsService;

    @Autowired
    private ISUserRelationService userRelationService;

    /**
     * 订单支付成功后计算奖励金额到冻结
     * 2分钟执行一次 (支付成功时间超过5分钟的订单处理)
     */
    @Scheduled(cron = "0 */2 * * * ?")
    @Transactional
    public void orderPaySuccessTask() {

        // 支付成功超过5分钟订单
        List<SOrder> orderPaySuccessList = this.orderService.findOrderPaySuccessList();

        // 买家立返佣金比例 （后续调整到Redis缓存读取）
        SParams params = new SParams();
        params = this.paramsService.queryBykeyForOne("buyer_rate");
        BigDecimal buyerRate = BigDecimal.valueOf(Double.parseDouble(params.getPValue()));
        // 同组任务躺赢佣金比例
        params = this.paramsService.queryBykeyForOne("same_group_rate");
        BigDecimal sameGroupRate = BigDecimal.valueOf(Double.parseDouble(params.getPValue()));
        // 横向上级躺赢佣金比例
        params = this.paramsService.queryBykeyForOne("upper_horizontal_rate");
        BigDecimal upperHorizontalRate = BigDecimal.valueOf(Double.parseDouble(params.getPValue()));
        // 纵向上级1躺赢佣金比例
        params = this.paramsService.queryBykeyForOne("upper_vertical1_rate1");
        BigDecimal upperVertical1Rate1 = BigDecimal.valueOf(Double.parseDouble(params.getPValue()));
        // 纵向上级2躺赢佣金比例
        params = this.paramsService.queryBykeyForOne("upper_vertical1_rate2");
        BigDecimal upperVertical1Rate2 = BigDecimal.valueOf(Double.parseDouble(params.getPValue()));
        // 纵向上级3躺赢佣金比例
        params = this.paramsService.queryBykeyForOne("upper_vertical1_rate3");
        BigDecimal upperVertical1Rate3 = BigDecimal.valueOf(Double.parseDouble(params.getPValue()));
        // 纵向上级0躺赢佣金比例
        params = this.paramsService.queryBykeyForOne("upper_vertical1_rate0");
        BigDecimal upperVertical1Rate0 = BigDecimal.valueOf(Double.parseDouble(params.getPValue()));

        for (SOrder order : orderPaySuccessList) {

            // 变更订单状态 （9:已结算到冻结）
            order.setPaymentState(9);
            this.orderService.updateById(order);

            SOrderDetail orderDetail = new SOrderDetail();
            orderDetail.setOrderId(order.getId());
            List<SOrderDetail> orderDetailPaySuccessList = this.orderDetailService.findOrderDetailList(orderDetail);

            SUser userLock = new SUser();
            for (SOrderDetail orderDetailPaySuccess : orderDetailPaySuccessList) {

                // 变更订单明细状态 （9:已结算到冻结）
                orderDetailPaySuccess.setPaymentState(9);
                this.orderDetailService.updateById(orderDetailPaySuccess);

                List<SOrderProduct> orderProductList = this.orderProductService.findOrderProductList(orderDetailPaySuccess.getId());

                for (SOrderProduct orderProduct : orderProductList) {

                    // 根据任务线中的订单商品ID，检索任务线表（s_task_line）
                    STaskLine taskLine = new STaskLine();
                    taskLine.setOrderProductId(orderProduct.getId());
                    List<STaskLine> taskLineList = this.taskLineService.findTaskLineList(taskLine);

                    for (STaskLine taskLineOne : taskLineList) {

                        // 独赢（买家立返）20%
                        SUserBonusLog userBonusLog = new SUserBonusLog();
                        userBonusLog.setUserId(orderProduct.getUserId());
                        userBonusLog.setOrderDetailId(orderDetailPaySuccess.getId());
                        userBonusLog.setTaskLineId(taskLineOne.getId());
                        userBonusLog.setUserTaskLineId(null);
                        userBonusLog.setProductId(orderProduct.getProductId());
                        userBonusLog.setBonusType(1);
                        userBonusLog.setBonusAmount(orderProduct.getTotalReward().multiply(buyerRate));
                        userBonusLog.setCreateTime(new Date());
                        userBonusLog.setUpdateTime(new Date());
                        userBonusLog.setStatus(0);
                        this.userBonusLogService.save(userBonusLog);

                        // 奖励金额到冻结（冻结+）
                        userLock = this.userService.getById(userBonusLog.getUserId());
                        userLock.setLockAmount(userLock.getLockAmount().add(userBonusLog.getBonusAmount()));
                        this.userService.updateById(userLock);

                        // 纵向躺赢 从买家的捆绑关系朝上查，上三级3个人按（8-5-2）分15%  若无上级 找预备队 5%
                        SUser userMe = this.userService.getById(orderProduct.getUserId());

                        // 有上级的情况下
                        if (userMe.getParentId() != null) {

                            // 上一级用户
                            SUser userSuperOne = this.userService.getById(userMe.getParentId());

                            userBonusLog = new SUserBonusLog();
                            userBonusLog.setOrderDetailId(orderDetailPaySuccess.getId());
                            userBonusLog.setTaskLineId(taskLineOne.getId());
                            userBonusLog.setUserTaskLineId(null);
                            userBonusLog.setProductId(orderProduct.getProductId());
                            userBonusLog.setCreateTime(new Date());
                            userBonusLog.setUpdateTime(new Date());
                            userBonusLog.setStatus(0);
                            userBonusLog.setUserId(userSuperOne.getId());
                            userBonusLog.setBonusType(4);
                            userBonusLog.setBonusAmount(orderProduct.getTotalReward().multiply(upperVertical1Rate1));

                            this.userBonusLogService.save(userBonusLog);

                            // 奖励金额到冻结（冻结+）
                            userSuperOne.setLockAmount(userSuperOne.getLockAmount().add(userBonusLog.getBonusAmount()));
                            this.userService.updateById(userLock);

                            if (userSuperOne.getParentId() != null) {

                                // 上二级用户
                                SUser userSuperTwo = this.userService.getById(userSuperOne.getParentId());

                                userBonusLog = new SUserBonusLog();
                                userBonusLog.setOrderDetailId(orderDetailPaySuccess.getId());
                                userBonusLog.setTaskLineId(taskLineOne.getId());
                                userBonusLog.setUserTaskLineId(null);
                                userBonusLog.setProductId(orderProduct.getProductId());
                                userBonusLog.setCreateTime(new Date());
                                userBonusLog.setUpdateTime(new Date());
                                userBonusLog.setStatus(0);
                                userBonusLog.setUserId(userSuperTwo.getId());
                                userBonusLog.setBonusType(4);
                                userBonusLog.setBonusAmount(orderProduct.getTotalReward().multiply(upperVertical1Rate2));
                                this.userBonusLogService.save(userBonusLog);

                                // 奖励金额到冻结（冻结+）
                                userSuperTwo.setLockAmount(userSuperTwo.getLockAmount().add(userBonusLog.getBonusAmount()));
                                this.userService.updateById(userSuperTwo);

                                if (userSuperTwo.getParentId() != null) {

                                    // 上三级用户
                                    SUser userSuperThree = this.userService.getById(userSuperTwo.getParentId());

                                    userBonusLog = new SUserBonusLog();
                                    userBonusLog.setOrderDetailId(orderDetailPaySuccess.getId());
                                    userBonusLog.setTaskLineId(taskLineOne.getId());
                                    userBonusLog.setUserTaskLineId(null);
                                    userBonusLog.setProductId(orderProduct.getProductId());
                                    userBonusLog.setCreateTime(new Date());
                                    userBonusLog.setUpdateTime(new Date());
                                    userBonusLog.setStatus(0);
                                    userBonusLog.setUserId(userSuperThree.getId());
                                    userBonusLog.setBonusType(4);
                                    userBonusLog.setBonusAmount(orderProduct.getTotalReward().multiply(upperVertical1Rate3));
                                    this.userBonusLogService.save(userBonusLog);

                                    // 奖励金额到冻结（冻结+）
                                    userSuperThree.setLockAmount(userSuperThree.getLockAmount().add(userBonusLog.getBonusAmount()));
                                    this.userService.updateById(userSuperThree);
                                }
                            }
                        } else {

                            // 无上级的情况下 则查看u最后一次是谁的预备队（假设是A某），则给A某分配5%
                            SUserRelation userRelation = new SUserRelation();
                            userRelation.setRelationType(0);
                            userRelation.setUnionId(userMe.getUnionId());
                            SUserRelation userRelationOne = this.userRelationService.findUserRelation(userRelation);
                            if (userRelationOne != null) {

                                userBonusLog = new SUserBonusLog();
                                userBonusLog.setOrderDetailId(orderDetailPaySuccess.getId());
                                userBonusLog.setTaskLineId(taskLineOne.getId());
                                userBonusLog.setUserTaskLineId(null);
                                userBonusLog.setProductId(orderProduct.getProductId());
                                userBonusLog.setCreateTime(new Date());
                                userBonusLog.setUpdateTime(new Date());
                                userBonusLog.setStatus(0);
                                userBonusLog.setUserId(userRelationOne.getParentId());
                                userBonusLog.setBonusType(4);
                                userBonusLog.setBonusAmount(orderProduct.getTotalReward().multiply(upperVertical1Rate0));
                                this.userBonusLogService.save(userBonusLog);

                                // 奖励金额到冻结（冻结+）
                                userLock = this.userService.getById(userBonusLog.getUserId());
                                userLock.setLockAmount(userLock.getLockAmount().add(userBonusLog.getBonusAmount()));
                                this.userService.updateById(userLock);
                            }
                        }

                        // 根据任务线ID 检索 用户任务线
                        SUserTaskLine userTaskLine = new SUserTaskLine();
                        userTaskLine.setTaskLineId(taskLineOne.getId());
                        List<SUserTaskLine> userTaskLineList = this.userTaskLineService.findUserTaskLineList(userTaskLine);

                        // 同组任务线上的拆家（X人）均分
                        BigDecimal sameGroup = new BigDecimal(0);
                        if (userTaskLineList.size() != 0) {
                            sameGroup = orderProduct.getTotalReward().multiply(sameGroupRate).divide(
                                    new BigDecimal(userTaskLineList.size()), 2, BigDecimal.ROUND_HALF_UP);
                        }

                        int taskUserSuperCnt = 0;
                        // 计算同组任务线上的每个拆家对应的上级个数（横向上级）
                        for (SUserTaskLine userTaskLineOne : userTaskLineList) {
                            SUser taskUser = this.userService.getById(userTaskLineOne.getUserId());
                            if (taskUser.getParentId() != null) {
                                taskUserSuperCnt = taskUserSuperCnt + 1;
                            }
                        }

                        BigDecimal upperHorizontal = new BigDecimal(0);
                        if (taskUserSuperCnt != 0) {
                            upperHorizontal = orderProduct.getTotalReward().multiply(upperHorizontalRate).divide(
                                    new BigDecimal(taskUserSuperCnt), 2, BigDecimal.ROUND_HALF_UP);
                        }

                        for (SUserTaskLine userTaskLineOne : userTaskLineList) {

                            // 变更用户任务线表状态（3 -> 4 佣金结算完成待入账）
                            userTaskLineOne.setStatus(4);
                            userTaskLineOne.setUpdateTime(new Date());
                            this.userTaskLineService.updateById(userTaskLineOne);

                            // 任务躺赢 (同组任务线上的拆家（X人）均分40%)
                            userBonusLog = new SUserBonusLog();
                            userBonusLog.setUserId(userTaskLineOne.getUserId());
                            userBonusLog.setOrderDetailId(orderDetailPaySuccess.getId());
                            userBonusLog.setTaskLineId(taskLineOne.getId());
                            userBonusLog.setUserTaskLineId(userTaskLineOne.getId());
                            userBonusLog.setProductId(orderProduct.getProductId());
                            userBonusLog.setBonusType(2);
                            userBonusLog.setBonusAmount(sameGroup);
                            userBonusLog.setCreateTime(new Date());
                            userBonusLog.setUpdateTime(new Date());
                            userBonusLog.setStatus(0);
                            this.userBonusLogService.save(userBonusLog);

                            // 奖励金额到冻结（冻结+）
                            userLock = this.userService.getById(userBonusLog.getUserId());
                            userLock.setLockAmount(userLock.getLockAmount().add(userBonusLog.getBonusAmount()));
                            this.userService.updateById(userLock);

                            // 横向躺赢 同组任务线上的每个拆家对应的上级（Y人，Y<=X）均分25%
                            SUser taskUser = this.userService.getById(userTaskLineOne.getUserId());
                            if (taskUser.getParentId() != null) {

                                userBonusLog = new SUserBonusLog();
                                userBonusLog.setUserId(taskUser.getParentId());
                                userBonusLog.setOrderDetailId(orderDetailPaySuccess.getId());
                                userBonusLog.setTaskLineId(taskLineOne.getId());
                                userBonusLog.setUserTaskLineId(userTaskLineOne.getId());
                                userBonusLog.setProductId(orderProduct.getProductId());
                                userBonusLog.setBonusType(3);
                                userBonusLog.setBonusAmount(upperHorizontal);
                                userBonusLog.setCreateTime(new Date());
                                userBonusLog.setUpdateTime(new Date());
                                userBonusLog.setStatus(0);
                                this.userBonusLogService.save(userBonusLog);

                                // 奖励金额到冻结（冻结+）
                                userLock = this.userService.getById(userBonusLog.getUserId());
                                userLock.setLockAmount(userLock.getLockAmount().add(userBonusLog.getBonusAmount()));
                                this.userService.updateById(userLock);
                            }

                            // 平台返回任务金
                            userBonusLog = new SUserBonusLog();
                            userBonusLog.setUserId(userTaskLineOne.getUserId());
                            userBonusLog.setOrderDetailId(orderDetailPaySuccess.getId());
                            userBonusLog.setTaskLineId(taskLineOne.getId());
                            userBonusLog.setUserTaskLineId(userTaskLineOne.getId());
                            userBonusLog.setProductId(orderProduct.getProductId());
                            userBonusLog.setBonusType(5);
                            userBonusLog.setBonusAmount(userTaskLineOne.getPayAmount());
                            userBonusLog.setCreateTime(new Date());
                            userBonusLog.setUpdateTime(new Date());
                            userBonusLog.setStatus(0);
                            this.userBonusLogService.save(userBonusLog);

                            // 奖励金额到冻结（冻结+）
                            userLock = this.userService.getById(userBonusLog.getUserId());
                            userLock.setLockAmount(userLock.getLockAmount().add(userBonusLog.getBonusAmount()));
                            this.userService.updateById(userLock);
                        }
                    }
                }
            }
        }
    }

}