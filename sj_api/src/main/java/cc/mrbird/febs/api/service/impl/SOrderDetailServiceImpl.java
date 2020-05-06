package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SOrderDetail;
import cc.mrbird.febs.api.entity.SOrderProduct;
import cc.mrbird.febs.api.mapper.SOrderDetailMapper;
import cc.mrbird.febs.api.service.ISOrderDetailService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
    public List<SOrderDetail> findOrderDetailList(SOrderDetail orderDetail) {

        LambdaQueryWrapper<SOrderDetail> queryWrapper = new LambdaQueryWrapper();

        // 批量支付订单ID
        if (orderDetail.getOrderId() != null) {
            queryWrapper.eq(SOrderDetail::getOrderId, orderDetail.getOrderId());
        }
        if (orderDetail.getUserId() != null) {
            queryWrapper.eq(SOrderDetail::getUserId, orderDetail.getUserId());
        }
        if (orderDetail.getOrderStatus() != null) {
            queryWrapper.eq(SOrderDetail::getOrderStatus, orderDetail.getOrderStatus());
        }
        if (orderDetail.getPaymentState() != null) {
            queryWrapper.eq(SOrderDetail::getUserId, orderDetail.getPaymentState());
        }
        return this.baseMapper.selectList(queryWrapper);
    }

}
