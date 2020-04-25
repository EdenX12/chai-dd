package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SProductSpec;
import cc.mrbird.febs.api.mapper.SProductSpecMapper;
import cc.mrbird.febs.api.service.ISProductSpecService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author MrBird
 */
@Service
public class SProductSpecServiceImpl extends ServiceImpl<SProductSpecMapper, SProductSpec> implements ISProductSpecService {

    @Override
    public List<SProductSpec> findProductSpecList(String productId) {

        LambdaQueryWrapper<SProductSpec> queryWrapper = new LambdaQueryWrapper();

        // 商品ID
        queryWrapper.eq(SProductSpec::getProductId, productId);

        // 删除标识：0未删除 1已删除
        queryWrapper.eq(SProductSpec::getDeleteFlag, 0);

        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public SProductSpec findProductSpec(String productSpecId) {

        LambdaQueryWrapper<SProductSpec> queryWrapper = new LambdaQueryWrapper();

        // 商品ID
        queryWrapper.eq(SProductSpec::getId, productSpecId);

        // 删除标识：0未删除 1已删除
        queryWrapper.eq(SProductSpec::getDeleteFlag, 0);

        return this.baseMapper.selectOne(queryWrapper);
    }
}
