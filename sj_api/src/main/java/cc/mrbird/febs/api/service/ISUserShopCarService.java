package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SUserShopCar;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author MrBird
 */
public interface ISUserShopCarService extends IService<SUserShopCar> {

    SUserShopCar addUserShopCar(SUserShopCar userShopCar);
}
