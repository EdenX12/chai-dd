package cc.mrbird.febs.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 规格值类
 * @author MrBird
 */
@Data
@TableName("s_product_spec_value")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SProductSpecValue implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type= IdType.UUID)
    private String id;

    /**
     * 商品分类id,s_product_type表主键
     */
    private String productTypeId;

    /**
     * 规格值名称（颜色）
     */
    private String valueType;

    /**
     * 规格值名称（黑色）
     */
    private String valueName;

    /**
     * 规格图片
     */
    private String valueImage;

    /**
     * 规格值排序
     */
    private Integer valueSort;

    /**
     * 创建日期
     */
    private Date createTime;

    /**
     * 更新日期
     */
    private Date updateTime;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 更新人
     */
    private String updateUser;
}
