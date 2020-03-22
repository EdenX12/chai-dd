package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.STaskOrder;
import cc.mrbird.febs.api.mapper.STaskOrderMapper;
import cc.mrbird.febs.api.service.ISTaskOrderService;
import cc.mrbird.febs.common.domain.FebsConstant;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.utils.SortUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author MrBird
 */
@Service
public class STaskOrderServiceImpl extends ServiceImpl<STaskOrderMapper, STaskOrder> implements ISTaskOrderService {

    @Override
    public STaskOrder createTaskOrder(STaskOrder taskOrder) {

        this.baseMapper.insert(taskOrder);

        return taskOrder;
    }

    @Override
    public STaskOrder findTaskOrder(STaskOrder taskOrder) {

        LambdaQueryWrapper<STaskOrder> queryWrapper = new LambdaQueryWrapper();

        // 用户ID不为空的情况下
        if (taskOrder.getUserId() != null) {
            queryWrapper.eq(STaskOrder::getUserId, taskOrder.getUserId());
        }

        // 转让ID不为空的情况下
        if (taskOrder.getId() != null) {
            queryWrapper.eq(STaskOrder::getId, taskOrder.getId());
        }

        // 转让任务ID不为空的情况下
        if (taskOrder.getTaskId() != null) {
            queryWrapper.eq(STaskOrder::getTaskId, taskOrder.getTaskId());
        }

        // 状态 0：转让中 1：已成交 2：未成交流标
        if (taskOrder.getStatus() != null) {
            queryWrapper.eq(STaskOrder::getStatus, taskOrder.getStatus());
        }

        return this.baseMapper.selectOne(queryWrapper);
    }


    @Override
    public List<STaskOrder> findTaskOrderList(STaskOrder taskOrder) {

        LambdaQueryWrapper<STaskOrder> queryWrapper = new LambdaQueryWrapper();

        // 转让任务ID不为空的情况下
        if (taskOrder.getTaskId() != null) {
            queryWrapper.eq(STaskOrder::getTaskId, taskOrder.getTaskId());
        }

        // 状态 0：转让中 1：已成交 2：未成交流标
        if (taskOrder.getStatus() != null) {
            queryWrapper.eq(STaskOrder::getStatus, taskOrder.getStatus());
        }

        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public IPage<Map> findTaskOrderList(STaskOrder taskOrder, QueryRequest request) {
        try {
            Page<Map> page = new Page<>();
            SortUtil.handlePageSort(request, page, "endTime", FebsConstant.ORDER_ASC, false);
            return this.baseMapper.findTaskOrderDetail(page, taskOrder);
        } catch (Exception e) {
            log.error("查询全部转让任务异常", e);
            return null;
        }
    }

    @Override
    public Map findTaskOrderDetail(STaskOrder taskOrder) {
        try {
            return this.baseMapper.findTaskOrderDetail(taskOrder);
        } catch (Exception e) {
            log.error("查询转让任务详情异常", e);
            return null;
        }
    }

}
