package org.ld.controller;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ld.annotation.NeedToken;
import org.ld.beans.Flush;
import org.ld.beans.Queue;
import org.ld.beans.RespBean;
import org.ld.beans.SetTarget;
import org.ld.pool.IOExecutor;
import org.ld.utils.AkkaUtil;
import org.ld.utils.JwtUtils;
import org.ld.utils.ZLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 */
@Api(tags = {"样例API"})
@Log4j2
@RestController
@SuppressWarnings("unused")
public class DemoController {

    @Autowired
    private ActorSystem actorSystem;

    @Autowired
    private ForkJoinPool forkJoinPool;

    @ApiOperation(value = "事例", produces = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping(value = "demo")
    public Mono<Map<Object, Object>> postDemo(@RequestBody RespBean<String> aaa) {
        var a = new HashMap<>();
        var b = new HashMap<>();
        b.put("wer", List.of("234", "333", "eee"));
        a.put("aaa", b);
        return Mono.fromSupplier(() -> a);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Order {
        @Id
        private Long id;
        private String name;
    }

    @ApiOperation(value = "错误事例", produces = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping(value = "errored")
    public Mono<Map<String, Object>> errorDemo() {
        return Mono.fromSupplier(() -> {
            Map<String, Object> a = new HashMap<>();
            Objects.requireNonNull(null);
            return a;
        });
    }

    @ApiOperation(value = "时间", produces = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping(value = "time")
    public Long time() {
        return (1575021600000L - System.currentTimeMillis()) / 1000;
    }

    @PostMapping("getToken")
    public Mono<String> getToken(@RequestParam String userName, @RequestParam String password) {
        if (userName.equals("admin") && password.equals("123456")) {
            return Mono.fromSupplier(() -> JwtUtils.sign("admin"));
        }
        return Mono.fromSupplier(() -> "用户名或密码错误");
    }

    @GetMapping("stringdemo")
    public Mono<String> getStringDemo() {
        return Mono.fromSupplier(() -> "获取数据...");
    }

    @NeedToken
    @GetMapping("getData")
    public Mono<String> getData() {
        return Mono.fromSupplier(() -> "获取数据...");
    }

    @GetMapping("quasarDemo")
    public Mono<String> quasarDemo() {
        var a = IntStream.rangeClosed(1, 100000).boxed().parallel().map(i -> CompletableFuture.supplyAsync(() -> {
            var client = HttpClient.newHttpClient();
            HttpRequest request = null;
            try {
                request = HttpRequest.newBuilder()
                        .uri(new URI("https://labs.consol.de/"))
                        .GET()
                        .build();
                var tempFile = Files.createTempFile("consol-labs-home", ".html");
                var response = client.send(request, HttpResponse.BodyHandlers.ofFile(tempFile));
                log.info(response.statusCode());
                log.info(response.body());
            } catch (URISyntaxException | IOException | InterruptedException e) {
                e.printStackTrace();
            }
            log.info(i + "");
            return i;
        }, IOExecutor.getInstance())).collect(Collectors.toList());
        var b = a.stream().map(CompletableFuture::join).collect(Collectors.toList());
        return Mono.fromSupplier(() -> "success");
    }

    @GetMapping("akkademo")
    public Mono<String> getAkkaDemo() {
        var ref = AkkaUtil.getActorRef("counter", "testActor");
        IntStream.rangeClosed(1, 100000).parallel().forEach(i -> ref.tell("hello", ActorRef.noSender()));
        return Mono.fromSupplier(() -> "success");
    }

    @GetMapping("akkademo1")
    public Mono<String> getAkkaDemo1() {
        var ref = AkkaUtil.getActorRef("counter", "testActor1");
        ref.tell("hello", ActorRef.noSender());
        return Mono.fromSupplier(() -> "success");
    }

    @GetMapping("akkafsmdemo1")
    public Mono<String> getAkkafsmDemo1() {
        var buncher = AkkaUtil.getActorRef("buncher", "testActor2");
        final ActorRef probe = ActorRef.noSender();
        buncher.tell(new SetTarget(probe), probe);
        buncher.tell(new org.ld.beans.Queue(42), probe);
        buncher.tell(new org.ld.beans.Queue(43), probe);
        LinkedList<Object> list1 = new LinkedList<>();
        list1.add(42);
        list1.add(43);
        buncher.tell(new org.ld.beans.Queue(44), probe);
        buncher.tell(Flush.Flush, probe);
        buncher.tell(new Queue(45), probe);
        LinkedList<Object> list2 = new LinkedList<>();
        list2.add(44);
        LinkedList<Object> list3 = new LinkedList<>();
        list3.add(45);
        return Mono.fromSupplier(() -> "success");
    }

    @GetMapping("stopdemo1")
    public Mono<String> stopAkkaDemo1() {
        AkkaUtil.stopActorRef("counter", "testActor1");
        return Mono.fromSupplier(() -> "success");
    }

    @GetMapping("fjdemo")
    public Mono<String> fjDemo() {
        forkJoinPool.submit(() -> System.out.println("aaaaa"));
        return Mono.fromSupplier(() -> "success");
    }

    /**
     * Mono
     * 0-1 的非阻塞结果
     * Reactive Streams JVM API Publisher
     * 非阻塞 Optional
     * Flux
     * 0-N 的非阻塞序列
     * Reactive Streams JVM API Publisher
     * 非阻塞 Stream
     * https://www.jianshu.com/p/2db1ecacb770
     * https://www.cnblogs.com/zhujiabin/p/9849669.html
     * https://blog.csdn.net/u011499747/article/details/78065544
     * https://www.cnblogs.com/Java3y/p/11880377.html
     */
    // WebFlux(返回的是Mono)
    @GetMapping("/hi")
    private Mono<String> get2() {
        ZLogger.newInstance().info("get2 start");
        Mono<String> result = Mono.fromSupplier(() -> {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "demo";
        });
        ZLogger.newInstance().info("get2 end.");
        return result;
    }

    @GetMapping(value = "/3", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> flux() {
        return Flux
                .fromStream(IntStream.range(1, 5).mapToObj(i -> {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException ignored) {
                    }
                    ZLogger.newInstance().info("flux data--" + i);
                    return "flux data--" + i;
                }));
    }

}
