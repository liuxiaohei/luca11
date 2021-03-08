https://doc.akka.io/docs/akka/current/index.html

若用协程需要添加如下jvm参数
-Dco.paralleluniverse.fibers.verifyInstrumentation=true
-javaagent:$MODULE_DIR$/../java-agent/quasar-core-0.8.0.jar

todo
https://blog.csdn.net/weixin_43146692/article/details/107561797
