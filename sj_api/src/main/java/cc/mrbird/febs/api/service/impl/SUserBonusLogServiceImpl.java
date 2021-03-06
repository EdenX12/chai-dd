package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SUserBonusLog;
import cc.mrbird.febs.api.mapper.SUserBonusLogMapper;
import cc.mrbird.febs.api.service.ISUserBonusLogService;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.utils.SortUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
    public List<SUserBonusLog> findUserBonus(String userId, String productId,Integer isToday) {
        return this.baseMapper.findUserBonus(userId,productId,isToday);
    }

    @Override
    public BigDecimal getSettlementAmt(String userId) {
        return this.baseMapper.getSettlementAmt(userId);
    }

    @Override
    public IPage<Map> getBonusDetails(QueryRequest request, String userId) {
        try {

            Page<Map> page = new Page<>();
            SortUtil.handlePageSort(request, page, null,null, false);
            IPage<Map> result = this.baseMapper.getBonusDetails(page, userId);

            return result;
        } catch (Exception e) {
            log.error("查询我的余额明细异常", e);
            return null;
        }
    }

}
