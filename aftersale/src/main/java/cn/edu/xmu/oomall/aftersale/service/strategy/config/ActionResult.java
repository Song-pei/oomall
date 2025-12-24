package cn.edu.xmu.oomall.aftersale.service.strategy.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActionResult<T> {
    /**
     * 业务返回的数据（泛型 T），可以为 null
     */
    private T data;

    /**
     * 执行 Action 后的下一个状态
     */
    private Integer nextStatus;

    /**
     * 静态快捷方法：仅返回状态，不带数据
     */
    public static <T> ActionResult<T> status(Integer nextStatus) {
        return new ActionResult<>(null, nextStatus);
    }

    /**
     * 静态快捷方法：带数据和状态
     */
    public static <T> ActionResult<T> success(T data, Integer nextStatus) {
        return new ActionResult<>(data, nextStatus);
    }
}