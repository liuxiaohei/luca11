package org.ld.beans;

import org.springframework.stereotype.Service;

@Service
public class GrpcServer {
    private void run(String arg) {
        System.out.println("gprc启动测试ing....." + arg);
    }
}
