package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SActivity;
import cc.mrbird.febs.api.entity.SOrder;
import cc.mrbird.febs.api.mapper.SOrderMapper;
import cc.mrbird.febs.api.service.ISOrderService;
import cc.mrbird.febs.common.domain.FebsConstant;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.utils.SortUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author MrBird
 */
@Service
public class SOrderServiceImpl extends ServiceImpl<SOrderMapper, SOrder> implements ISOrderService {

    @Override
    public SOrder addOrder(SOrder order) {

        this.baseMapper.insert(order);

        return order;
    }

    @Override
    public IPage<Map> queryPage(QueryRequest request, String userId, String status) {

        try {

            Page<Map> page = new Page<>();
            SortUtil.handlePageSort(request, page, null, null, false);
            IPage<Map> returnPage =  this.baseMapper.queryPage(page, status, userId);

            List<Map> list = returnPage.getRecords();

            if (list != null) {
                for (int i = 0; i < list.size(); i++) {

                    String orderDetailId = (String) list.get(i).get("orderDetailId");

                    if (orderDetailId != null) {

                        List<Map> productList = this.baseMapper.queryProductDetailId(orderDetailId);
                        list.get(i).put("productList", productList);
                    }
                }

                returnPage.setRecords(list);
            }

            return returnPage;
        } catch (Exception e) {
            log.error("查询用户购买订单异常", e);
            return null;
        }
    }

    @Override
    public Map<String, Object> queryOrderDetail(String orderDetailId) {

        Map<String, Object> result = this.baseMapper.queryOrderDetail(orderDetailId);

        if (result != null) {

            List<Map> productList = this.baseMapper.queryProductDetailId(orderDetailId);
            result.put("productList", productList);
        }

        return  result;
    }

    @Override
    public List<SOrder> findOrderPaySuccessList() {

        LambdaQueryWrapper<SOrder> queryWrapper = new LambdaQueryWrapper();

        // 付款状态:0:未付款;1:已付款 2:已结算到冻结
        queryWrapper.eq(SOrder::getPaymentState, 1);

        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public SOrder updateOrder(SOrder order) {

        LambdaQueryWrapper<SOrder> queryWrapper = new LambdaQueryWrapper<SOrder>();

        // 用户ID
        queryWrapper.eq(SOrder::getUserId, order.getUserId());

        // 订单ID
        queryWrapper.eq(SOrder::getId, order.getId());

        this.baseMapper.update(order, queryWrapper);
        return order;
    }

}
