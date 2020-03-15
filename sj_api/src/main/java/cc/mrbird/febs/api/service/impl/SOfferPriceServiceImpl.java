package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SOfferPrice;
import cc.mrbird.febs.api.mapper.SOfferPriceMapper;
import cc.mrbird.febs.api.service.ISOfferPriceService;
import cc.mrbird.febs.common.domain.FebsConstant;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.utils.SortUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author MrBird
 */
@Service
public class SOfferPriceServiceImpl extends ServiceImpl<SOfferPriceMapper, SOfferPrice> implements ISOfferPriceService {

    @Override
    @Transactional
    public Long createOfferPrice(SOfferPrice offerPrice) {

        this.baseMapper.insert(offerPrice);

        return offerPrice.getId();
    }

    @Override
    @Transactional
    public void updateOfferPriceOn(SOfferPrice offerPrice) {

        LambdaQueryWrapper<SOfferPrice> queryWrapper = new LambdaQueryWrapper<SOfferPrice>();

        // 竞标中 修改为 已成交
        queryWrapper.eq(SOfferPrice::getStatus, 1);

        queryWrapper.eq(SOfferPrice::getPayStatus, 1);

        queryWrapper.eq(SOfferPrice::getTaskOrderId, offerPrice.getTaskOrderId());

        offerPrice.setStatus(3);

        this.baseMapper.update(offerPrice, queryWrapper);
    }

    @Override
    @Transactional
    public void updateOfferPriceOut(SOfferPrice offerPrice) {

        LambdaQueryWrapper<SOfferPrice> queryWrapper = new LambdaQueryWrapper<SOfferPrice>();

        // 修改为 已出局
        queryWrapper.eq(SOfferPrice::getPayStatus, 1);

        queryWrapper.eq(SOfferPrice::getTaskOrderId, offerPrice.getTaskOrderId());

        offerPrice.setStatus(2);

        this.baseMapper.update(offerPrice, queryWrapper);
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

        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public IPage<Map> findOfferPriceList(SOfferPrice offerPrice, QueryRequest request) {
        try {
            Page<Map> page = new Page<>();
            SortUtil.handlePageSort(request, page, "createTime", FebsConstant.ORDER_ASC, false);
            return this.baseMapper.findOfferPriceDetail(page, offerPrice);
        } catch (Exception e) {
            log.error("查询我的报价异常", e);
            return null;
        }
    }
}
