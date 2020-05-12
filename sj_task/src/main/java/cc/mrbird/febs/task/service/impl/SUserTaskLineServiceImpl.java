package cc.mrbird.febs.task.service.impl;

import cc.mrbird.febs.task.entity.SUserTaskLine;
import cc.mrbird.febs.task.mapper.SUserTaskLineMapper;
import cc.mrbird.febs.task.service.ISUserTaskLineService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author MrBird
 */
@Service
public class SUserTaskLineServiceImpl extends ServiceImpl<SUserTaskLineMapper, SUserTaskLine> implements ISUserTaskLineService {

    @Override
    public List<SUserTaskLine> findUserTaskLineList(SUserTaskLine userTaskLine) {

        LambdaQueryWrapper<SUserTaskLine> queryWrapper = new LambdaQueryWrapper();

        if (userTaskLine.getId() != null) {
            queryWrapper.eq(SUserTaskLine::getId, userTaskLine.getId());
        }

        if (userTaskLine.getUserId() != null) {
            queryWrapper.eq(SUserTaskLine::getUserId, userTaskLine.getUserId());
        }

        if (userTaskLine.getTaskId() != null) {
            queryWrapper.eq(SUserTaskLine::getTaskId, userTaskLine.getTaskId());
        }

        if (userTaskLine.getProductId() != null) {
            queryWrapper.eq(SUserTaskLine::getProductId, userTaskLine.getProductId());
        }

        if (userTaskLine.getTaskLineId() != null) {
            queryWrapper.eq(SUserTaskLine::getTaskLineId, userTaskLine.getTaskLineId());
        }

        return this.baseMapper.selectList(queryWrapper);
    }

}
