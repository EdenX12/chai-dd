package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SUserTaskLine;
import cc.mrbird.febs.api.mapper.SUserTaskLineMapper;
import cc.mrbird.febs.api.service.ISUserTaskLineService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    @Override
    public Integer queryCountByUserIdAndProductId(String userId, String productId) {

        LambdaQueryWrapper<SUserTaskLine> queryWrapper = new LambdaQueryWrapper();

        queryWrapper.eq(SUserTaskLine::getUserId, userId);
        queryWrapper.eq(SUserTaskLine::getProductId, productId);
        queryWrapper.eq(SUserTaskLine::getPayStatus, 1);

        List<Integer> statusList = new ArrayList<>();
        statusList.add(0);
        statusList.add(1);
        statusList.add(3);
        statusList.add(4);

        queryWrapper.in(SUserTaskLine::getStatus, statusList);

        return this.baseMapper.selectCount(queryWrapper);
    }

}
