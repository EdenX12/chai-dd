package cc.mrbird.febs.api.mapper;

import cc.mrbird.febs.api.entity.SUserTaskLine;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author MrBird
 */
public interface SUserTaskLineMapper extends BaseMapper<SUserTaskLine> {
    String queryIdByTask(@Param("taskId")String taskId);


}
