package cn.edu.xmu.oomall.aftersale.service.strategy.enums;

/**
 * 售后单状态枚举
 * 对应状态流转图中的各个节点
 */
public enum AfterSalesStatus {

    PENDING_AUDIT(0, "待审核"),

    // 审核通过 -> 退货/换货
    WAIT_FOR_INSPECTION(1, "待验收"),

    // 审核通过 -> 服务 (维修等)
    SERVICE_ORDER_CREATED(3, "已生成服务单"),

    // 待验收 -> 验收成功(退货)
    WAIT_FOR_REFUND(5, "待退款"),

    // 待验收 -> 验收成功(换货)
    WAIT_FOR_EXCHANGE(2, "待换货"),

    CANCELLED(6, "取消"),

    REJECTED(7, "未接受"), // 审核不通过

    COMPLETED(8, "已完成");

    private final int code;
    private final String description;

    AfterSalesStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据状态码获取枚举对象
     */
    public static AfterSalesStatus fromCode(int code) {
        for (AfterSalesStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的售后状态码: " + code);
    }
}