package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.*;
import cc.mrbird.febs.api.service.*;
import cc.mrbird.febs.common.annotation.Limit;
import cc.mrbird.febs.common.annotation.Log;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.domain.FebsResponse;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.utils.FebsUtil;
import cc.mrbird.febs.common.utils.WeChatPayUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author MrBird
 */
@Slf4j
@RestController
@Configuration
@EnableScheduling
@RequestMapping("/api/s-order")
public class SOrderController extends BaseController {

    private String message;

    @Autowired
    private ISOrderService orderService;

    @Autowired
    private ISOrderDetailService orderDetailService;

    @Autowired
    private ISOrderProductService orderProductService;

    @Autowired
    private ISProductService productService;

    @Autowired
    private ISProductSpecService productSpecService;

    @Autowired
    private ISUserShopCarService userShopCarService;

    @Autowired
    private ISUserAddressService userAddressService;

    @Autowired
    private ISTaskCouponService taskCouponService;

    @Autowired
    private ISUserCouponService userCouponService;

    @Autowired
    private ISShopCouponService shopCouponService;

    @Autowired
    private ISTaskLineService taskLineService;

    @Autowired
    private ISUserTaskLineService userTaskLineService;

    @Autowired
    private ISUserService userService;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private ISUserAmountLogService userAmountLogService;

    @Autowired
    private ISUserBonusLogService userBonusLogService;

    @Autowired
    private ISUserMsgService userMsgService;

    @Autowired
    private ISParamsService paramsService;

    @Autowired
    private ISUserRelationService userRelationService;

    /**
     * 确认订单
     * 商品规格ID   productSpecId
     * 数量         productNumber
     */
    @Log("确认订单")
    @Transactional
    @PostMapping("/confirmOrder")
    public FebsResponse confirmOrder(@RequestBody String jsonString) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        Map<String, Object> resultMap = new HashMap<>();

