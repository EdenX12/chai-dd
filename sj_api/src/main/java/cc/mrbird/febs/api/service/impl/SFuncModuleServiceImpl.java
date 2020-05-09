package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SFuncModule;
import cc.mrbird.febs.api.mapper.SFuncModuleMapper;
import cc.mrbird.febs.api.service.ISFuncModuleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author MrBird
 */
@Service
public class SFuncModuleServiceImpl extends ServiceImpl<SFuncModuleMapper, SFuncModule> implements ISFuncModuleService {

    @Override
    public List<SFuncModule> findFuncModuleList() {

        LambdaQueryWrapper<SFuncModule> queryWrapper = new LambdaQueryWrapper();

        // 状态 0-创建 1-发布 2-下架 3-删除
        queryWrapper.eq(SFuncModule::getModuleStatus, 1);

        // 顺序
        queryWrapper.orderByAsc(SFuncModule::getSOrder);

        return this.baseMapper.selectList(queryWrapper);
    }
}
