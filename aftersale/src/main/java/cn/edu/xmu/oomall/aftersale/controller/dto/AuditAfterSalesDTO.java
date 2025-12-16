package cn.edu.xmu.oomall.aftersale.controller.dto;

import lombok.Data;

@Data
public class AuditAfterSalesDTO {


    private Boolean confirm;   // true=通过 false=拒绝


    private String conclusion; // 审核意见(同意/不同意)

    private String reason;  //审核理由 (用于拒绝时的详细说明，如 "人为损坏")

    private Integer type;      // 0换货 1退货 2维修
}