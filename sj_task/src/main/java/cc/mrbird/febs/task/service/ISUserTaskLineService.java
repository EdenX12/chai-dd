package cc.mrbird.febs.task.service;

import cc.mrbird.febs.task.entity.SUserTaskLine;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author MrBird
 */
public interface ISUserTaskLineService extends IService<SUserTaskLine> {

    List<SUserTaskLine> findUserTaskLineList(SUserTaskLine userTaskLine);
}
