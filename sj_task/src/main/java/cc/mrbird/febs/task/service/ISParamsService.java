package cc.mrbird.febs.task.service;

import cc.mrbird.febs.task.entity.SParams;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ISParamsService extends IService<SParams> {

    SParams  queryBykeyForOne(String key);
}
