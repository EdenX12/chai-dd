package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.STaskOrder;
import cc.mrbird.febs.api.entity.SUser;
import cc.mrbird.febs.api.entity.SUserLevel;
import cc.mrbird.febs.api.entity.SUserTask;
import cc.mrbird.febs.api.service.ISTaskOrderService;
import cc.mrbird.febs.api.service.ISUserLevelService;
import cc.mrbird.febs.api.service.ISUserService;
import cc.mrbird.febs.api.service.ISUserTaskService;
import cc.mrbird.febs.common.annotation.Limit;
import cc.mrbird.febs.common.annotation.Log;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.domain.FebsResponse;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.exception.FebsException;
import cc.mrbird.febs.common.utils.FebsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Date;
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
    private ISUserService userService;

    @Autowired
    private ISUserLevelService userLevelService;

    /**
     * 新增任务转让
     */
    @Log("新增任务转让")
    @PostMapping("/addTaskOrder")
    public void addTaskOrder(@Valid STaskOrder taskOrder) throws FebsException {

        try {

            // 用户任务检索
            SUserTask userTask = userTaskService.getById(taskOrder.getTaskId());

            // 转让份数小于任务份数的情况下，在原有的任务上更新转让数量 变为转让中  追加一条剩余转让数量的任务
            // 转让份数等于任务份数的情况下 直接修改为转让中
            if (taskOrder.getTaskNumber() < userTask.getTaskNumber()) {

                // 更新原任务的任务数量
                userTask.setTaskNumber(taskOrder.getTaskNumber());
                userTask.setPayAmount(userTask.getPayAmount().multiply( BigDecimal.valueOf(taskOrder.getTaskNumber() / userTask.getTaskNumber())));
                userTask.setStatus(1);
                userTaskService.updateUserTask(userTask);

                // 追加新的用户任务
                userTask.setTaskNumber(userTask.getTaskNumber() - taskOrder.getTaskNumber());
                userTask.setPayAmount(userTask.getPayAmount().multiply( BigDecimal.valueOf((userTask.getTaskNumber() - taskOrder.getTaskNumber()) / userTask.getTaskNumber())));
                userTask.setStatus(0);
                userTaskService.createUserTask(userTask);

            } else {

                userTask.setStatus(1);
                userTaskService.updateUserTask(userTask);
            }

            SUser user = FebsUtil.getCurrentUser();
            taskOrder.setUserId(user.getId());

            taskOrder.setCreateTime(new Date());
            this.taskOrderService.createTaskOrder(taskOrder);

            // 每参与一次任务转出 猎豆追加 10颗  * 猎人等级倍数
            SUserLevel userLevel = this.userLevelService.getById(user.getUserLevelId());
            user.setCanuseBean(user.getCanuseBean() + userLevel.getBeanRate().multiply(BigDecimal.valueOf(10)).intValue());
            this.userService.updateById(user);

        } catch (Exception e) {
            message = "新增任务转让失败";
            log.error(message, e);
            throw new FebsException(message);
        }
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

        Map<String, Object> taskOrderPageList = getDataTable(taskOrderService.findTaskOrderList(taskOrder, queryRequest));

        response.put("code", 0);
        response.data(taskOrderPageList);

        return response;
    }

}
