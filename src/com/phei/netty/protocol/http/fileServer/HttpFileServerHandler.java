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
package com.phei.netty.protocol.http.fileServer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author lilinfeng
 * @version 1.0
 * @date 2014年2月14日
 *
 * 文件服务器业务逻辑实现
 */
public class HttpFileServerHandler extends
        SimpleChannelInboundHandler<FullHttpRequest> {
    private final String url;

    public HttpFileServerHandler(String url) {
        this.url = url;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx,
                                FullHttpRequest request) throws Exception {
        // 1. 对解码结果进行判断
    	if (!request.getDecoderResult().isSuccess()) {
    		// 若解码失败，则状态行返回400
            sendError(ctx, BAD_REQUEST);
            return;
        }
    	// 2. 对支持的方法进行判断
        if (request.getMethod() != GET) {
            sendError(ctx, METHOD_NOT_ALLOWED);
            return;
        }
        final String uri = request.getUri();
        // 3. 封装uri
        final String path = sanitizeUri(uri);
        if (path == null) {
            sendError(ctx, FORBIDDEN);
            return;
        }
        // 4.file校验
        File file = new File(path);
        if (file.isHidden() || !file.exists()) {
        	// 4.1 文件不存在或是隐藏文件
            sendError(ctx, NOT_FOUND);
            return;
        }
        if (file.isDirectory()) {
        	// 4.2 若path对应的是目录则将目录链接返回给客户端
            if (uri.endsWith("/")) {
                sendListing(ctx, file);
            } else {
                sendRedirect(ctx, uri + '/');
            }
            return;
        }
        if (!file.isFile()) {
        	// 4.3 到此处说明file不是目录，但也不是文件，因此非法
            sendError(ctx, FORBIDDEN);
            return;
        }

        // 以下的逻辑都是处理具体某一个文件的
        RandomAccessFile randomAccessFile = null;
        try {
			// 5.采用随机文件读写类，以只读的方式打开文件
            randomAccessFile = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException fnfe) {
            sendError(ctx, NOT_FOUND);
            return;
        }
        // 6. 构造http响应报文
        long fileLength = randomAccessFile.length();
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        setContentLength(response, fileLength);
        setContentTypeHeader(response, file);
        if (isKeepAlive(request)) {
        	// 如果request是keep-alive的，则在响应头中也要设置
            response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }
        // 7. 发送响应报文到channelOutboundBuffer
        ctx.write(response);
        // 8. 文件发送
        ChannelFuture sendFileFuture;
        // 8.1 将文件randomAccessFile写入到发送缓冲区(此缓冲区并不是channelOutboundBuffer,而是专门用来传输大量数据的ChunkedWriteHandler.PendingWrite)
        sendFileFuture = ctx.write(new ChunkedFile(randomAccessFile, 0,
                fileLength, 8192), ctx.newProgressivePromise());
        //
        sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
            /**
             * 本方法在 ChunkedWriteHandler中将queue写入channelOutboundBuffer且写到channel之后 被调用
             * @param future
             * @param progress the progress of the operation so far (cumulative)
             * @param total the number that signifies the end of the operation when {@code progress} reaches at it.
             */
            @Override
            public void operationProgressed(ChannelProgressiveFuture future,
                                            long progress, long total) {
                if (total < 0) { // total unknown
                    System.err.println("Transfer progress: " + progress);
                } else {
                    System.err.println("Transfer progress: " + progress + " / "
                            + total);
                }
            }

            @Override
            public void operationComplete(ChannelProgressiveFuture future)
                    throws Exception {
                System.out.println("Transfer complete.");
            }
        });
        // 8.2 将文件数据从ChunkedWriteHandler.PendingWrite发送到channelOutboundBuffer(浏览器中的反应是"下载文件")
        ChannelFuture lastContentFuture = ctx
                .writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        if (!isKeepAlive(request)) {
            // 若链接不是keepAlive的，则在8.2中的数据写到channel之后将channel关闭（将链接关闭了）
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendError(ctx, INTERNAL_SERVER_ERROR);
        }
    }

    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

	/**
	 * 对uri进行包装
	 * @param uri
	 * @return
	 */
	private String sanitizeUri(String uri) {
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            try {
                uri = URLDecoder.decode(uri, "ISO-8859-1");
            } catch (UnsupportedEncodingException e1) {
                throw new Error();
            }
        }
        if (!uri.startsWith(url)) {
            return null;
        }
        if (!uri.startsWith("/")) {
            return null;
        }
        // 将硬编码的文件路径分隔符替换为本机操作系统的文件路径分隔符
        uri = uri.replace('/', File.separatorChar);
        // uri合法性校验
        if (uri.contains(File.separator + '.')
                || uri.contains('.' + File.separator) || uri.startsWith(".")
                || uri.endsWith(".") || INSECURE_URI.matcher(uri).matches()) {
            return null;
        }
        // 本机绝对路径
        return System.getProperty("user.dir") + File.separator + uri;
    }

    private static final Pattern ALLOWED_FILE_NAME = Pattern
            .compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");

    private static void sendListing(ChannelHandlerContext ctx, File dir) {
    	// 响应
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
        // 响应头
        response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
        // 响应消息体，需要将响应显示在客户端浏览器上，所以采用html的方式
        StringBuilder buf = new StringBuilder();
        String dirPath = dir.getPath();
        buf.append("<!DOCTYPE html>\r\n");
        buf.append("<html><head><title>");
        buf.append(dirPath);
        buf.append(" 目录：");
        buf.append("</title></head><body>\r\n");
        buf.append("<h3>");
        buf.append(dirPath).append(" 目录：");
        buf.append("</h3>\r\n");
        buf.append("<ul>");
        // '..'链接
        buf.append("<li>链接：<a href=\"../\">..</a></li>\r\n");
        // dir下的所有文件和目录
        for (File f : dir.listFiles()) {
            if (f.isHidden() || !f.canRead()) {
                continue;
            }
            String name = f.getName();
            if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
                continue;
            }
            // 采用链接标记
            buf.append("<li>链接：<a href=\"");
            buf.append(name);
            buf.append("\">");
            buf.append(name);
            buf.append("</a></li>\r\n");
        }
        buf.append("</ul></body></html>\r\n");
        ByteBuf buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);
        response.content().writeBytes(buffer);
        buffer.release();
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

	/**
	 * 重定向
     * myConfusionsv:貌似是客户端浏再次发起newUri的请求？
     * --The HTTP Location header is a response header that is used under 2 circumstances
     * to ask a browser to redirect a URL (status code 3xx) or provide information about
     * the location of a newly created resource (status code of 201).
	 * @param ctx
	 * @param newUri
	 */
	private static void sendRedirect(ChannelHandlerContext ctx, String newUri) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, FOUND);
        response.headers().set(LOCATION, newUri);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void sendError(ChannelHandlerContext ctx,
                                  HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                status, Unpooled.copiedBuffer("Failure: " + status.toString()
                + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void setContentTypeHeader(HttpResponse response, File file) {
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        response.headers().set(CONTENT_TYPE,
                mimeTypesMap.getContentType(file.getPath()));
    }
}
