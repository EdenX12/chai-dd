package cc.mrbird.febs.task.service;

import cc.mrbird.febs.task.entity.STaskLine;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author MrBird
 */
public interface ISTaskLineService extends IService<STaskLine> {

    /**
     * 根据条件检索任务线
     * @param taskLine STaskLine
     * @return List<STaskLine>
     */
    List<STaskLine> findTaskLineList(STaskLine taskLine);
}
