package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SOfferPrice;
import cc.mrbird.febs.api.entity.SUserTask;
import cc.mrbird.febs.common.domain.QueryRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @author MrBird
 */
public interface ISOfferPriceService extends IService<SOfferPrice> {

    /**
     * 新增任务报价
     *
     * @param offerPrice SOfferPrice
     */
    Long createOfferPrice(SOfferPrice offerPrice);

    /**
     * 更新任务报价出局
     *
     * @param offerPrice SOfferPrice
     */
    void updateOfferPriceOut(SOfferPrice offerPrice);

    /**
     * 更新任务报价成交
     *
     * @param offerPrice SOfferPrice
     */
    void updateOfferPriceOn(SOfferPrice offerPrice);

    /**
     * 查询转让任务的报价信息
     * @param offerPrice SOfferPrice
     * @return List<SOfferPrice>
     */
    List<SOfferPrice> findOfferPriceList(SOfferPrice offerPrice);

    /**
     * 查询用户对转让任务的最后一次报价信息
     * @param offerPrice SOfferPrice
     * @return SOfferPrice
     */
    SOfferPrice findOfferPriceDetail(SOfferPrice offerPrice);

    /**
     * 查询我的报价列表
     *
     * @param offerPrice SOfferPrice
     * @param queryRequest queryRequest
     * @return IPage
     */
    IPage<Map> findOfferPriceList(SOfferPrice offerPrice, QueryRequest queryRequest);
}
