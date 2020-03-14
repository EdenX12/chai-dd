package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SProduct;
import cc.mrbird.febs.common.domain.QueryRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author MrBird
 */
public interface ISProductService extends IService<SProduct> {

    /**
     * 查询商品详情列表，包括关注数量 已领任务数量
     *
     * @param product SProduct
     * @param queryRequest queryRequest
     * @return IPage
     */
    IPage<SProduct> findProductList(SProduct product, QueryRequest queryRequest);

    /**
     * 查询商品详情，包括关注数量 已领任务数量
     *
     * @param product SProduct
     * @return SProduct
     */
    SProduct findProductDetail(SProduct product);
}
