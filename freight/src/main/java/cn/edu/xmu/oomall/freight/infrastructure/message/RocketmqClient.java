package cn.edu.xmu.oomall.freight.infrastructure.message;

import cn.edu.xmu.javaee.core.model.UserToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@RequiredArgsConstructor
public class RocketmqClient {

    private final RocketMQTemplate rocketMQTemplate;
    /**
     * 向product模块发消息，删除商品与运费模板的关系
     * @param id
     * @param shopId
     */
    public void sendDelTemplateMsg(UserToken user, Long shopId, Long id){
        String json = String.format("{'shopId':%d, 'id':%d}", shopId, id);
        Message msg = MessageBuilder.withPayload(json).setHeader("user", user).build();
        this.rocketMQTemplate.sendMessageInTransaction("Del-Template", msg, null);
    }

}
