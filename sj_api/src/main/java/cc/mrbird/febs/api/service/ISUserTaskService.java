package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SUserTask;
import cc.mrbird.febs.common.domain.QueryRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

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
     @return 商品数量
     */
    Integer findProductCount(String userId,String productId);

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

    /**
     * 当前用户的并行商品数量
     * @param userId
     * @return
     */
    Integer queryProductCount(String userId);


    /**
     * 查询已经领取的和锁定的总任务
     * @param productId
     * @param taskLineId
     * @return
     */

    Integer queryReCount (@Param("productId") String productId,String taskLineId);

    /**
     * 支付失败更新任务线的已锁定的任务状态
     */
    void updateTaskForUnLock();

    /**
     * 支付失败，查询已锁定的用户任务线关联表ID
     * @return
     */
    List<String> getUnLockPayUserTaskLines();

    /**
     * 支付失败，查询锁定的任务线的ID
     * @return
     */
    List<String> getUnLockPayTaskLines();

    /**
     * 根据id集合批量更新支付状态为未支付
     * @param list
     */
    void updateUserTaskLineBatch(List<String> list);

    /**
     * 根据id集合批量更新任务线锁定数量
     * @param list
     */
    void updateTaskLineBatch(List<String> list);
}
