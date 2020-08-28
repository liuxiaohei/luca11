package org.ld.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.IntStream;

/**
 * @author ld
 */
@RestController
@RequestMapping("/")
public class MakeAvatarController {

    private final List<String> colors = Arrays.asList(
            "34CFA3",
            "3BA4FA",
            "FFC355",
            "00C2D0",
            "FE715D",
            "FE8C71",
            "FEA66A",
            "B7CE48",
            "4660E9"); //随机颜色的范围

    Random RANDOM = new Random();

    /**
     *
     */
    @GetMapping(value = "avatar", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] makeAvatar() {
        final String str = Optional.of("刘")
                .map(e -> e.substring(0, 1)).orElse(" ");
        final String color = colors.get(RANDOM.nextInt(colors.size() - 1)); // 颜色
        final int r = 120;
        BufferedImage bi = new BufferedImage(r, r, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = bi.createGraphics();
        g.setBackground(str2Color(color));
        g.clearRect(0, 0, r, r);//通过使用当前绘图表面的背景色进行填充来清除指定的矩形。
        g.dispose();
        BufferedImage watermark = makelogoText(
                str,
                getFont(1, (r * 2) / 5)
        );
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);//文字边缘去锯齿
        g.setFont(getFont(1, (r * 2) / 5));
        final FontMetrics m = g.getFontMetrics();
        mark(bi, watermark, (r - m.stringWidth(str)) / 2
        );
        return imageToBytes(bi, "jpg");
    }

    public static byte[] imageToBytes(BufferedImage bImage, String format) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(bImage, format, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    /**
     * 添加图片水印默认宽高
     */
    private static void mark(BufferedImage inputImg, BufferedImage markImg, int x) {
        final BufferedImage bufImg = new BufferedImage(
                inputImg.getWidth(null),
                inputImg.getHeight(null),
                BufferedImage.TYPE_INT_RGB);
        mark(bufImg, inputImg, markImg, markImg.getWidth(null), markImg.getHeight(null),
                x);
    }

    /**
     * 加图片水印
     */
    private static void mark(BufferedImage bufImg, Image img, Image markImg, int width, int height, int x) {
        Graphics2D g = bufImg.createGraphics();
        g.drawImage(img, 0, 0, bufImg.getWidth(), bufImg.getHeight(), null);
        g.drawImage(markImg, x, 30, width, height, null);
        g.dispose();
    }

    private static final Map<String, Font> FONT_TEMP = new HashMap<>();// 字体缓存


    public static Font getFont(Integer fontStyle, Integer fontSize) {
        return FONT_TEMP.computeIfAbsent("pingfang" + fontStyle + fontSize, fn -> {
            try {
                return Font.createFont(Font.TRUETYPE_FONT, new File("classpath:/PingFang.ttc"))
                        .deriveFont(fontStyle, fontSize);      // 默认字体为，苹方

            } catch (Exception e) {
                return new Font("pingfang", fontStyle, fontSize);    // 为本机测试用
            }
        });                                                        // 字体设置
    }

    private static BufferedImage makelogoText(
            String logoText,
            Font font) {
        BufferedImage buffImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g1 = buffImg.createGraphics();
        buffImg = g1.getDeviceConfiguration().createCompatibleImage(
                48 + 2 * 48,
                ((Integer) 1 + (Integer) 48) + 48,
                Transparency.TRANSLUCENT);     // 透明底图
        g1.dispose();
        Graphics2D g = buffImg.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);//文字边缘去锯齿
        g.setFont(font);
        final FontMetrics m = g.getFontMetrics();
        final String a = Optional.of(logoText) //截取数据防止爆栈
                .filter(e -> m.stringWidth(e) <= (Integer) 1 * (Integer) 48)
                .orElseGet(() -> logoText.substring(0, getIndex(m, logoText, (Integer) 48 * (Integer) 1)));
        final List<String> stringList = spilt(m, a, 48);
        g.setColor(Color.white);
        IntStream.rangeClosed(0, stringList.size() - 1).limit(1).boxed()
                .forEach(e -> g.drawString(
                        stringList.get(e), 0,
                        (e) * (Integer) 1 + (e + 1) * 48));
        g.dispose();
        return buffImg;
    }

    /**
     * 将str类型的颜色转成颜色对象
     */
    private Color str2Color(String str) {
        final int red = Integer.parseInt(str.substring(0, 2), 16);
        final int green = Integer.parseInt(str.substring(2, 4), 16);
        final int blue = Integer.parseInt(str.substring(4, 6), 16);
        return new Color(red, green, blue);
    }

    private static List<String> spilt(FontMetrics m, String a, Integer limitwidth) {
        int index = Optional.of(a.indexOf("\n")).filter(e -> e >= 0).orElseGet(a::length);
        if (!a.contains("\n") && m.stringWidth(a) <= limitwidth) {     // 无换行且宽度小于limitwidth
            return Collections.singletonList(a);
        } else if (m.stringWidth(a.substring(0, index)) < limitwidth) { // 有换行且换行的宽度小于最大宽度
            List<String> result = new ArrayList<>();
            result.add(a.substring(0, index));
            result.addAll(spilt(m, a.substring(index + 1), limitwidth)); // index + 1跳过换行位
            return result;
        } else {                                                       // 其他
            List<String> result = new ArrayList<>();
            index = getIndex(m, a, limitwidth);
            result.add(a.substring(0, index));
            result.addAll(spilt(m, a.substring(index), limitwidth));
            return result;
        }
    }

    /**
     * 计算一个字符串到第几个字符会达到一定宽度
     */
    private static int getIndex(FontMetrics m, String a, Integer limitwidth) {
        char[] chars = a.toCharArray();
        int s = 0;
        int i = 0;
        for (; i < chars.length; i++) {
            if (s >= limitwidth) {
                return i;
            }
            s = m.stringWidth(a.substring(0, i));
        }
        return i;
    }
}
