package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.SUserFollow;
import cc.mrbird.febs.api.service.ISUserFollowService;
import cc.mrbird.febs.common.annotation.Log;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.exception.FebsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

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

    /**
     * 新增关注 （任务可不传）
     * 用户ID 任务ID 产品ID
     */
    @Log("新增用户关注")
    @PostMapping("/addFollow")
    public void addFollow(@Valid SUserFollow userFollow) throws FebsException {

        try {
            this.userFollowService.createUserFollow(userFollow);
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
            this.userFollowService.updateUserFollow(userFollow);
        } catch (Exception e) {
            message = "取消用户关注失败";
            log.error(message, e);
            throw new FebsException(message);
        }
    }
}
