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
package com.phei.netty.frame.delimiter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author lilinfeng
 * @version 1.0
 * @date 2014年2月14日
 */
@Sharable
public class EchoServerHandler extends ChannelHandlerAdapter {

    int counter = 0;

	/**
	 * /Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/bin/java -Dvisualvm.id=1645420135326156 "-javaagent:/Applications/IntelliJ IDEA.app/Contents/lib/idea_rt.jar=61300:/Applications/IntelliJ IDEA.app/Contents/bin" -Dfile.encoding=UTF-8 -classpath /Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/jre/lib/charsets.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/jre/lib/deploy.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/jre/lib/ext/cldrdata.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/jre/lib/ext/dnsns.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/jre/lib/ext/jaccess.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/jre/lib/ext/jfxrt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/jre/lib/ext/localedata.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/jre/lib/ext/nashorn.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/jre/lib/ext/sunec.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/jre/lib/ext/sunjce_provider.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/jre/lib/ext/sunpkcs11.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/jre/lib/ext/zipfs.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/jre/lib/javaws.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/jre/lib/jce.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/jre/lib/jfr.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/jre/lib/jfxswt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/jre/lib/jsse.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/jre/lib/management-agent.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/jre/lib/plugin.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/jre/lib/resources.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/jre/lib/rt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/lib/ant-javafx.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/lib/dt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/lib/javafx-mx.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/lib/jconsole.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/lib/packager.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/lib/sa-jdi.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_241.jdk/Contents/Home/lib/tools.jar:/Users/ayutang/mygithub/nettybook2/target/classes:/Users/ayutang/mygithub/netty/example/target/classes:/Users/ayutang/mygithub/netty/transport/target/classes:/Users/ayutang/mygithub/netty/buffer/target/classes:/Users/ayutang/mygithub/netty/common/target/classes:/Users/ayutang/mygithub/netty/transport-sctp/target/classes:/Users/ayutang/mygithub/netty/codec/target/classes:/Users/ayutang/mygithub/netty/handler/target/classes:/Users/ayutang/mygithub/netty/codec-http/target/classes:/Users/ayutang/mygithub/netty/codec-socks/target/classes:/Users/ayutang/mavenRepository/com/jcraft/jzlib/1.1.2/jzlib-1.1.2.jar:/Users/ayutang/mavenRepository/org/javassist/javassist/3.18.0-GA/javassist-3.18.0-GA.jar:/Users/ayutang/mavenRepository/com/yammer/metrics/metrics-core/2.2.0/metrics-core-2.2.0.jar:/Users/ayutang/mavenRepository/org/slf4j/slf4j-api/1.7.2/slf4j-api-1.7.2.jar:/Users/ayutang/mygithub/netty/transport-udt/target/classes:/Users/ayutang/mavenRepository/com/barchart/udt/barchart-udt-bundle/2.3.0/barchart-udt-bundle-2.3.0.jar:/Users/ayutang/mygithub/netty/transport-rxtx/target/classes:/Users/ayutang/mavenRepository/org/rxtx/rxtx/2.1.7/rxtx-2.1.7.jar:/Users/ayutang/mavenRepository/com/google/protobuf/protobuf-java/2.5.0/protobuf-java-2.5.0.jar:/Users/ayutang/mavenRepository/org/jboss/marshalling/jboss-marshalling/1.4.10.Final/jboss-marshalling-1.4.10.Final.jar:/Users/ayutang/mavenRepository/org/jboss/marshalling/jboss-marshalling-serial/1.4.10.Final/jboss-marshalling-serial-1.4.10.Final.jar:/Users/ayutang/mavenRepository/org/apache/bcel/bcel/5.2/bcel-5.2.jar:/Users/ayutang/mavenRepository/jakarta-regexp/jakarta-regexp/1.4/jakarta-regexp-1.4.jar:/Users/ayutang/mavenRepository/stax/stax-api/1.0.1/stax-api-1.0.1.jar:/Users/ayutang/mavenRepository/org/codehaus/woodstox/wstx-asl/3.2.9/wstx-asl-3.2.9.jar:/Users/ayutang/mavenRepository/org/jibx/jibx-bind/1.2.5/jibx-bind-1.2.5.jar:/Users/ayutang/mavenRepository/bcel/bcel/5.1/bcel-5.1.jar:/Users/ayutang/mavenRepository/regexp/regexp/1.2/regexp-1.2.jar:/Users/ayutang/mavenRepository/org/jibx/jibx-run/1.2.5/jibx-run-1.2.5.jar:/Users/ayutang/mavenRepository/org/jibx/jibx-extras/1.2.5/jibx-extras-1.2.5.jar:/Users/ayutang/mavenRepository/dom4j/dom4j/1.6.1/dom4j-1.6.1.jar:/Users/ayutang/mavenRepository/xml-apis/xml-apis/1.0.b2/xml-apis-1.0.b2.jar:/Users/ayutang/mavenRepository/org/jdom/jdom/1.1.3/jdom-1.1.3.jar:/Users/ayutang/mavenRepository/org/jibx/jibx-schema/1.2.5/jibx-schema-1.2.5.jar:/Users/ayutang/mavenRepository/org/jibx/jibx-tools/1.2.5/jibx-tools-1.2.5.jar:/Users/ayutang/mavenRepository/org/jibx/config/3rdparty/org/eclipse/org.eclipse.core.contenttype/3.4.100.v20110423-0524/org.eclipse.core.contenttype-3.4.100.v20110423-0524.jar:/Users/ayutang/mavenRepository/org/jibx/config/3rdparty/org/eclipse/org.eclipse.core.jobs/3.5.100.v20110404/org.eclipse.core.jobs-3.5.100.v20110404.jar:/Users/ayutang/mavenRepository/org/jibx/config/3rdparty/org/eclipse/org.eclipse.core.resources/3.7.100.v20110510-0712/org.eclipse.core.resources-3.7.100.v20110510-0712.jar:/Users/ayutang/mavenRepository/org/jibx/config/3rdparty/org/eclipse/org.eclipse.core.runtime/3.7.0.v20110110/org.eclipse.core.runtime-3.7.0.v20110110.jar:/Users/ayutang/mavenRepository/org/jibx/config/3rdparty/org/eclipse/org.eclipse.equinox.common/3.6.0.v20110523/org.eclipse.equinox.common-3.6.0.v20110523.jar:/Users/ayutang/mavenRepository/org/jibx/config/3rdparty/org/eclipse/org.eclipse.equinox.preferences/3.4.0.v20110502/org.eclipse.equinox.preferences-3.4.0.v20110502.jar:/Users/ayutang/mavenRepository/org/jibx/config/3rdparty/org/eclipse/org.eclipse.jdt.core/3.7.0.v_B61/org.eclipse.jdt.core-3.7.0.v_B61.jar:/Users/ayutang/mavenRepository/org/jibx/config/3rdparty/org/eclipse/org.eclipse.osgi/3.7.0.v20110613/org.eclipse.osgi-3.7.0.v20110613.jar:/Users/ayutang/mavenRepository/org/jibx/config/3rdparty/org/eclipse/org.eclipse.text/3.5.100.v20110505-0800/org.eclipse.text-3.5.100.v20110505-0800.jar:/Users/ayutang/mavenRepository/log4j/log4j/1.2.17/log4j-1.2.17.jar:/Users/ayutang/mavenRepository/xpp3/xpp3/1.1.4c/xpp3-1.1.4c.jar:/Users/ayutang/mavenRepository/org/ogce/xpp3/1.1.6/xpp3-1.1.6.jar:/Users/ayutang/mavenRepository/org/apache/ant/ant/1.9.4/ant-1.9.4.jar:/Users/ayutang/mavenRepository/org/apache/ant/ant-launcher/1.9.4/ant-launcher-1.9.4.jar:/Users/ayutang/mavenRepository/joda-time/joda-time/2.6/joda-time-2.6.jar:/Users/ayutang/mavenRepository/com/thoughtworks/qdox/qdox/1.12.1/qdox-1.12.1.jar:/Users/ayutang/mavenRepository/commons-logging/commons-logging/1.1.1/commons-logging-1.1.1.jar com.phei.netty.frame.delimiter.EchoServer
	 * This is 1 times receive client : [Hi, Lilinfeng. Welcome to Netty.]
	 * This is 2 times receive client : [Hi, Lilinfeng. Welcome to Netty.]
	 * This is 3 times receive client : [Hi, Lilinfeng. Welcome to Netty.]
	 * This is 4 times receive client : [Hi, Lilinfeng. Welcome to Netty.]
	 * This is 5 times receive client : [Hi, Lilinfeng. Welcome to Netty.]
	 * This is 6 times receive client : [Hi, Lilinfeng. Welcome to Netty.]
	 * This is 7 times receive client : [Hi, Lilinfeng. Welcome to Netty.]
	 * This is 8 times receive client : [Hi, Lilinfeng. Welcome to Netty.]
	 * This is 9 times receive client : [Hi, Lilinfeng. Welcome to Netty.]
	 * This is 10 times receive client : [Hi, Lilinfeng. Welcome to Netty.]
	 *
	 * This is 1 times receive client : [Hi, Lilinfeng. Welcome to Netty.$_Hi, Lilinfeng. Welcome to Netty.$_Hi, Lilinfeng. Welcome to Netty.$_Hi, Lilinfeng. Welcome to Netty.$_Hi, Lilinfeng. Welcome to Netty.$_Hi, Lilinfeng. Welcome to Netty.$_Hi, Lilinfeng. Welcome to Netty.$_Hi, Lilinfeng. Welcome to Netty.$_Hi, Lilinfeng. Welcome to Netty.$_Hi, Lilinfeng. Welcome to Netty.$_]
	 *
	 * 如上是添加了分隔符解码器和未添加的情况，因此可以看出分隔符解码器的重要性了
	 * @param ctx
	 * @param msg
	 * @throws Exception
	 */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        String body = (String) msg;
        System.out.println("This is " + ++counter + " times receive client : ["
                + body + "]");
        body += "$_";
        ByteBuf echo = Unpooled.copiedBuffer(body.getBytes());
        ctx.writeAndFlush(echo);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();// 发生异常，关闭链路
    }
}
