package cc.mrbird.febs.api.mapper;

import cc.mrbird.febs.api.entity.SOfferPrice;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author MrBird
 */
public interface SOfferPriceMapper extends BaseMapper<SOfferPrice> {

    List<SOfferPrice> findOfferPriceList(@Param("sOfferPrice") SOfferPrice sOfferPrice);

}
