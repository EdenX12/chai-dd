package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.SActivity;
import cc.mrbird.febs.api.entity.SBanner;
import cc.mrbird.febs.api.entity.SProductType;
import cc.mrbird.febs.api.entity.SRecommendType;
import cc.mrbird.febs.api.service.*;
import cc.mrbird.febs.common.annotation.Limit;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.domain.FebsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author MrBird
 */
@Slf4j
@RestController
@RequestMapping("/api/home")
public class SIndexController extends BaseController {

    @Autowired
    private ISProductTypeService productTypeService;

    @Autowired
    private ISBannerService bannerService;

    @Autowired
    private ISActivityService activityService;

    @Autowired
    private ISRecommendTypeService recommendTypeService;

    @Autowired
    private ISProductRecommendService productRecommendService;

    /**
     * 取得首页信息
     * @return SUser
     */
    @PostMapping("/getIndex")
    @Limit(key = "getIndex", period = 60, count = 20, name = "检索首页信息接口", prefix = "limit")
    public FebsResponse getIndex(HttpServletRequest request) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        Map<String, Object> returnMap = new HashMap<>();

        // 推荐分类检索
        List<SProductType> productTypeList = this.productTypeService.findRecommendProductTypeList();
        returnMap.put("productTypeList", productTypeList);

        // 首页轮播图检索
        List<SBanner> bannerList = this.bannerService.findBannerList();
        returnMap.put("bannerList", bannerList);

        // 活动区域图片检索
        List<SActivity> activityList = this.activityService.findActivityList();
        returnMap.put("activityList", activityList);

        List<Map> recommendProductList = new ArrayList();

        // 推荐类别检索
        List<SRecommendType> recommendTypeList = this.recommendTypeService.findRecommendTypeList();
        // 推荐类别循环
        for (SRecommendType recommendType : recommendTypeList) {

            Map<String, Object> recommendTypeMap = new HashMap<>();
            List<Map> productRecommendList = productRecommendService.findProductRecommendList(recommendType.getId());

            recommendTypeMap.put("recommendType", recommendType);
            recommendTypeMap.put("productRecommendList", productRecommendList);

            recommendProductList.add(recommendTypeMap);
        }

        returnMap.put("recommendProductList", recommendProductList);

        response.data(returnMap);
        return response;
    }

}
