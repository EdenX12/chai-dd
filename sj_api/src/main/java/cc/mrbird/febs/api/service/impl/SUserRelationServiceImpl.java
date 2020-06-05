package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SOfferPrice;
import cc.mrbird.febs.api.entity.SUserAddress;
import cc.mrbird.febs.api.entity.SUserRelation;
import cc.mrbird.febs.api.mapper.SUserRelationMapper;
import cc.mrbird.febs.api.service.ISUserRelationService;
import cc.mrbird.febs.common.domain.FebsConstant;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.utils.SortUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

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

    @Override
    public IPage<Map> getFirstLevel(QueryRequest queryRequest, String userId, String relationType) {
        try {
            Page<Map> page = new Page<>();


            SortUtil.handlePageSort(queryRequest, page, null,null, false);

            return this.baseMapper.getFirstLevel(page,userId,relationType);
        } catch (Exception e) {
            log.error("查询一级禁卫军/预备队异常", e);
            return null;
        }
    }

    @Override
    public IPage<Map> getSecondLevel(QueryRequest queryRequest, String userId, String relationType) {
        try {
            Page<Map> page = new Page<>();

            SortUtil.handlePageSort(queryRequest, page, null,null, false);

            return this.baseMapper.getSecondLevel(page,userId,relationType);
        } catch (Exception e) {
            log.error("查询二级禁卫军/预备队异常", e);
            return null;
        }
    }

    @Override
    public IPage<Map> getThirdLevel(QueryRequest queryRequest, String userId, String relationType) {
        try {
            Page<Map> page = new Page<>();


            SortUtil.handlePageSort(queryRequest, page, null,null, false);

            return this.baseMapper.getThirdLevel(page,userId,relationType);
        } catch (Exception e) {
            log.error("查询三级禁卫军/预备队异常", e);
            return null;
        }
    }

    @Override
    public Map<String, Object> getMyTeamTotal(String userId, String relationType) {
        try {
            return this.baseMapper.getMyTeamTotal(userId,relationType);
        } catch (Exception e) {
            log.error("查询禁卫军/预备队异常", e);
            return null;
        }
    }

    @Override
    public IPage<Map> getTodayNewAdd(QueryRequest queryRequest, String userId) {
        try {
            Page<Map> page = new Page<>();


            SortUtil.handlePageSort(queryRequest, page, null,null, false);

            return this.baseMapper.getTodayNewAdd(page,userId);
        } catch (Exception e) {
            log.error("查询今日禁卫军/预备队异常", e);
            return null;
        }
    }

    @Override
    public IPage<Map> getFirstLeveForAmt(QueryRequest queryRequest, String userId, String relationType) {
        try {
            Page<Map> page = new Page<>();


            SortUtil.handlePageSort(queryRequest, page, null,null, false);

            return this.baseMapper.getFirstLevelForAmt(page,userId,relationType);
        } catch (Exception e) {
            log.error("查询一级禁卫军/预备队分佣明细异常", e);
            return null;
        }
    }

    @Override
    public IPage<Map> getSecondLevelForAmt(QueryRequest queryRequest, String userId, String relationType) {
        try {
            Page<Map> page = new Page<>();


            SortUtil.handlePageSort(queryRequest, page, null,null, false);

            return this.baseMapper.getSecondLevelForAmt(page,userId,relationType);
        } catch (Exception e) {
            log.error("查询二级禁卫军/预备队分佣明细异常", e);
            return null;
        }
    }

    @Override
    public IPage<Map> getThirdLevelForAmt(QueryRequest queryRequest, String userId, String relationType) {
        try {
            Page<Map> page = new Page<>();


            SortUtil.handlePageSort(queryRequest, page, null,null, false);

            return this.baseMapper.getThirdLevelForAmt(page,userId,relationType);
        } catch (Exception e) {
            log.error("查询三级禁卫军/预备队分佣明细异常", e);
            return null;
        }
    }

    @Override
    public Map<String, Object> getMyTeamTotalForAmt(String userId, String relationType) {
        //TODO
        return null;
    }

    @Override
    public IPage<Map> getTodayNewAddForAmt(QueryRequest queryRequest, String userId) {
        try {
            Page<Map> page = new Page<>();

            SortUtil.handlePageSort(queryRequest, page, null,null, false);

            return this.baseMapper.getTodayNewAddForAmt(page,userId);
        } catch (Exception e) {
            log.error("查询禁卫军/预备队今日新增分佣明细异常", e);
            return null;
        }
    }
}
