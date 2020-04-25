package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SActivity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author MrBird
 */
public interface ISActivityService extends IService<SActivity> {

    /**
     * 活动区域图片检索
     *
     * @return List<SActivity>
     */
    List<SActivity> findActivityList();
}
