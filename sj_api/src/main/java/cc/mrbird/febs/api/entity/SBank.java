package cc.mrbird.febs.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
/**
 * 用户类
 * @author pq
 */
@Data
@TableName("s_bank")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SBank implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type= IdType.UUID)
    private Integer id;
    private String bankName;//银行名称
    private String bankCode; //银行编码
    private String status; //'0 可用 1 不可用'
    private String bankIcon;//银行图标
}
