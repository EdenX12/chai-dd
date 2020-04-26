package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SUserShopCar;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author MrBird
 */
public interface ISUserShopCarService extends IService<SUserShopCar> {

    SUserShopCar addUserShopCar(SUserShopCar userShopCar);

    List<SUserShopCar> findUserShopCarList(SUserShopCar userShopCar);

    void deleteUserShopCar(SUserShopCar userShopCar);
}
