package org.example.bookstore.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.SocketOptions;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import org.example.bookstore.model.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;

@Configuration
public class NettyServerConfig {
    @Value("${netty.server.port}")
    private int port;

    @Autowired
    private BookServerHandler bookServerHandler;


    @Bean
    public ServerBootstrap serverBootstrap() {
        // 创建EventLoopGroup
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        //创建服务器启动引导类
        ServerBootstrap bootstrap = new ServerBootstrap();
        // 配置服务器引导类bossGroup 和workGroup,其中 bossGroup用于接收连接请求，workGroup用于处理连接请求,相当于selector
        bootstrap.group(bossGroup, workGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {


                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new LineBasedFrameDecoder(1024)); // 处理分隔符
                        pipeline.addLast(new Decoder());
                        pipeline.addLast(new Encoder());
                        pipeline.addLast(bookServerHandler);
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        return bootstrap;

    }

    // 解码器 (JSON -> BookRequest)
    class Decoder extends MessageToMessageDecoder<ByteBuf> {
        private final ObjectMapper mapper = new ObjectMapper();

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
            byte[] bytes = new byte[msg.readableBytes()];
            msg.readBytes(bytes);
            Book request = new Book();
            request.setAuthor("Shone");
            request.setTitle("Book Title");
            request.setId("701");
            request.setPrice(100.00d);
            System.out.println("Received request: " + request);
            out.add(request);
        }
    }

    public class Encoder extends MessageToMessageEncoder<List<Book>> {
        private final ObjectMapper mapper = new ObjectMapper();

        @Override
        protected void encode(ChannelHandlerContext ctx, List<Book> books, List<Object> out) {
            try {
                // 1. 序列化整个列表
                byte[] bytes = mapper.writeValueAsBytes(books);

                // 2. 分配缓冲区
                ByteBuf buffer = ctx.alloc().buffer(bytes.length);
                buffer.writeBytes(bytes);

                // 3. 添加到输出
                out.add(buffer);

                System.out.println("Encoded " + books.size() + " books");
            } catch (JsonProcessingException e) {
                System.err.println("Book list encoding error: " + e.getMessage());
                sendError(ctx, out);
            }
        }

        private void sendError(ChannelHandlerContext ctx, List<Object> out) {
            ByteBuf error = ctx.alloc().buffer()
                    .writeBytes("BOOK_LIST_ENCODING_ERROR".getBytes());
            out.add(error);
        }
    }
}

