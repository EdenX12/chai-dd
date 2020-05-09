package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SFuncModule;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author MrBird
 */
public interface ISFuncModuleService extends IService<SFuncModule> {

    List<SFuncModule> findFuncModuleList();
}
