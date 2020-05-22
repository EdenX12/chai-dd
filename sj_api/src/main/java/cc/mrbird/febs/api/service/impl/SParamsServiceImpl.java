package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SParams;
import cc.mrbird.febs.api.mapper.SParamsMapper;
import cc.mrbird.febs.api.service.ISParamsService;
import cc.mrbird.febs.common.exception.RedisConnectException;
import cc.mrbird.febs.common.service.RedisService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@Service
public class SParamsServiceImpl  extends ServiceImpl<SParamsMapper,SParams>  implements ISParamsService {
    org.slf4j.Logger logger = LoggerFactory.getLogger(SParamsServiceImpl.class);
    @Autowired
    private RedisService redisService;

    @Override
    public Set<String> queryBykey(String key) {
        /*LambdaQueryWrapper<SParams> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(SParams::getPKey,key);*/
        try {
            return redisService.getKeys(key);
        } catch (RedisConnectException e) {
            e.printStackTrace();
        }
        return  null;
    }

    @Override
    public String queryBykeyForOne(String key) {
        try {
            return redisService.get(key);
        } catch (RedisConnectException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void cacheParams() {
        LambdaQueryWrapper<SParams> queryWrapper = new LambdaQueryWrapper();
        List<SParams> paramsList = this.baseMapper.selectList(queryWrapper);
        if(paramsList != null){
            try {
                for(SParams param : paramsList) {
                    redisService.set(param.getPKey(),param.getPValue());
                }
            } catch (RedisConnectException e) {
                logger.error(e.getMessage(),e);
            }
        }
    }
}
