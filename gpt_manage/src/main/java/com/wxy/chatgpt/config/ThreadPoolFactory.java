package com.wxy.chatgpt.config;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolFactory {
    private static long keepAliveTime = 60;

    private static int corePoolSize = 5;

    private static int maxPoolSize = 5;

    public static final ThreadPoolExecutor threadPoolExecutor;

    static {
        threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<>(maxPoolSize));
    }

    public static void executeTask(Runnable runnable) {
        threadPoolExecutor.execute(runnable);
    }

}
