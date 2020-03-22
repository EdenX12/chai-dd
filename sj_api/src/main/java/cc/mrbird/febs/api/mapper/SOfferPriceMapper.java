package cc.mrbird.febs.api.mapper;

import cc.mrbird.febs.api.entity.SOfferPrice;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author MrBird
 */
public interface SOfferPriceMapper extends BaseMapper<SOfferPrice> {

    List<SOfferPrice> findOfferPriceList(@Param("sOfferPrice") SOfferPrice sOfferPrice);

}
