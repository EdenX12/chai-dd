package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.*;
import cc.mrbird.febs.api.service.*;
import cc.mrbird.febs.common.annotation.Limit;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.domain.FebsResponse;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.utils.FebsUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author MrBird
 */
@RestController
@RequestMapping("/api/s-product")
public class SProductController extends BaseController {

    private String message;

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

    @Autowired
    private ISProductImgService productImgService;

    @Autowired
    private ISParamsService paramsService;

    @Autowired
    private ISUserFollowService userFollowService;

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

            // 如果是大分类的话，取出其下面的所有小分类
            if (recommendProductType.getLevel() == 1) {

                SProductType productType = new SProductType();
                productType.setParentId(recommendProductType.getId());
                productType.setLevel(2);
                List<SProductType> productTypeList1 = this.productTypeService.findProductTypeList(productType);

                for (SProductType productType1 : productTypeList1) {

                    Map<String, Object> productSmallTypeMap = new HashMap<>();

                    // 分类ID
                    productSmallTypeMap.put("typeId", productType1.getId());
                    // 分类名称
                    productSmallTypeMap.put("typeName", productType1.getTypeName());
                    // 分类小图标
                    productSmallTypeMap.put("typeImg", productType1.getTypeImg());

                    productSmallTypeMapList.add(productSmallTypeMap);
                }

            } else {

                Map<String, Object> productSmallTypeMap = new HashMap<>();

                // 分类ID
                productSmallTypeMap.put("typeId", recommendProductType.getId());
                // 分类名称
                productSmallTypeMap.put("typeName", recommendProductType.getTypeName());
                // 分类小图标
                productSmallTypeMap.put("typeImg", recommendProductType.getTypeImg());

                productSmallTypeMapList.add(productSmallTypeMap);
            }
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

            productTypeMap = new HashMap<>();

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

        // 买家立返佣金比例 （后续调整到Redis缓存读取）
        SParams params = new SParams();
        params = this.paramsService.queryBykeyForOne("buyer_rate");
        BigDecimal buyerRate = BigDecimal.valueOf(Double.parseDouble(params.getPValue()));

        // 同组任务躺赢佣金比例
        params = this.paramsService.queryBykeyForOne("same_group_rate");
        BigDecimal sameGroupRate = BigDecimal.valueOf(Double.parseDouble(params.getPValue()));

        // 级别（1、2）
        if (productType.getLevel() == 1) {

            // 根据大分类检索商品列表
            IPage<Map> returnPage = this.productService.findProductListByBigTypeId(product, queryRequest);

            List<Map> list = returnPage.getRecords();

            for (Map returnMap : list) {

                // 商品图片
                List<SProductImg> productImgList = this.productImgService.findProductImgList((String)returnMap.get("productId"));
                returnMap.put("imgUrlList", productImgList);

                // 总佣金
                BigDecimal totalReward = new BigDecimal(returnMap.get("totalReward").toString());
                // 任务数量
                BigDecimal taskNumber = new BigDecimal(returnMap.get("taskNumber").toString());

                // 买家立返
                BigDecimal buyerReturnAmt = new BigDecimal(0);
                buyerReturnAmt = totalReward.multiply(buyerRate);
                returnMap.put("buyerReturnAmt", buyerReturnAmt);

                // 躺赢奖励
                BigDecimal taskReturnAmt = new BigDecimal(0);
                taskReturnAmt = totalReward.multiply(sameGroupRate).divide(taskNumber, 2, BigDecimal.ROUND_HALF_UP);
                returnMap.put("taskReturnAmt", taskReturnAmt);

                SUser user = FebsUtil.getCurrentUser();
                if (user == null) {
                    // 未登录显示未关注
                    returnMap.put("followFlag", false);
                } else {
                    // 是否已关注
                    SUserFollow userFollow = new SUserFollow();
                    userFollow.setUserId(user.getId());
                    userFollow.setFollowType(0);
                    userFollow.setProductId((String)returnMap.get("productId"));
                    userFollow = this.userFollowService.findUserFollowDetail(userFollow);
                    if (userFollow != null && userFollow.getStatus() == 1) {
                        returnMap.put("followFlag", true);
                    } else {
                        returnMap.put("followFlag", false);
                    }
                }
            }
            returnPage.setRecords(list);

            productPageList = getDataTable(returnPage);
        } else {

            // 根据小分类检索商品列表
            IPage<Map> returnPage = this.productService.findProductListBySmallTypeId(product, queryRequest);

            List<Map> list = returnPage.getRecords();

            for (Map returnMap : list) {

                // 商品图片
                List<SProductImg> productImgList = this.productImgService.findProductImgList((String)returnMap.get("productId"));
                returnMap.put("imgUrlList", productImgList);

                // 总佣金
                BigDecimal totalReward = new BigDecimal(returnMap.get("totalReward").toString());
                // 任务数量
                BigDecimal taskNumber = new BigDecimal(returnMap.get("taskNumber").toString());

                // 买家立返
                BigDecimal buyerReturnAmt = new BigDecimal(0);
                buyerReturnAmt = totalReward.multiply(buyerRate);
                returnMap.put("buyerReturnAmt", buyerReturnAmt);

                // 躺赢奖励
                BigDecimal taskReturnAmt = new BigDecimal(0);
                taskReturnAmt = totalReward.multiply(sameGroupRate).divide(taskNumber, 2, BigDecimal.ROUND_HALF_UP);
                returnMap.put("taskReturnAmt", taskReturnAmt);

                SUser user = FebsUtil.getCurrentUser();
                if (user == null) {
                    // 未登录显示未关注
                    returnMap.put("followFlag", false);
                } else {
                    // 是否已关注
                    SUserFollow userFollow = new SUserFollow();
                    userFollow.setUserId(user.getId());
                    userFollow.setFollowType(0);
                    userFollow.setProductId((String)returnMap.get("productId"));
                    userFollow = this.userFollowService.findUserFollowDetail(userFollow);
                    if (userFollow != null && userFollow.getStatus() == 1) {
                        returnMap.put("followFlag", true);
                    } else {
                        returnMap.put("followFlag", false);
                    }
                }
            }
            returnPage.setRecords(list);

            productPageList = getDataTable(returnPage);
        }

