package cc.mrbird.febs.task.service;

import cc.mrbird.febs.task.entity.SUserTask;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author MrBird
 */
public interface ISUserTaskService extends IService<SUserTask> {

    /**
     * 支付失败更新任务线的已锁定的任务状态
     */
    void updateTaskForUnLock();

    /**
     * 定时更新 锁定 批量更新支付状态为未支付
     */
    void updateUserTaskLineFailBatch();

    /**
     * 定时更新 锁定 批量更新任务线锁定数量
     */
    void updateTaskLineFailBatch();

}
