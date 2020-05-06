package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.SProduct;
import cc.mrbird.febs.api.entity.SProductSpec;
import cc.mrbird.febs.api.entity.SUser;
import cc.mrbird.febs.api.entity.SUserShopCar;
import cc.mrbird.febs.api.service.ISProductService;
import cc.mrbird.febs.api.service.ISProductSpecService;
import cc.mrbird.febs.api.service.ISUserShopCarService;
import cc.mrbird.febs.common.annotation.Limit;
import cc.mrbird.febs.common.annotation.Log;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.domain.FebsResponse;
import cc.mrbird.febs.common.utils.FebsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.*;

/**
 * @author MrBird
 */
@Slf4j
@RestController
@RequestMapping("/api/s-user-shop-car")
public class SUserShopCarController extends BaseController {

    private String message;

    @Autowired
    private ISUserShopCarService userShopCarService;

    @Autowired
    private ISProductSpecService productSpecService;

    @Autowired
    private ISProductService productService;

    /**
     * 商品加入购物车
     * 用户ID 商品规格ID 最初价格 数量
     */
    @Log("商品加入购物车")
    @Transactional
    @PostMapping("/addShopCar")
    public FebsResponse addShopCar(@Valid SUserShopCar userShopCar) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {

            SUser user = FebsUtil.getCurrentUser();
            userShopCar.setUserId(user.getId());

            this.userShopCarService.addUserShopCar(userShopCar);

        } catch (Exception e) {
            message = "商品加入购物车失败";
            response.put("code", 1);
            response.message(message);
            log.error( e.getMessage());
        }

        return response;
    }

    /**
     * 取得我的购物车
     * @return List<SUserMsg>
     */
    @PostMapping("/getUserShopCar")
    @Limit(key = "getUserShopCar", period = 60, count = 20, name = "检索我的购物车接口", prefix = "limit")
    public FebsResponse getUserShopCar()  {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        Map<String, Object> resultMap = new HashMap<>();

        SUser user = FebsUtil.getCurrentUser();

        // 我的购物车查询
        SUserShopCar userShopCar = new SUserShopCar();
        userShopCar.setUserId(user.getId());
        List<SUserShopCar> userShopCarList = this.userShopCarService.findUserShopCarList(userShopCar);

        List<Map<String, Object>> productList = new ArrayList<>();

        for (SUserShopCar userShopCarLoop : userShopCarList) {

            // 商品规格
            SProductSpec productSpec = this.productSpecService.findProductSpec(userShopCarLoop.getProductSpecId());

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

            // 任务金
            newProductDetail.put("taskPrice", productDetail.get("taskPrice"));

            // 躺赢奖励
            newProductDetail.put("taskReturnAmt", productDetail.get("taskReturnAmt"));

            // 已完成任务线
            newProductDetail.put("overTaskLineCnt", productDetail.get("overTaskLineCnt"));

            // 当前任务线份额
            newProductDetail.put("totalTaskNumber", productDetail.get("totalTaskNumber"));

            // 已完成任务份额
            newProductDetail.put("receivedTaskNumber", productDetail.get("receivedTaskNumber"));

            // 拆家人数
            newProductDetail.put("taskUserCnt", productDetail.get("taskUserCnt"));

            // 关注人数
            newProductDetail.put("followCnt", productDetail.get("followCnt"));

            // 快递邮费
            newProductDetail.put("expressFee", productDetail.get("expressFee"));

            // 商品规格
            newProductDetail.put("productSpecName", productSpec.getProductSpecValueName());

            // 商品规格价格
            newProductDetail.put("productPrice", productSpec.getProductPrice());

            // 商品规格划线价格
            newProductDetail.put("scribingPrice", productSpec.getScribingPrice());

            // 商品最初价格
            newProductDetail.put("initialPrice", userShopCarLoop.getPrice());

            // 商品数量
            newProductDetail.put("productNumber", userShopCarLoop.getCount());

            // 选中状态
            newProductDetail.put("checkStatus", userShopCarLoop.getCheckStatus());

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
        List<Map> shopList = new ArrayList();
        Map<String, Object> shopMap = new HashMap<>();
        List<Map> shopProductList = new ArrayList();

        // 拆单显示（不同的商户 显示到不同的订单）
        for (Map productMap : productList) {

            // 根据顺序，店铺不同时
            if (!shopId.equals(productMap.get("shopId"))) {

                if (!"".equals(shopId)) {

                    // 商家购物车商品列表
                    shopMap.put("shopProduct", shopProductList);

                    shopList.add(shopMap);
                }

                shopMap = new HashMap<>();
                shopMap.put("shopId", productMap.get("shopId"));
                shopMap.put("shopName", productMap.get("shopName"));

                shopProductList = new ArrayList();
                shopProductList.add(productMap);

            } else {

                shopProductList.add(productMap);
            }

            // 店铺ID
            shopId = (String) productMap.get("shopId");
        }
        shopMap.put("shopProduct", shopProductList);
        shopList.add(shopMap);

        // 购物车列表
        resultMap.put("shopList", shopList);

        response.data(resultMap);

        return response;
    }

    /**
     * 删除购物车商品
     * 用户ID 购物车ID
     */
    @Log("删除购物车商品")
    @Transactional
    @PostMapping("/deleteShopCar")
    public FebsResponse deleteShopCar(@RequestParam(value ="userShopCarIds") List<String> userShopCarIds) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {

            SUser user = FebsUtil.getCurrentUser();

            for (String userShopCarId : userShopCarIds) {

                SUserShopCar userShopCar = new SUserShopCar();
                userShopCar.setUserId(user.getId());
                userShopCar.setId(userShopCarId);

                this.userShopCarService.deleteUserShopCar(userShopCar);
            }

        } catch (Exception e) {
            message = "删除购物车商品失败";
            response.put("code", 1);
            response.message(message);
            log.error( e.getMessage());
        }

        return response;
    }

    /**
     * 更新购物车商品选中状态
     * 购物车ID 状态
     */
    @Log("更新购物车选中状态")
    @Transactional
    @PostMapping("/updateShopCarCheckStatus")
    public FebsResponse updateShopCarCheckStatus(String userShopCarId, int checkStatus) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {

            SUser user = FebsUtil.getCurrentUser();

            SUserShopCar userShopCar = new SUserShopCar();
            userShopCar.setUserId(user.getId());
            userShopCar.setId(userShopCarId);
            userShopCar.setCheckStatus(checkStatus);

            this.userShopCarService.updateById(userShopCar);

        } catch (Exception e) {
            message = "更新购物车选中状态失败";
            response.put("code", 1);
            response.message(message);
            log.error( e.getMessage());
        }

        return response;
    }
}
