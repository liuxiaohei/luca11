package org.ld;

import akka.actor.typed.ActorSystem;
import org.junit.jupiter.api.Test;
import org.ld.utils.FileUtil;
import org.ld.utils.JsonUtil;
import org.ld.utils.SnowflakeId;
import org.ld.utils.ZLogger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DemoTest {

    /**
     * 无限流
     */
    @Test
    public void infiniteStream() {
        Stream.generate(() -> SnowflakeId.get().toString()).limit(1000000).forEach(e -> ZLogger.newInstance().info("" + e));
    }

    /**
     * 无限流
     */
    @Test
    public void infiniteStream1() {
        Stream.iterate(0, i -> ++i).limit(1000).forEach(e -> ZLogger.newInstance().info("" + e));
    }

    public static void main(String... args) throws InterruptedException {
    }

    @Test
    public void idWorkerDemo() {
        List<Long> a = IntStream.rangeClosed(1, 1000000)
                .parallel()
                .boxed()
                .map(e -> SnowflakeId.get())
                .collect(Collectors.toList());
        a.stream().sorted().forEach(System.out::println);
    }

    @Test
    public void uuiddemo() {
        System.out.println(UUID.randomUUID());
    }

    @Test
    public void dabao() throws IOException {
        var tag = "studio-2.0.0-final";
        var list = Arrays.asList("tdt","syncher-server","syncher-client");
        var digestMap = new HashMap<String,String>();
        list.forEach(s -> {
            Process process;
            try {
                System.out.println("docker pull 172.16.1.99/postcommit/" + s + ":trunk");
                process = Runtime.getRuntime().exec("docker pull 172.16.1.99/postcommit/" + s + ":trunk");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            var result = convertStreamToStr(process.getInputStream());
            if(result.size() < 2) {
                throw new RuntimeException("拉取镜像失败");
            }
            digestMap.put(s,result.get(1));
        });
        var process = Runtime.getRuntime().exec("docker images");
        var result = convertStreamToStr(process.getInputStream());
        var s1 = result.stream().skip(1).filter(s -> s.startsWith("172.16.1.99/postcommit/")).collect(Collectors.toList());
        var imageIdMap = list.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        e -> s1.stream().filter(f -> f.contains(e)).findFirst()
                                .map(g -> Stream.of(g.split(" "))
                                        .filter(h -> h.length() > 0).skip(2)
                                        .findFirst().orElse(""))
                                .orElse("")));
        imageIdMap.forEach((name,imageId) -> {
            try {
                final var fullTag = "172.16.1.99/transwarp/" + name + ":" + tag;
                System.out.println("docker tag " + imageId + " " + fullTag);
                var p = Runtime.getRuntime().exec("docker tag " + imageId + " " + fullTag);
                p.waitFor();
                System.out.println("docker push " + fullTag);
                p = Runtime.getRuntime().exec("docker push " + fullTag);
                p.waitFor();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("=======");
        list.forEach(e -> {
            System.out.println("name :" + e);
            System.out.println("imageId :" + imageIdMap.get(e));
            System.out.println(digestMap.get(e));
            System.out.println("=======");
        });
    }


    @Test
    public void tdt1_Xdabao() throws IOException {
        var tag = "studio-1.5.0-rc1";
        var list = Arrays.asList("tdt","canal-server","canal-client");
        var digestMap = new HashMap<String,String>();
        list.forEach(s -> {
            Process process;
            try {
                System.out.println("docker pull 172.16.1.99/gold/" + s + ":studio-1.5");
                process = Runtime.getRuntime().exec("docker pull 172.16.1.99/gold/" + s + ":studio-1.5");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            var result = convertStreamToStr(process.getInputStream());
            if(result.size() < 2) {
                throw new RuntimeException("拉取镜像失败");
            }
            digestMap.put(s,result.get(1));
        });
        var process = Runtime.getRuntime().exec("docker images");
        var result = convertStreamToStr(process.getInputStream());
        var s1 = result.stream().skip(1).filter(s -> s.startsWith("172.16.1.99/postcommit/")).collect(Collectors.toList());
        var imageIdMap = list.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        e -> s1.stream().filter(f -> f.contains(e)).findFirst()
                                .map(g -> Stream.of(g.split(" "))
                                        .filter(h -> h.length() > 0).skip(2)
                                        .findFirst().orElse(""))
                                .orElse("")));
        imageIdMap.forEach((name,imageId) -> {
            try {
                final var fullTag = "172.16.1.99/transwarp/" + name + ":" + tag;
                System.out.println("docker tag " + imageId + " " + fullTag);
                var p = Runtime.getRuntime().exec("docker tag " + imageId + " " + fullTag);
                p.waitFor();
                System.out.println("docker push " + fullTag);
                p = Runtime.getRuntime().exec("docker push " + fullTag);
                p.waitFor();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("=======");
        list.forEach(e -> {
            System.out.println("name :" + e);
            System.out.println("imageId :" + imageIdMap.get(e));
            System.out.println(digestMap.get(e));
            System.out.println("=======");
        });
    }

    public List<String> convertStreamToStr(InputStream is) {
        final String chars = "\n";
        String result;
        if (is != null) {
            var writer = new StringWriter();
            var buffer = new char[1024];
            try (is) {
                var reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            result = writer.toString();
        } else {
            result = "";
        }
        return Stream.of(result.split(chars)).collect(Collectors.toList());
    }

    @Test
    public void test() {
        var user = new User();
        user.age = 20;
        user.username = "test";
        user.hobbies = Arrays.asList("x", "y", "z").toArray(new String[]{});
        user.phones = new HashSet<String>(Arrays.asList("a", "b", "c"));
        user.valid = true;
        System.out.println(JsonUtil.obj2PrettyJson(user));
    }

    @Test
    public void ab() throws InterruptedException {
        var atomicInteger = new AtomicInteger(0);// 0 t1 可写 1 t1 正在写 2 t2 可写 3 t2 正在写
        var t1 = new Thread(() -> IntStream.range(1,10).forEach(i -> {
            while (!atomicInteger.compareAndSet(0,1)) {}
            System.out.print(i + " ");
            atomicInteger.set(2);
        }));
        var t2 = new Thread(() -> Arrays.asList("a","b","c","d","e","f","g","h","i").forEach(i -> {
            while (!atomicInteger.compareAndSet(2,3)) {}
            System.out.print(i + " ");
            atomicInteger.set(0);
        }));
        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

}
