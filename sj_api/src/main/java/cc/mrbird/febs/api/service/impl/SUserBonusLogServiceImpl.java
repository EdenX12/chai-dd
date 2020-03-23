package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SUserBonusLog;
import cc.mrbird.febs.api.mapper.SUserBonusLogMapper;
import cc.mrbird.febs.api.service.ISUserBonusLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author MrBird
 */
@Service
public class SUserBonusLogServiceImpl extends ServiceImpl<SUserBonusLogMapper, SUserBonusLog> implements ISUserBonusLogService {

    @Override
    public List<SUserBonusLog> findUserBonusList(SUserBonusLog userBonusLog) {

        LambdaQueryWrapper<SUserBonusLog> queryWrapper = new LambdaQueryWrapper();

        // 购买订单ID不为空的情况下
        if (userBonusLog.getOrderId() != null) {
            queryWrapper.eq(SUserBonusLog::getOrderId, userBonusLog.getOrderId());
        }

        // 任务ID不为空的情况下
        if (userBonusLog.getTaskId() != null) {
            queryWrapper.eq(SUserBonusLog::getTaskId, userBonusLog.getTaskId());
        }

        // 用户ID不为空的情况下
        if (userBonusLog.getUserId() != null) {
            queryWrapper.eq(SUserBonusLog::getUserId, userBonusLog.getUserId());
        }

        return this.baseMapper.selectList(queryWrapper);
    }

}
