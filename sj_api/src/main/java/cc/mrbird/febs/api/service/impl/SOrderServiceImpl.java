package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SOrder;
import cc.mrbird.febs.api.entity.SUser;
import cc.mrbird.febs.api.mapper.SOrderMapper;
import cc.mrbird.febs.api.service.ISOrderService;
import cc.mrbird.febs.common.domain.FebsConstant;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.utils.FebsUtil;
import cc.mrbird.febs.common.utils.SortUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author MrBird
 */
@Service
public class SOrderServiceImpl extends ServiceImpl<SOrderMapper, SOrder> implements ISOrderService {

    @Override
    public SOrder addOrder(SOrder order) {

        this.baseMapper.insert(order);

        return order;
    }

    @Override
    public IPage<SOrder> findOrderList(SOrder order, QueryRequest request) {
        try {
            Page<SOrder> page = new Page<>();
            SortUtil.handlePageSort(request, page, "createTime", FebsConstant.ORDER_DESC, false);
            return this.baseMapper.findOrderDetail(page, order);
        } catch (Exception e) {
            log.error("查询用户全部购买订单异常", e);
            return null;
        }
    }

    @Override
    public SOrder findOrderDetail(SOrder order) {
        try {
            return this.baseMapper.findOrderDetail(order);
        } catch (Exception e) {
            log.error("查询用户购买订单详情异常", e);
            return null;
        }
    }

    @Override
    public SOrder updateOrder(SOrder order) {

        LambdaQueryWrapper<SOrder> queryWrapper = new LambdaQueryWrapper<SOrder>();

        // 用户ID
        queryWrapper.eq(SOrder::getUserId, order.getUserId());

        // 订单ID
        queryWrapper.eq(SOrder::getId, order.getId());

        this.baseMapper.update(order, queryWrapper);
        return order;
    }

}
