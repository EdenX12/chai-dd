package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SBanner;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author MrBird
 */
public interface ISBannerService extends IService<SBanner> {

    /**
     * 广告图片检索
     *
     * @return List<SBanner>
     */
    List<SBanner> findBannerList();
}
