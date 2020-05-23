package org.ld.controller;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.ld.annotation.NeedToken;
import org.ld.beans.RespBean;
import org.ld.config.SpringExtProvider;
import org.ld.enums.Events;
import org.ld.enums.States;
import org.ld.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.statemachine.StateMachine;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
@Api(tags = {"事例API"})
@RestController
@SuppressWarnings("unused")
public class DemoController {

    @Autowired
    private ActorSystem actorSystem;

    @ApiOperation(value = "事例", produces = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping(value = "demo")
    public Map<Object, Object> demo() {
        var a = new HashMap<>();
        var b = new HashMap<>();
        b.put("wer", List.of("234", "333", "eee"));
        a.put("aaa", b);
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

    @GetMapping("akkademo")
    public String getAkkaDemo() {
        var ref = actorSystem
                .actorOf(SpringExtProvider.getInstance()
                        .get(actorSystem)
                        .create("testActor"), "testActor");
        ref.tell("hello", ActorRef.noSender());
        return "success";
    }

    @Resource
    StateMachine<States, Events> stateMachine;

    @GetMapping("fsmdemo")
    public void run(String... args) throws Exception {
        stateMachine.start();
        stateMachine.sendEvent(Events.ONLINE);
        stateMachine.sendEvent(Events.PUBLISH);
        stateMachine.sendEvent(Events.ROLLBACK);
    }

}
