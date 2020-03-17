package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.SUser;
import cc.mrbird.febs.api.entity.SUserFollow;
import cc.mrbird.febs.api.entity.SUserLevel;
import cc.mrbird.febs.api.service.ISUserFollowService;
import cc.mrbird.febs.api.service.ISUserLevelService;
import cc.mrbird.febs.api.service.ISUserService;
import cc.mrbird.febs.common.annotation.Log;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.exception.FebsException;
import cc.mrbird.febs.common.utils.FebsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;

/**
 * @author MrBird
 */
@Slf4j
@RestController
@RequestMapping("/api/s-user-follow")
public class SUserFollowController extends BaseController {

    private String message;

    @Autowired
    private ISUserFollowService userFollowService;

    @Autowired
    private ISUserLevelService userLevelService;

    @Autowired
    private ISUserService userService;

    /**
     * 新增关注 （任务可不传）
     * 用户ID 任务ID 产品ID
     */
    @Log("新增用户关注")
    @PostMapping("/addFollow")
    public void addFollow(@Valid SUserFollow userFollow) throws FebsException {

        try {

            SUser user = FebsUtil.getCurrentUser();
            userFollow.setUserId(user.getId());

            this.userFollowService.createUserFollow(userFollow);

            // 每关注一个任务（任务广场，转让中心），（10颗） * 猎人等级倍数
            SUserLevel userLevel = this.userLevelService.getById(user.getUserLevelId());
            user.setCanuseBean(user.getCanuseBean() + userLevel.getBeanRate().multiply(BigDecimal.valueOf(10)).intValue());
            this.userService.updateById(user);

        } catch (Exception e) {
            message = "新增用户关注失败";
            log.error(message, e);
            throw new FebsException(message);
        }
    }

    /**
     * 取消关注 （任务可不传）
     * 用户ID 任务ID 产品ID
     */
    @Log("取消用户关注")
    @PostMapping("/cancelFollow")
    public void cancelFollow(@Valid SUserFollow userFollow) throws FebsException {

        try {

            SUser user = FebsUtil.getCurrentUser();
            userFollow.setUserId(user.getId());

            this.userFollowService.updateUserFollow(userFollow);

            // 每取消一个任务（任务广场，转让中心），（-10颗） * 猎人等级倍数
            SUserLevel userLevel = this.userLevelService.getById(user.getUserLevelId());
            user.setCanuseBean(user.getCanuseBean() - userLevel.getBeanRate().multiply(BigDecimal.valueOf(10)).intValue());
            this.userService.updateById(user);

        } catch (Exception e) {
            message = "取消用户关注失败";
            log.error(message, e);
            throw new FebsException(message);
        }
    }
}
