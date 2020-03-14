package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SUserTask;
import cc.mrbird.febs.common.domain.QueryRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @author MrBird
 */
public interface ISUserTaskService extends IService<SUserTask> {

    /**
     * 新增用户任务
     *
     * @param userTask SUserTask
     */
    int createUserTask(SUserTask userTask);

    /**
     * 更新用户任务
     *
     * @param userTask SUserTask
     */
    int updateUserTask(SUserTask userTask);

    /**
     * 检索用户任务
     *
     * @param userTask SUserTask
     */
    SUserTask findUserTask(SUserTask userTask);

    /**
     * 查询我的任务列表
     *
     * @param userTask SUserTask
     * @param queryRequest queryRequest
     * @return IPage
     */
    IPage<Map> findUserTaskList(SUserTask userTask, QueryRequest queryRequest);

}
