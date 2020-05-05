package cc.mrbird.febs.api.mapper;

import cc.mrbird.febs.api.entity.SUserRelation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author MrBird
 */
public interface SUserRelationMapper extends BaseMapper<SUserRelation> {

    Integer queryUserRelationCnt(@Param("userId") String userId);

    Integer queryUserRelationTodayCnt(@Param("userId") String userId);
}
