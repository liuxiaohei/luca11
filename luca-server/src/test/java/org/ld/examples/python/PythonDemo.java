package org.ld.examples.python;

import org.junit.jupiter.api.Test;
import org.python.util.PythonInterpreter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * https://www.cnblogs.com/wuwuyong/p/10600749.html
 */
public class PythonDemo {

    @Test
    public void test() {
        PythonInterpreter interpreter = new PythonInterpreter();
        interpreter.exec("a='hello world'; ");
        interpreter.exec("print a;");
    }

    @Test
    public void test1() {
        PythonInterpreter interpreter = new PythonInterpreter();
        interpreter.execfile("py/javaPythonFile.py");
    }

    /**
     * Permission denied
     */
    @Test
    public void test2() {
        Process proc;
        try {
            proc = Runtime.getRuntime().exec("py/javaPythonFile.py");
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
            proc.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
