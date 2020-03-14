package cc.mrbird.febs.api.entity;

import java.math.BigDecimal;
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

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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
     * 猎豆百分比
     */
    private BigDecimal beanRate;

    /**
     * 下级贡献收益百分比
     */
    private BigDecimal incomeRate;

    /**
     * 最大购买分数
     */
    private Integer buyNumber;
}
