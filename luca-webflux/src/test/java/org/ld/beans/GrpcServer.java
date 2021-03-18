package org.ld.beans;

import lombok.experimental.FieldNameConstants;
import org.springframework.stereotype.Service;

/**
 * https://www.jianshu.com/p/0aebc32ef0b9
 */
@Service
@FieldNameConstants(innerTypeName = "CONSTANT",asEnum = true)
public class GrpcServer {

    private String foo;

    private void run(String arg) {
        System.out.println("gprc启动测试ing....." + arg + CONSTANT.foo);
    }

    public static void main(String ... args) {
        System.out.println("gprc启动测试ing....." + CONSTANT.foo);
    }
}
