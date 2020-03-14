package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SUserFollow;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author MrBird
 */
public interface ISUserFollowService extends IService<SUserFollow> {

    /**
     * 新增用户关注 （可针对产品 也可针对任务）
     *
     * @param userFollow SUserFollow
     */
    void createUserFollow(SUserFollow userFollow);

    /**
     * 取消用户关注 （可针对产品 也可针对任务）
     *
     * @param userFollow SUserFollow
     */
    void updateUserFollow(SUserFollow userFollow);
}
