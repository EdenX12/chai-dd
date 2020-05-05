package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.*;
import cc.mrbird.febs.api.mapper.SProductMapper;
import cc.mrbird.febs.api.service.ISParamsService;
import cc.mrbird.febs.api.service.ISProductImgService;
import cc.mrbird.febs.api.service.ISProductService;
import cc.mrbird.febs.api.service.ISUserFollowService;
import cc.mrbird.febs.common.domain.FebsConstant;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.utils.SortUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author MrBird
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class SProductServiceImpl extends ServiceImpl<SProductMapper, SProduct> implements ISProductService {

    @Autowired
    private ISProductImgService productImgService;

    @Autowired
    private ISParamsService paramsService;

    @Autowired
    private ISUserFollowService userFollowService;

    @Override
    public IPage<Map> findProductListByBigTypeId(SProduct product, QueryRequest request) {

        try {

            Page<SProduct> page = new Page<>();

            SortUtil.handlePageSort(request, page, "sOrder", FebsConstant.ORDER_ASC, false);

            return this.baseMapper.findProductDetailByBigTypeId(page, product);

        } catch (Exception e) {
            log.error("查询全部商品异常", e);
            return null;
        }
    }

    @Override
    public IPage<Map> findProductListBySmallTypeId(SProduct product, QueryRequest request) {

        try {

            Page<SProduct> page = new Page<>();

            SortUtil.handlePageSort(request, page, "sOrder", FebsConstant.ORDER_ASC, false);

            return this.baseMapper.findProductDetailBySmallTypeId(page, product);

        } catch (Exception e) {
            log.error("查询全部商品异常", e);
            return null;
        }
    }

    @Override
    public Map findProductDetail(String productId, SUser user) {

        try {

            Map returnMap = this.baseMapper.findProductDetail(productId);

            // 买家立返佣金比例 （后续调整到Redis缓存读取）
            SParams params = new SParams();
            params = this.paramsService.queryBykeyForOne("buyer_rate");
            BigDecimal buyerRate = BigDecimal.valueOf(Double.parseDouble(params.getPValue()));

            // 同组任务躺赢佣金比例
            params = this.paramsService.queryBykeyForOne("same_group_rate");
            BigDecimal sameGroupRate = BigDecimal.valueOf(Double.parseDouble(params.getPValue()));

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

            return returnMap;

        } catch (Exception e) {
            log.error("查询商品详情异常", e);
            return null;
        }
    }

}
