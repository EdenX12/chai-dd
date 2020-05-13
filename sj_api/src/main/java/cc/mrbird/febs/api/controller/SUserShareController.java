package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.*;
import cc.mrbird.febs.api.service.*;
import cc.mrbird.febs.common.annotation.Log;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.domain.FebsResponse;
import cc.mrbird.febs.common.utils.FebsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @author MrBird
 */
@Slf4j
@RestController
@RequestMapping("/api/s-user-share")
public class SUserShareController extends BaseController {

    private String message;

    @Autowired
    private ISUserShareService userShareService;

    @Autowired
    private ISUserService userService;

    @Autowired
    private ISUserBeanLogService userBeanLogService;

    @Autowired
    private ISParamsService paramsService;

    /**
     * 转发分享商品
     * 商品ID 上级分享ID
     */
    @Log("转发分享商品")
    @Transactional
    @PostMapping("/addShare")
    public FebsResponse addShare(String productId, String parentId) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {

            SUser user = FebsUtil.getCurrentUser();

            int shareCnt = this.userShareService.findUserShareCount(user.getId(), productId);

            if (shareCnt == 0) {

                SUserShare userShare = new SUserShare();

                userShare.setUserId(user.getId());
                userShare.setProductId(productId);
                userShare.setParentId(parentId);

                userShare = this.userShareService.createUserShare(userShare);

                Integer shareBeanCnt = 0;
                SParams params = this.paramsService.queryBykeyForOne("share_bean_cnt");
                if (params != null) {
                    shareBeanCnt = Integer.valueOf(params.getPValue());
                }

                // 猎豆流水插入
                SUserBeanLog userBeanLog = new SUserBeanLog();
                userBeanLog.setUserId(user.getId());
                userBeanLog.setChangeType(3);
                userBeanLog.setChangeAmount(shareBeanCnt);
                userBeanLog.setChangeTime(new Date());
                userBeanLog.setRelationId(userShare.getId());
                userBeanLog.setRemark("转发分享商品ID");
                userBeanLog.setOldAmount(user.getRewardBean());
                this.userBeanLogService.save(userBeanLog);

                // 转发分享商品详情
                user.setRewardBean(user.getRewardBean() + shareBeanCnt);
                this.userService.updateById(user);
            }

        } catch (Exception e) {
            message = "转发分享商品";
            response.put("code", 1);
            response.message(message);
            log.error( e.getMessage());
        }

        return response;
    }

}
