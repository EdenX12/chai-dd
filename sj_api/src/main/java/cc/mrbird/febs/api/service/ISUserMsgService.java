package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SUserMsg;
import cc.mrbird.febs.common.domain.QueryRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author MrBird
 */
public interface ISUserMsgService extends IService<SUserMsg> {

    /**
     * 查询所有任务消息，可根据 消息类型、用户ID 按照时间降序
     * 表示件数  前端处理
     * @param userMsg SUserMsg
     * @return List<SUserMsg>
     */
    IPage<SUserMsg> findUserMsgList(SUserMsg userMsg, QueryRequest request);

    /**
     * 查询用户未读消息数量
     * @param userMsg SUserMsg
     * @return int
     */
    public int findUserMsgNotReadCount(SUserMsg userMsg);

}
