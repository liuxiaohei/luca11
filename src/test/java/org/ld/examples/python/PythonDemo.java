package org.ld.examples.python;

import org.junit.jupiter.api.Test;
import org.python.util.PythonInterpreter;

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
}
