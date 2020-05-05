package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SUserFollow;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author MrBird
 */
public interface ISUserFollowService extends IService<SUserFollow> {

    /**
     * 检索用户是否已关注
     *
     * @param userFollow SUserFollow
     */
    SUserFollow findUserFollowDetail(SUserFollow userFollow);

    /**
     * 新增用户关注 （可针对产品 也可针对任务）
     *
     * @param userFollow SUserFollow
     */
    SUserFollow createUserFollow(SUserFollow userFollow);

    /**
     * 取消用户关注 （可针对产品 也可针对任务）
     *
     * @param userFollow SUserFollow
     */
    SUserFollow updateUserFollow(SUserFollow userFollow);
}
