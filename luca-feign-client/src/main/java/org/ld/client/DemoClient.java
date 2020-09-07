package org.ld.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "demo")
public interface DemoClient {
    String PRE = "/demo";

    @RequestMapping(method = RequestMethod.GET, value = PRE + "/members/{ruleId}")
    String getMembers(@PathVariable("ruleId") Long ruleId);
}