        try {

            SUser user = FebsUtil.getCurrentUser();

            JSONObject json = JSON.parseObject(jsonString);

            // 收货地址
            String addressId = json.getString("addressId");

            // 店铺ID
            JSONArray productSpecList = json.getJSONArray("productSpecList");

            List<Map<String, Object>> productList = new ArrayList<>();

            for(int i=0; i<productSpecList.size(); i++){

                JSONObject productSpecJson = productSpecList.getJSONObject(i);

                // 规格商品ID
                String productSpecId = productSpecJson.get("productSpecId").toString();

                // 商品数量
                int productNumber = (Integer) productSpecJson.get("productNumber");

                // 商品规格
                SProductSpec productSpec = this.productSpecService.findProductSpec(productSpecId);

                // 库存数量判断
                if (productSpec.getStockNumber() < productNumber) {
                    message = "您选择的商品库存不足！";
                    response.put("code", 1);
                    response.message(message);
                    return response;
                }

                // 商品详情
                Map productDetail = this.productService.findProductDetail(productSpec.getProductId(), user);

                Map<String, Object> newProductDetail = new HashMap<>();

                // 所属店铺ID
                newProductDetail.put("shopId", productDetail.get("shopId"));

                // 所属店铺名称
                newProductDetail.put("shopName", productDetail.get("shopName"));

                // 商品ID
                newProductDetail.put("productId", productSpec.getProductId());

                // 商品规格ID
                newProductDetail.put("productSpecId", productSpec.getId());

                // 商品名称
                newProductDetail.put("productName", productDetail.get("productName"));

                // 商品简介
                newProductDetail.put("productDes", productDetail.get("productDes"));

                // 商品图片
                newProductDetail.put("productImg", productDetail.get("productImg"));
                newProductDetail.put("imgUrlList", productDetail.get("imgUrlList"));

                // 是否已关注
                newProductDetail.put("followFlag", productDetail.get("followFlag"));

                // 任务线最大数量
                newProductDetail.put("maxTaskNumber", productDetail.get("maxTaskNumber"));

                // 返还金额
                newProductDetail.put("buyerReturnAmt", productDetail.get("buyerReturnAmt"));

                // 快递邮费
                newProductDetail.put("expressFee", productDetail.get("expressFee"));

                // 商品规格ID
                newProductDetail.put("productSpecId", productSpec.getId());

                // 商品规格名称
                newProductDetail.put("productSpecName", productSpec.getProductSpecValueName());

                // 商品规格价格
                newProductDetail.put("productPrice", productSpec.getProductPrice());

                // 商品规格划线价格
                newProductDetail.put("scribingPrice", productSpec.getScribingPrice());

                // 商品数量
                newProductDetail.put("productNumber", productNumber);

                productList.add(newProductDetail);
            }

            // 根据店铺ID 排序
            Collections.sort(productList, new Comparator<Map<String, Object>>() {
                public int compare(Map<String, Object> product1, Map<String, Object> product2) {
                    return ((String)product1.get("shopId")).compareTo((String)product2.get("shopId"));
                }
            });

            // 店铺ID
            String shopId = "";
            List<Map> orderList = new ArrayList();

            Map<String, Object> orderMap = new HashMap<>();

            List<Map> orderProductList = new ArrayList();

            // 商品合计金额
            BigDecimal totalProductAmt = new BigDecimal(0);

            // 订单买家返还金额
            BigDecimal orderReturnAmt = new BigDecimal(0);

            // 买家总返还金额
            BigDecimal totalReturnAmt = new BigDecimal(0);

            // 订单快递费用
            BigDecimal orderExpressAmt = new BigDecimal(0);

            // 快递总费用
            BigDecimal totalExpressAmt = new BigDecimal(0);

            // 订单赠送拆豆
            int orderBeanCnt = 0;

            // 赠送拆豆总数
            int totalBeanCnt = 0;

            SParams params = this.paramsService.queryBykeyForOne("product_bean_cnt");
            Integer productBeanCnt = Integer.valueOf(params.getPValue());

            // 拆单显示（不同的商户 显示到不同的订单）
            for (Map productMap : productList) {

                // 根据顺序，店铺不同时
                if (!shopId.equals(productMap.get("shopId"))) {

                    if (!"".equals(shopId)) {
                        // 商家订单商品列表
                        orderMap.put("orderProduct", orderProductList);
                        // 商家订单返还金额
                        orderMap.put("orderReturnAmt", orderReturnAmt);
                        // 商家订单快递费用
                        orderMap.put("orderExpressAmt", orderExpressAmt);
                        // 商家赠送拆豆数量
                        orderMap.put("orderBeanCnt", orderBeanCnt);

                        // 返优惠券名称
                        STaskCoupon taskCoupon = this.taskCouponService.findReturnTaskCoupon();
                        if (taskCoupon != null) {
                            orderMap.put("couponReturnName", taskCoupon.getCouponName());
                        }

                        orderList.add(orderMap);
                    }

                    orderMap = new HashMap<>();
                    orderMap.put("shopId", productMap.get("shopId"));
                    orderMap.put("shopName", productMap.get("shopName"));

                    orderProductList = new ArrayList();
                    orderProductList.add(productMap);

                    // 商家订单返还金额
                    orderReturnAmt = new BigDecimal(productMap.get("buyerReturnAmt").toString());
                    // 商家订单快递费用
                    orderExpressAmt = new BigDecimal(productMap.get("expressFee").toString());
                    // 商家订单赠送拆豆
                    orderBeanCnt =  (int) productMap.get("productNumber") * productBeanCnt;

                } else {

                    // 商家订单返还金额
                    orderReturnAmt = orderReturnAmt.add( (new BigDecimal(productMap.get("buyerReturnAmt").toString())).multiply(new BigDecimal(productMap.get("productNumber").toString())) );
                    // 商家订单快递费用
                    orderExpressAmt = orderExpressAmt.add( (new BigDecimal(productMap.get("expressFee").toString())).multiply(new BigDecimal(productMap.get("productNumber").toString())) );
                    // 商家订单赠送拆豆
                    orderBeanCnt = orderBeanCnt + (int) productMap.get("productNumber") * productBeanCnt;

                    orderProductList.add(productMap);
                }

                // 店铺ID
                shopId = String.valueOf(productMap.get("shopId"));

                // 商品合计金额
                totalProductAmt = totalProductAmt.add( (new BigDecimal(productMap.get("productPrice").toString())).multiply(new BigDecimal(productMap.get("productNumber").toString())) );

                // 总订单返还金额
                totalReturnAmt = totalReturnAmt.add( (new BigDecimal(productMap.get("buyerReturnAmt").toString())).multiply(new BigDecimal(productMap.get("productNumber").toString())) );

                // 总订单快递费用
                totalExpressAmt = totalExpressAmt.add( (new BigDecimal(productMap.get("expressFee").toString())).multiply(new BigDecimal(productMap.get("productNumber").toString())) );

                // 赠送拆豆总数 (数量 * 8)
                totalBeanCnt = totalBeanCnt +  (int) productMap.get("productNumber") * productBeanCnt;
            }
            orderMap.put("orderProduct", orderProductList);
            // 商家订单返还金额
            orderMap.put("orderReturnAmt", orderReturnAmt);
            // 商家订单快递费用
            orderMap.put("orderExpressAmt", orderExpressAmt);
            // 商家赠送拆豆数量
            orderMap.put("orderBeanCnt", orderBeanCnt);
            // 返优惠券名称
            STaskCoupon taskCoupon = this.taskCouponService.findReturnTaskCoupon();
            if (taskCoupon != null) {
                orderMap.put("couponReturnName", taskCoupon.getCouponName());
            }

            orderList.add(orderMap);

            // {"confirmOrder": { { "shopId": "11", "shopName": "**店", "orderProduct": {  }  }}
            // 确认订单列表
            resultMap.put("shopOrder", orderList);

            // 商品合计金额
            resultMap.put("totalProductAmt", totalProductAmt);

            // 总订单返还金额
            resultMap.put("totalReturnAmt", totalReturnAmt);

            // 总订单快递费用
            resultMap.put("totalExpressAmt", totalExpressAmt);

            // 赠送拆豆总数
            resultMap.put("totalBeanCnt", totalBeanCnt);

            // 返优惠券（张）
            resultMap.put("totalReturnCouponCnt", orderList.size());

            // 收货地址
            if (StringUtils.isNotBlank(addressId)) {
                SUserAddress userAddress = userAddressService.getById(addressId);
                resultMap.put("userAddress", userAddress);
            } else {
                SUserAddress userAddress = new SUserAddress();
                userAddress.setUserId(user.getId());
                resultMap.put("userAddress", userAddressService.findUserAddress(userAddress));
            }

            response.data(resultMap);

        } catch (Exception e) {
            message = "用户确认订单失败";
            response.put("code", 1);
            response.message(message);
            log.error(e.getMessage());
        }

