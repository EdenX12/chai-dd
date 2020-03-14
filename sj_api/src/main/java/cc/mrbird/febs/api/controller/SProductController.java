package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.SProduct;
import cc.mrbird.febs.api.entity.SProductType;
import cc.mrbird.febs.api.entity.SUser;
import cc.mrbird.febs.api.service.ISProductService;
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

import javax.servlet.http.HttpServletRequest;
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

    /**
     * 取得所有商品分类信息
     * @return List<SProductType>
     */
    @PostMapping("/getProductType")
    @Limit(key = "getProductType", period = 60, count = 20, name = "检索商品分类接口", prefix = "limit")
    public FebsResponse getProductType(HttpServletRequest request) throws Exception {

        FebsResponse response = new FebsResponse();

        List<SProductType> productTypeList = productTypeService.findByTypeStatus(1);

        response.put("code", 0);
        response.data(productTypeList);

        return response;
    }

    /**
     * 根据商品分类和产品类型取得所有商品信息
     * @return List<SProduct>
     */
    @PostMapping("/getProductList")
    @Limit(key = "getProductList", period = 60, count = 20, name = "检索全部商品接口", prefix = "limit")
    public FebsResponse getProductList(QueryRequest queryRequest, SProduct product) {

        FebsResponse response = new FebsResponse();

        SUser user = FebsUtil.getCurrentUser();
        product.setUserId(user.getId());

        Map<String, Object> productPageList = getDataTable(productService.findProductList(product, queryRequest));

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
    public FebsResponse getProductDetail(QueryRequest queryRequest, SProduct product) {

        FebsResponse response = new FebsResponse();

        SUser user = FebsUtil.getCurrentUser();
        product.setUserId(user.getId());

        SProduct productDetail = productService.findProductDetail(product);

        response.put("code", 0);
        response.data(productDetail);

        return response;
    }

}
