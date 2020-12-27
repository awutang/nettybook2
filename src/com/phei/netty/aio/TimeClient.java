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

/**
 * @author lilinfeng
 * @version 1.0
 * @date 2014年2月14日
 */
public class TimeClient {

    /**
     * @param args
     */
    public static void main(String[] args) {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                // 采用默认值
            }

        }
        // 实际项目中不需要单独开启一个线程去发起连接，因为底层都是通过系统回调实现的
        // --意思是connect是异步操作，且write与read在AsyncTimeClientHandler.complete中可通过回调实现，因此相比NIO中
        // 的代码，并不需要开启一个线程去做connect与write与read
        // TODO:系统回调如何实现异步的？？？--通过观察javavisualVm 查看线程执行堆栈可以看到AsyncTimeClientHandler.complete中的方法
        // 是由ThreadPoolExecutor执行AsynchronousChannelGroupImpl.run实现 --这个明天debug下。
        new Thread(new AsyncTimeClientHandler("127.0.0.1", port),
                "AIO-AsyncTimeClientHandler-001").start();

    }
}
