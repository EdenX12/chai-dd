package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SProductType;
import cc.mrbird.febs.api.mapper.SProductTypeMapper;
import cc.mrbird.febs.api.service.ISProductTypeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author MrBird
 */
@Service
public class SProductTypeServiceImpl extends ServiceImpl<SProductTypeMapper, SProductType> implements ISProductTypeService {

    @Override
    public List<SProductType> findRecommendProductTypeList() {

        LambdaQueryWrapper<SProductType> queryWrapper = new LambdaQueryWrapper();

        // 分类状态 1 可用
        queryWrapper.eq(SProductType::getTypeStatus, 1);

        // 1-推荐
        queryWrapper.eq(SProductType::getFlag, 1);

        // 顺序
        queryWrapper.orderByAsc(SProductType::getSOrder);

        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<SProductType> findProductTypeList(SProductType productType) {

        LambdaQueryWrapper<SProductType> queryWrapper = new LambdaQueryWrapper();

        // 上级
        if (productType.getParentId() != null) {
            queryWrapper.eq(SProductType::getParentId, productType.getParentId());
        }

        // 级别
        queryWrapper.eq(SProductType::getLevel, productType.getLevel());

        // 分类状态 1 可用
        queryWrapper.eq(SProductType::getTypeStatus, 1);

        // 顺序
        queryWrapper.orderByAsc(SProductType::getSOrder);

        return this.baseMapper.selectList(queryWrapper);
    }


}
