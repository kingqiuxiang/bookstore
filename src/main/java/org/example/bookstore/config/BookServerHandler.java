package org.example.bookstore.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.bookstore.model.Book;
import org.example.bookstore.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@ChannelHandler.Sharable
public class BookServerHandler extends SimpleChannelInboundHandler<Book> {
    @Autowired
    private BookService bookService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ExecutorService businessExecutor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors() * 2,
            new ThreadFactoryBuilder().setNameFormat("book-service-%d").build()
    );

    public BookServerHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Book request) {
        System.out.println("Received book request: " + request);

        CompletableFuture.supplyAsync(() -> {
                    System.out.println("Fetching books in business thread");
                    return bookService.getAllBooks();
                }, businessExecutor)
                .thenAcceptAsync(books -> {
                    try {
                        sendBookList(ctx, books);
                    } catch (Exception e) {
                        handleError(ctx, e);
                    }
                }, ctx.executor()); // 回到EventLoop线程
    }

    private void sendBookList(ChannelHandlerContext ctx, List<Book> books) {
        try {
            byte[] responseBytes = mapper.writeValueAsBytes(books);
            System.out.println("Serialized " + books.size() + " books (" + responseBytes.length + " bytes)");

            ByteBuf buffer = ctx.alloc().buffer(responseBytes.length);
            buffer.writeBytes(responseBytes);

            ctx.writeAndFlush(buffer).addListener(future -> {
                if (future.isSuccess()) {
                    System.out.println("Successfully sent " + books.size() + " books");
                } else {
                    System.err.println("Send failed: " + future.cause());
                }
            });
        } catch (JsonProcessingException e) {
            handleError(ctx, new RuntimeException("JSON error", e));
        }
    }

    private void handleError(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("Handler error: " + cause.getMessage());

        try {
            ByteBuf errorBuf = ctx.alloc().buffer();
            errorBuf.writeBytes(("ERROR: " + cause.getMessage()).getBytes(StandardCharsets.UTF_8));
            ctx.writeAndFlush(errorBuf).addListener(future -> ctx.close());
        } catch (Exception e) {
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("Channel error: " + cause.getMessage());
        ctx.close();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        businessExecutor.shutdown();
        try {
            if (!businessExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                businessExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            businessExecutor.shutdownNow();
        }
    }
}