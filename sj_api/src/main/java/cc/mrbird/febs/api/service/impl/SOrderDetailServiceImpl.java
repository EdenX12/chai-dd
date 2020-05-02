package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SOrderDetail;
import cc.mrbird.febs.api.mapper.SOrderDetailMapper;
import cc.mrbird.febs.api.service.ISOrderDetailService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author MrBird
 */
@Service
public class SOrderDetailServiceImpl extends ServiceImpl<SOrderDetailMapper, SOrderDetail> implements ISOrderDetailService {

    @Override
    public SOrderDetail addOrderDetail(SOrderDetail orderDetail) {

        this.baseMapper.insert(orderDetail);

        return orderDetail;
    }

    @Override
    public void updateOrderDetail(SOrderDetail orderDetail) {

        LambdaQueryWrapper<SOrderDetail> queryWrapper = new LambdaQueryWrapper<SOrderDetail>();

        // 用户ID
        queryWrapper.eq(SOrderDetail::getUserId, orderDetail.getUserId());

        // 订单ID
        queryWrapper.eq(SOrderDetail::getOrderId, orderDetail.getOrderId());

        this.baseMapper.update(orderDetail, queryWrapper);
    }

    @Override
    public List<SOrderDetail> findOrderDetailList(SOrderDetail orderDetail) {

        LambdaQueryWrapper<SOrderDetail> queryWrapper = new LambdaQueryWrapper();

        // 批量支付订单ID
        if (orderDetail.getOrderId() != null) {
            queryWrapper.eq(SOrderDetail::getOrderId, orderDetail.getOrderId());
        }

        return this.baseMapper.selectList(queryWrapper);
    }

}
