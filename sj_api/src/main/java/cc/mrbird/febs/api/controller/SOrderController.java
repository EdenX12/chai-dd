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
import freemarker.template.utility.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.*;

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
    private ISOrderDetailService orderDetailService;

    @Autowired
    private ISOrderProductService orderProductService;

    @Autowired
    private ISProductService productService;

    @Autowired
    private ISProductSpecService productSpecService;

    @Autowired
    private ISUserAddressService userAddressService;

    @Autowired
    private ISTaskCouponService taskCouponService;

    @Autowired
    private ISUserCouponService userCouponService;

    @Autowired
    private ISShopCouponService shopCouponService;


    @Autowired
    private ISUserTaskService userTaskService;

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

    /**
     * 确认订单
     * 商品规格ID   productSpecId
     * 数量         productNumber
     */
    @Log("确认订单")
    @Transactional
    @PostMapping("/confirmOrder")
    public FebsResponse confirmOrder(List<Map> productSpecList) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        Map<String, Object> resultMap = new HashMap<>();

        try {

            SUser user = FebsUtil.getCurrentUser();

            List<Map<String, Object>> productList = new ArrayList<>();

            for (Map productSpecMap : productSpecList) {

                // 规格商品ID
                String productSpecId = (String) productSpecMap.get("productSpecId");

                // 商品数量
                int productNumber = (Integer) productSpecMap.get("productNumber");

                // 商品规格
                SProductSpec productSpec = this.productSpecService.findProductSpec(productSpecId);

                // 商品详情
                SProduct product = new SProduct();
                product.setId(productSpec.getProductId());
                Map productDetail = this.productService.findProductDetail(product);

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

                // 返还金额
                newProductDetail.put("buyerReturnAmt", productDetail.get("buyerReturnAmt"));

                // 快递邮费
                newProductDetail.put("expressFee", productDetail.get("expressFee"));

                // 商品规格
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
                    orderReturnAmt = (BigDecimal) productMap.get("buyerReturnAmt");
                    // 商家订单快递费用
                    orderExpressAmt = (BigDecimal) productMap.get("expressFee");
                    // 商家订单赠送拆豆
                    orderBeanCnt =  (int) productMap.get("productNumber") * 8;

                } else {

                    // 商家订单返还金额
                    orderReturnAmt = orderReturnAmt.add( ((BigDecimal) productMap.get("buyerReturnAmt")).multiply((BigDecimal) productMap.get("productNumber")) );
                    // 商家订单快递费用
                    orderExpressAmt = orderExpressAmt.add( ((BigDecimal) productMap.get("expressFee")).multiply((BigDecimal) productMap.get("productNumber")) );
                    // 商家订单赠送拆豆
                    orderBeanCnt = orderBeanCnt + (int) productMap.get("productNumber") * 8;

                    orderProductList.add(productMap);
                }

                // 店铺ID
                shopId = (String) productMap.get("shopId");

                // 商品合计金额
                totalProductAmt = totalProductAmt.add( ((BigDecimal) productMap.get("productPrice")).multiply((BigDecimal) productMap.get("productNumber")) );

                // 总订单返还金额
                totalReturnAmt = totalReturnAmt.add( ((BigDecimal) productMap.get("buyerReturnAmt")).multiply((BigDecimal) productMap.get("productNumber")) );

                // 总订单快递费用
                totalExpressAmt = totalExpressAmt.add( ((BigDecimal) productMap.get("expressFee")).multiply((BigDecimal) productMap.get("productNumber")) );

                // 赠送拆豆总数 (数量 * 8)
                totalBeanCnt = totalBeanCnt +  (int) productMap.get("productNumber") * 8;
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

            response.data(resultMap);

        } catch (Exception e) {
            message = "用户确认订单失败";
            response.put("code", 1);
            response.message(message);
            log.error(message, e);
        }

        return response;
    }

    /**
     * 确认支付购买订单
     */
    @Log("确认支付购买订单")
    @Transactional
    @PostMapping("/payOrder")
    public FebsResponse payOrder(HttpServletRequest request, String jsonString) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {

            SUser user = FebsUtil.getCurrentUser();

            JSONObject json = JSON.parseObject(jsonString);

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
            order.setPaymentState(0);
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

                JSONObject confirmOrderJson = confirmOrder.getJSONObject(i);

                // 店铺ID
                String shopId = confirmOrderJson.getString("shopId");

                // 店铺订单
                JSONArray orderProduct = confirmOrderJson.getJSONArray("orderProduct");

                // 订单留言
                String orderMessage = confirmOrderJson.getString("orderMessage");

                // 生成订单明细表
                SOrderDetail orderDetail = new SOrderDetail();
                orderDetail.setOrderId(order.getId());
                orderDetail.setShopId(shopId);
                orderDetail.setUserId(user.getId());
                orderDetail.setOrderSn("O" + System.currentTimeMillis());
                orderDetail.setPaymentType(paymentType);
                orderDetail.setPaymentState(0);
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

                BigDecimal orderAmount = new BigDecimal(0);
                BigDecimal shippingFee = new BigDecimal(0);
                BigDecimal payAmount = new BigDecimal(0);
                BigDecimal couponAmount = new BigDecimal(0);

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
                    orderProductInsert.setProductSpecValueName(productSpec.getProductSpecValueName());
                    orderProductInsert.setProductNumber(productNumber);
                    orderProductInsert.setProductPrice(productSpec.getProductPrice());
                    orderProductInsert.setScribingPrice(productSpec.getScribingPrice());
                    orderProductInsert.setProductName(product.getProductName());
                    orderProductInsert.setProductImg(product.getProductImg());
                    orderProductInsert.setProductDes(product.getProductDes());
                    orderProductInsert.setProductDetail(product.getProductDetail());
                    orderProductInsert.setCreateTime(new Date());

                    this.orderProductService.addOrderProduct(orderProductInsert);

                    orderAmount = orderAmount.add( productSpec.getProductPrice().multiply(new BigDecimal(productNumber)));
                    shippingFee = shippingFee.add(product.getExpressFee());

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
            JSONObject jsonObject = weChatPayUtil.weChatPay(String.valueOf(order.getId()),
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
    public FebsResponse getOrderList(QueryRequest queryRequest, String status) {

        FebsResponse response = new FebsResponse();

        SUser user = FebsUtil.getCurrentUser();

        Map<String, Object> orderPageList = getDataTable(this.orderService.queryPage(queryRequest, user.getId(), status));

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
    public FebsResponse confirmFinishOrder(@Valid SOrder order) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {

            SUser user = FebsUtil.getCurrentUser();
            order.setUserId(user.getId());

//            order.setOrderStatus(3);
//            order = this.orderService.updateOrder(order);
//
//            if (order.getPaymentState() != 1) {
//                message = "此订单还没有完成支付！";
//                response.put("code", 1);
//                response.message(message);
//                return response;
//            }
//
//            if (order.getOrderStatus() != 2) {
//                message = "此订单还没有完成发货！";
//                response.put("code", 1);
//                response.message(message);
//                return response;
//            }
//
//            SProduct product = productService.getById(order.getProductId());
//
//            // 产品下所有已完结 任务金退还（冻结 - > 余额）  并且修改状态为佣金已入账
//            SUserTask userTask = new SUserTask();
//            userTask.setProductId(order.getProductId());
//            userTask.setPayStatus(1);
//            userTask.setStatus(3);
//            List<SUserTask> userTaskList = userTaskService.findUserTaskList(userTask);
//            for (SUserTask userTasked : userTaskList) {
//
//                // 佣金已入账 状态更新
//                userTasked.setUpdateTime(new Date());
//                userTasked.setStatus(4);
//                this.userTaskService.updateById(userTasked);
//            }
//
//            SUserBonusLog userBonusLog = new SUserBonusLog();
//
//            userBonusLog.setOrderId(order.getId());
//
//            List<SUserBonusLog> userBonusLogList = userBonusLogService.findUserBonusList(userBonusLog);
//
//            // 独赢用户
//            String rewardUserName = "";
//            // 独赢奖励
//            BigDecimal successReward = null;
//            // 躺赢人数
//            int rewardCount = 0;
//            // 躺赢奖励
//            BigDecimal everyReward = null;
//
//            for (SUserBonusLog userBonus : userBonusLogList) {
//
//                if (userBonus.getBonusType() == 3) {
//
//                    // 独赢奖励 （余额+）
//                    SUser user1 = this.userService.getById(userBonus.getUserId());
//
//                    // 金额流水插入
//                    SUserAmountLog userAmountLog = new SUserAmountLog();
//                    userAmountLog.setUserId(user1.getId());
//                    userAmountLog.setChangeType(3);
//                    userAmountLog.setChangeAmount(userBonus.getBonusAmount());
//                    userAmountLog.setChangeTime(new Date());
//                    userAmountLog.setRelationId(userBonus.getTaskId());
//                    userAmountLog.setRemark("关联任务ID");
//                    userAmountLog.setOldAmount(user1.getTotalAmount());
//                    this.userAmountLogService.save(userAmountLog);
//
//                    user1.setTotalAmount(user1.getTotalAmount().add(userBonus.getBonusAmount()));
//                    this.userService.updateById(user1);
//
//                    SUserMsg userMsg = new SUserMsg();
//                    userMsg.setUserId(user1.getId());
//                    userMsg.setMsgTime(new Date());
//                    userMsg.setMsgType(1);
//                    userMsg.setStatus(0);
//                    userMsg.setMsgTitle("恭喜您获得独赢奖励" + userBonus.getBonusAmount() + "元。");
//                    userMsg.setMsgInfo("恭喜您获得独赢奖励" + userBonus.getBonusAmount() + "元。");
//
//                    userMsgService.save(userMsg);
//
//                    rewardUserName = user1.getNickName();
//                    successReward = userBonus.getBonusAmount();
//
//                } else if (userBonus.getBonusType() == 4) {
//
//                    rewardCount = rewardCount + 1;
//
//                    // 躺赢奖励（余额+）
//                    SUser user1 = this.userService.getById(userBonus.getUserId());
//
//                    // 躺赢金额流水插入
//                    SUserAmountLog userAmountLog = new SUserAmountLog();
//                    userAmountLog = new SUserAmountLog();
//                    userAmountLog.setUserId(user1.getId());
//                    userAmountLog.setChangeType(4);
//                    userAmountLog.setChangeAmount(userBonus.getBonusAmount().multiply(BigDecimal.valueOf(userBonus.getTaskNumber())));
//                    userAmountLog.setChangeTime(new Date());
//                    userAmountLog.setRelationId(userBonus.getTaskId());
//                    userAmountLog.setRemark("关联任务ID");
//                    userAmountLog.setOldAmount(user1.getTotalAmount());
//                    this.userAmountLogService.save(userAmountLog);
//
//                    user1.setTotalAmount(user1.getTotalAmount().add(userBonus.getBonusAmount().multiply(BigDecimal.valueOf(userBonus.getTaskNumber()))));
//                    this.userService.updateById(user1);
//
//                    SUserMsg userMsg = new SUserMsg();
//                    userMsg.setUserId(user1.getId());
//                    userMsg.setMsgTime(new Date());
//                    userMsg.setMsgType(2);
//                    userMsg.setStatus(0);
//                    userMsg.setMsgTitle("恭喜您获得躺赢奖励" + userBonus.getBonusAmount() + "元。");
//                    userMsg.setMsgInfo("恭喜您获得躺赢奖励" + userBonus.getBonusAmount() + "元。");
//
//                    userMsgService.save(userMsg);
//
//                    everyReward = userBonus.getBonusAmount();
//
//                } else if (userBonus.getBonusType() == 5) {
//
//                    // 下级奖励（余额+）
//                    SUser user1 = this.userService.getById(userBonus.getUserId());
//
//                    // 躺赢金额流水插入
//                    SUserAmountLog userAmountLog = new SUserAmountLog();
//                    userAmountLog = new SUserAmountLog();
//                    userAmountLog.setUserId(user1.getId());
//                    userAmountLog.setChangeType(5);
//                    userAmountLog.setChangeAmount(userBonus.getBonusAmount());
//                    userAmountLog.setChangeTime(new Date());
//                    userAmountLog.setRelationId(userBonus.getTaskId());
//                    userAmountLog.setRemark("关联任务ID");
//                    userAmountLog.setOldAmount(user1.getTotalAmount());
//                    this.userAmountLogService.save(userAmountLog);
//
//                    user1.setTotalAmount(user1.getTotalAmount().add(userBonus.getBonusAmount()));
//                    this.userService.updateById(user1);
//
//                    SUserMsg userMsg = new SUserMsg();
//                    userMsg.setUserId(user1.getId());
//                    userMsg.setMsgTime(new Date());
//                    userMsg.setMsgType(3);
//                    userMsg.setStatus(0);
//                    userMsg.setMsgTitle("恭喜您获得下级奖励" + userBonus.getBonusAmount() + "元。");
//                    userMsg.setMsgInfo("恭喜您获得下级奖励" + userBonus.getBonusAmount() + "元。");
//
//                    userMsgService.save(userMsg);
//
//                } else if (userBonus.getBonusType() == 9) {
//
//                    // 任务金奖励（余额+ 冻结-）
//                    SUser user1 = this.userService.getById(userBonus.getUserId());
//
//                    // 冻结-
//                    user1.setLockAmount(user1.getLockAmount().subtract(userBonus.getBonusAmount().multiply(BigDecimal.valueOf(userBonus.getTaskNumber()))));
//                    // 余额+
//                    user1.setTotalAmount(user1.getTotalAmount().add(userBonus.getBonusAmount().multiply(BigDecimal.valueOf(userBonus.getTaskNumber()))));
//
//                    this.userService.updateById(user1);
//
//                    // 任务解冻金额流水插入
//                    SUserAmountLog userAmountLog = new SUserAmountLog();
//                    userAmountLog.setUserId(user1.getId());
//                    userAmountLog.setChangeType(9);
//                    userAmountLog.setChangeAmount(userBonus.getBonusAmount().multiply(BigDecimal.valueOf(userBonus.getTaskNumber())));
//                    userAmountLog.setChangeTime(new Date());
//                    userAmountLog.setRelationId(userBonus.getTaskId());
//                    userAmountLog.setRemark("关联任务ID");
//                    userAmountLog.setOldAmount(user1.getTotalAmount());
//                    this.userAmountLogService.save(userAmountLog);
//                }
//            }

//            SUserMsg userMsg = new SUserMsg();
//            userMsg.setUserId(null);
//            userMsg.setMsgTime(new Date());
//            userMsg.setMsgType(0);
//            userMsg.setStatus(0);
//            userMsg.setMsgTitle("恭喜" + rewardUserName + "独赢" + successReward + "元，其他" + rewardCount + "人分配躺赢奖金" + everyReward.multiply(BigDecimal.valueOf(rewardCount))+ "元。");
//            userMsg.setMsgInfo("恭喜" + rewardUserName + "独赢" + successReward + "元，其他" + rewardCount + "人分配躺赢奖金" + everyReward.multiply(BigDecimal.valueOf(rewardCount))+ "元。");
//
//            userMsgService.save(userMsg);

        } catch (Exception e) {
            message = "更新用户购买订单状态失败";
            response.put("code", 1);
            response.message(message);
            log.error(message, e);
        }

        return response;
    }


}
