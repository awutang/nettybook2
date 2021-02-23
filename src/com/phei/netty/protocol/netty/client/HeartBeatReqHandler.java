/*
 * Copyright 2013-2018 Lilinfeng.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.phei.netty.protocol.netty.client;

import com.phei.netty.protocol.netty.MessageType;
import com.phei.netty.protocol.netty.struct.Header;
import com.phei.netty.protocol.netty.struct.NettyMessage;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Lilinfeng
 * @version 1.0
 * @date 2014年3月15日
 *
 * 私有协议栈-心跳检测（客户端发送）
 */
public class HeartBeatReqHandler extends ChannelHandlerAdapter {

    private static final Log LOG = LogFactory.getLog(HeartBeatReqHandler.class);

    private volatile ScheduledFuture<?> heartBeat;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        NettyMessage message = (NettyMessage) msg;
        // 1. 握手成功，主动发送心跳消息
        if (message.getHeader() != null
                && message.getHeader().getType() == MessageType.LOGIN_RESP
                .value()) {
            // 添加心跳检测延时任务，每5s发送一次心跳请求给服务端
            heartBeat = ctx.executor().scheduleAtFixedRate(
                    new HeartBeatReqHandler.HeartBeatTask(ctx), 0, 5000,
                    TimeUnit.MILLISECONDS);
        } else if (message.getHeader() != null
                && message.getHeader().getType() == MessageType.HEARTBEAT_RESP
                .value()) {
            // 2. 收到心跳应答消息
            System.out.println("Client receive server heart beat message : ---> "
                    + message);
        } else
            ctx.fireChannelRead(msg);
    }

    private class HeartBeatTask implements Runnable {
        private final ChannelHandlerContext ctx;

        public HeartBeatTask(final ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        /**
         * 发送心跳检测请求到服务端
         */
        @Override
        public void run() {
            NettyMessage heatBeat = buildHeatBeat();
            System.out.println("Client send heart beat messsage to server : ---> "
                    + heatBeat);
            ctx.writeAndFlush(heatBeat);
        }

        /**
         * 心跳检测的request，由于只做心跳检测，因此不需要body
         * @return
         */
        private NettyMessage buildHeatBeat() {
            NettyMessage message = new NettyMessage();
            Header header = new Header();
            header.setType(MessageType.HEARTBEAT_REQ.value());
            message.setHeader(header);
            return message;
        }
    }

    /**
     * 心跳检测超时（服务端的心跳响应超时了），从ReadTimeoutHandler执行到此
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
        if (heartBeat != null) {
            // 取消心跳检测任务
            heartBeat.cancel(true);
            heartBeat = null;
        }
        ctx.fireExceptionCaught(cause);
    }
}
