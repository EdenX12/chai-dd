package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SProduct;
import cc.mrbird.febs.api.mapper.SProductMapper;
import cc.mrbird.febs.api.service.ISProductService;
import cc.mrbird.febs.common.domain.FebsConstant;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.utils.SortUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author MrBird
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class SProductServiceImpl extends ServiceImpl<SProductMapper, SProduct> implements ISProductService {

    @Override
    public IPage<SProduct> findProductList(SProduct product, QueryRequest request) {
        try {
            Page<SProduct> page = new Page<>();
            SortUtil.handlePageSort(request, page, "updateTime", FebsConstant.ORDER_DESC, false);

            return this.baseMapper.findProductDetail(page, product);
        } catch (Exception e) {
            log.error("查询全部商品异常", e);
            return null;
        }
    }

    @Override
    public SProduct findProductDetail(SProduct product) {
        try {

            return this.baseMapper.findProductDetail(product);

        } catch (Exception e) {
            log.error("查询商品详情异常", e);
            return null;
        }
    }

}
