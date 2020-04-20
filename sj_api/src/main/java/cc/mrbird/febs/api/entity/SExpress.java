package cc.mrbird.febs.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 快递公司类
 * @author MrBird
 */
@Data
@TableName("s_express")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SExpress implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 公司名称
     */
    private String eName;

    /**
     * 状态
     */
    private Integer eState;

    /**
     * 编号
     */
    private String eCode;

    /**
     * 首字母
     */
    private String eLetter;

    /**
     * 1常用0不常用
     */
    private Integer eOrder;

    /**
     * 公司网址
     */
    private String eUrl;

    /**
     * 删除标记
     */
    private Integer isDel;
}
