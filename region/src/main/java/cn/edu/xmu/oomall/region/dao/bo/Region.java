//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.oomall.region.dao.bo;

import cn.edu.xmu.javaee.core.clonefactory.CopyFrom;
import cn.edu.xmu.javaee.core.clonefactory.CopyNotNullTo;
import cn.edu.xmu.javaee.core.clonefactory.CopyTo;
import cn.edu.xmu.javaee.core.exception.BusinessException;
import cn.edu.xmu.javaee.core.model.ReturnNo;
import cn.edu.xmu.javaee.core.model.OOMallObject;
import cn.edu.xmu.javaee.core.model.UserToken;
import cn.edu.xmu.oomall.region.dao.RegionDao;
import cn.edu.xmu.oomall.region.mapper.po.RegionPo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.edu.xmu.javaee.core.model.Constants.MAX_RETURN;

@NoArgsConstructor
@Slf4j
@AllArgsConstructor
@ToString(callSuper = true, doNotUseGetters = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@CopyFrom(RegionPo.class)
@CopyNotNullTo(RegionPo.class)
public class Region implements Serializable {

    /**
     * 两种特殊id
     * 0 -- 最高级地区
     * -1 -- 不存在
     */
    @ToString.Exclude
    @JsonIgnore
    public static final Long TOP_ID = 0L;

    /**
     * 共三种状态
     */
    //有效
    @ToString.Exclude
    @JsonIgnore
    public static final Byte VALID = 0;
    //停用
    @ToString.Exclude
    @JsonIgnore
    public static final Byte SUSPENDED = 1;
    //废弃
    @ToString.Exclude
    @JsonIgnore
    public static final Byte ABANDONED = 2;

    @ToString.Exclude
    @JsonIgnore
    public static final Map<Byte, String> STATUSNAMES = new HashMap<>() {
        {
            put(VALID, "有效");
            put(SUSPENDED, "停用");
            put(ABANDONED, "废弃");
        }
    };

    /**
     * 允许的状态迁移
     */
    @JsonIgnore
    @ToString.Exclude
    private static final Map<Byte, Set<Byte>> toStatus = new HashMap<>() {
        {
            put(VALID, new HashSet<>() {
                {
                    add(SUSPENDED);
                    add(ABANDONED);
                }
            });
            put(SUSPENDED, new HashSet<>() {
                {
                    add(VALID);
                    add(ABANDONED);
                }
            });
        }
    };

    /**
     * 是否允许状态迁移
     * @param status 迁移去的状态
     */
    public boolean allowTransitStatus(Byte status) {
        boolean ret = false;
        assert (!Objects.isNull(this.status)):String.format("地区对象(id=%d)的状态为null",this.getId());
        Set<Byte> allowStatusSet = toStatus.get(this.status);
        if (!Objects.isNull(allowStatusSet)) {
            ret = allowStatusSet.contains(status);
        }
        return ret;
    }

    /**
     * 获得当前状态名称
     */
    @JsonIgnore
    public String getStatusName() {
        return STATUSNAMES.get(this.status);
    }

    @Setter
    @Getter
    private Long id;
    /**
     * 创建者
     */
    @Setter
    @Getter
    private String creator;

    /**
     * 修改者
     */
    @Setter
    @Getter
    private String modifier;

    /**
     * 创建时间
     */
    @Setter
    @Getter
    private Instant gmtCreate;

    /**
     * 修改时间
     */
    @Setter
    @Getter
    private Instant gmtModified;

    @Setter
    @Getter
    private Long pid;
    @Setter
    private Byte level;
    @Setter
    @Getter
    private String areaCode;
    @Setter
    @Getter
    private String zipCode;
    @Setter
    @Getter
    private String cityCode;
    @Setter
    @Getter
    private String name;
    @Setter
    @Getter
    private String shortName;
    @Setter
    @Getter
    private String mergerName;
    @Setter
    @Getter
    private String pinyin;
    @Setter
    @Getter
    private Double lng;
    @Setter
    @Getter
    private Double lat;
    @Setter
    @Getter
    private Byte status;

    @JsonIgnore
    @ToString.Exclude
    private Region parentRegion;

    @Setter
    @JsonIgnore
    @ToString.Exclude
    private RegionDao regionDao;

    @JsonIgnore
    public Region getParentRegion() {
        log.debug("getParentRegion: pid = {}", this.pid);
        if (!TOP_ID.equals(this.id) && Objects.isNull(this.parentRegion) && Objects.nonNull(this.regionDao)) {
            this.parentRegion = this.regionDao.findById(pid);
        }
        return this.parentRegion;
    }

    @JsonIgnore
    public Byte getLevel() {
        if (Objects.isNull(this.level)) {
            if (TOP_ID.equals(this.pid)) {
                this.level = 0;
            } else {
                Region parentRegion = this.getParentRegion();
                if (Objects.nonNull(parentRegion)) {
                    this.level = (byte) (parentRegion.getLevel() + 1);
                }
            }
        }
        return this.level;
    }


    /**
     * 恢复地区
     *
     * @param user 操作者
     * @return 删除的redis key
     */
    public List<String> resume(UserToken user) {
        if (!this.allowTransitStatus(Region.VALID)) {
            // 状态不允许变动
            throw new BusinessException(ReturnNo.STATENOTALLOW, String.format(ReturnNo.STATENOTALLOW.getMessage(), "地区", this.id, STATUSNAMES.get(this.status)));
        }

        List<Region> ancestors = this.regionDao.retrieveParentsRegions(this);
        List<Region> invalidRegions = ancestors.stream().filter(o -> !o.getStatus().equals(Region.VALID)).collect(Collectors.toList());
        if (0 == invalidRegions.size()) {
            return this.changeStatus(Region.VALID, user);
        } else {
            throw new BusinessException(ReturnNo.REGION_INVALID, String.format("上级地区%s不是正常的状态", invalidRegions.get(0).getName()));
        }
    }

    /**
     * 递归修改地区状态
     *
     * @param status 状态
     */
    public List<String> changeStatus(Byte status, UserToken user) {
        log.debug("changeStatus: id = {}, status = {}", this.id, status);

        if (!this.allowTransitStatus(status)) {
            // 状态不允许变动
            throw new BusinessException(ReturnNo.STATENOTALLOW, String.format(ReturnNo.STATENOTALLOW.getMessage(), "地区", this.id, STATUSNAMES.get(this.status)));
        }

        Region region = new Region();
        region.setStatus(status);
        region.setId(this.id);
        String key = this.regionDao.save(region, user);

        List<Region> subRegions = this.regionDao.retrieveSubRegionsById(this.id, 1, MAX_RETURN);
        log.debug("changeStatus: subRegion = {}", subRegions);
        List<String> keys = subRegions.stream().filter(o -> !o.getStatus().equals(Region.ABANDONED))
                .flatMap(subRegion -> {
                    List sub = null;
                    if (subRegion.allowTransitStatus(status)) {
                        sub = subRegion.changeStatus(status, user);
                        return (Stream<String>) sub.stream();
                    }else{
                        return new ArrayList<String>(0).stream();
                    }
                }).distinct().collect(Collectors.toList());
        keys.add(key);
        log.debug("changeStatus: keys = {}", keys);
        return keys;
    }


    /**
     * 创建下级地区
     * 仅仅只能在valid和suspend状态下创建，创建出来的地区是syspend状态
     * @param region 下级地区
     * @param user   创建者
     * @return
     */
    public Region createSubRegion(Region region, UserToken user) {
        if (VALID.equals(this.status) || SUSPENDED.equals(this.status)) {
            region.setStatus(Region.SUSPENDED);
            region.setLevel((byte) (this.getLevel() + 1));
            region.setPid(this.id);
            log.debug("createSubRegion: region = {}", region);
            return this.regionDao.insert(region, user);
        } else {
            throw new BusinessException(ReturnNo.REGION_ABANDONE, String.format(ReturnNo.REGION_ABANDONE.getMessage(), this.id));
        }

    }
}
