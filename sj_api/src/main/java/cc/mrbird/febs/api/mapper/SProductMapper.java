package cc.mrbird.febs.api.mapper;

import cc.mrbird.febs.api.entity.SProduct;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @author MrBird
 */
public interface SProductMapper extends BaseMapper<SProduct> {

    IPage<SProduct> findProductDetail(Page page, @Param("sProduct") SProduct sProduct);

    SProduct findProductDetail(@Param("sProduct") SProduct sProduct);

}
