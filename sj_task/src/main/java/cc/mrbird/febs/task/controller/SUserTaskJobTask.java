package cc.mrbird.febs.task.controller;

import cc.mrbird.febs.task.service.ISUserTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author MrBird
 */
@Slf4j
@RestController
@Configuration
@EnableScheduling
public class SUserTaskJobTask {

    @Autowired
    private ISUserTaskService userTaskService;

    /**
     * 任务支付失败时 锁定去除 修改状态为 3-不支付[取消或超期] 并且修改任务线 锁定任务数
     * 2分钟执行一次 (支付失败时间超过5分钟的任务处理)
     */
    @Scheduled(cron = "0 */2 * * * ?")
    @Transactional
    public void unLockPayFailTask() {

        // 抽取s_user_task中 支付状态（锁定） 支付时间大于5分钟的 数据 修改状态为 3-不支付[取消或超期]
        this.userTaskService.updateTaskForUnLock();

        // 再根据s_user_task_line中的task_line_id到 表s_task_line 修改 冻结任务数量-1
        this.userTaskService.updateTaskLineFailBatch();

        // 同时把s_user_task_line中的相关数据也同样修改为 3-不支付[取消或超期]
        this.userTaskService.updateUserTaskLineFailBatch();
    }

}
