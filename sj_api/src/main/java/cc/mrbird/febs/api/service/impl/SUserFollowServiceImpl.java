package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SUserFollow;
import cc.mrbird.febs.api.mapper.SUserFollowMapper;
import cc.mrbird.febs.api.service.ISUserFollowService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author MrBird
 */
@Service
public class SUserFollowServiceImpl extends ServiceImpl<SUserFollowMapper, SUserFollow> implements ISUserFollowService {

    @Override
    public SUserFollow findUserFollowDetail(SUserFollow userFollow) {

        LambdaQueryWrapper<SUserFollow> queryWrapper = new LambdaQueryWrapper();

        // 用户ID
        queryWrapper.eq(SUserFollow::getUserId, userFollow.getUserId());

        // 转让任务ID不为空的情况下
        if (userFollow.getTaskOrderId() != null) {
            queryWrapper.eq(SUserFollow::getTaskOrderId, userFollow.getTaskOrderId());
        }

        // 商品ID不为空的情况下
        if (userFollow.getProductId() != null) {
            queryWrapper.eq(SUserFollow::getProductId, userFollow.getProductId());
        }

        queryWrapper.eq(SUserFollow::getFollowType, userFollow.getFollowType());
        queryWrapper.last("limit 1");

        return this.baseMapper.selectOne(queryWrapper);
    }

    @Override
    public SUserFollow createUserFollow(SUserFollow userFollow) {

        LambdaQueryWrapper<SUserFollow> queryWrapper = new LambdaQueryWrapper<SUserFollow>();

        // 用户ID
        queryWrapper.eq(SUserFollow::getUserId, userFollow.getUserId());

        // 产品ID
        if (userFollow.getProductId() != null) {
            queryWrapper.eq(SUserFollow::getProductId, userFollow.getProductId());
        }

        // 转让任务ID
        if (userFollow.getTaskOrderId() != null) {
            queryWrapper.eq(SUserFollow::getTaskOrderId, userFollow.getTaskOrderId());
        }

        SUserFollow userFollowOne = this.baseMapper.selectOne(queryWrapper);

        if (userFollowOne == null) {

            userFollow.setCreateTime(new Date());
            userFollow.setUpdateTime(new Date());
            userFollow.setStatus(1);

            this.baseMapper.insert(userFollow);

            return userFollow;
        } else {

            userFollowOne.setUpdateTime(new Date());
            userFollowOne.setStatus(1);

            this.baseMapper.updateById(userFollowOne);

            return userFollowOne;
        }
    }

    @Override
    public SUserFollow updateUserFollow(SUserFollow userFollow) {

        LambdaQueryWrapper<SUserFollow> queryWrapper = new LambdaQueryWrapper<SUserFollow>();

        // 用户ID
        queryWrapper.eq(SUserFollow::getUserId, userFollow.getUserId());

        // 产品ID
        if (userFollow.getProductId() != null) {
            queryWrapper.eq(SUserFollow::getProductId, userFollow.getProductId());
        }

        // 转让任务ID
        if (userFollow.getTaskOrderId() != null) {
            queryWrapper.eq(SUserFollow::getTaskOrderId, userFollow.getTaskOrderId());
        }

        SUserFollow userFollowOne = this.baseMapper.selectOne(queryWrapper);

        if (userFollowOne != null) {
            userFollowOne.setUpdateTime(new Date());
            userFollowOne.setStatus(0);

            this.baseMapper.updateById(userFollowOne);
        }

        return userFollowOne;
    }
}
