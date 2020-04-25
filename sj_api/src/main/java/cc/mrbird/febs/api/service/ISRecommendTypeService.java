package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SRecommendType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author MrBird
 */
public interface ISRecommendTypeService extends IService<SRecommendType> {

    /**
     * 检索推荐类别
     *
     * @return List<SRecommendType>
     */
    List<SRecommendType> findRecommendTypeList();
}
