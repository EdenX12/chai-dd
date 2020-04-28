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
     * 检索进行中+结算中 并行商品数量
     *
     * @param userTask SUserTask
     @return 商品数量
     */
    Integer findProductCount(SUserTask userTask);

    /**
     * 查询我的任务【进行中】列表
     *
     * @param queryRequest queryRequest
     * @return IPage
     */
    IPage<Map> findUserTaskingDetail(QueryRequest queryRequest,String userId);

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
    IPage<Map> findUserTaskFollowList(QueryRequest queryRequest,String userId);

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
    List<String> findUserIdsByParent(String userId);
}
