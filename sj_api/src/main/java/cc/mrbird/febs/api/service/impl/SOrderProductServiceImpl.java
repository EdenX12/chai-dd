package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SOrderProduct;
import cc.mrbird.febs.api.mapper.SOrderProductMapper;
import cc.mrbird.febs.api.service.ISOrderProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author MrBird
 */
@Service
public class SOrderProductServiceImpl extends ServiceImpl<SOrderProductMapper, SOrderProduct> implements ISOrderProductService {

    @Override
    public SOrderProduct addOrderProduct(SOrderProduct orderProduct) {

        this.baseMapper.insert(orderProduct);

        return orderProduct;
    }

    @Override
    public List<SOrderProduct> findOrderProductList(String orderDetailId) {

        LambdaQueryWrapper<SOrderProduct> queryWrapper = new LambdaQueryWrapper();

        queryWrapper.eq(SOrderProduct::getOrderDetailId, orderDetailId);

        return this.baseMapper.selectList(queryWrapper);
    }

}
