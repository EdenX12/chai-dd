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
    SUserTask createUserTask(SUserTask userTask);

    /**
     * 更新用户任务
     *
     * @param userTask SUserTask
     */
    SUserTask updateUserTask(SUserTask userTask);

    /**
     * 检索用户任务
     *
     * @param userTask SUserTask
     */
    List<SUserTask> findUserTaskList(SUserTask userTask);

    /**
     * 查询我的任务列表
     *
     * @param queryRequest queryRequest
     * @return IPage
     */
    IPage<Map> findTaskDetailByStatus(QueryRequest queryRequest, String userId, int status);

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
     * 查询我的任务【关注中】列表
     *
     * @param userId userId
     * @param queryRequest queryRequest
     * @return IPage
     */
    IPage<Map> findUserTaskFollowList(QueryRequest queryRequest, String userId);

    /**
     * 当前用户的并行商品数量
     * @param userId
     * @return
     */
    Integer queryProductCount(String userId);

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

    /**
     * 支付成功后更新为 已支付
     */
    void updateUserTaskLineSuccessBatch(String taskId);

    /**
     * 支付成功后更新为 锁定数量->已接任务数量
     */
    void updateTaskLineSuccessBatch(String taskLineId);
}
