package cc.mrbird.febs.task.mapper;

import cc.mrbird.febs.task.entity.SUserTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @author MrBird
 */
public interface SUserTaskMapper extends BaseMapper<SUserTask> {

    void updateTaskForUnLock();

    void updateUserTaskLineFailBatch();

    void updateTaskLineFailBatch();
}
