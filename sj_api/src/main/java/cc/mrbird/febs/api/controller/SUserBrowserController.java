package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.*;
import cc.mrbird.febs.api.service.*;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @author MrBird
 */
@Slf4j
@RestController
@RequestMapping("/api/s-user-browser")
public class SUserBrowserController extends BaseController {

    private String message;

    @Autowired
    private ISUserShareService userShareService;

    @Autowired
    private ISUserBrowserService userBrowserService;

    @Autowired
    private ISUserRelationService userRelationService;

    @Autowired
    private ISUserService userService;

    @Autowired
    private ISUserBeanLogService userBeanLogService;

    @Autowired
    private ISParamsService paramsService;

    /**
     * 点击阅读转发内容
     * 分享ID
     */
    @Log("点击阅读转发内容")
    @Transactional
    @PostMapping("/addBrowser")
    @Limit(key = "addBrowser", period = 60, count = 2000, name = "点击阅读转发内容口", prefix = "limit")
    public FebsResponse addBrowser(String shareId) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {

            SUser user = FebsUtil.getCurrentUser();

            SUserBrowser userBrowser = new SUserBrowser();

            if (user != null) {
                userBrowser.setUserId(user.getId());
                userBrowser.setUnionId(user.getUnionId());
            }

            userBrowser.setShareId(shareId);

            SUserBrowser userBrowserOne = this.userBrowserService.findUserBrowser(userBrowser);

            // 第一次读取 重复查看忽略
            if (userBrowserOne == null) {

                SUserShare userShare = this.userShareService.getById(shareId);

                // 0-APP,1-微信公众号,2-小程序
                userBrowser.setChannel(2);
                userBrowser.setProductId(userShare.getProductId());

                userBrowser = this.userBrowserService.createUserBrowser(userBrowser);

                Integer browserBeanCnt = 0;
                SParams params = this.paramsService.queryBykeyForOne("browser_bean_cnt");
                if (params != null) {
                    browserBeanCnt = Integer.valueOf(params.getPValue());
                }

                // 猎豆流水插入
                SUserBeanLog userBeanLog = new SUserBeanLog();
                userBeanLog.setUserId(userShare.getUserId());
                userBeanLog.setChangeType(7);
                userBeanLog.setChangeAmount(browserBeanCnt);
                userBeanLog.setChangeTime(new Date());
                userBeanLog.setRelationId(userBrowser.getId());
                userBeanLog.setRemark("点击阅读转发内容");
                userBeanLog.setOldAmount(user.getRewardBean());
                this.userBeanLogService.save(userBeanLog);

                // 点击阅读转发内容
                user.setRewardBean(user.getRewardBean() + browserBeanCnt);
                this.userService.updateById(user);

                // 关系追加 (预备队关系)
                SUserRelation userRelation = new SUserRelation();
                if (user != null) {
                    userRelation.setUserId(user.getId());
                    userRelation.setUnionId(user.getUnionId());
                }
                userRelation.setParentId(userShare.getUserId());
                SUserRelation userRelationOne = this.userRelationService.findUserRelation(userRelation);
                if (userRelationOne == null) {
                    // 预备队
                    userRelation.setRelationType(0);
                }
                this.userRelationService.createUserRelation(userRelation);
            }

        } catch (Exception e) {
            message = "点击阅读转发内容";
            response.put("code", 1);
            response.message(message);
            log.error( e.getMessage(),e);
        }

        return response;
    }

}
