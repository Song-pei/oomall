package cn.edu.xmu.oomall.aftersale.controller.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ServiceOrderCreateDTO {


    private Integer type;          // 0上门 1寄件 2线下


    private Consignee consignee;   // 可选收件人信息

    @Builder
    @Data
    public static class Consignee {

        private String name;



        private String mobile;


        private Integer regionId;


        private String address;
    }
}