package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SUser;
import cc.mrbird.febs.api.entity.SUserFollow;
import cc.mrbird.febs.api.mapper.SUserFollowMapper;
import cc.mrbird.febs.api.service.ISUserFollowService;
import cc.mrbird.febs.common.utils.FebsUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author MrBird
 */
@Service
public class SUserFollowServiceImpl extends ServiceImpl<SUserFollowMapper, SUserFollow> implements ISUserFollowService {

    @Override
    @Transactional
    public void createUserFollow(SUserFollow userFollow) {

        SUser user = FebsUtil.getCurrentUser();
        userFollow.setUserId(user.getId());

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

            this.save(userFollow);

        } else {

            userFollowOne.setUpdateTime(new Date());
            userFollowOne.setStatus(1);

            this.baseMapper.updateById(userFollowOne);
        }
    }

    @Override
    @Transactional
    public void updateUserFollow(SUserFollow userFollow) {

        SUser user = FebsUtil.getCurrentUser();
        userFollow.setUserId(user.getId());

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
    }
}
