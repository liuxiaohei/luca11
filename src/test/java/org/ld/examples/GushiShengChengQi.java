package org.ld.examples;

import java.util.Random;
import java.util.stream.IntStream;

/**
 * https://blog.csdn.net/u011545382/article/details/51524689?utm_source=blogxgwz9
 */
public class GushiShengChengQi {

    public static final String[] TWO_CHARS_WORDS = {
            "朱砂", "天下", "杀伐", "人家", "韶华", "风华", "繁华", "血染", "墨染", "白衣", "素衣", "嫁衣", "倾城", "孤城", "空城",
            "旧城", "旧人", "伊人", "春风", "心醉", "古琴", "无情", "迷离", "奈何", "断弦", "焚尽", "散乱", "陌路", "乱世", "笑靥",
            "浅笑", "明眸", "轻叹", "烟火", "一生", "三生", "浮生", "桃花", "梨花", "落花", "烟花", "离殇", "情殇", "爱殇", "剑殇",
            "灼伤", "仓皇", "匆忙", "陌上", "清商", "焚香", "墨香", "微凉", "断肠", "痴狂", "凄凉", "黄梁", "未央", "成双", "无恙",
            "虚妄", "凝霜", "洛阳", "长安", "江南", "忘川", "千年", "纸伞", "烟雨", "回眸", "公子", "红尘", "红颜", "红衣", "红豆",
            "红线", "青丝", "青史", "青冢", "白发", "白首", "白骨", "黄土", "黄泉", "碧落", "紫陌"
    };

    public static final String[] FOUR_CHARS_WORDS = {
            "情深缘浅", "情深不寿", "莫失莫忘", "阴阳相隔", "如花美眷", "似水流年 ",
            "眉目如画", "曲终人散", "繁华落尽", "不诉离殇", "一世长安"
    };

    public static final String[] SENTENCE_MODEL = {
            "xxxx，xxxx，不过是一场xxxx。",
            "你说xxxx，我说xxxx，最后不过xxxx。",
            "xx，xx，许我一场xxxx。 ",
            "你说xxxxxxxx，后来xxxxxxxx。",
            "一x一x一xx，半x半x半xx。",
            "xxxx，xxxx，终不敌xxxx。"
    };

    public static String produceSentence() {
        var ra = new Random();
        var r = SENTENCE_MODEL[ra.nextInt(SENTENCE_MODEL.length)];
        while (r.contains("xxxx")) {
            r = r.replaceFirst("xxxx", FOUR_CHARS_WORDS[ra.nextInt(FOUR_CHARS_WORDS.length)]);
        }
        while (r.contains("xx")) {
            r = r.replaceFirst("xx", TWO_CHARS_WORDS[ra.nextInt(TWO_CHARS_WORDS.length)]);
        }
        while (r.contains("x")) {
            r = r.replaceFirst("x", TWO_CHARS_WORDS[ra.nextInt(TWO_CHARS_WORDS.length)]
                    .charAt(ra.nextInt(2)) + "");
        }
        return r;
    }

    public static void main(String[] args) {
        IntStream.rangeClosed(1,22).forEach(i -> System.out.println(produceSentence()));
    }

}
