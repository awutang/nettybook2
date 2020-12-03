/**
 * 哪里体现了异步？
 */

package com.phei.netty.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.CountDownLatch;


public class AsyncTimeServerHandler implements Runnable {

    private int port;

    CountDownLatch latch;
    AsynchronousServerSocketChannel asynchronousServerSocketChannel;

    public AsyncTimeServerHandler(int port) {
        this.port = port;
        try {
            asynchronousServerSocketChannel = AsynchronousServerSocketChannel.open(); // 创建
            asynchronousServerSocketChannel.bind(new InetSocketAddress(port)); // 绑定ip:port
            System.out.println("The time server is start in port : " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        latch = new CountDownLatch(1);
        doAccept();
        try {
			// 当state为0时返回，accept operation fail时才会退出，因为其他情况下需要服务端可以一直alive接受请求？
			// --是的，否则run执行，线程就结束了，就不能再一直accept客户端连接了
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void doAccept() {
    	// AcceptCompletionHandler作为accept操作成功的通知handler
        asynchronousServerSocketChannel.accept(this, new AcceptCompletionHandler());
    }

}
