



package com.phei.netty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class MultiplexerTimeServer implements Runnable {

    private Selector selector;

    private ServerSocketChannel servChannel;

    private volatile boolean stop;

    /**
     * 初始化多路复用器、绑定监听端口
     *
     * @param port
     */
    public MultiplexerTimeServer(int port) {
        try {
            selector = Selector.open(); // 创建selector
            servChannel = ServerSocketChannel.open(); // 创建ServerSocketChannel
            servChannel.configureBlocking(false); // 非阻塞
            servChannel.socket().bind(new InetSocketAddress(port), 1024); // 绑定端口，类似BIO服务端
			// 将ServerSocketChannel注册到selector，selector监听客户端的连接
            servChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("The time server is start in port : " + port);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void stop() {
        this.stop = true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        while (!stop) {
            try {
                // epoll_wait操作开始，timeOut为1s
                selector.select(1000);
				// 返回ready的channel,这个channel是客户端的还是服务器的？
				// --从下面的代码看来，如果是已经连接了的则就是客户端channel,这也确实，本来就是监听客户端channel的read
                // --TODO: 在源码层面是如何将SocketChannel与在服务端中创建的selector关联起来的？因为SocketChannel是注册到客户端selector的，估计是是通过ip port connect时做的
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectedKeys.iterator();
                SelectionKey key = null;
                while (it.hasNext()) {
                    key = it.next();
                    it.remove();
                    try {
                        handleInput(key);
                    } catch (Exception e) {
                        if (key != null) {
                            key.cancel();
                            if (key.channel() != null)
                                key.channel().close();
                        }
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        // 多路复用器关闭后，所有注册在上面的Channel和Pipe等资源都会被自动去注册并关闭，所以不需要重复释放资源
        if (selector != null)
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private void handleInput(SelectionKey key) throws IOException {

        if (key.isValid()) {
            // 处理新接入的请求消息
            if (key.isAcceptable()) { // 根据operation bit 判断网络事件的类型
                // Accept the new connection
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                SocketChannel sc = ssc.accept(); // 三次握手建立连接、创建SocketChannel,TODO:这与在客户端中创建的SocketChannel有啥关系？
                sc.configureBlocking(false);
                // Add the new connection to the selector
                sc.register(selector, SelectionKey.OP_READ);
            }
            if (key.isReadable()) {
                // Read the data
                SocketChannel sc = (SocketChannel) key.channel();
                ByteBuffer readBuffer = ByteBuffer.allocate(1024); // 因为无法预先知道客户端传输过来的数据大小，因此预先开辟1M的缓存
                int readBytes = sc.read(readBuffer); // 非阻塞读 epoll_wait可以设置某一超时时间返回，因此不会被阻塞
                if (readBytes > 0) {
                    // 将limit设置为position,position设置为0，应该是之前read时将position写移动到了有效元素的下一个，
                    // 因此如果之后要读取readBuffer的话需要将limit移到当前position处并position左移到0
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String body = new String(bytes, "UTF-8");
                    System.out.println("The time server receive order : "
                            + body);
                    String currentTime = "QUERY TIME ORDER"
                            .equalsIgnoreCase(body) ? new java.util.Date(
                            System.currentTimeMillis()).toString()
                            : "BAD ORDER";
                    doWrite(sc, currentTime);
                } else if (readBytes < 0) {
                    // 对端链路关闭
                    key.cancel();
                    sc.close();
                } else
                    ; // 读到0字节，忽略
            }
        }
    }

    private void doWrite(SocketChannel channel, String response)
            throws IOException {
        if (response != null && response.trim().length() > 0) {
            byte[] bytes = response.getBytes();
            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
            // 此处position会移到最后write的下一个位置，因此需要flip()准备下次读取writeBuffer的数据将之写到channel中去
            writeBuffer.put(bytes);
            writeBuffer.flip();

            channel.write(writeBuffer); // 可能会发生写半包
        }
    }
}
