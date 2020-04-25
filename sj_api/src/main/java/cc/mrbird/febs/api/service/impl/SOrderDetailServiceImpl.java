package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SOrderDetail;
import cc.mrbird.febs.api.mapper.SOrderDetailMapper;
import cc.mrbird.febs.api.service.ISOrderDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
