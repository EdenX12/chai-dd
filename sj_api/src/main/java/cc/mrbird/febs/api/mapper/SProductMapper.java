package cc.mrbird.febs.api.mapper;

import cc.mrbird.febs.api.entity.SProduct;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.Map;

/**
 * @author MrBird
 */
public interface SProductMapper extends BaseMapper<SProduct> {

    IPage<Map> findProductDetailByBigTypeId(Page page, @Param("sProduct") SProduct sProduct);

    IPage<Map> findProductDetailBySmallTypeId(Page page, @Param("sProduct") SProduct sProduct);

    Map findProductDetail(@Param("productId") String productId);

}
