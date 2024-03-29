package com.dx.netty.websocket.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Description:
 *
 * @author rockstarsteve
 * @version 1.0
 * @copyright Copyright (c) 电信
 * @since 2022/10/12
 */
public class WsServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        //websocket基于http协议，所以要有http的编解码器
        pipeline.addLast(new HttpServerCodec());
        //对大数据流的支持
        pipeline.addLast(new ChunkedWriteHandler());
        //对httpmessage进行聚合，聚合成FullHttpRequest或者httpResponse
        //1024*64：消息最大的长度
        pipeline.addLast(new HttpObjectAggregator(1024 * 64));

        //================================以上是用于http协议的支持=====================================

        /**
         * websocket服务器处理的协议，用于指定给客户端连接的访问路由：/ws
         * 本handler会帮忙处理一些任务，handshaking（close，ping，pone）ping+pong=心跳
         * 对于websocket来讲，都是以frames进行传输的，不同的数据类型对应的frams也是不同的
         */
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));

        //自定义处理消息的的handle
        pipeline.addLast(new CustomHandler());


        /**
         * 自定义处理心跳的handler
         *
         */
        //激活状态的,针对客户端，如果一分钟没有向服务端发送读写心跳（ALL），则主动断开，如果是读空闲或者写空闲则不做处理
        pipeline.addLast(new IdleStateHandler(8, 10, 30));
        //自定义的空闲状态检测
        pipeline.addLast(new HeartBeatHandler());

    }

}
