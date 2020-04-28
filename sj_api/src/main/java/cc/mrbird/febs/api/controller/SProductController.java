package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.*;
import cc.mrbird.febs.api.service.*;
import cc.mrbird.febs.common.annotation.Limit;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.domain.FebsResponse;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.utils.FebsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @author MrBird
 */
@RestController
@RequestMapping("/api/s-product")
public class SProductController extends BaseController {

    @Autowired
    private ISProductTypeService productTypeService;

    @Autowired
    private ISProductService productService;

    @Autowired
    private ISProductSpecService productSpecService;

    @Autowired
    private ISUserCouponService userCouponService;

    @Autowired
    private ISShopCouponService shopCouponService;

    @Autowired
    private ISTaskCouponService taskCouponService;

    /**
     * 取得所有商品分类信息
     * @return List<SProductType>
     */
    @PostMapping("/getProductType")
    @Limit(key = "getProductType", period = 60, count = 20, name = "检索商品分类接口", prefix = "limit")
    public FebsResponse getProductType() {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        List<Map> productTypeList = new ArrayList();
        Map<String, Object> productTypeMap = new HashMap<>();

        // 推荐分类及子分类
        productTypeMap.put("productBigTypeName", "推荐分类");
        // 0-普通; 1-推荐
        productTypeMap.put("flag", 1);
        // 分类ID
        productTypeMap.put("typeId", null);

        List<Map> productSmallTypeMapList = new ArrayList();

        List<SProductType> recommendProductTypeList = this.productTypeService.findRecommendProductTypeList();

        for (SProductType recommendProductType : recommendProductTypeList) {

            Map<String, Object> productSmallTypeMap = new HashMap<>();

            // 分类ID
            productSmallTypeMap.put("typeId", recommendProductType.getId());
            // 分类名称
            productSmallTypeMap.put("typeName", recommendProductType.getTypeName());
            // 分类小图标
            productSmallTypeMap.put("typeImg", recommendProductType.getTypeImg());

            productSmallTypeMapList.add(productSmallTypeMap);
        }
        productTypeMap.put("productSmallType", productSmallTypeMapList);
        productTypeList.add(productTypeMap);

        // 普通分类及子分类
        SProductType productType = new SProductType();
        productType.setLevel(1);

        // 商品大分类检索
        List<SProductType> productBigTypeList = this.productTypeService.findProductTypeList(productType);

        // 商品大分类循环
        for (SProductType productBigType : productBigTypeList) {

            // 普通分类及子分类
            productTypeMap.put("productBigTypeName", productBigType.getTypeName());
            // 0-普通; 1-推荐
            productTypeMap.put("flag", 0);
            // 分类ID
            productTypeMap.put("typeId", productBigType.getId());

            productType.setLevel(2);
            productType.setParentId(productBigType.getId());

            // 商品小分类检索
            List<SProductType> productSmallTypeList = this.productTypeService.findProductTypeList(productType);

            productSmallTypeMapList = new ArrayList();

            for (SProductType productSmallType : productSmallTypeList) {

                Map<String, Object> productSmallTypeMap = new HashMap<>();

                // 分类ID
                productSmallTypeMap.put("typeId", productSmallType.getId());
                // 分类名称
                productSmallTypeMap.put("typeName", productSmallType.getTypeName());
                // 分类小图标
                productSmallTypeMap.put("typeImg", productSmallType.getTypeImg());

                productSmallTypeMapList.add(productSmallTypeMap);
            }
            productTypeMap.put("productSmallType", productSmallTypeMapList);

            productTypeList.add(productTypeMap);
        }

        response.data(productTypeList);

        return response;
    }

    /**
     * 根据商品分类取得所有商品信息
     * @return List<Map>
     */
    @PostMapping("/getProductListByTypeId")
    @Limit(key = "getProductListByTypeId", period = 60, count = 20, name = "检索全部商品接口", prefix = "limit")
    public FebsResponse getProductListByTypeId(QueryRequest queryRequest, String typeId) {

        FebsResponse response = new FebsResponse();

        SProductType productType = this.productTypeService.getById(typeId);

        Map<String, Object> productPageList = new HashMap<>();

        SProduct product = new SProduct();
        product.setTypeId(typeId);

        // 级别（1、2）
        if (productType.getLevel() == 1) {

            // 根据大分类检索商品列表
            productPageList = getDataTable(
                    this.productService.findProductListByBigTypeId(product, queryRequest));
        } else {

            // 根据小分类检索商品列表
            productPageList = getDataTable(
                    this.productService.findProductListBySmallTypeId(product, queryRequest));
        }

        response.put("code", 0);
        response.data(productPageList);

        return response;
    }

    /**
     * 根据商品ID取得商品信息
     * @return SProduct
     */
    @PostMapping("/getProductDetail")
    @Limit(key = "getProductDetail", period = 60, count = 20, name = "检索商品详情接口", prefix = "limit")
    public FebsResponse getProductDetail(String productId) {

        FebsResponse response = new FebsResponse();

        SProduct product = new SProduct();
        product.setId(productId);

        // 商品详情
        Map productDetail = this.productService.findProductDetail(product);

        // 规格组合（第一条默认值）
        List<SProductSpec> productSpecList = this.productSpecService.findProductSpecList(productId);
        productDetail.put("productSpec", productSpecList);

        SUser user = FebsUtil.getCurrentUser();

        List<Map> couponList = new ArrayList();

        if (user != null) {

            // 用户可用优惠券
            List<SUserCoupon> userCouponList = this.userCouponService.findUserCouponList(user.getId(), productId, 0, null);

            for (SUserCoupon userCoupon : userCouponList) {

                Map<String, Object> couponMap = new HashMap<>();

                // 券类型 0-任务金 1-商铺券
                if (userCoupon.getCouponType() == 0) {
                    STaskCoupon taskCoupon = this.taskCouponService.getById(userCoupon.getCouponId());

                    // 券类型 0-任务金
                    couponMap.put("couponType", userCoupon.getCouponType());
                    // 券名称
                    couponMap.put("couponName", taskCoupon.getCouponName());
                    // 券面值
                    couponMap.put("couponAmount", taskCoupon.getCouponAmount());
                    // 券开始日期
                    couponMap.put("startDate", taskCoupon.getStartDate());
                    // 券截止日期
                    couponMap.put("endDate", taskCoupon.getEndDate());
                    // 使用条件 1-固定金额券 2-超级抵扣券 3-购买任务返任务金优惠券
                    couponMap.put("useCon", taskCoupon.getUseCon());

                    couponList.add(couponMap);

                } else {
                    SShopCoupon shopCoupon = this.shopCouponService.getById(userCoupon.getCouponId());

                    // 券类型 1-商铺券
                    couponMap.put("couponType", userCoupon.getCouponType());
                    // 券名称
                    couponMap.put("couponName", shopCoupon.getCouponName());
                    // 券面值
                    couponMap.put("couponAmount", shopCoupon.getCouponAmount());
                    // 券开始日期
                    couponMap.put("startDate", shopCoupon.getStartDate());
                    // 券截止日期
                    couponMap.put("endDate", shopCoupon.getEndDate());
                    // 使用条件 0-立减 1-满减
                    couponMap.put("useCon", shopCoupon.getUseCon());

                    couponList.add(couponMap);
                }
            }

            // 可用优惠券
            productDetail.put("couponList", couponList);
        }

        response.put("code", 0);
        response.data(productDetail);

        return response;
    }

}
