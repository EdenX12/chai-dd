package cc.mrbird.febs.api.mapper;

import cc.mrbird.febs.api.entity.SUserTask;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @author MrBird
 */
public interface SUserTaskMapper extends BaseMapper<SUserTask> {

    IPage<Map> findTaskDetailByStatus(Page page, @Param("userId") String userId, @Param("status") int status);

    IPage<Map> findUserTaskOutDetail(Page page, @Param("sUserTask") SUserTask sUserTask);

    IPage<Map> findUserTaskOfferDetail(Page page, @Param("sUserTask") SUserTask sUserTask);

    IPage<Map> findUserTaskFollowDetail(Page page, @Param("userId") String userId);

    Integer queryProductCount(@Param("userId") String userId);

    void updateTaskForUnLock();

    void updateUserTaskLineFailBatch();

    void updateTaskLineFailBatch();

    void updateUserTaskLineSuccessBatch(String taskId);

    void updateTaskLineSuccessBatch(String taskId);
}
