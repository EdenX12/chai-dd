package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.STaskLine;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

/**
 * @author MrBird
 */
public interface ISTaskLineService extends IService<STaskLine> {

    /**
     * 查询当前任务线id
     * @param productId
     * @return
     */
    String currentTaskLine(String productId);
    /**
     *  查询产品的任务线未满的最小顺序
     * @param productId
     * @return
     */
    Integer  queryMinLineOrder(@Param("productId") String productId);

}