        return response;
    }

    /**
     * 确认支付购买订单
     */
    @Log("确认支付购买订单")
    @Transactional
    @PostMapping("/payOrder")
    public FebsResponse payOrder(HttpServletRequest request, @RequestBody String jsonString) {

//        {
//            "orderDetailId": "1",  默认nul  待支付订单中 去支付 才会传此值 如传递订单明细ID 只可能一个店铺
//            "addressId": "1",
//            "paymentType": 1,
//            "shopOrder": [
//            {
//                "shopId": "1",
//                "orderMessage": "221221",
//                "userCouponId": 1,
//                "orderProduct": [
//                    {
//                    "productNumber": 2,
//                    "productSpecId": 1
//                    }
//                ]
//            }
//            ]
//        }

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {

            SUser user = FebsUtil.getCurrentUser();

            JSONObject json = JSON.parseObject(jsonString);

            // 订单明细ID  如果 orderDetailId 不为Null s_order_detail表做更新，s_order和s_user_pay重新生成
            String orderDetailId = json.getString("orderDetailId");

            // 收货地址
            String addressId = json.getString("addressId");
            SUserAddress userAddress = new SUserAddress();
            userAddress.setUserId(user.getId());
            userAddress.setId(addressId);
            userAddress = this.userAddressService.findUserAddress(userAddress);

            // 支付方式  0 微信 1 支付宝 3 余额
            int paymentType = json.getIntValue("paymentType");

            // 店铺ID
            JSONArray confirmOrder = json.getJSONArray("shopOrder");

            // 生成批量订单表
            SOrder order = new SOrder();
            order.setUserId(user.getId());
            order.setCreateTime(new Date());
            // 付款状态: 0 锁定（支付中）1 已支付 2 待支付 3 不支付（取消或过期）9 已结算到冻结
            order.setPaymentState(2);
            order.setPaymentTime(new Date());
            // 实付金额
            order.setPayAmount(null);
            order.setPaymentType(paymentType);
            order.setChannel(2);
            order = this.orderService.addOrder(order);

            BigDecimal totalPayAmount = new BigDecimal(0);
            BigDecimal totalCouponAmount = new BigDecimal(0);
            BigDecimal totalShippingFee = new BigDecimal(0);

            for(int i=0; i<confirmOrder.size(); i++){

                BigDecimal orderAmount = new BigDecimal(0);
                BigDecimal shippingFee = new BigDecimal(0);
                BigDecimal payAmount = new BigDecimal(0);
                BigDecimal couponAmount = new BigDecimal(0);

                JSONObject confirmOrderJson = confirmOrder.getJSONObject(i);

                // 店铺ID
                String shopId = confirmOrderJson.getString("shopId");

                // 店铺订单
                JSONArray orderProduct = confirmOrderJson.getJSONArray("orderProduct");

                // 订单留言
                String orderMessage = confirmOrderJson.getString("orderMessage");

                // 生成订单明细表
                SOrderDetail orderDetail = new SOrderDetail();

                if (orderDetailId == null) {

                    orderDetail.setOrderId(order.getId());
                    orderDetail.setShopId(shopId);
                    orderDetail.setUserId(user.getId());
                    orderDetail.setOrderSn("O" + System.currentTimeMillis());
                    orderDetail.setPaymentType(paymentType);
                    // 付款状态: 0 锁定（支付中）1 已支付 2 待支付 3 不支付（取消或过期）9 已结算到冻结
                    orderDetail.setPaymentState(2);
                    orderDetail.setPaymentTime(new Date());
                    orderDetail.setAddressId(addressId);
                    orderDetail.setOrderMessage(orderMessage);
                    orderDetail.setOrderStatus(0);
                    orderDetail.setAddressName(userAddress.getTrueName());
                    orderDetail.setAddressPhone(userAddress.getTelPhone());
                    orderDetail.setAddressDetail(userAddress.getProvinceName() + userAddress.getCityName() + userAddress.getAreaName() + userAddress.getAreaInfo());
                    orderDetail.setChannel(2);
                    orderDetail.setCreateTime(new Date());

                    orderDetail = this.orderDetailService.addOrderDetail(orderDetail);

                    for(int j=0; j<orderProduct.size(); j++){

                        JSONObject orderProductJson = orderProduct.getJSONObject(j);

                        // 商品数量
                        int productNumber = orderProductJson.getIntValue("productNumber");

                        // 商品规格ID
                        String productSpecId = orderProductJson.getString("productSpecId");

                        SProductSpec productSpec = this.productSpecService.findProductSpec(productSpecId);

                        SProduct product = this.productService.getById(productSpec.getProductId());

                        // 生成订单产品表
                        SOrderProduct orderProductInsert = new SOrderProduct();

                        orderProductInsert.setUserId(user.getId());
                        orderProductInsert.setOrderDetailId(orderDetail.getId());
                        orderProductInsert.setProductId(productSpec.getProductId());
                        orderProductInsert.setProductSpecId(productSpec.getId());
                        orderProductInsert.setProductSpecValueName(productSpec.getProductSpecValueName());
                        orderProductInsert.setProductNumber(productNumber);
                        orderProductInsert.setProductPrice(productSpec.getProductPrice());
                        orderProductInsert.setScribingPrice(productSpec.getScribingPrice());
                        orderProductInsert.setTotalReward(product.getTotalReward());
                        orderProductInsert.setTaskPrice(product.getTaskPrice());
                        orderProductInsert.setProductName(product.getProductName());
                        orderProductInsert.setProductImg(product.getProductImg());
                        orderProductInsert.setProductDes(product.getProductDes());
                        orderProductInsert.setProductDetail(product.getProductDetail());
                        orderProductInsert.setCreateTime(new Date());

                        this.orderProductService.addOrderProduct(orderProductInsert);

                        orderAmount = orderAmount.add( productSpec.getProductPrice().multiply(new BigDecimal(productNumber)));
                        shippingFee = shippingFee.add(product.getExpressFee());

                        // 如果是从购物车中购买的话，购物车中商品删除
                        SUserShopCar userShopCar = new SUserShopCar();
                        userShopCar.setUserId(user.getId());
                        userShopCar.setProductSpecId(productSpecId);
                        this.userShopCarService.deleteUserShopCar(userShopCar);
                    }

                } else {

                    // 订单列表 去支付
                    orderDetail = this.orderDetailService.getById(orderDetailId);

                    // 重新再设定订单ID
                    orderDetail.setOrderId(order.getId());

                    orderAmount = orderDetail.getOrderAmount();
                    shippingFee = orderDetail.getShippingFee();
                }

                // 用户选用优惠券ID
                String userCouponId = confirmOrderJson.getString("userCouponId");

                if (userCouponId != null && !userCouponId.equals("")) {
                    SUserCoupon userCoupon = this.userCouponService.findUserCoupon(user.getId(), userCouponId);
                    if (userCoupon != null) {
                        SShopCoupon shopCoupon = this.shopCouponService.getById(userCoupon.getCouponId());

                        // 优惠金额总计
                        couponAmount = shopCoupon.getCouponAmount();

                        // 满减条件判断
                        if (shopCoupon.getUseCon() == 1) {
                            if (orderAmount.compareTo(shopCoupon.getMinConsumeAmount()) < 0 ) {
                                message = "没有达到最低消费金额，不能使用此优惠券！";
                                response.put("code", 1);
                                response.message(message);
                                return response;
                            }
                        }
                    }
                }

                // 应付金额
                payAmount = orderAmount.subtract(shippingFee).subtract(couponAmount);

                // 应付金额
                orderDetail.setOrderAmount(orderAmount);
                // 用户优惠券ID
                orderDetail.setUserCouponId(userCouponId);
                // 优惠金额
                orderDetail.setCouponAmount(couponAmount);
                // 运费
                orderDetail.setShippingFee(shippingFee);
                // 实付金额
                orderDetail.setPayAmount(payAmount);

                this.orderDetailService.saveOrUpdate(orderDetail);

                totalPayAmount = totalPayAmount.add(payAmount);
                totalCouponAmount = totalCouponAmount.add(couponAmount);
                totalShippingFee = totalShippingFee.add(shippingFee);
            }

            // 实付金额
            order.setPayAmount(totalPayAmount);
            this.orderService.saveOrUpdate(order);

            // 调起微信支付
            JSONObject jsonObject = weChatPayUtil.weChatPay(order.getId(),
                    totalPayAmount.toString(),
                    user.getOpenId(),
                    request.getRemoteAddr(),
                    "2",
                    "购买商品");

            response.data(jsonObject);

        } catch (Exception e) {
            message = "购买订单失败";
            response.put("code", 1);
            response.message(message);
            log.error( e.getMessage());
        }

        return response;
    }

    /**
     * 根据用户ID取得我的购买订单列表信息
     * @return List<SOrder>
     */
    @PostMapping("/getOrderList")
    @Limit(key = "getOrderList", period = 60, count = 20, name = "检索我的购买订单接口", prefix = "limit")
    public FebsResponse getOrderList(QueryRequest queryRequest, String status) {

        FebsResponse response = new FebsResponse();

        SUser user = FebsUtil.getCurrentUser();

        Map<String, Object> orderPageList = getDataTable(
                this.orderService.queryPage(queryRequest, user.getId(), status));

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
    public FebsResponse getOrderDetail(String orderDetailId) {

        FebsResponse response = new FebsResponse();

        if (orderDetailId == null) {
            message = "请指定订单！";
            response.put("code", 1);
            response.message(message);
            return response;
        }

        Map<String, Object> orderDetail = this.orderService.queryOrderDetail(orderDetailId);

        response.put("code", 0);
        response.data(orderDetail);
        return response;
    }

    /**
     * 确认收货
     * 用户ID 订单ID 状态
     */
    @Log("用户确认收货")
    @Transactional
    @PostMapping("/confirmFinishOrder")
    public FebsResponse confirmFinishOrder(@Valid String orderDetailId) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {

            SUser user = FebsUtil.getCurrentUser();

            SOrderDetail orderDetail = new SOrderDetail();
            orderDetail = this.orderDetailService.getById(orderDetailId);
            orderDetail.setUserId(user.getId());
           

            if (orderDetail.getPaymentState() != 1 && orderDetail.getPaymentState() != 9) {
                message = "此订单还没有完成支付！";
                response.put("code", 1);
                response.message(message);
                return response;
            }

            if (orderDetail.getOrderStatus() != 2) {
                message = "此订单还没有完成发货！";
                response.put("code", 1);
                response.message(message);
                return response;
            }
            // 确认收货
            orderDetail.setOrderStatus(3);
            this.orderDetailService.updateById(orderDetail);

            // 任务线状态修改为 结算状态  【2： 已分润】 用户任务线状态 修改为 【5 佣金已入账】
            List<STaskLine> updateTaskLineList = Lists.newArrayList();
            List<SUserTaskLine> updateUserTaskLineList = Lists.newArrayList();
            List<SOrderProduct> orderProductList = this.orderProductService.findOrderProductList(orderDetail.getId());
            for (SOrderProduct orderProduct : orderProductList) {

                STaskLine taskLine = new STaskLine();
                taskLine.setOrderProductId(orderProduct.getId());
                List<STaskLine> taskLineList = this.taskLineService.findTaskLineList(taskLine);

                for (STaskLine taskLineOne : taskLineList) {
                    taskLineOne.setSettleStatus(2);
                    taskLineOne.setUpdateTime(new Date());
                    updateTaskLineList.add(taskLineOne);

                    // 用户任务线状态 修改为 【5 佣金已入账】
                    SUserTaskLine userTaskLine = new SUserTaskLine();
                    userTaskLine.setTaskLineId(taskLineOne.getId());
                    List<SUserTaskLine> userTaskLineList = this.userTaskLineService.findUserTaskLineList(userTaskLine);

                    for (SUserTaskLine userTaskLineOne : userTaskLineList) {
                        userTaskLineOne.setStatus(5);
                        userTaskLineOne.setUpdateTime(new Date());
                        updateUserTaskLineList.add(userTaskLineOne);
                    }

                }
            }
            if(updateTaskLineList!=null&&updateTaskLineList.size()>0)
            this.taskLineService.updateBatchById(updateTaskLineList);
            if(updateUserTaskLineList!=null&&updateUserTaskLineList.size()>0)
            this.userTaskLineService.updateBatchById(updateUserTaskLineList);

            // 冻结金额 -> 余额
            List<SUserBonusLog> updateUserBonusLogList = Lists.newArrayList();
            List<SUserAmountLog> saveUserAmountLogList = Lists.newArrayList();

            String buyUserName = "";
            BigDecimal buyBonusAmt = new BigDecimal(0);
            BigDecimal taskBonusAmt = new BigDecimal(0);

            SUserBonusLog userBonusLog = new SUserBonusLog();
            userBonusLog.setOrderDetailId(orderDetail.getId());
            List<SUserBonusLog> userBonusLogList = this.userBonusLogService.findUserBonusList(userBonusLog);

            for (SUserBonusLog userBonusLogOne : userBonusLogList) {

                SUser userOne = this.userService.getById(userBonusLogOne.getUserId());

                // 金额流水插入
                SUserAmountLog userAmountLog = new SUserAmountLog();
                userAmountLog.setUserId(userBonusLogOne.getUserId());

                // 类型 1-买家独赢;2-任务躺赢;3-横向躺赢;4-纵向躺赢;5-平台返回任务金;
                // 变动类型 3 买家独赢 31 纵向躺赢 4 任务躺赢 41 横向躺赢 9 任务解冻金额
                if (userBonusLogOne.getBonusType() == 1) {
                    userAmountLog.setChangeType(3);

                    // 买家
                    buyUserName = userOne.getNickName();
                    buyBonusAmt = buyBonusAmt.add(userBonusLogOne.getBonusAmount());

                } else if (userBonusLogOne.getBonusType() == 2) {
                    userAmountLog.setChangeType(4);
                    taskBonusAmt = taskBonusAmt.add(userBonusLogOne.getBonusAmount());
                } else if (userBonusLogOne.getBonusType() == 3) {
                    userAmountLog.setChangeType(41);
                    taskBonusAmt = taskBonusAmt.add(userBonusLogOne.getBonusAmount());
                } else if (userBonusLogOne.getBonusType() == 4) {
                    userAmountLog.setChangeType(31);
                    taskBonusAmt = taskBonusAmt.add(userBonusLogOne.getBonusAmount());
                } else if (userBonusLogOne.getBonusType() == 5) {
                    userAmountLog.setChangeType(9);
                }

                userAmountLog.setChangeAmount(userBonusLogOne.getBonusAmount());
                userAmountLog.setChangeTime(new Date());
                userAmountLog.setRelationId(userBonusLogOne.getOrderDetailId());
                userAmountLog.setRemark("关联购买订单ID");
                userAmountLog.setOldAmount(userOne.getTotalAmount());

                saveUserAmountLogList.add(userAmountLog);

                // 冻结-
                userOne.setLockAmount(userOne.getLockAmount().subtract(userBonusLogOne.getBonusAmount()));
                // 余额+
                userOne.setTotalAmount(userOne.getTotalAmount().add(userBonusLogOne.getBonusAmount()));
                this.userService.updateById(userOne);

                // 结算完成
                userBonusLogOne.setStatus(1);
                userBonusLogOne.setUpdateTime(new Date());
                updateUserBonusLogList.add(userBonusLogOne);
            }
            if(updateUserBonusLogList!=null&&updateUserBonusLogList.size()>0)
            this.userBonusLogService.updateBatchById(updateUserBonusLogList);
            if(saveUserAmountLogList!=null&&saveUserAmountLogList.size()>0)
            this.userAmountLogService.saveBatch(saveUserAmountLogList);

            SUserMsg userMsg = new SUserMsg();
            userMsg.setUserId(null);
            userMsg.setMsgTime(new Date());
            userMsg.setMsgType(0);
            userMsg.setStatus(0);
            userMsg.setMsgTitle("恭喜" + buyUserName + "独赢" + buyBonusAmt + "元，其他人分配躺赢奖金" + taskBonusAmt + "元。");
            userMsg.setMsgInfo("恭喜" + buyUserName + "独赢" + buyBonusAmt + "元，其他人分配躺赢奖金" + taskBonusAmt + "元。");

            userMsgService.save(userMsg);

        } catch (Exception e) {
            message = "更新用户购买订单状态失败";
            response.put("code", 1);
            response.message(message);
            log.error( e.getMessage());
        }

        return response;
    }

    /**
     * 订单支付成功后计算奖励金额到冻结
     * 2分钟执行一次 (支付成功时间超过5分钟的订单处理)
     */
    @Scheduled(cron = "0 */2 * * * ?")
    @Transactional
    public void orderPaySuccessTask() {

        // 支付成功超过5分钟订单
        List<SOrder> orderPaySuccessList = this.orderService.findOrderPaySuccessList();

        // 买家立返佣金比例 （后续调整到Redis缓存读取）
        SParams params = new SParams();
        params = this.paramsService.queryBykeyForOne("buyer_rate");
        BigDecimal buyerRate = BigDecimal.valueOf(Double.parseDouble(params.getPValue()));
        // 同组任务躺赢佣金比例
        params = this.paramsService.queryBykeyForOne("same_group_rate");
        BigDecimal sameGroupRate = BigDecimal.valueOf(Double.parseDouble(params.getPValue()));
        // 横向上级躺赢佣金比例
        params = this.paramsService.queryBykeyForOne("upper_horizontal_rate");
        BigDecimal upperHorizontalRate = BigDecimal.valueOf(Double.parseDouble(params.getPValue()));
        // 纵向上级1躺赢佣金比例
        params = this.paramsService.queryBykeyForOne("upper_vertical1_rate1");
        BigDecimal upperVertical1Rate1 = BigDecimal.valueOf(Double.parseDouble(params.getPValue()));
        // 纵向上级2躺赢佣金比例
        params = this.paramsService.queryBykeyForOne("upper_vertical1_rate2");
        BigDecimal upperVertical1Rate2 = BigDecimal.valueOf(Double.parseDouble(params.getPValue()));
        // 纵向上级3躺赢佣金比例
        params = this.paramsService.queryBykeyForOne("upper_vertical1_rate3");
        BigDecimal upperVertical1Rate3 = BigDecimal.valueOf(Double.parseDouble(params.getPValue()));
        // 纵向上级0躺赢佣金比例
        params = this.paramsService.queryBykeyForOne("upper_vertical1_rate0");
        BigDecimal upperVertical1Rate0 = BigDecimal.valueOf(Double.parseDouble(params.getPValue()));

        for (SOrder order : orderPaySuccessList) {

            // 变更订单状态 （9:已结算到冻结）
            order.setPaymentState(9);
            this.orderService.updateById(order);

            SOrderDetail orderDetail = new SOrderDetail();
            orderDetail.setOrderId(order.getId());
            List<SOrderDetail> orderDetailPaySuccessList = this.orderDetailService.findOrderDetailList(orderDetail);

            SUser userLock = new SUser();
            for (SOrderDetail orderDetailPaySuccess : orderDetailPaySuccessList) {

                // 变更订单明细状态 （9:已结算到冻结）
                orderDetailPaySuccess.setPaymentState(9);
                this.orderDetailService.updateById(orderDetailPaySuccess);

                List<SOrderProduct> orderProductList = this.orderProductService.findOrderProductList(orderDetailPaySuccess.getId());

                for (SOrderProduct orderProduct : orderProductList) {

                    // 根据任务线中的订单商品ID，检索任务线表（s_task_line）
                    STaskLine taskLine = new STaskLine();
                    taskLine.setOrderProductId(orderProduct.getId());
                    List<STaskLine> taskLineList = this.taskLineService.findTaskLineList(taskLine);

                    for (STaskLine taskLineOne : taskLineList) {

                        // 独赢（买家立返）20%
                        SUserBonusLog userBonusLog = new SUserBonusLog();
                        userBonusLog.setUserId(orderProduct.getUserId());
                        userBonusLog.setOrderDetailId(orderDetailPaySuccess.getId());
                        userBonusLog.setTaskLineId(taskLineOne.getId());
                        userBonusLog.setUserTaskLineId(null);
                        userBonusLog.setProductId(orderProduct.getProductId());
                        userBonusLog.setBonusType(1);
                        userBonusLog.setBonusAmount(orderProduct.getTotalReward().multiply(buyerRate));
                        userBonusLog.setCreateTime(new Date());
                        userBonusLog.setUpdateTime(new Date());
                        userBonusLog.setStatus(0);
                        this.userBonusLogService.save(userBonusLog);

                        // 奖励金额到冻结（冻结+）
                        userLock = this.userService.getById(userBonusLog.getUserId());
                        userLock.setLockAmount(userLock.getLockAmount().add(userBonusLog.getBonusAmount()));
                        this.userService.updateById(userLock);

                        // 纵向躺赢 从买家的捆绑关系朝上查，上三级3个人按（8-5-2）分15%  若无上级 找预备队 5%
                        SUser userMe = this.userService.getById(orderProduct.getUserId());

                        // 有上级的情况下
                        if (userMe.getParentId() != null) {

                            // 上一级用户
                            SUser userSuperOne = this.userService.getById(userMe.getParentId());

                            userBonusLog = new SUserBonusLog();
                            userBonusLog.setOrderDetailId(orderDetailPaySuccess.getId());
                            userBonusLog.setTaskLineId(taskLineOne.getId());
                            userBonusLog.setUserTaskLineId(null);
                            userBonusLog.setProductId(orderProduct.getProductId());
                            userBonusLog.setCreateTime(new Date());
                            userBonusLog.setUpdateTime(new Date());
                            userBonusLog.setStatus(0);
                            userBonusLog.setUserId(userSuperOne.getId());
                            userBonusLog.setBonusType(4);
                            userBonusLog.setBonusAmount(orderProduct.getTotalReward().multiply(upperVertical1Rate1));

                            this.userBonusLogService.save(userBonusLog);

                            // 奖励金额到冻结（冻结+）
                            userSuperOne.setLockAmount(userSuperOne.getLockAmount().add(userBonusLog.getBonusAmount()));
                            this.userService.updateById(userLock);

                            if (userSuperOne.getParentId() != null) {

                                // 上二级用户
                                SUser userSuperTwo = this.userService.getById(userSuperOne.getParentId());

                                userBonusLog = new SUserBonusLog();
                                userBonusLog.setOrderDetailId(orderDetailPaySuccess.getId());
                                userBonusLog.setTaskLineId(taskLineOne.getId());
                                userBonusLog.setUserTaskLineId(null);
                                userBonusLog.setProductId(orderProduct.getProductId());
                                userBonusLog.setCreateTime(new Date());
                                userBonusLog.setUpdateTime(new Date());
                                userBonusLog.setStatus(0);
                                userBonusLog.setUserId(userSuperTwo.getId());
                                userBonusLog.setBonusType(4);
                                userBonusLog.setBonusAmount(orderProduct.getTotalReward().multiply(upperVertical1Rate2));
                                this.userBonusLogService.save(userBonusLog);

                                // 奖励金额到冻结（冻结+）
                                userSuperTwo.setLockAmount(userSuperTwo.getLockAmount().add(userBonusLog.getBonusAmount()));
                                this.userService.updateById(userSuperTwo);

                                if (userSuperTwo.getParentId() != null) {

                                    // 上三级用户
                                    SUser userSuperThree = this.userService.getById(userSuperTwo.getParentId());

                                    userBonusLog = new SUserBonusLog();
                                    userBonusLog.setOrderDetailId(orderDetailPaySuccess.getId());
                                    userBonusLog.setTaskLineId(taskLineOne.getId());
                                    userBonusLog.setUserTaskLineId(null);
                                    userBonusLog.setProductId(orderProduct.getProductId());
                                    userBonusLog.setCreateTime(new Date());
                                    userBonusLog.setUpdateTime(new Date());
                                    userBonusLog.setStatus(0);
                                    userBonusLog.setUserId(userSuperThree.getId());
                                    userBonusLog.setBonusType(4);
                                    userBonusLog.setBonusAmount(orderProduct.getTotalReward().multiply(upperVertical1Rate3));
                                    this.userBonusLogService.save(userBonusLog);

                                    // 奖励金额到冻结（冻结+）
                                    userSuperThree.setLockAmount(userSuperThree.getLockAmount().add(userBonusLog.getBonusAmount()));
                                    this.userService.updateById(userSuperThree);
                                }
                            }
                        } else {

                            // 无上级的情况下 则查看u最后一次是谁的预备队（假设是A某），则给A某分配5%
                            SUserRelation userRelation = new SUserRelation();
                            userRelation.setRelationType(0);
                            userRelation.setUnionId(userMe.getUnionId());
                            SUserRelation userRelationOne = this.userRelationService.findUserRelation(userRelation);
                            if (userRelationOne != null) {

                                userBonusLog = new SUserBonusLog();
                                userBonusLog.setOrderDetailId(orderDetailPaySuccess.getId());
                                userBonusLog.setTaskLineId(taskLineOne.getId());
                                userBonusLog.setUserTaskLineId(null);
                                userBonusLog.setProductId(orderProduct.getProductId());
                                userBonusLog.setCreateTime(new Date());
                                userBonusLog.setUpdateTime(new Date());
                                userBonusLog.setStatus(0);
                                userBonusLog.setUserId(userRelationOne.getParentId());
                                userBonusLog.setBonusType(4);
                                userBonusLog.setBonusAmount(orderProduct.getTotalReward().multiply(upperVertical1Rate0));
                                this.userBonusLogService.save(userBonusLog);

                                // 奖励金额到冻结（冻结+）
                                userLock = this.userService.getById(userBonusLog.getUserId());
                                userLock.setLockAmount(userLock.getLockAmount().add(userBonusLog.getBonusAmount()));
                                this.userService.updateById(userLock);
                            }
                        }

                        // 根据任务线ID 检索 用户任务线
                        SUserTaskLine userTaskLine = new SUserTaskLine();
                        userTaskLine.setTaskLineId(taskLineOne.getId());
                        List<SUserTaskLine> userTaskLineList = this.userTaskLineService.findUserTaskLineList(userTaskLine);

                        // 同组任务线上的拆家（X人）均分
                        BigDecimal sameGroup = new BigDecimal(0);
                        if (userTaskLineList.size() != 0) {
                            sameGroup = orderProduct.getTotalReward().multiply(sameGroupRate).divide(
                                    new BigDecimal(userTaskLineList.size()), 2, BigDecimal.ROUND_HALF_UP);
                        }

                        int taskUserSuperCnt = 0;
                        // 计算同组任务线上的每个拆家对应的上级个数（横向上级）
                        for (SUserTaskLine userTaskLineOne : userTaskLineList) {
                            SUser taskUser = this.userService.getById(userTaskLineOne.getUserId());
                            if (taskUser.getParentId() != null) {
                                taskUserSuperCnt = taskUserSuperCnt + 1;
                            }
                        }

                        BigDecimal upperHorizontal = new BigDecimal(0);
                        if (taskUserSuperCnt != 0) {
                            upperHorizontal = orderProduct.getTotalReward().multiply(upperHorizontalRate).divide(
                                    new BigDecimal(taskUserSuperCnt), 2, BigDecimal.ROUND_HALF_UP);
                        }

                        for (SUserTaskLine userTaskLineOne : userTaskLineList) {

                            // 变更用户任务线表状态（3 -> 4 佣金结算完成待入账）
                            userTaskLineOne.setStatus(4);
                            userTaskLineOne.setUpdateTime(new Date());
                            this.userTaskLineService.updateById(userTaskLineOne);

                            // 任务躺赢 (同组任务线上的拆家（X人）均分40%)
                            userBonusLog = new SUserBonusLog();
                            userBonusLog.setUserId(userTaskLineOne.getUserId());
                            userBonusLog.setOrderDetailId(orderDetailPaySuccess.getId());
                            userBonusLog.setTaskLineId(taskLineOne.getId());
                            userBonusLog.setUserTaskLineId(userTaskLineOne.getId());
                            userBonusLog.setProductId(orderProduct.getProductId());
                            userBonusLog.setBonusType(2);
                            userBonusLog.setBonusAmount(sameGroup);
                            userBonusLog.setCreateTime(new Date());
                            userBonusLog.setUpdateTime(new Date());
                            userBonusLog.setStatus(0);
                            this.userBonusLogService.save(userBonusLog);

                            // 奖励金额到冻结（冻结+）
                            userLock = this.userService.getById(userBonusLog.getUserId());
                            userLock.setLockAmount(userLock.getLockAmount().add(userBonusLog.getBonusAmount()));
                            this.userService.updateById(userLock);

                            // 横向躺赢 同组任务线上的每个拆家对应的上级（Y人，Y<=X）均分25%
                            SUser taskUser = this.userService.getById(userTaskLineOne.getUserId());
                            if (taskUser.getParentId() != null) {

                                userBonusLog = new SUserBonusLog();
                                userBonusLog.setUserId(taskUser.getParentId());
                                userBonusLog.setOrderDetailId(orderDetailPaySuccess.getId());
                                userBonusLog.setTaskLineId(taskLineOne.getId());
                                userBonusLog.setUserTaskLineId(userTaskLineOne.getId());
                                userBonusLog.setProductId(orderProduct.getProductId());
                                userBonusLog.setBonusType(3);
                                userBonusLog.setBonusAmount(upperHorizontal);
                                userBonusLog.setCreateTime(new Date());
                                userBonusLog.setUpdateTime(new Date());
                                userBonusLog.setStatus(0);
                                this.userBonusLogService.save(userBonusLog);

                                // 奖励金额到冻结（冻结+）
                                userLock = this.userService.getById(userBonusLog.getUserId());
                                userLock.setLockAmount(userLock.getLockAmount().add(userBonusLog.getBonusAmount()));
                                this.userService.updateById(userLock);
                            }

                            // 平台返回任务金
                            userBonusLog = new SUserBonusLog();
                            userBonusLog.setUserId(userTaskLineOne.getUserId());
                            userBonusLog.setOrderDetailId(orderDetailPaySuccess.getId());
                            userBonusLog.setTaskLineId(taskLineOne.getId());
                            userBonusLog.setUserTaskLineId(userTaskLineOne.getId());
                            userBonusLog.setProductId(orderProduct.getProductId());
                            userBonusLog.setBonusType(5);
                            userBonusLog.setBonusAmount(userTaskLineOne.getPayAmount());
                            userBonusLog.setCreateTime(new Date());
                            userBonusLog.setUpdateTime(new Date());
                            userBonusLog.setStatus(0);
                            this.userBonusLogService.save(userBonusLog);

                            // 奖励金额到冻结（冻结+）
                            userLock = this.userService.getById(userBonusLog.getUserId());
                            userLock.setLockAmount(userLock.getLockAmount().add(userBonusLog.getBonusAmount()));
                            this.userService.updateById(userLock);
                        }
                    }
                }
            }
        }
    }

    @PostMapping("/cancleOrder")
    @Limit(key = "cancleOrder", period = 60, count = 20, name = "取消订单接口", prefix = "limit")
    public FebsResponse cancleOrder(@NotEmpty(message="订单id不可为空") String orderDetailId) {

        FebsResponse response = new FebsResponse();
        SOrderDetail orderDetail = orderDetailService.getById(orderDetailId);

        if (orderDetail == null) {
            response.put("code", 1);
            response.message("订单不存在");
            return response;
        }

        if (orderDetail.getOrderStatus() != 0) {
            response.put("code", 1);
            response.message("订单当前状态不可取消");
            return response;
        }

        // 订单状态  0未付款  1已付款待发货  2已发货  3已确认收货 4 申请退货退款 5 已退货退款 6 超时关闭 -1 已取消
        orderDetail.setOrderStatus(-1);
        orderDetailService.updateById(orderDetail);
        response.put("code", 0);
        return response;
    }
}