package cn.edu.xmu.oomall.aftersale.service.strategy;
import cn.edu.xmu.oomall.aftersale.dao.bo.AftersaleOrder;

public interface TypeStrategy {
    void audit(AftersaleOrder bo, String conclusion);
    void accept(AftersaleOrder bo);
    void complete(AftersaleOrder bo);
    void cancel(AftersaleOrder bo);
}