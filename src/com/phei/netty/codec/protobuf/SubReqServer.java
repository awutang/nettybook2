/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.phei.netty.codec.protobuf;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @author lilinfeng
 * @version 1.0
 * @date 2014年2月14日
 */
public class SubReqServer {
    public void bind(int port) throws Exception {
        // 配置服务端的NIO线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {

                        	// 解码器从前往后，编码器从后往前
                            /**
                             * io.netty.handler.codec.DecoderException: com.google.protobuf.InvalidProtocolBufferException: Protocol message tag had invalid wire type.
                             * 	at io.netty.handler.codec.MessageToMessageDecoder.channelRead(MessageToMessageDecoder.java:105)
                             * 	at io.netty.channel.ChannelHandlerInvokerUtil.invokeChannelReadNow(ChannelHandlerInvokerUtil.java:74)
                             * 	at io.netty.channel.DefaultChannelHandlerInvoker.invokeChannelRead(DefaultChannelHandlerInvoker.java:138)
                             * 	at io.netty.channel.DefaultChannelHandlerContext.fireChannelRead(DefaultChannelHandlerContext.java:321)
                             * 	at io.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:883)
                             * 	at io.netty.channel.nio.AbstractNioByteChannel$NioByteUnsafe.read(AbstractNioByteChannel.java:150)
                             * 	at io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:588)
                             * 	at io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:528)
                             * 	at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:409)
                             * 	at io.netty.util.concurrent.SingleThreadEventExecutor$5.run(SingleThreadEventExecutor.java:870)
                             * 	at java.lang.Thread.run(Thread.java:748)
                             * Caused by: com.google.protobuf.InvalidProtocolBufferException: Protocol message tag had invalid wire type.
                             * 	at com.google.protobuf.InvalidProtocolBufferException.invalidWireType(InvalidProtocolBufferException.java:99)
                             * 	at com.google.protobuf.UnknownFieldSet$Builder.mergeFieldFrom(UnknownFieldSet.java:498)
                             * 	at com.google.protobuf.GeneratedMessage.parseUnknownField(GeneratedMessage.java:193)
                             * 	at com.phei.netty.codec.protobuf.SubscribeReqProto$SubscribeReq.<init>(SubscribeReqProto.java:144)
                             * 	at com.phei.netty.codec.protobuf.SubscribeReqProto$SubscribeReq.<init>(SubscribeReqProto.java:96)
                             * 	at com.phei.netty.codec.protobuf.SubscribeReqProto$SubscribeReq$1.parsePartialFrom(SubscribeReqProto.java:207)
                             * 	at com.phei.netty.codec.protobuf.SubscribeReqProto$SubscribeReq$1.parsePartialFrom(SubscribeReqProto.java:202)
                             * 	at com.google.protobuf.AbstractParser.parsePartialFrom(AbstractParser.java:141)
                             * 	at com.google.protobuf.AbstractParser.parseFrom(AbstractParser.java:176)
                             * 	at com.google.protobuf.AbstractParser.parseFrom(AbstractParser.java:182)
                             * 	at com.google.protobuf.AbstractParser.parseFrom(AbstractParser.java:49)
                             * 	at io.netty.handler.codec.protobuf.ProtobufDecoder.decode(ProtobufDecoder.java:114)
                             * 	at io.netty.handler.codec.protobuf.ProtobufDecoder.decode(ProtobufDecoder.java:62)
                             * 	at io.netty.handler.codec.MessageToMessageDecoder.channelRead(MessageToMessageDecoder.java:93)
                             * 	... 10 more
                             *
                             * 	如果去掉ProtobufVarint32FrameDecoder就会发生读半包问题
                             */
//                             ch.pipeline().addLast(
//                             new ProtobufVarint32FrameDecoder());
                            ch.pipeline().addLast(
                                    new ProtobufDecoder(
                                            SubscribeReqProto.SubscribeReq
                                                    .getDefaultInstance()));
                            ch.pipeline().addLast(
                                    new ProtobufVarint32LengthFieldPrepender());
                            ch.pipeline().addLast(new ProtobufEncoder());
                            ch.pipeline().addLast(new SubReqServerHandler());
                        }
                    });

            // 绑定端口，同步等待成功
            ChannelFuture f = b.bind(port).sync();

            // 等待服务端监听端口关闭
            f.channel().closeFuture().sync();
        } finally {
            // 优雅退出，释放线程池资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                // 采用默认值
            }
        }
        new SubReqServer().bind(port);
    }
}
