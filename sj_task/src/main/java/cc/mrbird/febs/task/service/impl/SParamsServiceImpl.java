package cc.mrbird.febs.task.service.impl;

import cc.mrbird.febs.task.entity.SParams;
import cc.mrbird.febs.task.mapper.SParamsMapper;
import cc.mrbird.febs.task.service.ISParamsService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SParamsServiceImpl  extends ServiceImpl<SParamsMapper, SParams>  implements ISParamsService {

    @Override
    public SParams queryBykeyForOne(String key) {
        LambdaQueryWrapper<SParams> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(SParams::getPKey,key);
        List<SParams> list =   this.baseMapper.selectList(queryWrapper);
        if(list != null && list.size() >0){
            return list.get(0);
        }
        return null;
    }
}
