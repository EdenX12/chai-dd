package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SUserLevel;
import cc.mrbird.febs.api.mapper.SUserLevelMapper;
import cc.mrbird.febs.api.service.ISUserLevelService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author MrBird
 */
@Service
public class SUserLevelServiceImpl extends ServiceImpl<SUserLevelMapper, SUserLevel> implements ISUserLevelService {

    @Override
    public SUserLevel findByLevelType(Integer userLevelType) {

        LambdaQueryWrapper<SUserLevel> queryWrapper = new LambdaQueryWrapper<SUserLevel>();

        // 用户等级
        queryWrapper.in(SUserLevel::getLevelType, userLevelType);

        return this.baseMapper.selectOne(queryWrapper);
    }

}
