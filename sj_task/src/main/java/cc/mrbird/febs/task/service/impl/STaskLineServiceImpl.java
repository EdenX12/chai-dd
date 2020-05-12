package cc.mrbird.febs.task.service.impl;

import cc.mrbird.febs.task.entity.STaskLine;
import cc.mrbird.febs.task.mapper.STaskLineMapper;
import cc.mrbird.febs.task.service.ISTaskLineService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author MrBird
 */
@Service
public class STaskLineServiceImpl extends ServiceImpl<STaskLineMapper, STaskLine> implements ISTaskLineService {

    @Override
    public List<STaskLine> findTaskLineList(STaskLine taskLine) {

        LambdaQueryWrapper<STaskLine> queryWrapper = new LambdaQueryWrapper();

        if (taskLine.getId() != null) {
            queryWrapper.eq(STaskLine::getId, taskLine.getId());
        }

        if (taskLine.getProductId() != null) {
            queryWrapper.eq(STaskLine::getProductId, taskLine.getProductId());
        }

        if (taskLine.getOrderProductId() != null) {
            queryWrapper.eq(STaskLine::getOrderProductId, taskLine.getOrderProductId());
        }

        if (taskLine.getLineStatus() != null) {
            queryWrapper.eq(STaskLine::getLineStatus, taskLine.getLineStatus());
        }

        if (taskLine.getSettleStatus() != null) {
            queryWrapper.eq(STaskLine::getSettleStatus, taskLine.getSettleStatus());
        }

        return this.baseMapper.selectList(queryWrapper);
    }

}
