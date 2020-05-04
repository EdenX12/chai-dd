package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SOrderProduct;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author MrBird
 */
public interface ISOrderProductService extends IService<SOrderProduct> {

    SOrderProduct addOrderProduct(SOrderProduct orderProduct);

    List<SOrderProduct> findOrderProductList(String orderDetailId);
}
