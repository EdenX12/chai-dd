package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SUserTaskLine;
import cc.mrbird.febs.api.mapper.SUserTaskLineMapper;
import cc.mrbird.febs.api.service.ISUserTaskLineService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author MrBird
 */
@Service
public class SUserTaskLineServiceImpl extends ServiceImpl<SUserTaskLineMapper, SUserTaskLine> implements ISUserTaskLineService {

    @Override
    public String queryIdByTask(String taskId) {
        return this.baseMapper.queryIdByTask(taskId);
    }
}
