package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SUserTaskLine;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

/**
 * @author MrBird
 */
public interface ISUserTaskLineService extends IService<SUserTaskLine> {

    String  queryIdByTask(String taskId);


}
