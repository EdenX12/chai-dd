package cc.mrbird.febs.api.mapper;

import cc.mrbird.febs.api.entity.SUserShare;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author MrBird
 */
public interface SUserShareMapper extends BaseMapper<SUserShare> {
    /**
     * 查询最新的一条浏览
     * @param productId
     * @param userId
     * @return
     */
    String getCurrentShareId(@Param("productId")String productId,@Param("userId")String userId);

}
