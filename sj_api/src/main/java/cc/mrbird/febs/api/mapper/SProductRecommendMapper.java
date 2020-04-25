package cc.mrbird.febs.api.mapper;

import cc.mrbird.febs.api.entity.SProductRecommend;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author MrBird
 */
public interface SProductRecommendMapper extends BaseMapper<SProductRecommend> {

    List<Map> findProductRecommendList(@Param("sProductRecommend") SProductRecommend sProductRecommend);
}
