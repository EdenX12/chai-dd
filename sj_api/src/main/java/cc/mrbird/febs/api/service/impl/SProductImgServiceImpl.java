package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SProductImg;
import cc.mrbird.febs.api.mapper.SProductImgMapper;
import cc.mrbird.febs.api.service.ISProductImgService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author MrBird
 */
@Service
public class SProductImgServiceImpl extends ServiceImpl<SProductImgMapper, SProductImg> implements ISProductImgService {

    @Override
    public List<SProductImg> findProductImgList(String productId) {

        LambdaQueryWrapper<SProductImg> queryWrapper = new LambdaQueryWrapper();

        // 商品ID
        queryWrapper.eq(SProductImg::getProductId, productId);

        return this.baseMapper.selectList(queryWrapper);
    }

}
