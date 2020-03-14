package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SArea;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author MrBird
 */
public interface ISAreaService extends IService<SArea> {


    /**
     * 查询所有地区列表信息
     * @return List<SArea>
     */
    List<SArea> findAreaList();

}
