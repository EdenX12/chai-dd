package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.STaskOrder;
import cc.mrbird.febs.api.entity.SUser;
import cc.mrbird.febs.api.mapper.STaskOrderMapper;
import cc.mrbird.febs.api.service.ISTaskOrderService;
import cc.mrbird.febs.common.domain.FebsConstant;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.utils.FebsUtil;
import cc.mrbird.febs.common.utils.SortUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;

/**
 * @author MrBird
 */
@Service
public class STaskOrderServiceImpl extends ServiceImpl<STaskOrderMapper, STaskOrder> implements ISTaskOrderService {

    @Override
    @Transactional
    public Long createTaskOrder(STaskOrder taskOrder) {

        this.baseMapper.insert(taskOrder);

        return taskOrder.getId();
    }

    @Override
    public IPage<Map> findTaskOrderList(STaskOrder taskOrder, QueryRequest request) {
        try {
            Page<Map> page = new Page<>();
            SortUtil.handlePageSort(request, page, "endTime", FebsConstant.ORDER_ASC, false);
            return this.baseMapper.findTaskOrderDetail(page, taskOrder);
        } catch (Exception e) {
            log.error("查询全部转让任务异常", e);
            return null;
        }
    }

    @Override
    public Map findTaskOrderDetail(STaskOrder taskOrder) {
        try {
            return this.baseMapper.findTaskOrderDetail(taskOrder);
        } catch (Exception e) {
            log.error("查询转让任务详情异常", e);
            return null;
        }
    }

}
