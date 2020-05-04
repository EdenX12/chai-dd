package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SUserTaskLine;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author MrBird
 */
public interface ISUserTaskLineService extends IService<SUserTaskLine> {

    List<SUserTaskLine> findUserTaskLineList(SUserTaskLine userTaskLine);

    List<SUserTaskLine> queryByTaskLineId(String taskLineId);
}
