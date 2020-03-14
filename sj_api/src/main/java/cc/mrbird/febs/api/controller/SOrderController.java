package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.SOrder;
import cc.mrbird.febs.api.entity.SProduct;
import cc.mrbird.febs.api.entity.SUser;
import cc.mrbird.febs.api.service.ISOrderService;
import cc.mrbird.febs.api.service.ISProductService;
import cc.mrbird.febs.common.annotation.Limit;
import cc.mrbird.febs.common.annotation.Log;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.domain.FebsResponse;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.exception.FebsException;
import cc.mrbird.febs.common.utils.FebsUtil;
import cc.mrbird.febs.common.utils.WeChatPayUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * @author MrBird
 */
@Slf4j
@RestController
@RequestMapping("/api/s-order")
public class SOrderController extends BaseController {

    private String message;

    @Autowired
    private ISOrderService orderService;

    @Autowired
    private ISProductService productService;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    /**
     * 新增用户购买订单
     */
    @Log("新增用户购买订单")
    @PostMapping("/addOrder")
    public FebsResponse addOrder(HttpServletRequest request, @Valid SOrder order) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {

            SUser user = FebsUtil.getCurrentUser();
            order.setUserId(user.getId());

            SProduct product = productService.getById(order.getProductId());

            order.setOrderStatus(0);
            order.setCreateTime(new Date());
            order.setPaymentType(1);
            order.setPaymentState(0);
            order.setOrderAmount(product.getProductPrice().multiply(BigDecimal.valueOf(order.getProductNumber())));
            order.setPayAmount(product.getProductPrice().multiply(BigDecimal.valueOf(order.getProductNumber())));

            int orderId = this.orderService.addOrder(order);

            // 调起微信支付
            JSONObject jsonObject = weChatPayUtil.weChatPay(String.valueOf(orderId),
                    product.getProductPrice().multiply(BigDecimal.valueOf(order.getProductNumber().longValue())).toString(),
                    user.getOpenId(),
                    request.getRemoteAddr(),
                    "2",
                    "购买商品");

            response.data(jsonObject);

        } catch (Exception e) {
            message = "购买订单失败";
            response.put("code", 1);
            response.message(message);
            log.error(message, e);
        }

        return response;
    }

    /**
     * 根据用户ID取得我的购买订单列表信息
     * @return List<SOrder>
     */
    @PostMapping("/getOrderList")
    @Limit(key = "getOrderList", period = 60, count = 20, name = "检索我的购买订单接口", prefix = "limit")
    public FebsResponse getOrderList(QueryRequest queryRequest, SOrder order) {

        FebsResponse response = new FebsResponse();

        SUser user = FebsUtil.getCurrentUser();
        order.setUserId(user.getId());

        Map<String, Object> orderPageList = getDataTable(orderService.findOrderList(order, queryRequest));

        response.put("code", 0);
        response.data(orderPageList);

        return response;
    }

    /**
     * 根据用户ID取得我的购买订单详情
     * @return SOrder
     */
    @PostMapping("/getOrderDetail")
    @Limit(key = "getOrderDetail", period = 60, count = 20, name = "检索用户购买订单详情接口", prefix = "limit")
    public FebsResponse getOrderDetail(SOrder order) {

        FebsResponse response = new FebsResponse();

        SUser user = FebsUtil.getCurrentUser();
        order.setUserId(user.getId());

        SOrder orderDetail = orderService.findOrderDetail(order);

        response.put("code", 0);
        response.data(orderDetail);

        return response;
    }

    /**
     * 更新用户购买订单状态
     * 用户ID 订单ID 状态
     */
    @Log("更新用户购买订单状态")
    @PostMapping("/updateOrder")
    public void updateOrder(@Valid SOrder order) throws FebsException {

        try {
            this.orderService.updateOrder(order);
        } catch (Exception e) {
            message = "更新用户购买订单状态失败";
            log.error(message, e);
            throw new FebsException(message);
        }
    }
}
