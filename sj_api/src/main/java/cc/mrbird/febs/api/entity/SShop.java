package cc.mrbird.febs.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 商铺类
 * @author MrBird
 */
@Data
@TableName("s_shop")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SShop implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type= IdType.UUID)
    private String id;

    /**
     * 店铺logo
     */
    private String shopLogo;

    /**
     * 店铺名称
     */
    private String shopName;

}
