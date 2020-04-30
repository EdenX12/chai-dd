package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SParams;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ISParamsService extends IService<SParams> {
    List<SParams> queryBykey(String key);
    SParams  queryBykeyForOne(String key);
}
