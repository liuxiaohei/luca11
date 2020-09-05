package org.ld.examples.json;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * @author ld
 * 使用Gson比较两个json字符串是否等价
 */
public class JsonSame {

//    /**
//     * 比较两个bean是否等价
//     */
//    public static boolean same(Object a, Object b) {
//        if (a == null) {
//            return b == null;
//        }
//        return same(gson.toJson(a), gson.toJson(b));
//    }
//
//    /**
//     * 比较两个json字符串是否等价
//     */
//    public static boolean same(String a, String b) {
//        if (a == null) {
//            return b == null;
//        }
//        if (a.equals(b)) {
//            return true;
//        }
//        JsonElement aElement = parser.parse(a);
//        JsonElement bElement = parser.parse(b);
//        if (gson.toJson(aElement).equals(gson.toJson(bElement))) {
//            return true;
//        }
//        return same(aElement, bElement);
//    }
//
//    private static boolean same(JsonElement a, JsonElement b) {
//        System.out.println("A:" + a.toString());
//        System.out.println("B:" + b.toString());
//        if (a.isJsonObject() && b.isJsonObject()) {
//            return same((JsonObject) a, (JsonObject) b);
//        } else if (a.isJsonArray() && b.isJsonArray()) {
//            return same((JsonArray) a, (JsonArray) b);
//        } else if (a.isJsonPrimitive() && b.isJsonPrimitive()) {
//            return same((JsonPrimitive) a, (JsonPrimitive) b);
//        } else if (a.isJsonNull() && b.isJsonNull()) {
//            return same((JsonNull) a, (JsonNull) b);
//        } else {
//            return Boolean.FALSE;
//        }
//    }
//
//    private static boolean same(JsonObject a, JsonObject b) {
//        Set<String> aSet = a.keySet();
//        Set<String> bSet = b.keySet();
//        if (!aSet.equals(bSet)) {
//            return false;
//        }
//        for (String aKey : aSet) {
//            if (!same(a.get(aKey), b.get(aKey))) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    private static boolean same(JsonArray a, JsonArray b) {
//        if (a.size() != b.size()) {
//            return false;
//        }
//        List<JsonElement> aList = toSortedList(a);
//        List<JsonElement> bList = toSortedList(b);
//        for (int i = 0; i < aList.size(); i++) {
//            if (!same(aList.get(i), bList.get(i))) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    private static boolean same(JsonPrimitive a, JsonPrimitive b) {
//
//        return a.equals(b);
//    }
//
//    private static boolean same(JsonNull a, JsonNull b) {
//        return true;
//    }
//
//    private static List<JsonElement> toSortedList(JsonArray a) {
//        List<JsonElement> aList = new ArrayList<>();
//        a.forEach(aList::add);
//        aList.sort(Comparator.comparing(gson::toJson));
//        return aList;
//    }
//
//    /**
//     * 指定url的post请求
//     */
//    public static String postReq(String u,Object params) {
//        try {
//            final URL url = new URL(u);
//            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
//            httpURLConnection.setRequestMethod("POST");
//            httpURLConnection.setRequestProperty("Content-Type","application/json; charset=UTF-8");
//            httpURLConnection.setDoOutput(true);
//            httpURLConnection.setDoInput(true);
//            final PrintWriter printWriter = new PrintWriter(httpURLConnection.getOutputStream());
//            printWriter.write(JSONUtil.obj2Json(params));
//            printWriter.flush();
//            InputStream in = httpURLConnection.getInputStream();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//            String resp = bufferedReader2json(reader);
//            return resp;
//        } catch (Exception e) {
//            throw new RuntimeException(e.getMessage());
//        }
//    }
//
//    /**
//     * I/O 将BufferedReader转化成json格式
//     */
//    private static String bufferedReader2json(BufferedReader bufferedReader) {
//        StringBuilder sb = new StringBuilder();
//        String line;
//        try {
//            while (Objects.nonNull(line = bufferedReader.readLine())) {
//                sb.append(line);
//            }
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//        return sb.toString();
//    }
}