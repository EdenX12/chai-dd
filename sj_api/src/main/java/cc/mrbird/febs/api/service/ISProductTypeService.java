package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SProductType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author MrBird
 */
public interface ISProductTypeService extends IService<SProductType> {

    /**
     * 检索商品推荐分类
     *
     * @return List<SProductType>
     */
    List<SProductType> findRecommendProductTypeList();

    /**
     * 检索商品分类
     *
     * @return List<SProductType>
     */
    List<SProductType> findProductTypeList(SProductType productType);
}
