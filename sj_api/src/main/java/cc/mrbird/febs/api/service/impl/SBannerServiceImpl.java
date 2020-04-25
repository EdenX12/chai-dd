package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SBanner;
import cc.mrbird.febs.api.mapper.SBannerMapper;
import cc.mrbird.febs.api.service.ISBannerService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author MrBird
 */
@Service
public class SBannerServiceImpl extends ServiceImpl<SBannerMapper, SBanner> implements ISBannerService {

    @Override
    public List<SBanner> findBannerList() {

        LambdaQueryWrapper<SBanner> queryWrapper = new LambdaQueryWrapper();

        // 类型 首页轮播图
        queryWrapper.eq(SBanner::getBannerType, 1);

        // 发布状态
        queryWrapper.eq(SBanner::getBannerStatus, 1);

        // 顺序
        queryWrapper.orderByAsc(SBanner::getSOrder);

        return this.baseMapper.selectList(queryWrapper);
    }

}
