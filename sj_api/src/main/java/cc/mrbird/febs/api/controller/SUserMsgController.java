package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.SUser;
import cc.mrbird.febs.api.entity.SUserMsg;
import cc.mrbird.febs.api.service.ISUserMsgService;
import cc.mrbird.febs.common.annotation.Limit;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.domain.FebsResponse;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.utils.FebsUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author MrBird
 */
@RestController
@RequestMapping("/api/s-user-msg")
public class SUserMsgController extends BaseController {

    @Autowired
    private ISUserMsgService userMsgService;

    /**
     * 取得所有任务消息
     * @return List<SUserMsg>
     */
    @PostMapping("/getUserMsg")
    @Limit(key = "getUserMsg", period = 60, count = 20, name = "检索任务消息接口", prefix = "limit")
    public FebsResponse getUserMsg(QueryRequest queryRequest, SUserMsg userMsg)  {

        FebsResponse response = new FebsResponse();

        // 如果userType!=1 只能查询当前登录人的
        SUser user = FebsUtil.getCurrentUser();
        if (userMsg.getMsgType() == null) {
            userMsg.setUserId(user.getId());
        }

        Map<String, Object> userMsgPageList = getDataTable(userMsgService.findUserMsgList(userMsg, queryRequest));

        response.put("code", 0);
        response.data(userMsgPageList);

        return response;
    }

}
