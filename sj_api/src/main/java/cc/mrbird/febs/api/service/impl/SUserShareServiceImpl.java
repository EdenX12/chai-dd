package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SUserShare;
import cc.mrbird.febs.api.mapper.SUserShareMapper;
import cc.mrbird.febs.api.service.ISUserShareService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author MrBird
 */
@Service
public class SUserShareServiceImpl extends ServiceImpl<SUserShareMapper, SUserShare> implements ISUserShareService {

    @Override
    public SUserShare createUserShare(SUserShare userShare) {

        userShare.setShareStatus(1);
        userShare.setCreateTime(new Date());

        this.baseMapper.insert(userShare);

        return userShare;
    }

    @Override
    public int findUserShareCount(String userId, String productId) {

        LambdaQueryWrapper<SUserShare> queryWrapper = new LambdaQueryWrapper<SUserShare>();

        // 用户ID
        queryWrapper.eq(SUserShare::getUserId, userId);

        // 产品ID
        queryWrapper.eq(SUserShare::getProductId, productId);

        return this.baseMapper.selectCount(queryWrapper);
    }

}
