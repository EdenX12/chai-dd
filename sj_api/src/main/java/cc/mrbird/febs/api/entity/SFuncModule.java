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
 * 功能模块类
 * @author MrBird
 */
@Data
@TableName("s_func_module")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SFuncModule implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 功能模块名
     */
    private String moduleName;

    /**
     * 图标url
     */
    private String moduleUrl;

    /**
     * 功能区跳转url
     */
    private String jumpUrl;

    /**
     * 状态 0-创建 1-发布 2-下架 3-删除
     */
    private Integer moduleStatus;

    /**
     * 显示顺序
     */
    private Integer sOrder;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 创建者
     */
    private Long createUser;

    /**
     * 更新者
     */
    private Long updateUser;
}
