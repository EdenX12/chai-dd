package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SParams;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Set;

public interface ISParamsService extends IService<SParams> {

    Set<String> queryBykey(String key);

    String  queryBykeyForOne(String key);

    void cacheParams();
}
