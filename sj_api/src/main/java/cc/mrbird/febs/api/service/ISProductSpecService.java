package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SProductSpec;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author MrBird
 */
public interface ISProductSpecService extends IService<SProductSpec> {

    /**
     * 根据商品ID查询商品规格列表
     *
     * @param productId String
     * @return List<SProductSpec>
     */
    List<SProductSpec> findProductSpecList(String productId);

    /**
     * 根据商品规格ID查询商品规格
     *
     * @param productSpecId String
     * @return SProductSpec
     */
    SProductSpec findProductSpec(String productSpecId);
}