        response.put("code", 0);
        response.data(productPageList);

        return response;
    }

    /**
     * 全文模糊检索商品
     * @return List<Map>
     */
    @PostMapping("/selectProductList")
    @Limit(key = "selectProductList", period = 60, count = 20, name = "全文模糊检索接口", prefix = "limit")
    public FebsResponse selectProductList(QueryRequest queryRequest, String productName) {

        FebsResponse response = new FebsResponse();

        Map<String, Object> productPageList = new HashMap<>();

        // 买家立返佣金比例 （后续调整到Redis缓存读取）
        SParams params = new SParams();
        params = this.paramsService.queryBykeyForOne("buyer_rate");
        BigDecimal buyerRate = BigDecimal.valueOf(Double.parseDouble(params.getPValue()));

        // 同组任务躺赢佣金比例
        params = this.paramsService.queryBykeyForOne("same_group_rate");
        BigDecimal sameGroupRate = BigDecimal.valueOf(Double.parseDouble(params.getPValue()));

        // 模糊检索商品列表
        IPage<Map> returnPage = this.productService.findProductListByProductName(productName, queryRequest);

        List<Map> list = returnPage.getRecords();

        for (Map returnMap : list) {

            // 商品图片
            List<SProductImg> productImgList = this.productImgService.findProductImgList((String)returnMap.get("productId"));
            returnMap.put("imgUrlList", productImgList);

            // 总佣金
            BigDecimal totalReward = new BigDecimal(returnMap.get("totalReward").toString());
            // 任务数量
            BigDecimal taskNumber = new BigDecimal(returnMap.get("taskNumber").toString());

            // 买家立返
            BigDecimal buyerReturnAmt = totalReward.multiply(buyerRate);
            returnMap.put("buyerReturnAmt", buyerReturnAmt);

            // 躺赢奖励
            BigDecimal taskReturnAmt = totalReward.multiply(sameGroupRate).divide(taskNumber, 2, BigDecimal.ROUND_HALF_UP);
            returnMap.put("taskReturnAmt", taskReturnAmt);

            SUser user = FebsUtil.getCurrentUser();
            if (user == null) {
                // 未登录显示未关注
                returnMap.put("followFlag", false);
            } else {
                // 是否已关注
                SUserFollow userFollow = new SUserFollow();
                userFollow.setUserId(user.getId());
                userFollow.setFollowType(0);
                userFollow.setProductId((String)returnMap.get("productId"));
                userFollow = this.userFollowService.findUserFollowDetail(userFollow);
                if (userFollow != null && userFollow.getStatus() == 1) {
                    returnMap.put("followFlag", true);
                } else {
                    returnMap.put("followFlag", false);
                }
            }
        }
        returnPage.setRecords(list);
        productPageList = getDataTable(returnPage);

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

        // 商品详情
        SUser user = FebsUtil.getCurrentUser();
        Map productDetail = this.productService.findProductDetail(productId, user);

        if (productDetail == null) {
            message = "您选择的商品不存在！";
            response.put("code", 1);
            response.message(message);
            return response;
        }

        // 规格组合（第一条默认值）
        List<SProductSpec> productSpecList = this.productSpecService.findProductSpecList(productId);
        productDetail.put("productSpec", productSpecList);

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
