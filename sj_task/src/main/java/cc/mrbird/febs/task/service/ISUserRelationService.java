package cc.mrbird.febs.task.service;

import cc.mrbird.febs.task.entity.SUserRelation;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author MrBird
 */
public interface ISUserRelationService extends IService<SUserRelation> {

    SUserRelation findUserRelation(SUserRelation userRelation);
}
