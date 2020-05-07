package org.ld.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.ld.annotation.NeedToken;
import org.ld.beans.RespBean;
import org.ld.utils.JwtUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 */
@Api(tags = {"事例API"})
@RestController
@SuppressWarnings("unused")
public class DemoController {

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
        return (1575021600000L - System.currentTimeMillis())/1000;
    }

    @PostMapping("getToken")
    public String getToken(@RequestParam String userName, @RequestParam String password){
        if(userName.equals("admin") && password.equals("123456")){
            return JwtUtils.sign("admin");
        }
        return "用户名或密码错误";
    }

    @NeedToken
    @GetMapping("getData")
    public String getData() {
        return "获取数据...";
    }

}
