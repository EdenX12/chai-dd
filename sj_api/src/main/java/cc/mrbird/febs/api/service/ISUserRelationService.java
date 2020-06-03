package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SUserRelation;
import cc.mrbird.febs.common.domain.QueryRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.Map;

/**
 * @author MrBird
 */
public interface ISUserRelationService extends IService<SUserRelation> {

    SUserRelation createUserRelation(SUserRelation userRelation);

    SUserRelation findUserRelation(SUserRelation userRelation);

    Integer findUserRelationCnt(String userId);

    Integer findUserRelationTodayCnt(String userId);

    /**
     *  查询一级禁卫军/预备队
     */
    IPage<Map> getFirstLevel(QueryRequest queryRequest,String userId, String relationType);
    /**
     *  查询二级禁卫军/预备队
     */
    IPage<Map> getSecondLevel(QueryRequest queryRequest,String userId, String relationType);
    /**
     *  查询三级禁卫军/预备队
     */
    IPage<Map> getThirdLevel(QueryRequest queryRequest,String userId, String relationType);
    /**
     *  查询禁卫军/预备队总数
     */
    Map<String,Object> getMyTeamTotal(String userId,String relationType);

    /**
     * 今日新增
     * @param queryRequest
     * @param userId
     * @return
     */
    IPage<Map> getTodayNewAdd(QueryRequest queryRequest,String userId);
}
