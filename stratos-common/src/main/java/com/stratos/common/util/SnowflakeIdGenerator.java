package com.stratos.common.util;

/**
 * 雪花算法ID生成器
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
public class SnowflakeIdGenerator {

    /** 起始时间戳（2024-01-01） */
    private static final long START_TIMESTAMP = 1704038400000L;

    /** 机器ID所占位数 */
    private static final long WORKER_ID_BITS = 5L;

    /** 数据中心ID所占位数 */
    private static final long DATACENTER_ID_BITS = 5L;

    /** 序列号所占位数 */
    private static final long SEQUENCE_BITS = 12L;

    /** 机器ID最大值 */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /** 数据中心ID最大值 */
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);

    /** 序列号掩码 */
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    /** 机器ID左移位数 */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /** 数据中心ID左移位数 */
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /** 时间戳左移位数 */
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    private final long workerId;
    private final long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(String.format("workerId必须在0到%d之间", MAX_WORKER_ID));
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenterId必须在0到%d之间", MAX_DATACENTER_ID));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨，拒绝生成ID");
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

}
