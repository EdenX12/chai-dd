package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SProductType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author MrBird
 */
public interface ISProductTypeService extends IService<SProductType> {

    /**
     * 通过产品分类状态查找所有分类
     *
     * @param typeStatus 分类状态（0：不可用 :1：可用）
     * @return SUser
     */
    List<SProductType> findByTypeStatus(int typeStatus);
}
