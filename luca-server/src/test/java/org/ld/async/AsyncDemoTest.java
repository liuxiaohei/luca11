package org.ld.async;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author ld
 * 组合式异步的前身与现在
 * 1 Future
 * 2 FutureTask
 * 3 CompletionService
 * 4 Runnable
 * 5 Callback
 * 6 CompletableFuture
 */
public class AsyncDemoTest {


    /**
     * 这里是一个延时的方法，用于放慢程序的执行过程便于观察异步的执行过程
     */
    private void wait1() {
        try {
            Thread.sleep(10);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 一个可观察执行过程的任务
     */
    public String tast(String s) {
        wait1();
        System.out.println(s + "：start");
        wait1();
        System.out.println(s + "：run");
        wait1();
        System.out.println(s + ": end");
        return s;
    }

    /**
     * 以前执行异步的手段
     * 继承Thead类,重写run方法
     * 实现runable接口，实现run方法
     * 匿名内部类编写thread或者实现runable的类，当然在java8中可以用lambda表达式简化
     * 使用futureTask进行附带返回值的异步编程
     * 使用线程池和Future来实现异步编程
     * spring框架下的@async获得异步编程支持
     * 异步实现的手段非常多比如
     */
    @Test
    public void testRunnable() {
        tast("A");
        new Thread(() -> tast("B")).start();
        tast("C");
        // 这里可以观察出来 A与B 同步执行，而BC是异步执行的，也就是说C任务无需等待B执行结束就可以开始执行
        // 实现了时间的叠加
    }

    /**
     * Fucture
     * Callable
     */
    @Test
    public void testFucture() throws Exception {
        ExecutorService pool = Executors.newCachedThreadPool();
        try {
            Future<String> future = pool.submit(() -> tast("A"));
            tast("B");
            String result = future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("任务计算抛出了一个异常!");
        } catch (ExecutionException e) {
            System.out.println("线程在等待的过程中被中断了!");
        } catch (TimeoutException e) {
            System.out.println("future对象等待时间超时了!");
        }
    }

    /**
     * FutureTask
     * CompletionService
     */
    @Test
    public void fuctureTask() {
        ExecutorService pool = Executors.newCachedThreadPool();
        try {
            FutureTask<String> futureTask = new FutureTask<>(() -> tast("A"));
            pool.submit(futureTask);
            pool.shutdown();
            tast("B");
            String result = futureTask.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("任务计算抛出了一个异常!");
        } catch (ExecutionException e) {
            System.out.println("线程在等待的过程中被中断了!");
        } catch (TimeoutException e) {
            System.out.println("future对象等待时间超时了!");
        }
    }

    /**
     * ExecutorService
     */
    @Test
    public void testExecutorService() throws Exception {
        ExecutorService pool = Executors.newCachedThreadPool();
        CompletionService<String> cService = new ExecutorCompletionService<>(pool);
        cService.submit(() -> tast("A"));
        tast("B");
        pool.shutdown();
    }

    /**
     * 然而这样的异步编程方式仅仅能满足基本的需要，
     * 稍微复杂的一些异步处理以上手段接口似乎就有点束手无策了
     * 比如：
     * 将两个异步计算合并为一个——这两个异步计算之间相互独立，同时第二个又依赖于第一个的结果。
     * 等待 Future 集合中的所有任务都完成。
     * 仅等待 Future 集合中最快结束的任务完成（有可能因为它们试图通过不同的方式计算同一个值） ，并返回它的结果。
     * 通过编程方式完成一个 Future 任务的执行（即以手工设定异步操作结果的方式） 。应对 Future 的完成事件（即当 Future 的完成事件发生时会收到通知，并能使用 Future计算的结果进行下一步的操作，不只是简单地阻塞等待操作的结果）
     */

    /**
     * 这种感觉其实就很像没有stream之前的collections的操作感觉一样，同样的
     * 对于future，java8提供了它的函数式升级版本CompletableFuture，从名字就可以看出来这绝对是future的升级版。
     * 这是一个包含50个方法左右的类，提供了非常强大的Fucture的扩展功能，可以帮助我们简化异步编程的复杂性，提供了函数式编程的
     * 能力，通过回调的方式处理计算结果，并且提供转换和组合CompletalbleFucture的方法
     * 不得不提一个接口就是CompletionStage，它提供了大量的方法，使用它可以方便的响应任务事件，构建任务流水线，实现组合式异步编程。
     */

    /**
     * 让我门看一看它的功能吧
     * task由同步转异步
     * 这里构造一个completableFuture对象，并另起一个异步线程，将异步计算的结果使用futurePrice.complete来接受，
     * 无需等待直接返回future结果调用类使用Stringr result = future.get(10, TimeUnit.SECONDS)来接受返回的结果，
     */
    private Future<String> asyncTask(String s) {
        CompletableFuture<String> future = new CompletableFuture<>();
        new Thread(() -> {
            String r = tast(s);
            future.complete(r);
        }).start();
        return future;
    }

    /**
     * 如果等待超时则抛出异常。另外，如果异步线程发生异常，并且在排查问题的时候想要知道具体是什么原因导致的，
     * 可以在使用completeExcepitonally来得到异常信息并且结束这次异步任务，代码如下
     * 这样，基本的功能就实现了。
     */
    public Future<String> asyncTask1(String s) {
        CompletableFuture<String> future = new CompletableFuture<>();
        new Thread(() -> {
            try {
                String r = tast(s);
                future.complete(r);
            } catch (Exception ex ) {
                future.completeExceptionally(ex);
            }
        }).start();
        return future;
    }

    /**
     * 也许你看到上面的代码，会说:"我晕，你这写法比原来还复杂哦，而且我也没看出啥区别啊。"，是的，
     * 上文的写法可以算是原生态的写法了，目的为为下面的知识做一个简单的铺垫。
     *
     * 事实上，CompleteFuture本身提供了大量的工厂方法来供我们十分方便的实现一个异步编程，他封装了前篇一律的异常与结果接收，
     * 你只需要编写真正的异步逻辑部分就可以了，同时借住于lambda表达式，可以更进一步。
     *
     * supplyAsync 方法接受一个生产者（Supplier）作为参数，返回一个 CompletableFuture
     * 对象， 该对象完成异步执行后会读取调用生产者方法的返回值。 生产者方法会交由 ForkJoinPool
     * 池中的某个执行线程（Executor）运行，但是你也可以使用 supplyAsync 方法的重载版本，传
     * 递第二个参数指定不同的执行线程执行生产者方法。
     *
     * 于是上文的例子可以改写如下
     */
    public Future<String> asyncTask3(String s) {
        return CompletableFuture.supplyAsync(() -> tast(s));
    }

    /**
     * 是不是简洁了许多呢？
     * 可现在还有问题，这里我们成功的编写了一个十分简洁的异步方法，可实际的情况中，
     * 我们所能调用的API大部分都是同步的，因此下面将介绍如何使用异步的方法去操作这些同步API。
     */
    private List<String> numbers = IntStream.rangeClosed(0,10).boxed().map(e -> e + "").collect(Collectors.toList());

    /**
     * 转化成字符串
     */
    public List<String> findPrice() {
        return numbers.stream().map(e -> "price:" + e).collect(Collectors.toList());
    }

    /**
     * 这里提供一下异步实现
     * 借助Optional和Stream简化一下实现
     * 我之前说过那个Fucture的list的取值是必须分开的，经过研究发现如下这样写就ok啦
     */
    @Test
    public void demo1() {
        Optional.of(
                numbers.stream().map(e -> CompletableFuture.supplyAsync(() -> "price:" + e))
                        .collect(Collectors.toList())
        ).map(
                e -> e.stream().map(CompletableFuture::join)
                        .collect(Collectors.toList())
        );
        // 使用这种方式，你会得到一个,List<CompletableFuture>，
        // 列表中的每个CompletableFuture 对象在计算完成后都包含一个String类型的名称。
        // 但是，由于你用CompletableFutures 实现的方法要求返回一个List，你需要等待所有的future
        // 执行完毕，将其包含的值抽取出来，填充到列表中才能返回。为了实现这个效果，
        // 你可以向最初的 List<CompletableFuture> 施加第二个map 操作，
        // 对 List 中的所有future对象执行join操作，一个接一个地等待它们运行结束。
        // 注意CompletableFuture类中的join方法和Future接口中的get有相同的含义，
        // 并且也声明在Future 接口中，它们唯一的不同是join不会抛出任何检测到的异常。
        // 使用它你不再需要使用try / catch 语句块让你传递给第二个map方法的Lambda表达式变得过于臃肿。

        //以上的代码你可能会疑惑，为什么不直接按照numbers->completableFuture->join->collect的方式进行流处理呢？
        // 那是因为join这一步本身是阻塞的，对于流操作来说，前一个没有处理完之前，是不会处理下一个的，
        // 所以对于每一个节点，处理到join这一步的时候就会阻塞住等待一段时间，这样的话，这个流水线本身就会变回阻塞的了。
        // 而上文的编写方法可以看出 numbr->completableFuture->collect 这个操作本身是非阻塞的，
        // 顺利的将所有的请求都发出去了，随后再使用join来完成结果的收集。
    }

    /**
     * 进阶的异步操作
     *
     * 既然我们已经将异步操作与流相结合了，因此很容易的就会想到对于异步流来说，应该有会有类似于集合流的一些非常好用的API吧？
     * 事实上，JAVA8的确为我们提供了这些API。
     *
     * 构造同步和异步操作
     * 如同集合流操作一样，异步流也可以提前安排一系列的任务，然后让异步任务有条不紊的按照这个顺序去执行。
     * 同步任务
     * 使用future.thenApply(Function)来实现,该方法接受一个Function对象
     * 你可以规划这样的任务 任务A(异步)->任务B(同步)，语法可能是这样的
     * stream()
     * .map(xxx->supplayAsync(()->任务A)) //这一步已经异步的映射成了任务A
     * .map(future->future.thenApply(任务B)//执行同步的任务B
     * .collect
     *
     * 异步任务
     * 与同步几乎一样，方法变为future.thenCompose(Function)
     * 你可以规划这样的任务 任务A(异步)->任务B(同步)->任务C(异步)，语法可能是这样的
     *
     * stream()
     * .map(xxx->supplayAsync(()->任务A)) //这一步已经异步的映射成了任务A
     * .map(future->future.thenApply(任务B)//执行同步的任务B
     * .map(future->future.thenCompose(任务C))//再异步执行任务C
     * .collect
     */

    /**
     * 其实lambda表达式以面向对象的角度来理解就是一个匿名内部类
     * 如果以函数式的思维来理解就是一段尚未执行的代码，虽然从逻辑层面来看就是
     * 一层语法糖的区别，但是前者总有种隔靴搔痒的感觉，
     * 打个比方最开始学习多线程的时候，碰到又是继承，又是接口回调，给萌新来说总会有种哇好高大上好高端的感觉
     * 但是如果是以函数式编程的角度来看多线程呢？无论是Runable Fucture 还是Callable其实仅仅是一个用来承载一段
     * 尚未执行的代码的容器，而这个容器之中的具体动作在什么时候执行，是取决于调用这个容器的方法来决定的，多线程只
     * 是把这段尚未执行的代码交给一个新的线程执行而已
     * 如果这样想的话那多线程的实现从本质上并不是局限于这几个接口，而是任何一个函数式接口理论上都可以作为一个多线程
     * 的使用来服务的，以这个想法为基础，加上CompletionStage对"阶段的组合"进行抽象实现，组合式异步的雏形就实现了
     * 它着重可以更方便更安全的解决那些，即需要同步又需要异步的io密集型操作
     * 接下来就是真正的正片对那38个组合方式的实现手段的罗列，表格我放到石墨里了
     */

    /**
     * 进行变换
     */
    @Test
    public void thenApplyTest() {
        CompletableFuture.supplyAsync(() -> tast("A"))
                .thenApply(e -> e + tast("B")).join();
    }

    /**
     * 1进行变换
     */
    @Test
    public void thenApplyAsyncTest() {
        CompletableFuture.supplyAsync(() -> tast("A"))
                .thenApplyAsync(e -> e + tast("B")).join();
    }

    /**
     * 3进行消耗
     */
    @Test
    public void thenAccept() {
        CompletableFuture.supplyAsync(() -> tast("A"))
                .thenAccept(e -> System.out.println( e + tast("B"))).join();
    }

    @Test
    public void thenAcceptAsync() {
        CompletableFuture.supplyAsync(() -> tast("A"))
                .thenAcceptAsync(e -> System.out.println( e + tast("B"))).join();
    }

    /**
     * 3对上一步的计算结果不关心，执行下一个操作。
     */
    @Test
    public void thenRun() {
        CompletableFuture.supplyAsync(() -> tast("A"))
                .thenRun(() -> System.out.println(tast("B"))).join();
    }

    @Test
    public void thenRunAsync() {
        CompletableFuture.supplyAsync(() -> tast("A"))
                .thenRunAsync(() -> System.out.println(tast("B"))).join();
    }

    /**
     * 4.结合两个CompletionStage的结果，进行转化后返回
     */
    @Test
    public void thenCombine() {
        String s = CompletableFuture.supplyAsync(() -> tast("A"))
                .thenCombine(CompletableFuture.supplyAsync(() -> tast("B"))
                ,(A,B) -> A + B).join();
        System.out.println(s);
    }

    @Test
    public void thenComposeAsync() {
        String s = CompletableFuture.supplyAsync(() -> tast("A"))
                .thenCombineAsync(CompletableFuture.supplyAsync(() -> tast("B"))
                        ,(A,B) -> A + B).join();
        System.out.println(s);
    }

    /**
     * 5 结合两个CompletionStage的结果，进行消耗
     */
    @Test
    public void thenAcceptBoth() {
        CompletableFuture.supplyAsync(() -> tast("A"))
                .thenAcceptBoth(CompletableFuture.supplyAsync(() -> tast("B"))
                        ,(A,B) -> System.out.println(A + B)).join();
    }

    @Test
    public void thenAcceptBothAsync() {
        CompletableFuture.supplyAsync(() -> tast("A"))
                .thenAcceptBothAsync(CompletableFuture.supplyAsync(() -> tast("B"))
                        ,(A,B) -> System.out.println(A + B)).join();
    }

    /**
     * 6 在两个CompletionStage都运行完执行。
     */
    @Test
    public void runAfterBoth() {
        CompletableFuture.supplyAsync(() -> tast("A"))
                .runAfterBoth(CompletableFuture.supplyAsync(() -> tast("B"))
                        ,() -> System.out.println(tast("C"))).join();
    }

    @Test
    public void runAfterBothAsync() {
        CompletableFuture.supplyAsync(() -> tast("A"))
                .runAfterBothAsync(CompletableFuture.supplyAsync(() -> tast("B"))
                        ,() -> System.out.println(tast("C"))).join();
    }

    /**
     * 7.两个CompletionStage，谁计算的快，我就用那个CompletionStage的结果进行下一步的转化操作。
     */
    @Test
    public void applyToEither() {
        IntStream.rangeClosed(1,10).boxed().forEach( i -> {
                    String s = CompletableFuture.supplyAsync(() -> tast("A" + i))
                            .applyToEither(CompletableFuture.supplyAsync(() -> tast("B" + i))
                                    , e -> "demo" + e).join();
                    System.out.println("第" + i + "次：" + s);
                    System.out.println("-------------------------");
                }
        );
    }

    @Test
    public void applyToEitherAsync() {
        IntStream.rangeClosed(1,10).boxed().forEach( i -> {
                    String s = CompletableFuture.supplyAsync(() -> tast("A" + i))
                            .applyToEitherAsync(CompletableFuture.supplyAsync(() -> tast("B" + i))
                                    , e -> "demo" + e).join();
                    System.out.println("第" + i + "次：" + s);
                    System.out.println("-------------------------");
                }
        );
    }

    /**
     * 8 两个CompletionStage，谁计算的快，我就用那个CompletionStage的结果进行下一步的消耗操作。
     */
    @Test
    public void acceptEither() {
        IntStream.rangeClosed(1,10).boxed().forEach( i -> {
                    CompletableFuture.supplyAsync(() -> tast("A" + i))
                            .acceptEither(CompletableFuture.supplyAsync(() -> tast("B" + i))
                                    , e -> System.out.println("第" + i + "次：" + e)).join();
                    System.out.println("-------------------------");
                }
        );
    }

    @Test
    public void acceptEitherAsync() {
        IntStream.rangeClosed(1,10).boxed().forEach( i -> {
                    CompletableFuture.supplyAsync(() -> tast("A" + i))
                            .acceptEitherAsync(CompletableFuture.supplyAsync(() -> tast("B" + i))
                                    , e -> System.out.println("第" + i + "次：" + e)).join();
                    System.out.println("-------------------------");
                }
        );
    }

    /**
     * 9 两个CompletionStage，任何一个完成了都会执行下一步的操作（Runnable）。
     */
    @Test
    public void runAfterEither() {
        IntStream.rangeClosed(1,10).boxed().forEach( i -> {
                    CompletableFuture.supplyAsync(() -> tast("A" + i))
                            .runAfterEither(CompletableFuture.supplyAsync(() -> tast("B" + i))
                                    , () -> System.out.println("第" + i + "次：" + "finish")).join();
                    System.out.println("-------------------------");
                }
        );
    }

    @Test
    public void runAfterEitherAsync() {
        IntStream.rangeClosed(1,10).boxed().forEach( i -> {
                    CompletableFuture.supplyAsync(() -> tast("A" + i))
                            .runAfterEitherAsync(CompletableFuture.supplyAsync(() -> tast("B" + i))
                                    , () -> System.out.println("第" + i + "次：" + "finish")).join();
                    System.out.println("-------------------------");
                }
        );
    }

    private void error() {
        throw new RuntimeException("一个异常");
    }

    /**
     * 10 当运行时出现了异常，可以通过exceptionally进行补偿。
     */
    @Test
    public void exceptionallyError() {
        String s = CompletableFuture.supplyAsync(() -> {
            error();
            return tast("A");
        }).exceptionally(error -> error.getMessage() + "massage").join();
        System.out.println(s);
    }

    @Test
    public void exceptionally() {
        String s = CompletableFuture.supplyAsync(() -> {
            return tast("A");
        }).exceptionally(error -> error.getMessage() + "massage").join();
        System.out.println(s);
    }

    /**
     * 11 当运行完成时，对结果的记录。这里的完成时有两种情况，
     * 一种是正常执行，返回值。另外一种是遇到异常抛出造成程序的中断。
     * 这里为什么要说成记录，因为这几个方法都会返回CompletableFuture，
     * 当Action执行完毕后它的结果返回原始的CompletableFuture的计算结果或者返回异常。
     * 所以不会对结果产生任何的作用
     */
    @Test
    public void whenComplete() {
        String s = CompletableFuture.supplyAsync(() -> {
            return tast("A");
        }).whenComplete((result,error) -> {
            System.out.println("结果" + result);
            System.out.println("异常" + error);
        }).join();
        System.out.println(s);
    }

    @Test
    public void whenCompleteAsync() {
        String s = CompletableFuture.supplyAsync(() -> {
            return tast("A");
        }).whenCompleteAsync((result,error) -> {
            System.out.println("结果" + result);
            System.out.println("异常" + error);
        }).join();
        System.out.println(s);
    }

    @Test
    public void whenCompleteError() {
        String s = CompletableFuture.supplyAsync(() -> {
            error();
            return tast("A");
        }).whenComplete((result,error) -> {
            System.out.println("结果" + result);
            System.out.println("异常" + error);
        }).join();
        System.out.println(s);
    }

    @Test
    public void whenCompleteAsyncError() {
        String s = CompletableFuture.supplyAsync(() -> {
            error();
            return tast("A");
        }).whenCompleteAsync((result,error) -> {
            System.out.println("结果" + result);
            System.out.println("异常" + error);
        }).join();
        System.out.println(s);
    }

    /**
     * 12 运行完成时，对结果的处理。
     */
    @Test
    public void handle() {
        String s = CompletableFuture.supplyAsync(() -> {
            return tast("A");
        }).handle((result,error) -> {
            return "结果" + result + "\n" + "异常" + error;
        }).join();
        System.out.println(s);
    }

    @Test
    public void handleAsync() {
        String s = CompletableFuture.supplyAsync(() -> {
            return tast("A");
        }).handleAsync((result,error) -> {
            return "结果" + result + "\n" + "异常" + error;
        }).join();
        System.out.println(s);
    }

    @Test
    public void handleError() {
        String s = CompletableFuture.supplyAsync(() -> {
            error();
            return tast("A");
        }).handle((result,error) -> {
            return "结果" + result + "\n" + "异常" + error;
        }).join();
        System.out.println(s);
    }

    @Test
    public void handleAsyncError() {
        String s = CompletableFuture.supplyAsync(() -> {
            error();
            return tast("A");
        }).handleAsync((result,error) -> {
            return "结果" + result + "\n" + "异常" + error;
        }).join();
        System.out.println(s);
    }

    /**
     * 13 使用allOf与anyOf对结果进行处理
     * allOf等待所有执行结束
     * anyOf及早求值
     */
    @Test
    public void anyOf() {
        Optional.of(IntStream.rangeClosed(1,20).boxed()
                .map(e -> CompletableFuture.supplyAsync(() -> tast("num：" + e))).collect(Collectors.toList())
        ).map(e -> e.toArray(new CompletableFuture[e.size()]))
                .map(CompletableFuture::anyOf)
                .map(CompletableFuture::join)
                .map(e -> "结果" + e)
                .ifPresent(System.out::println);
        System.out.println("结束");
    }

    @Test
    public void allOf() {
        Optional.of(IntStream.rangeClosed(1,20).boxed()
                        .map(e -> CompletableFuture.supplyAsync(() -> tast("num：" + e))).collect(Collectors.toList())
        ).map(e -> e.toArray(new CompletableFuture[e.size()]))
                .map(CompletableFuture::allOf)
                .map(CompletableFuture::join)
                .map(e -> "结果" + e)
                .ifPresent(System.out::println);
        System.out.println("结束");
    }

    /**
     * 补充一下并行流的概念，这差不多是最低成本写异步程序的手段了
     * 可以很简单的处理CPU密集型以及对线程数要求并不高的问题
     */
    @Test
    public void parallelStream() {
        List<String> list = IntStream.rangeClosed(1,20)
                .boxed().parallel()
                .map(e -> tast("" + e)).collect(Collectors.toList());
        System.out.println(list);
    }

    @Test
    public void stream() {
        List<String> list = IntStream.rangeClosed(1,20)
                .boxed()
                .map(e -> tast("" + e)).collect(Collectors.toList());
        System.out.println(list);
    }

}
