package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SUserShare;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author MrBird
 */
public interface ISUserShareService extends IService<SUserShare> {
    /**
     * 查询最新的一条浏览
     * @param productId
     * @param userId
     * @return
     */
    String getCurrentShareId(String productId,String userId);

}
