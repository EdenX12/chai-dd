package cc.mrbird.febs.task.service;

import cc.mrbird.febs.task.entity.SOrderProduct;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author MrBird
 */
public interface ISOrderProductService extends IService<SOrderProduct> {

    List<SOrderProduct> findOrderProductList(String orderDetailId);
}
