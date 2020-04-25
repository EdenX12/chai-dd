package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SProductRecommend;
import cc.mrbird.febs.api.mapper.SProductRecommendMapper;
import cc.mrbird.febs.api.service.ISProductRecommendService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author MrBird
 */
@Service
public class SProductRecommendServiceImpl extends ServiceImpl<SProductRecommendMapper, SProductRecommend> implements ISProductRecommendService {

    @Override
    public List<Map> findProductRecommendList(String recommendTypeId) {

        SProductRecommend productRecommend = new SProductRecommend();

        productRecommend.setRecommendTypeId(recommendTypeId);

        return this.baseMapper.findProductRecommendList(productRecommend);
    }

}
