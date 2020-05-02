package cc.mrbird.febs.api.mapper;

import cc.mrbird.febs.api.entity.STaskLine;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author MrBird
 */
public interface STaskLineMapper extends BaseMapper<STaskLine> {

    /**
     * 查询某产品当前任务线
     * @param productId
     * @return
     */
   String  queryCurrentTaskLine(@Param("productId") String productId);

    /**
     * 查询产品的任务线未满的最小顺序
     * @param productId
     * @return
     */
    Integer  queryMinLineOrder(@Param("productId") String productId);


}
