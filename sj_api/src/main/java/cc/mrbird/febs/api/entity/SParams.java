package cc.mrbird.febs.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 参数类
 * @author MrBird
 */
@Data
@TableName("s_params")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SParams {
    /**
     * 主键ID
     */
    @TableId(value = "id", type= IdType.UUID)
    private Integer id;
    /**
     * key
     */
    private String pKey;
    /**
     * 值
     */
    private String pValue;
    /**
     * 备注
     */
    private String remark;
    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;
}
