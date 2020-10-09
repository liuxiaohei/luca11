package org.ld.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @author ld
 */
@RestController
@RequestMapping("/")
public class AvatarController {

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

    @Value(value = "classpath:PingFang.ttc")
    private Resource resource;

    private Font font;

    Font getFont() {
        try {
            if (font == null) {
                font = Font.createFont(Font.TRUETYPE_FONT, resource.getFile()).deriveFont(1, 48);
            }
            return font;
        } catch (FontFormatException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    @GetMapping(value = "avatar", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] makeAvatar(@RequestParam String str) throws IOException {
        final String color = colors.get(RANDOM.nextInt(colors.size() - 1));
        BufferedImage bi = new BufferedImage(120, 120, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g1 = bi.createGraphics();
        g1.setBackground(str2Color(color));
        g1.clearRect(0, 0, 120, 120);
        g1.dispose();
        Graphics2D g2 = bi.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);//文字边缘去锯齿
        g2.setFont(getFont());
        g2.setColor(Color.white);
        g2.drawString(str, 36, 80);
        g2.dispose();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(bi, "png", out);
        return out.toByteArray();
    }

    /**
     * 将str类型的颜色转成颜色对象
     */
    private static Color str2Color(String str) {
        final int red = Integer.parseInt(str.substring(0, 2), 16);
        final int green = Integer.parseInt(str.substring(2, 4), 16);
        final int blue = Integer.parseInt(str.substring(4, 6), 16);
        return new Color(red, green, blue);
    }

}
