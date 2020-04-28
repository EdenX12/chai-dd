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

import java.util.Map;

/**
 * @author MrBird
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class SProductServiceImpl extends ServiceImpl<SProductMapper, SProduct> implements ISProductService {

    @Override
    public IPage<Map> findProductListByBigTypeId(SProduct product, QueryRequest request) {

        try {

            Page<SProduct> page = new Page<>();

            SortUtil.handlePageSort(request, page, "sOrder", FebsConstant.ORDER_ASC, false);

            return this.baseMapper.findProductDetailByBigTypeId(page, product);

        } catch (Exception e) {
            log.error("查询全部商品异常", e);
            return null;
        }
    }

    @Override
    public IPage<Map> findProductListBySmallTypeId(SProduct product, QueryRequest request) {

        try {

            Page<SProduct> page = new Page<>();

            SortUtil.handlePageSort(request, page, "sOrder", FebsConstant.ORDER_ASC, false);

            return this.baseMapper.findProductDetailBySmallTypeId(page, product);

        } catch (Exception e) {
            log.error("查询全部商品异常", e);
            return null;
        }
    }

    @Override
    public Map findProductDetail(SProduct product) {

        try {

            return this.baseMapper.findProductDetail(product);

        } catch (Exception e) {
            log.error("查询商品详情异常", e);
            return null;
        }
    }

    @Override
    public Map findProductDetail(String productId) {
        try {
            SProduct product = new  SProduct();
            product.setId(productId);
            return this.baseMapper.findProductDetail(product);

        } catch (Exception e) {
            log.error("查询商品详情异常", e);
            return null;
        }
    }

}
