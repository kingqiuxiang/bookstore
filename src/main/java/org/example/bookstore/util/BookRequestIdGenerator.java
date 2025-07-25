package org.example.bookstore.util;
import org.springframework.stereotype.Component;

@Component
public class BookRequestIdGenerator {
    // 起始时间戳 (2024-01-01)
    private static final long EPOCH = 1704067200000L;

    // 各部分的位数
    private static final long BOOK_ID_BITS = 24L;
    private static final long TIMESTAMP_BITS = 41L;
    private static final long NODE_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    // 最大值计算
    private static final long MAX_BOOK_ID = (1L << BOOK_ID_BITS) - 1;
    private static final long MAX_NODE = (1L << NODE_BITS) - 1;
    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;

    // 位移设置
    private static final long TIMESTAMP_SHIFT = BOOK_ID_BITS + NODE_BITS + SEQUENCE_BITS;
    private static final long BOOK_ID_SHIFT = NODE_BITS + SEQUENCE_BITS;
    private static final long NODE_SHIFT = SEQUENCE_BITS;

    private final long nodeId=0L;
    private long lastTimestamp = -1L;
    private long sequence = 0L;


    public synchronized Long generate(long bookId) {
        if (bookId > MAX_BOOK_ID || bookId < 0) {
            throw new IllegalArgumentException("Book ID exceeds maximum value: " + MAX_BOOK_ID);
        }

        long currentTimestamp = System.currentTimeMillis();

        if (currentTimestamp < lastTimestamp) {
            throw new IllegalStateException("Clock moved backwards");
        }

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                currentTimestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = currentTimestamp;

        // 组合各部分生成 ID
        long timestampPart = (currentTimestamp - EPOCH) << TIMESTAMP_SHIFT;
        long bookIdPart = bookId << BOOK_ID_SHIFT;
        long nodePart = nodeId << NODE_SHIFT;

        // 最终 ID 格式: 时间戳 + bookId + 节点 + 序列号
        return timestampPart | bookIdPart | nodePart | sequence;
    }

    private long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}