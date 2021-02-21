package com.phei.netty.protocol.http.xml.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.net.InetAddress;
import java.util.List;

/**
 * http+xml 消息编码
 */
public class HttpXmlRequestEncoder extends
        AbstractHttpXmlEncoder<HttpXmlRequest> {

	/**
	 * encode是在write时用到的，难道不应该拼装httpResponse吗？--这是客户端用的，构造订购请求消息，然后发给服务端
	 * @param ctx           the {@link ChannelHandlerContext} which this {@link MessageToMessageEncoder} belongs to
	 * @param msg           the message to encode to an other one
	 * @param out           the {@link List} into which the encoded msg should be added
	 *                      needs to do some kind of aggragation
	 * @throws Exception
	 */
    @Override
    protected void encode(ChannelHandlerContext ctx, HttpXmlRequest msg,
                          List<Object> out) throws Exception {

    	// 1. POJO->xml
        ByteBuf body = encode0(ctx, msg.getBody());

        // 2. 构造FullHttpRequest
        FullHttpRequest request = msg.getRequest();
        if (request == null) {
            // 请求消息体body 底层是xml字符串
            request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                    HttpMethod.GET, "/do", body);
            // 请求头
            HttpHeaders headers = request.headers();
            headers.set(HttpHeaders.Names.HOST, InetAddress.getLocalHost()
                    .getHostAddress());
            headers.set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
            headers.set(HttpHeaders.Names.ACCEPT_ENCODING,
                    HttpHeaders.Values.GZIP.toString() + ','
                            + HttpHeaders.Values.DEFLATE.toString());
            headers.set(HttpHeaders.Names.ACCEPT_CHARSET,
                    "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
            headers.set(HttpHeaders.Names.ACCEPT_LANGUAGE, "zh");
            headers.set(HttpHeaders.Names.USER_AGENT,
                    "Netty xml Http Client side");
            headers.set(HttpHeaders.Names.ACCEPT,
                    "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        }
        // 消息体长度
        HttpHeaders.setContentLength(request, body.readableBytes());
        out.add(request);
    }

}
