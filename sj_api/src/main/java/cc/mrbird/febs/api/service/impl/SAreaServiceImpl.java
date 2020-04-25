package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SArea;
import cc.mrbird.febs.api.mapper.SAreaMapper;
import cc.mrbird.febs.api.service.ISAreaService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author MrBird
 */
@Service
public class SAreaServiceImpl extends ServiceImpl<SAreaMapper, SArea> implements ISAreaService {

    @Override
    public List<SArea> findAreaList(SArea area) {

        LambdaQueryWrapper<SArea> queryWrapper = new LambdaQueryWrapper();

        queryWrapper.eq(SArea::getAreaParentId, area.getAreaParentId());
        queryWrapper.eq(SArea::getIsDel, 0);

        queryWrapper.orderByAsc(SArea::getAreaSort);

        return this.baseMapper.selectList(queryWrapper);
    }

}
