package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.STaskLine;
import cc.mrbird.febs.api.mapper.STaskLineMapper;
import cc.mrbird.febs.api.service.ISTaskLineService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author MrBird
 */
@Service
public class STaskLineServiceImpl extends ServiceImpl<STaskLineMapper, STaskLine> implements ISTaskLineService {

    @Override
    public Integer queryTaskLineCount(String productId) {
        return this.baseMapper.queryTaskLineCount(productId);
    }

    @Override
    public Integer queryMinLineOrder(String productId) {
        return this.baseMapper.queryMinLineOrder(productId);
    }

    @Override
    public String queryIdByLineOrder(String productId, Integer lineOrder) {
        return this.baseMapper.queryIdByLineOrder(productId,lineOrder);
    }
}
