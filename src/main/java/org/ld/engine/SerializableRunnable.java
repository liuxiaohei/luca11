package org.ld.engine;

import java.io.Serializable;

/**
 * 有时这个写法会成功 有时会失效因此新建了如下的接口
 * Map<String,Runnable> demo = new HashMap<>();
 * demo.put("aaa",(Runnable & Serializable)() -> System.out.println("测试"));
 */
public interface SerializableRunnable extends Runnable, Serializable {
}
