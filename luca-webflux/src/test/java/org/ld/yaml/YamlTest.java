package org.ld.yaml;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.net.URL;
import java.util.Map;

// https://www.cnblogs.com/huzi007/p/5841699.html
public class YamlTest {
    public static void main(String[] args) {
        try {
            Yaml yaml = new Yaml();
            URL url = YamlTest.class.getClassLoader().getResource("test.yaml");
            if (url != null) {
                //获取test.yaml文件中的配置数据，然后转换为obj，
                Object obj =yaml.load(new FileInputStream(url.getFile()));
                System.out.println(obj);
                //也可以将值转换为Map
                Map map = yaml.load(new FileInputStream(url.getFile()));
                System.out.println(map);
                //通过map我们取值就可以了.
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
