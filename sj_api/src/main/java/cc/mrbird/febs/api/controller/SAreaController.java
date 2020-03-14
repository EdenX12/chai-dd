package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.SArea;
import cc.mrbird.febs.api.service.ISAreaService;
import cc.mrbird.febs.common.annotation.Limit;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.domain.FebsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author MrBird
 */
@RestController
@RequestMapping("/api/s-area")
public class SAreaController extends BaseController {

    @Autowired
    private ISAreaService areaService;

    /**
     * 取得所有地区列表信息
     * @return List<SArea>
     */
    @PostMapping("/getAreaList")
    @Limit(key = "getAreaList", period = 60, count = 20, name = "检索所有地区列表接口", prefix = "limit")
    public FebsResponse getAreaList(SArea area) {

        FebsResponse response = new FebsResponse();

        List<SArea> areaList = areaService.findAreaList();

        response.put("code", 0);
        response.data(areaList);

        return response;
    }

}
