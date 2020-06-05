package cc.mrbird.febs.api.mapper;

import cc.mrbird.febs.api.entity.SUserRelation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @author MrBird
 */
public interface SUserRelationMapper extends BaseMapper<SUserRelation> {

    Integer queryUserRelationCnt(@Param("userId") String userId);

    Integer queryUserRelationTodayCnt(@Param("userId") String userId);

    Map<String,Object> getMyTeamTotal(@Param("userId") String userId,@Param("relationType")String relationType);

    IPage<Map> getFirstLevel(Page page, @Param("userId") String userId, @Param("relationType")String relationType);

    IPage<Map> getSecondLevel(Page page, @Param("userId") String userId, @Param("relationType")String relationType);

    IPage<Map> getThirdLevel(Page page, @Param("userId") String userId, @Param("relationType")String relationType);

    IPage<Map>  getTodayNewAdd (Page page, @Param("userId") String userId);

    IPage<Map> getFirstLevelForAmt(Page page, @Param("userId") String userId, @Param("relationType")String relationType);

    IPage<Map> getSecondLevelForAmt(Page page, @Param("userId") String userId, @Param("relationType")String relationType);

    IPage<Map> getThirdLevelForAmt(Page page, @Param("userId") String userId, @Param("relationType")String relationType);

    IPage<Map>  getTodayNewAddForAmt (Page page, @Param("userId") String userId);
}
