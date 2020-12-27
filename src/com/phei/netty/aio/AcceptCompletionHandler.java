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
package com.phei.netty.aio;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * 对accept operation回调
 */
public class AcceptCompletionHandler implements
        CompletionHandler<AsynchronousSocketChannel, AsyncTimeServerHandler> {

    /**
     * 当accept operation成功后
     * @param result
     * @param attachment
     */
    @Override
    public void completed(AsynchronousSocketChannel result,
                          AsyncTimeServerHandler attachment) {
        // 接受新的客户端的连接，连接成功后回调completed（) 每当有一个客户端连接成功后，再异步接收新的客户端连接
        // TODO:此处accept应该是开了一个线程future在异步等待accept吧，否则哪里体现异步性和回调呢？--后续分析源码
        attachment.asynchronousServerSocketChannel.accept(attachment, this);
        ByteBuffer buffer = ByteBuffer.allocate(1024); // 分配1MB缓存即缓冲区
        // 发起异步读，当读取成功后回调ReadCompletionHandler
        result.read(buffer, buffer, new ReadCompletionHandler(result));
    }

    @Override
    public void failed(Throwable exc, AsyncTimeServerHandler attachment) {
        exc.printStackTrace();
        attachment.latch.countDown(); // accept失败时减1，TODO：但是attachment实例只有一个，难道只要有一个accept失败就减1，然后服务端执行完程序结束吗？
    }

}
