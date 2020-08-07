package org.ld.controller;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.RandomPool;
import io.r2dbc.spi.ConnectionFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.ld.actors.ProcessChecker;
import org.ld.actors.ProcessDispatcher;
import org.ld.actors.ProcessExecutorAdapter;
import org.ld.actors.ProcessStarter;
import org.ld.annotation.NeedToken;
import org.ld.beans.ProcessData;
import org.ld.beans.OnSuccessResp;
import org.ld.beans.User;
import org.ld.beans.UserRepository;
import org.ld.config.AkkaConfig;
import org.ld.engine.ExecutorEngine;
import org.ld.enums.ProcessState;
import org.ld.utils.JwtUtils;
import org.ld.utils.ZLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
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
    private ExecutorEngine descriptor;

    @Autowired
    private ForkJoinPool forkJoinPool;

    @Autowired
    private ConnectionFactory connectionFactory;

    @ApiOperation(value = "事例", produces = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping(value = "demo")
    public Mono<Map<String, Object>> demo(@RequestParam String param) {
        Map<String, Object> a = new HashMap<>();
        var b = new HashMap<>();
        b.put("wer", List.of("234", "333", "eee"));
        a.put("aaa", b);
        descriptor.runAsync(() -> ZLogger.newInstance().info(param));
        return Mono.fromSupplier(() -> a);
    }

    @ApiOperation(value = "事例", produces = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping(value = "demo")
    public Mono<Map<Object, Object>> postDemo(@RequestBody OnSuccessResp<String> aaa) {
        var a = new HashMap<>();
        var b = new HashMap<>();
        b.put("wer", List.of("234", "333", "eee"));
        a.put("aaa", b);
        return Mono.fromSupplier(() -> a);
    }

    private static Mono<Void> executeStatement(DatabaseClient client, String sql) {
        String[] statements;
        if (sql.contains(";")) {
            statements = sql.split(";");
        } else {
            statements = new String[]{sql};
        }
        return Flux
                .fromArray(statements)
                .flatMap(s -> client.execute().sql(s).then())
                .then();
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Order {
        private Long id;
        private String name;
    }

    @ApiOperation(value = "r2dbc", produces = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping(value = "r2dbc")
    public Mono<Void> r2dbc(@RequestBody OnSuccessResp<String> aaa) {
        var schemaStatement = executeStatement(DatabaseClient.create(connectionFactory),
                "CREATE TABLE  IF NOT EXISTS orders  (\n" +
                        "  id bigint primary key auto_increment not null ,\n" +
                        "  fn varchar(255) not null\n" +
                        ");\n" +
                        "truncate orders ;");
        var dataStatement = executeStatement(DatabaseClient.create(connectionFactory),
                "insert into orders(fn) values('Jane');\n" +
                        "insert into orders(fn) values('John');");

        var orderFlux = DatabaseClient.create(connectionFactory)
                .execute()
                .sql(" SELECT * FROM orders ")
                .fetch()
                .all()
                .map(e -> {
                    var id = (Long) e.get("id");
                    var fn = (String) e.get("fn");
                    return new Order(id, fn);
                });
        schemaStatement
                .thenMany(dataStatement)
                .thenMany(orderFlux)
                .subscribe(log::info);
        return Mono.empty();
    }

    /**
     * 查询
     *
     * @return 返回Flux序列 包含所有的ClientUser
     */
    @GetMapping("/get")
    public Flux<Order> clientUserFlux() {
        return DatabaseClient.create(connectionFactory)
                .execute()
                .sql("select * from orders ")
                .fetch()
                .all()
                .map(e -> {
                    var id = (Long) e.get("id");
                    var fn = (String) e.get("fn");
                    return new Order(id, fn);
                });
    }

//    /**
//     * 响应式写入.
//     *
//     * @return Mono对象包含更新成功的条数
//     */
//    @GetMapping("/add")
//    public Mono<Integer> insert() {
//        Order clientUser = new Order();
//        clientUser.setUserId("34345514644");
//        clientUser.setUsername("felord.cn");
//        clientUser.setPhoneNumber("3456121");
//        clientUser.setGender(1);
//
//        return client.insert().into(ClientUser.class)
//                .using(clientUser)
//                .fetch().rowsUpdated();
//    }

//}

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

    @Autowired
    AkkaConfig akkaConfig;

    @GetMapping("akkademo")
    public Mono<String> getAkkaDemo() throws Exception {
        var ref = akkaConfig.getActorRef("counter", "testActor");
        IntStream.rangeClosed(1, 100000).parallel().forEach(i -> ref.tell("hello", ActorRef.noSender()));
        //        actorSystem.terminate(); // 这个方法终止 actor
        return Mono.fromSupplier(() -> "success");
    }

    @GetMapping("akkademo1")
    public Mono<String> getAkkaDemo1() throws Exception {
        var ref = akkaConfig.getActorRef("counter", "testActor1");
        ref.tell("hello", ActorRef.noSender());
        //        actorSystem.terminate(); // 这个方法终止 actor
        return Mono.fromSupplier(() -> "success");
    }

    @GetMapping("fsmdemo")
    public Mono<String> getFSMDemo() {
        var processExecutorAdapter = new ProcessExecutorAdapter();
        var processStarter = actorSystem.actorOf(new RandomPool(10)
                .props(Props.create(ProcessStarter.class, processExecutorAdapter)));
        var processChecker = actorSystem.actorOf(new RandomPool(10)
                .props(Props.create(ProcessChecker.class, processExecutorAdapter)));
        actorSystem.actorOf(Props.create(
                ProcessDispatcher.class,
                ProcessState.CREATED,
                new ProcessData().setParams("5000", ""),
                processStarter,
                processChecker));
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

    @Autowired
    UserRepository userRepository;

    /**
     * @return 返回Flux 非阻塞序列
     */
    @GetMapping("users")
    public Flux<User> getAll() {
        String threadName = Thread.currentThread().getName();
        System.out.println("HelloWorldAsyncController[" + threadName + "]: " + "获取HTTP请求");
        return Flux.fromStream(userRepository.getUsers().values().stream());
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
