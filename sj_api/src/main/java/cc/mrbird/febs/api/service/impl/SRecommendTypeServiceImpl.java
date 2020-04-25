package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SRecommendType;
import cc.mrbird.febs.api.mapper.SRecommendTypeMapper;
import cc.mrbird.febs.api.service.ISRecommendTypeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author MrBird
 */
@Service
public class SRecommendTypeServiceImpl extends ServiceImpl<SRecommendTypeMapper, SRecommendType> implements ISRecommendTypeService {

    @Override
    public List<SRecommendType> findRecommendTypeList() {

        LambdaQueryWrapper<SRecommendType> queryWrapper = new LambdaQueryWrapper();

        // 状态 0-创建 1-发布 2-下架 3-删除
        queryWrapper.eq(SRecommendType::getRecommendStatus, 1);

        // 顺序
        queryWrapper.orderByAsc(SRecommendType::getSOrder);

        return this.baseMapper.selectList(queryWrapper);
    }


}
