package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SOrder;
import cc.mrbird.febs.api.entity.SProductImg;
import cc.mrbird.febs.api.mapper.SOrderMapper;
import cc.mrbird.febs.api.service.ISOrderService;
import cc.mrbird.febs.api.service.ISParamsService;
import cc.mrbird.febs.api.service.ISProductImgService;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.utils.SortUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author MrBird
 */
@Service
public class SOrderServiceImpl extends ServiceImpl<SOrderMapper, SOrder> implements ISOrderService {

    @Autowired
    private ISProductImgService productImgService;

    @Autowired
    private ISParamsService paramsService;

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

        // 买家立返佣金比例 （后续调整到Redis缓存读取）
        String value = this.paramsService.queryBykeyForOne("buyer_rate");
        BigDecimal buyerRate = BigDecimal.valueOf(Double.parseDouble(value));

//        params = this.paramsService.queryBykeyForOne("product_bean_cnt");
//        Integer productBeanCnt = Integer.valueOf(params.getPValue());

        Map<String, Object> result = this.baseMapper.queryOrderDetail(orderDetailId);


        if (result != null) {
            List<Map> productList = this.baseMapper.queryProductDetailId(orderDetailId);
            result.put("productList", productList);

            // 买家立返
            BigDecimal buyerReturnAmt = new BigDecimal(0);

            // 赠送拆豆
            Integer rewardBean = 0;

            for (Map productMap : productList) {

                // 商品图片
                List<SProductImg> productImgList = this.productImgService.findProductImgList((String)productMap.get("productId"));
                productMap.put("imgUrlList", productImgList);

                // 总佣金
                BigDecimal totalReward = new BigDecimal(productMap.get("totalReward").toString());

                // 商品数量
                BigDecimal productNumber = new BigDecimal(productMap.get("productNumber").toString());

                // 买家立返
                buyerReturnAmt = buyerReturnAmt.add(totalReward.multiply(buyerRate).multiply(productNumber));

//                // 赠送拆豆
//                rewardBean = rewardBean + productBeanCnt * Integer.parseInt(productMap.get("productNumber").toString());
            }

            // 赠送拆豆 暂时变更为商品价格*10
            rewardBean = new BigDecimal(result.get("payAmount").toString()).multiply(new BigDecimal(10)).intValue();

            // 计算返现合计
            result.put("buyerReturnAmt", buyerReturnAmt);

            // 计算赠送拆豆
            result.put("rewardBean", rewardBean);
        }

        return  result;
    }

    @Override
    public List<SOrder> findOrderPaySuccessList() {

        return this.baseMapper.queryOrderPaySuccessList();
    }

    @Override
    public List<Map<String, Object>> getTotalOrderCount(String userId) {
        return this.baseMapper.getTotalOrderCount(userId);
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
