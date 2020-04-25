package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SActivity;
import cc.mrbird.febs.api.mapper.SActivityMapper;
import cc.mrbird.febs.api.service.ISActivityService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author MrBird
 */
@Service
public class SActivityServiceImpl extends ServiceImpl<SActivityMapper, SActivity> implements ISActivityService {

    @Override
    public List<SActivity> findActivityList() {

        LambdaQueryWrapper<SActivity> queryWrapper = new LambdaQueryWrapper();

        // 状态 0-创建 1-发布 2-下架 3-删除
        queryWrapper.eq(SActivity::getActStatus, 1);

        // 顺序
        queryWrapper.orderByAsc(SActivity::getSOrder);

        return this.baseMapper.selectList(queryWrapper);
    }
}
