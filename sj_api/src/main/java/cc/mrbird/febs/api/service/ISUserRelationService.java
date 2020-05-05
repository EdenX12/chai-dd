package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SUserRelation;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author MrBird
 */
public interface ISUserRelationService extends IService<SUserRelation> {

    SUserRelation createUserRelation(SUserRelation userRelation);

    SUserRelation findUserRelation(SUserRelation userRelation);

    Integer findUserRelationCnt(String userId);

    Integer findUserRelationTodayCnt(String userId);
}
