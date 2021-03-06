package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SOfferPrice;
import cc.mrbird.febs.api.mapper.SOfferPriceMapper;
import cc.mrbird.febs.api.service.ISOfferPriceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author MrBird
 */
@Service
public class SOfferPriceServiceImpl extends ServiceImpl<SOfferPriceMapper, SOfferPrice> implements ISOfferPriceService {

    @Override
    public SOfferPrice createOfferPrice(SOfferPrice offerPrice) {

        this.baseMapper.insert(offerPrice);

        return offerPrice;
    }

    @Override
    public SOfferPrice updateOfferPriceOn(SOfferPrice offerPrice) {

        LambdaQueryWrapper<SOfferPrice> queryWrapper = new LambdaQueryWrapper<SOfferPrice>();

        // 竞标中 修改为 已成交
        queryWrapper.eq(SOfferPrice::getStatus, 1);

        queryWrapper.eq(SOfferPrice::getPayStatus, 1);

        queryWrapper.eq(SOfferPrice::getTaskOrderId, offerPrice.getTaskOrderId());

        offerPrice = this.baseMapper.selectOne(queryWrapper);

        offerPrice.setStatus(3);

        this.baseMapper.update(offerPrice, queryWrapper);
        return offerPrice;
    }

    @Override
    public SOfferPrice updateOfferPriceOut(SOfferPrice offerPrice) {

        LambdaQueryWrapper<SOfferPrice> queryWrapper = new LambdaQueryWrapper<SOfferPrice>();

        // 修改为 已出局
        queryWrapper.eq(SOfferPrice::getPayStatus, 1);

        queryWrapper.eq(SOfferPrice::getTaskOrderId, offerPrice.getTaskOrderId());

        offerPrice = this.baseMapper.selectOne(queryWrapper);

        if (offerPrice != null) {

            offerPrice.setStatus(2);
            this.baseMapper.update(offerPrice, queryWrapper);
        }

        return offerPrice;
    }

    @Override
    public List<SOfferPrice> findOfferPriceOutList(SOfferPrice offerPrice) {

        LambdaQueryWrapper<SOfferPrice> queryWrapper = new LambdaQueryWrapper();

        // 转让任务ID不为空的情况下
        if (offerPrice.getTaskOrderId() != null) {
            queryWrapper.eq(SOfferPrice::getTaskOrderId, offerPrice.getTaskOrderId());
        }

        // 已支付
        queryWrapper.eq(SOfferPrice::getPayStatus, 1);

        // 已出局
        queryWrapper.eq(SOfferPrice::getStatus, 2);

        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<SOfferPrice> findOfferPriceList(SOfferPrice offerPrice) {

        return baseMapper.findOfferPriceList(offerPrice);
    }

    @Override
    public SOfferPrice findOfferPriceDetail(SOfferPrice offerPrice) {

        LambdaQueryWrapper<SOfferPrice> queryWrapper = new LambdaQueryWrapper();

        // 用户ID
        if (offerPrice.getUserId() != null) {
            queryWrapper.eq(SOfferPrice::getUserId, offerPrice.getUserId());
        }

        // 转让任务ID不为空的情况下
        if (offerPrice.getTaskOrderId() != null) {
            queryWrapper.eq(SOfferPrice::getTaskOrderId, offerPrice.getTaskOrderId());
        }

        // 已支付
        queryWrapper.eq(SOfferPrice::getPayStatus, 1);

        // 按照报价金额降序（求最高报价）
        queryWrapper.orderByDesc(SOfferPrice::getAmount);

        return this.baseMapper.selectOne(queryWrapper);
    }

}
