package cn.edu.xmu.oomall.aftersale.controller.dto;

import lombok.Data;

@Data
public class ServiceOrderCreateDTO {


    private Integer type;          // 0上门 1寄件 2线下


    private Consignee consignee;   // 可选收件人信息

    @Data
    public static class Consignee {

        private String name;



        private String mobile;


        private Integer regionId;


        private String address;
    }
}