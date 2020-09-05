package com.luca.mybatis.generator.plugins.el;

/**
 * @author Vladimir Lokhov
 */
public interface Evaluator {

    Tokenizer getTokenizer();

    public Evaluator compile(String expression);

    public String evaluate(Context context);

    public boolean evaluateBoolean(Context context);

    public Object evaluateObject(Context context);

}
