package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SOfferPrice;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author MrBird
 */
public interface ISOfferPriceService extends IService<SOfferPrice> {

    /**
     * 新增任务报价
     *
     * @param offerPrice SOfferPrice
     */
    SOfferPrice createOfferPrice(SOfferPrice offerPrice);

    /**
     * 更新任务报价出局
     *
     * @param offerPrice SOfferPrice
     */
    SOfferPrice updateOfferPriceOut(SOfferPrice offerPrice);

    /**
     * 更新任务报价成交
     *
     * @param offerPrice SOfferPrice
     */
    SOfferPrice updateOfferPriceOn(SOfferPrice offerPrice);

    /**
     * 查询出局者的报价信息
     * @param offerPrice SOfferPrice
     * @return List<SOfferPrice>
     */
    List<SOfferPrice> findOfferPriceOutList(SOfferPrice offerPrice);

    /**
     * 查询转让任务的报价信息
     * @param offerPrice SOfferPrice
     * @return List<SOfferPrice>
     */
    List<SOfferPrice> findOfferPriceList(SOfferPrice offerPrice);

    /**
     * 查询用户对转让任务的最高报价信息
     * @param offerPrice SOfferPrice
     * @return SOfferPrice
     */
    SOfferPrice findOfferPriceDetail(SOfferPrice offerPrice);

}
