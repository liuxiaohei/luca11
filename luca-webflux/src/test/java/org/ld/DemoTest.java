package org.ld;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.ld.actors.Buncher;
import org.ld.beans.Flush;
import org.ld.beans.Queue;
import org.ld.beans.SetTarget;
import org.ld.utils.ActorSystemHolder;
import org.ld.utils.JsonUtil;
import org.ld.utils.SnowflakeId;
import org.ld.utils.ZLogger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
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
        var list = Arrays.asList("tdt", "syncher-server", "syncher-client");
        var digestMap = new HashMap<String, String>();
        list.forEach(s -> {
            Process process;
            try {
                System.out.println("docker pull 172.16.1.99/postcommit/" + s + ":trunk");
                process = Runtime.getRuntime().exec("docker pull 172.16.1.99/postcommit/" + s + ":trunk");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            var result = convertStreamToStr(process.getInputStream());
            if (result.size() < 2) {
                throw new RuntimeException("拉取镜像失败");
            }
            digestMap.put(s, result.get(1));
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
        imageIdMap.forEach((name, imageId) -> {
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
        var tag = "studio-1.5.0-rc4";
        var list = Arrays.asList("tdt", "canal-server", "canal-client");
        var digestMap = new HashMap<String, String>();
        list.forEach(s -> {
            Process process;
            try {
                System.out.println("docker pull 172.16.1.99/gold/" + s + ":studio-1.5");
                process = Runtime.getRuntime().exec("docker pull 172.16.1.99/gold/" + s + ":studio-1.5");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            var result = convertStreamToStr(process.getInputStream());
            if (result.size() < 2) {
                throw new RuntimeException("拉取镜像失败");
            }
            digestMap.put(s, result.get(1));
        });
        var process = Runtime.getRuntime().exec("docker images");
        var result = convertStreamToStr(process.getInputStream());
        var s1 = result.stream().skip(1).filter(s -> s.startsWith("172.16.1.99/gold/")).collect(Collectors.toList());
        var imageIdMap = list.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        e -> s1.stream().filter(f -> f.contains(e)).findFirst()
                                .map(g -> Stream.of(g.split(" "))
                                        .filter(h -> h.length() > 0).skip(2)
                                        .findFirst().orElse(""))
                                .orElse("")));
        imageIdMap.forEach((name, imageId) -> {
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
    public void ab() {
        var atomicInteger = new AtomicInteger(0);// 0 t1 可写 1 t1 正在写 2 t2 可写 3 t2 正在写
        var t1 = CompletableFuture.runAsync(() -> IntStream.range(1, 10)
                .forEach(i -> {
                    while (!atomicInteger.compareAndSet(0, 1)) ;
                    log.info(i + "");
                    atomicInteger.set(2); //至成 2 相当于给线程2 发出一个可执行信号 1 3 只允许内部修改
                }));
        var t2 = CompletableFuture.runAsync(() -> Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i")
                .forEach(i -> {
                    while (!atomicInteger.compareAndSet(2, 3)) ;
                    log.info(i);
                    atomicInteger.set(0); // 至成 0 相当于给线程1 发出一个可执行信号 1 3 只允许内部修改
                }));
        CompletableFuture.allOf(t1, t2).join();
    }


    /**
     * 使用递归的二分查找
     * title:recursionBinarySearch
     *
     * @param arr 有序数组
     * @param key 待查找关键字
     * @return 找到的位置
     */
    public static int recursionBinarySearch(int[] arr, int key, int low, int high) {
        if (key < arr[low] || key > arr[high] || low > high) {
            return -1;
        }
        int middle = (low + high) / 2;            //初始中间位置
        if (arr[middle] > key) {
            return recursionBinarySearch(arr, key, low, middle - 1);
        } else if (arr[middle] < key) {
            return recursionBinarySearch(arr, key, middle + 1, high);
        } else {
            return middle;
        }
    }

    /**
     * 不使用递归的二分查找
     * title:commonBinarySearch
     *
     * @param arr
     * @param key
     * @return 关键字位置
     */
    public static int commonBinarySearch(int[] arr, int key) {
        int low = 0;
        int high = arr.length - 1;
        int middle;
        if (key < arr[low] || key > arr[high]) {
            return -1;
        }
        while (low <= high) {
            middle = (low + high) / 2;
            if (arr[middle] > key) {
                high = middle - 1;
            } else if (arr[middle] < key) {
                low = middle + 1;
            } else {
                return middle;
            }
        }
        return -1;        //最后仍然没有找到，则返回-1
    }

    @Test
    public void aaa() {
        int[] arr = {1, 3, 5, 7, 9, 11};
        int key = 5;
        int positionx = recursionBinarySearch(arr, key, 0, arr.length - 1);
        int position = commonBinarySearch(arr, key);
        if (positionx == -1) {
            System.out.println("查找的是" + key + ",序列中没有该数！");
        } else {
            System.out.println("查找的是" + key + ",找到位置为：" + positionx);
        }
        if (position == -1) {
            System.out.println("查找的是" + key + ",序列中没有该数！");
        } else {
            System.out.println("查找的是" + key + ",找到位置为：" + position);
        }

    }


    @Test
    public void testBuncherActorBatchesCorrectly() throws InterruptedException {
        ActorSystem system = ActorSystemHolder.ACTORSYSTEM;
        final ActorRef buncher = system.actorOf(Props.create(Buncher.class));
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
        Thread.sleep(1000);
        system.stop(buncher);
    }

    int searchDisorderedArray(int[] a, int key, int begin, int end) {
        if (begin == end && a[begin] != key)
            return -1;
        int mid = (begin + end) / 2;
        if (a[mid] == key)
            return mid;
        if (a[mid] > a[end]) { // mid 之前是有序数组
            if (key >= a[begin] && key <= a[mid]) // 如果key 的大小在前半段的范围 就只能从前半段查找 否则只能从后半段查找
                return searchDisorderedArray(a, key, begin, mid - 1);
            else
                return searchDisorderedArray(a, key, mid + 1, end);
        } else {               // mid 之后是有序数组
            if (key >= a[mid] && key <= a[end])  // 如果key 的大小在前后段的范围 就只能从后半段查找 否则只能从前半段查找
                return searchDisorderedArray(a, key, mid + 1, end);
            else
                return searchDisorderedArray(a, key, begin, mid - 1);
        }
    }

    @Test
    public void searchDisorderedArrayTest() {
        System.out.println(searchDisorderedArray(new int[]{4, 5, 6, 7, 1, 2, 3}, 1, 0, 6));
    }


    Function<Integer, Integer> adder() {
        final AtomicInteger sum = new AtomicInteger(0);
        return (Integer value) -> {
            sum.addAndGet(value);
            return sum.get();
        };
    }

    @Test
    public void demo111() {
        Function<Integer, Integer> function = adder();
        for (int i = 0; i < 10; i++) {
            System.out.println(function.apply(i));
        }
    }
}
