package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SProduct;
import cc.mrbird.febs.api.entity.SUser;
import cc.mrbird.febs.common.domain.QueryRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @author MrBird
 */
public interface ISProductService extends IService<SProduct> {

    /**
     * 模糊检索商品列表
     *
     * @param productName String
     * @param request queryRequest
     * @return IPage
     */
    IPage<Map> findProductListByProductName(String productName, QueryRequest request);

    /**
     * 根据商品大分类查询商品列表
     *
     * @param product SProduct
     * @param queryRequest queryRequest
     * @return IPage
     */
    IPage<Map> findProductListByBigTypeId(SProduct product, QueryRequest queryRequest);

    /**
     * 根据商品小分类查询商品列表
     *
     * @param product SProduct
     * @param queryRequest queryRequest
     * @return IPage
     */
    IPage<Map> findProductListBySmallTypeId(SProduct product, QueryRequest queryRequest);

    /**
     * 查询商品详情，包括关注数量 已领任务数量
     *
     * @param productId String
     * @return SProduct
     */
    Map findProductDetail(String  productId, SUser user);
}
