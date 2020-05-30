package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.*;
import cc.mrbird.febs.api.mapper.SProductMapper;
import cc.mrbird.febs.api.service.*;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.utils.SortUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

    @Autowired
    private ISUserLevelService userLevelService;

    @Autowired
    private ISUserBonusLogService userBonusLogService;

    @Autowired
    private ISUserTaskLineService userTaskLineService;

    @Autowired
    private ISTaskLineService taskLineService;

    @Override
    public IPage<Map> findProductListByProductName(String productName, QueryRequest request) {

        try {

            Page<SProduct> page = new Page<>();

            SortUtil.handlePageSort(request, page, null, null, false);

            return this.baseMapper.findProductDetailByProductName(page, productName);

        } catch (Exception e) {
            log.error("模糊查询全部商品异常", e);
            return null;
        }
    }

    @Override
    public IPage<Map> findNewProductList(QueryRequest request) {

        try {

            Page<SProduct> page = new Page<>();

            SortUtil.handlePageSort(request, page, null, null, false);

            return this.baseMapper.findNewProductDetail(page);

        } catch (Exception e) {
            log.error("检索新手商品异常", e);
            return null;
        }
    }

    @Override
    public IPage<Map> findActivityProductList(String activityId, QueryRequest request) {

        try {

            Page<SProduct> page = new Page<>();

            SortUtil.handlePageSort(request, page, null, null, false);

            return this.baseMapper.findActivityProductDetail(page, activityId);

        } catch (Exception e) {
            log.error("模糊查询全部商品异常", e);
            return null;
        }
    }

    @Override
    public IPage<Map> findProductListByBigTypeId(SProduct product, QueryRequest request) {

        try {

            Page<SProduct> page = new Page<>();

            SortUtil.handlePageSort(request, page, null, null, false);

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

            SortUtil.handlePageSort(request, page, null, null, false);

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
            String value  = this.paramsService.queryBykeyForOne("buyer_rate");
            BigDecimal buyerRate = BigDecimal.valueOf(Double.parseDouble(value));

            // 同组任务躺赢佣金比例
            value = this.paramsService.queryBykeyForOne("same_group_rate");
            BigDecimal sameGroupRate = BigDecimal.valueOf(Double.parseDouble(value));

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

            // 躺赢奖励（起）
            BigDecimal taskReturnAmt = totalReward.multiply(sameGroupRate).divide(taskNumber, 2, BigDecimal.ROUND_HALF_UP);
            returnMap.put("taskReturnAmt", taskReturnAmt);

            Integer frontProductCount = 0;
            List<String> taskLineList =  this.baseMapper.getMyTaskLineIds(user == null ? null : user.getId(),productId);
            frontProductCount = this.baseMapper.myFrontCount(null,productId);
            if(taskLineList != null && user!= null){
                Integer minLineOrder = this.baseMapper.getMinOrder(taskLineList);
                if(minLineOrder != null && minLineOrder > 0 ){
                    frontProductCount = this.baseMapper.myFrontCount(minLineOrder,productId);
                }
            }
            if (user == null) {
                // 未登录显示未关注
                returnMap.put("followFlag", false);

                // 此单前方商品数量 （已满未结算）
                returnMap.put("frontProductCount", frontProductCount );

            } else {
                returnMap.put("frontProductCount", frontProductCount);
                // 此单前方商品数量 （小于我已有未结算任务线 的 已满未结算）



                // 任务躺赢（实际）
                BigDecimal taskTaskRewardAmt0 = BigDecimal.ZERO;
                // 组织躺赢（纵向实际）【结算中状态显示】
                BigDecimal taskOrgRewardAmt0 = BigDecimal.ZERO;

                // 组织躺赢（横向实际）【结算中状态显示】
                BigDecimal taskOrgHorizRewardAmt0 = BigDecimal.ZERO;

                // 买家返 （实际）【结算中状态显示】
                BigDecimal taskBuyerRewardAmt0 = BigDecimal.ZERO;
                // 任务躺赢（实际）  【已完成 状态显示】 根据userId productId从bonusLog中读取
                BigDecimal taskTaskRewardAmt1 = BigDecimal.ZERO;
                // 组织躺赢（纵向 实际）【已完成 状态显示】
                BigDecimal taskOrgRewardAmt1 = BigDecimal.ZERO;

                // 组织躺赢（横向 实际）【已完成 状态显示】
                BigDecimal taskOrgHorizRewardAmt1 = BigDecimal.ZERO;

                // 买家返 （实际）【已完成 状态显示】
                BigDecimal taskBuyerRewardAmt1 = BigDecimal.ZERO;
                List<SUserBonusLog> BonusLogList = this.userBonusLogService.findUserBonus(user.getId(),productId,null);
                if(BonusLogList != null && BonusLogList.size() > 0){

                    for(SUserBonusLog usl : BonusLogList){
                        if(usl == null){
                            continue;
                        }
                        if(usl.getStatus() == 0 && usl.getBonusType() == 2){
                            taskTaskRewardAmt0 =  taskTaskRewardAmt0.add(usl.getBonusAmount());
                        }
                        if(usl.getStatus() == 0 && usl.getBonusType() == 3){
                            taskOrgHorizRewardAmt0 =  taskOrgHorizRewardAmt0.add(usl.getBonusAmount());
                        }
                        if(usl.getStatus() == 0 && usl.getBonusType() == 4){
                            taskOrgRewardAmt0 =  taskOrgRewardAmt0.add(usl.getBonusAmount());
                        }
                        if(usl.getStatus() == 0 && usl.getBonusType() == 1){
                            taskBuyerRewardAmt0 =  taskBuyerRewardAmt0.add(usl.getBonusAmount());
                        }
                        if(usl.getStatus() == 1 && usl.getBonusType() == 2){
                            taskTaskRewardAmt1 =  taskTaskRewardAmt1.add(usl.getBonusAmount());
                        }
                        if(usl.getStatus() == 1 && usl.getBonusType() == 3){
                            taskOrgHorizRewardAmt1 =  taskOrgHorizRewardAmt1.add(usl.getBonusAmount());
                        }
                        if(usl.getStatus() == 1 && usl.getBonusType() == 4){
                            taskOrgRewardAmt1 =  taskOrgRewardAmt1.add(usl.getBonusAmount());
                        }
                        if(usl.getStatus() == 1 && usl.getBonusType() == 1){
                            taskBuyerRewardAmt1 =  taskBuyerRewardAmt1.add(usl.getBonusAmount());
                        }
                    }
                }
                // 任务躺赢（实际）  【结算中状态显示】 根据userId productId从bonusLog中读取
                returnMap.put("taskTaskRewardAmt0", taskTaskRewardAmt0);

                // 组织躺赢（横向 实际）【结算中状态显示】
                returnMap.put("taskOrgHorizRewardAmt0", taskOrgHorizRewardAmt0);

                // 组织躺赢（纵向 实际）【结算中状态显示】
                returnMap.put("taskOrgRewardAmt0", taskOrgRewardAmt0);

                // 买家返 （实际）【结算中状态显示】
                returnMap.put("taskBuyerRewardAmt0", taskBuyerRewardAmt0);

                // 任务躺赢（实际）  【已完成 状态显示】 根据userId productId从bonusLog中读取
                returnMap.put("taskTaskRewardAmt1", taskTaskRewardAmt1);

                // 组织躺赢（横向 实际）【已完成 状态显示】
                returnMap.put("taskOrgHorizRewardAmt1", taskOrgHorizRewardAmt1);

                // 组织躺赢（纵向 实际）【已完成 状态显示】
                returnMap.put("taskOrgRewardAmt1", taskOrgRewardAmt1);

                // 买家返 （实际）【已完成 状态显示】
                returnMap.put("taskBuyerRewardAmt1", taskBuyerRewardAmt1);

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

                // 任务线最大数量
                SUserLevel userLevel = this.userLevelService.findByLevelType(user.getUserLevelType());
                returnMap.put("maxTaskNumber", userLevel.getBuyNumber());
            }

            return returnMap;

        } catch (Exception e) {
            log.error("查询商品详情异常", e);
            return null;
        }
    }

    @Override
    public List<Map<String, Object>> getUserCountForProduct() {
        return this.baseMapper.getUserCountForProduct();
    }

    @Override
    public List<Map<String, Object>> getOverCount() {
        return this.baseMapper.getOverCount();
    }

    @Override
    public void updateForOverBatch(List<Map<String,Object>> list) {
        this.baseMapper.updateForOverBatch(list);
    }

    @Override
    public void updateForUserCountBatch(List<Map<String,Object>> list) {
        this.baseMapper.updateForUserCountBatch(list);
    }

}
