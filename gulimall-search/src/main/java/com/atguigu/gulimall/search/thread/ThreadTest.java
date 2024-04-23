package com.atguigu.gulimall.search.thread;

import java.util.concurrent.*;

/**
 * ClassName: ThreadTest
 * Package: com.atguigu.gulimall.search.thread
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/22 下午8:23
 * @Version 1.0
 */
public class ThreadTest {

    public static ExecutorService executorService = Executors.newFixedThreadPool(10);


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main：当前线程 " + Thread.currentThread().getId() + " 开始...");

//        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
//            System.out.println("thread01：当前线程 " + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("thread01：当前线程 " + Thread.currentThread().getId() + " i = " + i);
//        }, executorService);

//        CompletableFuture<Integer> completableFuture = CompletableFuture
//                .supplyAsync(() -> {
//                    System.out.println("当前线程 " + Thread.currentThread().getId());
//                    int i = 10 / 0;
//                    System.out.println("当前线程 " + Thread.currentThread().getId() + " i = " + i);
//                    return i;
//                }, executorService)
//                .whenComplete((result, exception) -> {
//                    System.out.println("当前线程 " + Thread.currentThread().getId() + " 异步任务完成，" + "result = " + result + "; exception = " + exception);
//                })
//                .exceptionally(throwable -> 10);
//        System.out.println("completableFuture = " + completableFuture.get());

//        CompletableFuture<Integer> completableFuture = CompletableFuture
//                .supplyAsync(() -> {
//                    System.out.println("当前线程 " + Thread.currentThread().getId());
//                    int i = 10 / 4;
//                    System.out.println("当前线程 " + Thread.currentThread().getId() + " i = " + i);
//                    return i;
//                }, executorService)
//                .handle((result, exception) -> {
//                    if (result != null) {
//                        return result * 2;
//                    }
//                    if (exception != null) {
//                        return 10;
//                    }
//                    return 0;
//                });

//        CompletableFuture
//                .supplyAsync(() -> {
//                    System.out.println("当前线程 " + Thread.currentThread().getId());
//                    int i = 10 / 4;
//                    System.out.println("当前线程 " + Thread.currentThread().getId() + " i = " + i);
//                    return i;
//                }, executorService)
//                .thenRunAsync(() -> {
//                    System.out.println("任务2 当前线程 " + Thread.currentThread().getId());
//                }, executorService);
//        CompletableFuture
//                .supplyAsync(() -> {
//                    System.out.println("当前线程 " + Thread.currentThread().getId());
//                    int i = 10 / 4;
//                    System.out.println("当前线程 " + Thread.currentThread().getId() + " i = " + i);
//                    return i;
//                }, executorService)
//                .thenAcceptAsync(result -> {
//                    System.out.println("任务2 当前线程 " + Thread.currentThread().getId() + " result = " + result);
//                }, executorService);

//        CompletableFuture<Object> future01 = CompletableFuture
//                .supplyAsync(() -> {
//                    System.out.println("任务1 线程开始 " + Thread.currentThread().getId());
//                    int i = 10 / 4;
//                    System.out.println("任务1 线程结束 " + Thread.currentThread().getId() + " i = " + i);
//                    return i;
//                }, executorService);
//        CompletableFuture<Object> future02 = CompletableFuture
//                .supplyAsync(() -> {
//                    System.out.println("任务2 线程开始 " + Thread.currentThread().getId());
//                    try {
//                        Thread.sleep(3000);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                    System.out.println("任务2 线程结束 " + Thread.currentThread().getId());
//                    return "Hello";
//                }, executorService);

//        future01.runAfterBothAsync(future02, () -> {
//            System.out.println("任务3 线程 " + Thread.currentThread().getId());
//        }, executorService);

//        future01.thenAcceptBothAsync(future02, (f1, f2) -> {
//            System.out.println("任务3 线程 " + Thread.currentThread().getId() + " f1 = " + f1 + "; f2 = " + f2);
//        }, executorService);

//        CompletableFuture<String> stringCompletableFuture = future01.thenCombineAsync(future02, (f1, f2) -> {
//            System.out.println("任务3 线程 " + Thread.currentThread().getId() + " f1 = " + f1 + "; f2 = " + f2);
//            return f1 + " ---- " + f2;
//        }, executorService);
//        System.out.println("stringCompletableFuture = " + stringCompletableFuture);

//        future01.runAfterEitherAsync(future02, () -> {
//            System.out.println("任务3 线程 " + Thread.currentThread().getId());
//        }, executorService);
//
//        future01.acceptEitherAsync(future02, result -> {
//            System.out.println("任务3 线程 " + Thread.currentThread().getId() + " result = " + result);
//        }, executorService);

//        CompletableFuture<String> stringCompletableFuture = future01.applyToEitherAsync(future02, result -> {
//            System.out.println("任务3 线程 " + Thread.currentThread().getId() + " result = " + result);
//            return result + " ----";
//        }, executorService);
//        System.out.println("stringCompletableFuture = " + stringCompletableFuture.get());

