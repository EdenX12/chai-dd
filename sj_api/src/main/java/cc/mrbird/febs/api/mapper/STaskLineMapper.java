package cc.mrbird.febs.api.mapper;

import cc.mrbird.febs.api.entity.STaskLine;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author MrBird
 */
public interface STaskLineMapper extends BaseMapper<STaskLine> {

    /**
     * 查询任务线上是否有足够任务
     * @param productId
     * @return
     */
    Integer queryTaskLineCount(@Param("productId") String productId);

    /**
     * 查询产品的任务线未满的最小顺序
     * @param productId
     * @return
     */
    Integer queryMinLineOrder(@Param("productId") String productId);

    /**
     * 查询排序查询任务线Id
     * @param productId
     * @return
     */
    String queryIdByLineOrder(@Param("productId") String productId,
                              @Param("lineOrder") Integer lineOrder);

    /**
     * 批量更新任务线
     * @param list
     */
    void updateUserTaskLineForSettle(List<String> list);

}
