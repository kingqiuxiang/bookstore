package org.example.bookstore.config;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class NettyServerRunner implements ApplicationRunner {
    
    private final ServerBootstrap serverBootstrap;
    private final int port;
    
    public NettyServerRunner(ServerBootstrap serverBootstrap, 
                           @Value("${netty.server.port:8888}") int port) {
        this.serverBootstrap = serverBootstrap;
        this.port = port;
    }
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 启动Netty服务器,port可以从配置文件中读取,sync()会阻塞直到绑定成功
        ChannelFuture future = serverBootstrap.bind(port).sync();
        // 绑定成功后，添加关闭监听器,只有在服务器关闭时才会执行,listener会监听一个事件，当这个事件发生时，执行关闭操作
        future.channel().closeFuture().addListener(f -> {
            // 优雅关闭,只有在服务器关闭时才会执行
            EventLoopGroup bossGroup = serverBootstrap.config().group();
            EventLoopGroup workerGroup = serverBootstrap.config().childGroup();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        });
        System.out.println("Netty server started on port: " + port);
    }
}