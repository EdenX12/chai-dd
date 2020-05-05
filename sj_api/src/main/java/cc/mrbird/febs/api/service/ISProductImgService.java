package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SProductImg;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author MrBird
 */
public interface ISProductImgService extends IService<SProductImg> {

    List<SProductImg> findProductImgList(String productId);
}
