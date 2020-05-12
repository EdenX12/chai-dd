package cc.mrbird.febs.task.service.impl;

import cc.mrbird.febs.task.entity.SUserTask;
import cc.mrbird.febs.task.mapper.SUserTaskMapper;
import cc.mrbird.febs.task.service.ISUserTaskService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author MrBird
 */
@Service
public class  SUserTaskServiceImpl extends ServiceImpl<SUserTaskMapper, SUserTask> implements ISUserTaskService {

    @Override
    public void updateTaskForUnLock() {
         this.baseMapper.updateTaskForUnLock();
    }

    @Override
    public void updateUserTaskLineFailBatch() {
         this.baseMapper.updateUserTaskLineFailBatch();
    }

    @Override
    public void updateTaskLineFailBatch() {
         this.baseMapper.updateTaskLineFailBatch();
    }

}
