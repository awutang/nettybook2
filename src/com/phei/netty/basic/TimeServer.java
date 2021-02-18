package com.phei.netty.basic;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 采用netty框架实现的NIO server
 * 1.如果采用jdk nio,需要很多行代码才能实现server,但是采用netty就很简单，这也是选择netty实现NIO的原因之一了，使用简单嘛
 */
public class TimeServer {

    public void bind(int port) throws Exception {
        // 配置服务端的NIO线程组,处理网络事件，其实这就是reactor线程组，以下两个一个用于接收客户端连接、一个用于读写
        // 但是NioEventLoop中连接、读写都可以操作，所以为啥要搞两个线程组？--一个负责接收客户端连接，一个负责读写
        // 线程组的功能是如何区分开的？--服务端接收客户端连接后生成客户端channel，这些channel对应的eventLoop是workerGroup的

        // 其实是创建了一个线程池,用于服务端接收客户端的连接
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // 处理连接后的读写
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // ServerBootstrap是netty用于启动NIO服务端的辅助启动类，目的是降低服务端的开发复杂度
            // 如何降低的？jdknio的代码需要十多行才能完成基本的消息读取与发送，用了ServerBootStrap就不需要了
            ServerBootstrap b = new ServerBootstrap();
            // 将两个线程组传入serverBootStrap中
            b.group(bossGroup, workerGroup)
                    // 设置即将创建的channel为NioServerSocketChannel,NioServerSocketChannel对应于jdk NIO中的ServerSocketChannel
                    .channel(NioServerSocketChannel.class)
                    // 设置channel的tcp参数
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    // 绑定IO事件的处理类为ChildChannelHandler
                    .childHandler(new ChildChannelHandler());
            // bind()绑定监听端口，sync()同步等待绑定操作完成，ChannelFuture用于异步操作的通知回调
            // TODO:epoll是IO多路复用给的，并不是AIO，所以哪些是异步操作？--这里ServerBootstrap设置的异步指的是操作系统交互，比如ServerSocketChannel.accept()
            ChannelFuture f = b.bind(port).sync();

            // 等待服务端监听端口关闭操作完成
            f.channel().closeFuture().sync();
        } finally {
            // 优雅退出，释放线程池资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel arg0) throws Exception {
            arg0.pipeline().addLast(new TimeServerHandler());
        }

    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                // 采用默认值
            }
        }
        new TimeServer().bind(port);
    }
}
