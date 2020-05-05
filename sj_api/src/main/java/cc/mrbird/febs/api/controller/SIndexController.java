package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.*;
import cc.mrbird.febs.api.service.*;
import cc.mrbird.febs.common.annotation.Limit;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.domain.FebsResponse;
import cc.mrbird.febs.common.utils.FebsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
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

    @Autowired
    private ISProductImgService productImgService;

    @Autowired
    private ISParamsService paramsService;

    @Autowired
    private ISUserFollowService userFollowService;

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

        // 买家立返佣金比例 （后续调整到Redis缓存读取）
        SParams params = new SParams();
        params = this.paramsService.queryBykeyForOne("buyer_rate");
        BigDecimal buyerRate = BigDecimal.valueOf(Double.parseDouble(params.getPValue()));

        // 同组任务躺赢佣金比例
        params = this.paramsService.queryBykeyForOne("same_group_rate");
        BigDecimal sameGroupRate = BigDecimal.valueOf(Double.parseDouble(params.getPValue()));

        // 推荐类别检索
        List<SRecommendType> recommendTypeList = this.recommendTypeService.findRecommendTypeList();
        // 推荐类别循环
        for (SRecommendType recommendType : recommendTypeList) {

            Map<String, Object> recommendTypeMap = new HashMap<>();
            List<Map> productRecommendList = this.productRecommendService.findProductRecommendList(recommendType.getId());

            for (Map productRecommendMap : productRecommendList) {

                // 商品图片
                List<SProductImg> productImgList = this.productImgService.findProductImgList((String)productRecommendMap.get("productId"));
                productRecommendMap.put("imgUrlList", productImgList);

                // 总佣金
                BigDecimal totalReward = new BigDecimal(productRecommendMap.get("totalReward").toString());
                // 任务数量
                BigDecimal taskNumber = new BigDecimal(productRecommendMap.get("taskNumber").toString());

                // 买家立返
                BigDecimal buyerReturnAmt = new BigDecimal(0);
                buyerReturnAmt = totalReward.multiply(buyerRate);
                productRecommendMap.put("buyerReturnAmt", buyerReturnAmt);

                // 躺赢奖励
                BigDecimal taskReturnAmt = new BigDecimal(0);
                taskReturnAmt = totalReward.multiply(sameGroupRate).divide(taskNumber, 2, BigDecimal.ROUND_HALF_UP);
                productRecommendMap.put("taskReturnAmt", taskReturnAmt);

                SUser user = FebsUtil.getCurrentUser();
                if (user == null) {
                    // 未登录显示未关注
                    productRecommendMap.put("followFlag", false);
                } else {
                    // 是否已关注
                    SUserFollow userFollow = new SUserFollow();
                    userFollow.setUserId(user.getId());
                    userFollow.setFollowType(0);
                    userFollow.setProductId((String)productRecommendMap.get("productId"));
                    userFollow = this.userFollowService.findUserFollowDetail(userFollow);
                    if (userFollow != null && userFollow.getStatus() == 1) {
                        productRecommendMap.put("followFlag", true);
                    } else {
                        productRecommendMap.put("followFlag", false);
                    }
                }
            }

            recommendTypeMap.put("recommendType", recommendType);
            recommendTypeMap.put("productRecommendList", productRecommendList);

            recommendProductList.add(recommendTypeMap);
        }

        returnMap.put("recommendProductList", recommendProductList);

        response.data(returnMap);
        return response;
    }

}
