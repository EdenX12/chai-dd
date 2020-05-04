package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SUserRelation;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author MrBird
 */
public interface ISUserRelationService extends IService<SUserRelation> {

    SUserRelation createUserRelation(SUserRelation userRelation);

    SUserRelation findUserRelation(SUserRelation userRelation);
}
