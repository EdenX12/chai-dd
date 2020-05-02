package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SUserShare;
import cc.mrbird.febs.api.mapper.SUserShareMapper;
import cc.mrbird.febs.api.service.ISUserShareService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author MrBird
 */
@Service
public class SUserShareServiceImpl extends ServiceImpl<SUserShareMapper, SUserShare> implements ISUserShareService {

    @Override
    public String getCurrentShareId(String productId, String userId) {
        return this.baseMapper.getCurrentShareId(productId,userId);
    }
}
