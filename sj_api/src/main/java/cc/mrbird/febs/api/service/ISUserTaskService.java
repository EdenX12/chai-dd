package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SUserTask;
import cc.mrbird.febs.common.domain.QueryRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
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
    Long createUserTask(SUserTask userTask);

    /**
     * 更新用户任务
     *
     * @param userTask SUserTask
     */
    Long updateUserTask(SUserTask userTask);

    /**
     * 检索用户任务
     *
     * @param userTask SUserTask
     */
    List<SUserTask> findUserTaskList(SUserTask userTask);

    /**
     * 查询我的任务【进行中】列表
     *
     * @param userTask SUserTask
     * @param queryRequest queryRequest
     * @return IPage
     */
    IPage<Map> findUserTaskList(SUserTask userTask, QueryRequest queryRequest);

    /**
     * 查询我的任务【转让中】列表
     *
     * @param userTask SUserTask
     * @param queryRequest queryRequest
     * @return IPage
     */
    IPage<Map> findUserTaskOutList(SUserTask userTask, QueryRequest queryRequest);

    /**
     * 查询我的任务【收购中】列表
     *
     * @param userTask SUserTask
     * @param queryRequest queryRequest
     * @return IPage
     */
    IPage<Map> findUserTaskOfferList(SUserTask userTask, QueryRequest queryRequest);

    /**
     * 查询我的任务【已完成】列表
     *
     * @param userTask SUserTask
     * @param queryRequest queryRequest
     * @return IPage
     */
    IPage<Map> findUserTaskEndList(SUserTask userTask, QueryRequest queryRequest);

    /**
     * 查询我的预备队
     *
     * @param userId Long
     * @return List
     */
    List<Long> findUserIdsByParent(Long userId);
}
