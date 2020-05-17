package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SUserBonusLog;
import cc.mrbird.febs.api.mapper.SUserBonusLogMapper;
import cc.mrbird.febs.api.service.ISUserBonusLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author MrBird
 */
@Service
public class SUserBonusLogServiceImpl extends ServiceImpl<SUserBonusLogMapper, SUserBonusLog> implements ISUserBonusLogService {

    @Override
    public List<SUserBonusLog> findUserBonusList(SUserBonusLog userBonusLog) {

        LambdaQueryWrapper<SUserBonusLog> queryWrapper = new LambdaQueryWrapper();

        // 用户ID不为空的情况下
        if (userBonusLog.getUserId() != null) {
            queryWrapper.eq(SUserBonusLog::getUserId, userBonusLog.getUserId());
        }

        // 商品ID不为空的情况下
        if (userBonusLog.getProductId() != null) {
            queryWrapper.eq(SUserBonusLog::getProductId, userBonusLog.getProductId());
        }

        // 购买订单ID不为空的情况下
        if (userBonusLog.getOrderDetailId() != null) {
            queryWrapper.eq(SUserBonusLog::getOrderDetailId, userBonusLog.getOrderDetailId());
        }

        // 任务线ID不为空的情况下
        if (userBonusLog.getTaskLineId() != null) {
            queryWrapper.eq(SUserBonusLog::getTaskLineId, userBonusLog.getTaskLineId());
        }

        // 用户任务线ID不为空的情况下
        if (userBonusLog.getUserTaskLineId() != null) {
            queryWrapper.eq(SUserBonusLog::getUserTaskLineId, userBonusLog.getUserTaskLineId());
        }

        // 类型不为空的情况下
        if (userBonusLog.getBonusType() != null) {
            queryWrapper.eq(SUserBonusLog::getBonusType, userBonusLog.getBonusType());
        }

        // 状态不为空的情况下
        if (userBonusLog.getStatus() != null) {
            queryWrapper.eq(SUserBonusLog::getStatus, userBonusLog.getStatus());
        }

        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public BigDecimal findUserBonusOrgRewardTodaySum(String userId) {

        return this.baseMapper.findUserBonusOrgRewardTodaySum(userId);
    }

    @Override
    public BigDecimal findUserBonusOrgRewardSum0(String userId, String productId) {

        return this.baseMapper.findUserBonusOrgRewardSum0(userId, productId);
    }

    @Override
    public BigDecimal findUserBonusTaskRewardSum0(String userId, String productId) {

        return this.baseMapper.findUserBonusTaskRewardSum0(userId, productId);
    }

    @Override
    public BigDecimal findUserBonusBuyerRewardSum0(String userId, String productId) {

        return this.baseMapper.findUserBonusBuyerRewardSum0(userId, productId);
    }

    @Override
    public BigDecimal findUserBonusOrgRewardSum1(String userId, String productId) {

        return this.baseMapper.findUserBonusOrgRewardSum1(userId, productId);
    }

    @Override
    public BigDecimal findUserBonusTaskRewardSum1(String userId, String productId) {

        return this.baseMapper.findUserBonusTaskRewardSum1(userId, productId);
    }

    @Override
    public BigDecimal findUserBonusBuyerRewardSum1(String userId, String productId) {

        return this.baseMapper.findUserBonusBuyerRewardSum1(userId, productId);
    }
}
