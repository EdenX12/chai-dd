package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.STaskLine;
import cc.mrbird.febs.api.mapper.STaskLineMapper;
import cc.mrbird.febs.api.service.ISTaskLineService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

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
        return this.baseMapper.queryIdByLineOrder(productId, lineOrder);
    }

    @Override
    public List<STaskLine> findTaskLineList(STaskLine taskLine) {

        LambdaQueryWrapper<STaskLine> queryWrapper = new LambdaQueryWrapper();

        if (taskLine.getId() != null) {
            queryWrapper.eq(STaskLine::getId, taskLine.getId());
        }

        if (taskLine.getProductId() != null) {
            queryWrapper.eq(STaskLine::getProductId, taskLine.getProductId());
        }

        if (taskLine.getOrderProductId() != null) {
            queryWrapper.eq(STaskLine::getOrderProductId, taskLine.getOrderProductId());
        }

        if (taskLine.getLineStatus() != null) {
            queryWrapper.eq(STaskLine::getLineStatus, taskLine.getLineStatus());
        }

        if (taskLine.getSettleStatus() != null) {
            queryWrapper.eq(STaskLine::getSettleStatus, taskLine.getSettleStatus());
        }

        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public STaskLine findTaskLineForSettle(String productId) {

        LambdaQueryWrapper<STaskLine> queryWrapper = new LambdaQueryWrapper();

        // 商品ID
        queryWrapper.eq(STaskLine::getProductId, productId);

        // 结算状态  0：未完成
        queryWrapper.eq(STaskLine::getSettleStatus, 0);

        queryWrapper.orderByAsc(STaskLine::getLineOrder);

        return this.baseMapper.selectOne(queryWrapper);
    }
    public String queryForSettle(String productId ) {
        return this.baseMapper.queryForSettle(productId);
    }


    @Override
    public void updateUserTaskLineForSettle(List<String> list) {
        this.baseMapper.updateUserTaskLineForSettle(list);
    }

}