        CompletableFuture<String> futureImg = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品图片信息 线程 id = " + Thread.currentThread().getId());
            return "商品图片";
        }, executorService);

        CompletableFuture<String> futureDesc = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品描述信息开始 线程 id = " + Thread.currentThread().getId());
            try {
                Thread.sleep(3000);
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("查询商品描述信息结束 线程 id = " + Thread.currentThread().getId());
            return "商品描述";
        }, executorService);

        CompletableFuture<String> futureInfo = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品信息 线程 id = " + Thread.currentThread().getId());
            return "商品信息";
        }, executorService);

//        CompletableFuture<Void> allOf = CompletableFuture.allOf(futureImg, futureDesc, futureInfo);
//        allOf.get();

        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(futureImg, futureDesc, futureInfo);
        anyOf.get();

        System.out.println(anyOf.get());



        System.out.println("main：当前线程 " + Thread.currentThread().getId() + " 结束...");
    }


    public void thread(String[] args) {

//        System.out.println("main：当前线程 " + Thread.currentThread().getId() + " 开始...");
//        Thread01 thread = new Thread01();
//        thread.start();
//        System.out.println("main：当前线程 " + Thread.currentThread().getId() + " 结束...");

//        Runnable01 runnable = new Runnable01();
//        new Thread(runnable).start();
//        System.out.println("main：当前线程 " + Thread.currentThread().getId() + " 结束...");

//        FutureTask<Integer> futureTask = new FutureTask<>(new Callable01());
//        new Thread(futureTask).start();
//        try {
//            // 阻塞等待线程执行结果
//            Integer integer = futureTask.get();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        } catch (ExecutionException e) {
//            throw new RuntimeException(e);
//        }
//        executorService.execute(new Runnable01());

        /**
         * 创建一个线程池执行器(ThreadPoolExecutor)实例。
         *
         * 参数说明：
         * 1. corePoolSize：核心线程数，即一直保持活跃的线程数，最少为2。
         * 2. maximumPoolSize：最大线程数，当任务队列满时，线程池最多扩展到的线程数，最大为5。
         * 3. keepAliveTime：空闲线程的存活时间，当线程池中的线程数量超过corePoolSize时，多出的空闲线程等待新任务的最长时间为1秒。
         * 4. unit：keepAliveTime的时间单位，这里为秒。
         * 5. workQueue：任务队列，这里使用链式阻塞队列(LinkedBlockingDeque)，最多可存储3个待处理任务。
         * 6. threadFactory：线程工厂，用于创建新线程，这里使用Java的默认线程工厂。
         * 7. handler：拒绝策略，当线程池和任务队列都满时，新提交的任务将被拒绝，这里采用AbortPolicy策略，即抛出RejectedExecutionException异常。
         */
//        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
//                2,
//                5,
//                1,
//                TimeUnit.SECONDS,
//                new LinkedBlockingDeque<>(3),
//                Executors.defaultThreadFactory(),
//                new ThreadPoolExecutor.AbortPolicy()
//        );
        /**
         * 1. 提交任务：
         * 当应用程序通过threadPoolExecutor.submit()或threadPoolExecutor.execute()方法提交一个Runnable或Callable任务时，该任务会被添加到线程池中。
         * 2. 检查核心线程：
         * 首先，线程池会检查当前活动的核心线程数是否小于核心线程数上限（即构造函数中的2）。如果是，则直接创建一个新的核心线程来执行提交的任务。
         * 3. 放入任务队列：
         * 如果当前核心线程数已达到上限，但任务队列（即构造函数中的new LinkedBlockingDeque<>(3)）未满，则将任务放入队列等待执行。
         * 4. 增加非核心线程：
         * 若核心线程已满且任务队列也已满（队列容量为3），但线程总数（包括核心线程和非核心线程）尚未达到最大线程数（即构造函数中的5），则线程池会创建一个非核心线程来执行任务。
         * 5. 应用拒绝策略：
         * 如果所有核心线程都在工作，任务队列已满，且线程总数已达到最大线程数，此时再有新的任务提交，将触发拒绝策略（即构造函数中的new ThreadPoolExecutor.AbortPolicy()）。根据指定的AbortPolicy，线程池会抛出RejectedExecutionException异常，拒绝处理该任务。
         * 6. 线程复用与回收：
         * 已完成任务的线程不会立即销毁，而是会继续处理任务队列中的其他任务。若在1秒（即构造函数中的1, TimeUnit.SECONDS）内无新任务提交，超出核心线程数的非核心线程将被终止，以释放系统资源。
         * 7. 线程空闲存活时间：
         * 对于核心线程，即使在空闲状态下也会保持存活，除非线程池被显式地关闭。而非核心线程在空闲状态下，如果超过1秒仍未收到新任务，将被终止
         */

        System.out.println("main：当前线程 " + Thread.currentThread().getId() + " 结束...");

    }

    public static class Thread01 extends Thread {
        @Override
        public void run() {
            System.out.println("thread01：当前线程 " + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("thread01：当前线程 " + Thread.currentThread().getId() + " i = " + i);
        }
    }

    public static class Runnable01 implements Runnable {
        @Override
        public void run() {
            System.out.println("runnable01：当前线程 " + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("runnable01：当前线程 " + Thread.currentThread().getId() + " i = " + i);
        }
    }

    public static class Callable01 implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            System.out.println("callable01：当前线程 " + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("callable01：当前线程 " + Thread.currentThread().getId() + " i = " + i);
            return i;
        }
    }
}
