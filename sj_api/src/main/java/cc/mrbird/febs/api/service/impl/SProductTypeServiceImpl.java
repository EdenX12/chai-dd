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
    public List<SProductType> findByTypeStatus(int typeStatus) {
        return this.baseMapper.selectList(new LambdaQueryWrapper<SProductType>().eq(SProductType::getTypeStatus, typeStatus));
    }
}
