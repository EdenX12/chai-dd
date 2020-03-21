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
    	
        return baseMapper.selectList(new LambdaQueryWrapper<SArea>().eq(SArea::getAreaParentId, area.getAreaParentId()).eq(SArea::getIsDel, 0).orderByAsc(SArea::getAreaSort));
    }

}
