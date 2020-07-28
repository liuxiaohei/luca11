package org.ld.controller;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.RandomPool;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.ld.actors.ProcessChecker;
import org.ld.actors.ProcessDispatcher;
import org.ld.actors.ProcessExecutorAdapter;
import org.ld.actors.ProcessStarter;
import org.ld.annotation.NeedToken;
import org.ld.beans.ProcessData;
import org.ld.beans.RespBean;
import org.ld.config.AkkaConfig;
import org.ld.engine.ExecutorEngine;
import org.ld.enums.ProcessState;
import org.ld.utils.JwtUtils;
import org.ld.utils.ZLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;

/**
 *
 */
@Api(tags = {"样例API"})
@RestController
@SuppressWarnings("unused")
public class DemoController {

    @Autowired
    private ActorSystem actorSystem;

    @Autowired
    private ExecutorEngine descriptor;

    @Autowired
    private ForkJoinPool forkJoinPool;

    @ApiOperation(value = "事例", produces = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping(value = "demo")
    public Map<String, Object> demo(@RequestParam String param) {
        Map<String, Object> a = new HashMap<>();
        var b = new HashMap<>();
        b.put("wer", List.of("234", "333", "eee"));
        a.put("aaa", b);
        descriptor.runAsync(() -> ZLogger.newInstance().info(param));
        return a;
    }

    @ApiOperation(value = "事例", produces = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping(value = "demo")
    public Map<Object, Object> postDemo(@RequestBody RespBean<String> aaa) {
        var a = new HashMap<>();
        var b = new HashMap<>();
        b.put("wer", List.of("234", "333", "eee"));
        a.put("aaa", b);
        return a;
    }

    @ApiOperation(value = "错误事例", produces = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping(value = "errored")
    public Map<String, Object> errorDemo() {
        Map<String, Object> a = new HashMap<>();
        Objects.requireNonNull(null);
        return a;
    }

    @ApiOperation(value = "时间", produces = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping(value = "time")
    public Long time() {
        return (1575021600000L - System.currentTimeMillis()) / 1000;
    }

    @PostMapping("getToken")
    public String getToken(@RequestParam String userName, @RequestParam String password) {
        if (userName.equals("admin") && password.equals("123456")) {
            return JwtUtils.sign("admin");
        }
        return "用户名或密码错误";
    }

    @GetMapping("stringdemo")
    public String getStringDemo() {
        return "获取数据...";
    }

    @NeedToken
    @GetMapping("getData")
    public String getData() {
        return "获取数据...";
    }

    @Autowired
    AkkaConfig akkaConfig;

    @GetMapping("akkademo")
    public String getAkkaDemo() throws Exception {
        var ref = akkaConfig.getActorRef("counter", "testActor");
        ref.tell("hello", ActorRef.noSender());
        //        actorSystem.terminate(); // 这个方法终止 actor
        return "success";
    }

    @GetMapping("akkademo1")
    public String getAkkaDemo1() throws Exception {
        var ref = akkaConfig.getActorRef("counter", "testActor1");
        ref.tell("hello", ActorRef.noSender());
        //        actorSystem.terminate(); // 这个方法终止 actor
        return "success";
    }

    @GetMapping("fsmdemo")
    public String getFSMDemo() {
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
        return "success";
    }

    @GetMapping("fjdemo")
    public String fjDemo() {
        forkJoinPool.submit(() -> System.out.println("aaaaa"));
        return "success";
    }

}
