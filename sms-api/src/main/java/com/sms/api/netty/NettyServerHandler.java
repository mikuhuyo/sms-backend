package com.sms.api.netty;

import com.alibaba.fastjson.JSON;
import com.pd.core.utils.SpringUtils;
import com.sms.api.dto.SmsParamsDTO;
import com.sms.api.service.impl.SmsSendServiceImpl;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.net.InetSocketAddress;

/**
 * 服务端处理器
 */
@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        log.info("接收请求开始: ======= 接收报文: " + msg);

        String respMsg = "success";
        try {
            //解析报文
            SmsParamsDTO smsParamsDTO = parseMessage(msg);
            if (null == smsParamsDTO) {
                log.info("报文解析失败");
                return;
            }
            SpringUtils.getBean(SmsSendServiceImpl.class).send(smsParamsDTO);

        } catch (Exception e) {
            respMsg = e.getMessage();
        }
        log.info("返回报文 ========== " + respMsg);
        // 这个地方必须加上"\n", 不然客户端接收不到消息
        ctx.writeAndFlush(respMsg + "\n");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = insocket.getAddress().getHostAddress();
        log.info("收到客户端[ip:" + clientIp + "]连接");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 当出现异常就关闭连接
        ctx.close();
    }

    /**
     * 解析报文
     * <p>
     * 设备不同报文也不同, 直接使用json格式传输
     */
    private SmsParamsDTO parseMessage(String body) {
        if (StringUtils.isBlank(body)) {
            log.warn("报文为空");
            return null;
        }
        body = body.trim();
        // 其它格式的报文需要解析后放入SmsParamsDTO实体
        SmsParamsDTO message = JSON.parseObject(body, SmsParamsDTO.class);
        if (message == null || StringUtils.isBlank(message.getMobile()) || StringUtils.isBlank(message.getSignature()) || StringUtils.isBlank(message.getTemplate())) {
            log.warn("报文内容异常");
            return null;
        }

        return message;
    }
}
