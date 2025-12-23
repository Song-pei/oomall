package cn.edu.xmu.oomall.aftersale.controller.dto;

///此类用于对应服务模块根据服务单id返回服务单的api
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceFind {
    Long id;
    Byte  type;
    String consignee;
    @Builder
    @Data
    public static class Region {
        Long  id;
        String name;
    }
    String address;
    String mobile;
    String status;
    private Maintainer maintainer;
    private  Creator creator;
    private  Modifier modifier;
    private Region region;
    String maintainerName;
    String maintainerMobile;
    String description;
    String result;

    @Data
    public  class Maintainer{
        Long  id;
        String name;
        // 将构造方法改为 public 权限
        public Maintainer() {
            this.id = id;
            this.name = name;
        }
    }


    @Data
    public  class Creator{
        Long  id;
        String name;
    }
    String gmtCreate;
    String gmtModified;

    @Data
    public  class Modifier{
        Long  id;
        String name;
    }

}
