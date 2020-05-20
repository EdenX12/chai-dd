package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SOfferPrice;
import cc.mrbird.febs.api.entity.SUserRelation;
import cc.mrbird.febs.api.mapper.SUserRelationMapper;
import cc.mrbird.febs.api.service.ISUserRelationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author MrBird
 */
@Service
public class SUserRelationServiceImpl extends ServiceImpl<SUserRelationMapper, SUserRelation> implements ISUserRelationService {

    @Override
    public SUserRelation createUserRelation(SUserRelation userRelation) {

        userRelation.setUpdateTime(new Date());
        userRelation.setCreateTime(new Date());

        this.baseMapper.insert(userRelation);

        return userRelation;
    }

    @Override
    public SUserRelation findUserRelation(SUserRelation userRelation) {

        LambdaQueryWrapper<SUserRelation> queryWrapper = new LambdaQueryWrapper<SUserRelation>();

        // 用户ID
        if (userRelation.getUserId() != null) {
            queryWrapper.eq(SUserRelation::getUserId, userRelation.getUserId());
        }

        // 父ID
        if (userRelation.getParentId() != null) {
            queryWrapper.eq(SUserRelation::getParentId, userRelation.getParentId());
        }

        // 微信unionid
        if (userRelation.getUnionId() != null) {
            queryWrapper.eq(SUserRelation::getUnionId, userRelation.getUnionId());
        }

        // 关系类型 0-预备队 1-近卫军
        if (userRelation.getRelationType() != null) {
            queryWrapper.eq(SUserRelation::getRelationType, userRelation.getRelationType());
        }

        queryWrapper.orderByDesc(SUserRelation::getCreateTime);

        List<SUserRelation> list = this.baseMapper.selectList(queryWrapper);

        if (list != null && list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Integer findUserRelationCnt(String userId) {

        return this.baseMapper.queryUserRelationCnt(userId);
    }

    @Override
    public Integer findUserRelationTodayCnt(String userId) {

        return this.baseMapper.queryUserRelationTodayCnt(userId);
    }
}
