package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.SProduct;
import cc.mrbird.febs.api.entity.SProductSpec;
import cc.mrbird.febs.api.entity.SProductType;
import cc.mrbird.febs.api.entity.SUser;
import cc.mrbird.febs.api.service.ISProductService;
import cc.mrbird.febs.api.service.ISProductSpecService;
import cc.mrbird.febs.api.service.ISProductTypeService;
import cc.mrbird.febs.common.annotation.Limit;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.domain.FebsResponse;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.utils.FebsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public FebsResponse getProductDetail(QueryRequest queryRequest, String productId) {

        FebsResponse response = new FebsResponse();

        SProduct product = new SProduct();
        product.setId(productId);

        // 商品详情
        Map productDetail = this.productService.findProductDetail(product);

        // 规格组合（第一条默认值）
        List<SProductSpec> productSpecList = this.productSpecService.findProductSpecList(productId);
        productDetail.put("productSpec", productSpecList);

        SUser user = FebsUtil.getCurrentUser();
        if (user != null) {

            // 用户优惠券

        }

        // 赠送拆豆

        // 返优惠券

        response.put("code", 0);
        response.data(productDetail);

        return response;
    }

}
