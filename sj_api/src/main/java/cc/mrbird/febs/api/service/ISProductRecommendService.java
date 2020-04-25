package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SProductRecommend;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @author MrBird
 */
public interface ISProductRecommendService extends IService<SProductRecommend> {

    /**
     * 查询推荐关联的商品信息
     *
     * @param recommendTypeId String
     * @return IPage
     */
    List<Map> findProductRecommendList(String recommendTypeId);
}
