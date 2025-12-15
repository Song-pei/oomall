//School of Informatics Xiamen University, GPL-3.0 license

package cn.edu.xmu.oomall.freight.domain.bo;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.io.Serializable;
import java.util.*;

/**
 * 商铺对象
 */
@ToString(callSuper = true, doNotUseGetters = true)
@NoArgsConstructor
@Data
public class Shop implements Serializable {

    /**
     * 申请
     */
    @JsonIgnore
    @ToString.Exclude
    public static final Byte NEW = 0;
    /**
     * 下线
     */
    @JsonIgnore
    @ToString.Exclude
    public static final Byte OFFLINE = 1;
    /**
     * 上线
     */
    @JsonIgnore
    @ToString.Exclude
    public static final Byte ONLINE = 2;
    /**
     * 停用
     */
    @JsonIgnore
    @ToString.Exclude
    public static final Byte ABANDON = 3;


    private Long id;
    /**
     * 商铺名称
     */
    private String name;


    /**
     * 状态
     */
    private Byte status;
}
