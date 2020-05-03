package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.SParams;
import cc.mrbird.febs.api.entity.SUser;
import cc.mrbird.febs.api.entity.SUserBeanLog;
import cc.mrbird.febs.api.entity.SUserFollow;
import cc.mrbird.febs.api.service.ISParamsService;
import cc.mrbird.febs.api.service.ISUserBeanLogService;
import cc.mrbird.febs.api.service.ISUserFollowService;
import cc.mrbird.febs.api.service.ISUserService;
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

import javax.validation.Valid;
import java.util.Date;

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
    private ISUserService userService;

    @Autowired
    private ISUserBeanLogService userBeanLogService;

    @Autowired
    private ISParamsService paramsService;

    /**
     * 新增关注 （任务可不传）
     * 用户ID 任务ID 产品ID
     */
    @Log("新增用户关注")
    @Transactional
    @PostMapping("/addFollow")
    public FebsResponse addFollow(@Valid SUserFollow userFollow) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {

            SUser user = FebsUtil.getCurrentUser();
            userFollow.setUserId(user.getId());

            userFollow = this.userFollowService.createUserFollow(userFollow);

            Integer followBeanCnt = 0;
            SParams params = this.paramsService.queryBykeyForOne("follow_bean_cnt");
            if (params != null) {
                followBeanCnt = Integer.valueOf(params.getPValue());
            }

            // 猎豆流水插入
            SUserBeanLog userBeanLog = new SUserBeanLog();
            userBeanLog.setUserId(user.getId());
            userBeanLog.setChangeType(3);
            userBeanLog.setChangeAmount(followBeanCnt);
            userBeanLog.setChangeTime(new Date());
            userBeanLog.setRelationId(userFollow.getId());
            userBeanLog.setRemark("关联用户关注ID");
            userBeanLog.setOldAmount(user.getCanuseBean());
            this.userBeanLogService.save(userBeanLog);

            // 每关注一个任务（任务广场，转让中心），（+拆豆1颗）
            user.setCanuseBean(user.getCanuseBean() + followBeanCnt);
            this.userService.updateById(user);

        } catch (Exception e) {
            message = "新增用户关注失败";
            response.put("code", 1);
            response.message(message);
            log.error(message, e);
        }

        return response;
    }

    /**
     * 取消关注 （任务可不传）
     * 用户ID 任务ID 产品ID
     */
    @Log("取消用户关注")
    @Transactional
    @PostMapping("/cancelFollow")
    public FebsResponse cancelFollow(@Valid SUserFollow userFollow) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {

            SUser user = FebsUtil.getCurrentUser();
            userFollow.setUserId(user.getId());

            userFollow = this.userFollowService.updateUserFollow(userFollow);

            Integer followBeanCnt = 0;
            SParams params = this.paramsService.queryBykeyForOne("follow_bean_cnt");
            if (params != null) {
                followBeanCnt = Integer.valueOf(params.getPValue());
            }

            // 猎豆流水插入
            SUserBeanLog userBeanLog = new SUserBeanLog();
            userBeanLog.setUserId(user.getId());
            userBeanLog.setChangeType(4);
            userBeanLog.setChangeAmount(followBeanCnt * (-1));
            userBeanLog.setChangeTime(new Date());
            userBeanLog.setRelationId(userFollow.getId());
            userBeanLog.setRemark("关联用户关注ID");
            userBeanLog.setOldAmount(user.getCanuseBean());
            this.userBeanLogService.save(userBeanLog);

            // 每取消一个任务（任务广场，转让中心），（-1颗）
            user.setCanuseBean(user.getCanuseBean() - followBeanCnt);
            this.userService.updateById(user);

        } catch (Exception e) {
            message = "取消用户关注失败";
            response.put("code", 1);
            response.message(message);
            log.error(message, e);
        }

        return response;
    }
}
