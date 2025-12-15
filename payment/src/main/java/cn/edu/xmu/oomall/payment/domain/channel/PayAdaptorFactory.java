//School of Informatics Xiamen University, GPL-3.0 license

package cn.edu.xmu.oomall.payment.domain.channel;

import cn.edu.xmu.oomall.payment.domain.bo.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PayAdaptorFactory {

    private final ApplicationContext context;

    /**
     * 返回商铺的支付渠道服务
     * 简单工厂模式
     *
     * @param channel 支付渠道
     * @return
     * @author Ming Qiu
     * <p>
     * date: 2022-11-06 18:05
     */
    public PayAdaptor createPayAdaptor(Channel channel) {
        log.debug("createPayAdaptor: channel = {}",channel);
        return (PayAdaptor) context.getBean(channel.getBeanName());
    }
}
