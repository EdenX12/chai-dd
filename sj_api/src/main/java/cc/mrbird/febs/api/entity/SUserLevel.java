package cc.mrbird.febs.api.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 用户等级类
 * @author MrBird
 */
@Data
@TableName("s_user_level")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SUserLevel implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type= IdType.UUID)
    private String id;

    /**
     * 等级类型
     */
    private Integer levelType;

    /**
     * 等级名称
     */
    private String levelName;

    /**
     * 达标单数
     */
    private Integer minNumber;

    /**
     * 最大单数
     */
    private Integer maxNumber;

    /**
     * 最大购买任务线数量
     */
    private Integer buyNumber;

    /**
     * 最多并行商品数量
     */
    private Integer productNumber;
}
