package cc.mrbird.febs.api.mapper;

import cc.mrbird.febs.api.entity.SProduct;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
 * @author MrBird
 */
public interface SProductMapper extends BaseMapper<SProduct> {

    IPage<Map> findProductDetailByProductName(Page page, @Param("productName") String productName);

    IPage<Map> findNewProductDetail(Page page);

    IPage<Map> findActivityProductDetail(Page page, @Param("activityId") String activityId);

    IPage<Map> findProductDetailByBigTypeId(Page page, @Param("sProduct") SProduct sProduct);

    IPage<Map> findProductDetailBySmallTypeId(Page page, @Param("sProduct") SProduct sProduct);

    Map findProductDetail(@Param("productId") String productId);

    List<Map<String,Object>> getUserCountForProduct();

    List<Map<String,Object>> getOverCount();

    void updateForOverBatch(@Param("list")List<Map<String,Object>> list);

    void updateForUserCountBatch(@Param("list")List<Map<String,Object>> list);
}
