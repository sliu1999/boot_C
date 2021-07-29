package sliu.unit;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

@Component
public class ThreadOperate {
    private static HashMap<Integer, ExecutorService> threadPools = new HashMap();

    public ThreadOperate() {
    }

    public static Integer runSingleThread(String name, Runnable command) {
        ExecutorService singleThreadPool = newSingleThreadPool(name);

        try {
            singleThreadPool.execute(command);
            threadPools.put(singleThreadPool.hashCode(), singleThreadPool);
            return singleThreadPool.hashCode();
        } catch (Exception var4) {
            var4.printStackTrace();
            singleThreadPool.shutdownNow();
            return null;
        }
    }

    public static Integer runThreads(String name, List<Runnable> commands) {
        ExecutorService threadPool = newThreadPool(name, commands.size());

        try {
            commands.forEach((command) -> {
                threadPool.execute(command);
            });
            threadPools.put(threadPool.hashCode(), threadPool);
            return threadPool.hashCode();
        } catch (Exception var4) {
            var4.printStackTrace();
            threadPool.shutdownNow();
            return null;
        }
    }

    public static void removeThreadPool(Integer hashCode) {
        ExecutorService threadPool = (ExecutorService)threadPools.get(hashCode);
        threadPool.shutdown();
        threadPools.remove(hashCode);
    }

    public static void removeThreadPoolNow(Integer hashCode) {
        ExecutorService threadPool = (ExecutorService)threadPools.get(hashCode);
        threadPool.shutdownNow();
        threadPools.remove(hashCode);
    }

    public static ExecutorService getThreadPool(Integer hashCode) {
        return (ExecutorService)threadPools.get(hashCode);
    }


    private static ExecutorService newSingleThreadPool(String nameFormat) {
        ThreadFactory threadFactory = (new ThreadFactoryBuilder()).setNameFormat(nameFormat).build();
        ExecutorService singleThreadPool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(1024), threadFactory, new ThreadPoolExecutor.AbortPolicy());
        return singleThreadPool;
    }

    private static ExecutorService newThreadPool(String nameFormat, Integer poolSize) {
        ThreadFactory threadFactory = (new ThreadFactoryBuilder()).setNameFormat(nameFormat).build();
        ExecutorService singleThreadPool = new ThreadPoolExecutor(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(1024), threadFactory, new ThreadPoolExecutor.AbortPolicy());
        return singleThreadPool;
    }
}
