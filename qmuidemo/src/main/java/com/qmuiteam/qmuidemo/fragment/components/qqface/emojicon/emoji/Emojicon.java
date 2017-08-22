package com.qmuiteam.qmuidemo.fragment.components.qqface.emojicon.emoji;

import java.io.Serializable;

public class Emojicon implements Serializable {
    private static final long serialVersionUID = 1L;
    private int icon;
    private char value;
    private String emoji;

    private Emojicon() {
    }

    public static Emojicon fromResource(int icon, int value) {
        Emojicon emoji = new Emojicon();
        emoji.icon = icon;
        emoji.value = (char) value;
        return emoji;
    }

    public static Emojicon fromCodePoint(int codePoint) {
        Emojicon emoji = new Emojicon();
        emoji.emoji = newString(codePoint);
        return emoji;
    }

    public static Emojicon fromChar(char ch) {
        Emojicon emoji = new Emojicon();
        emoji.emoji = Character.toString(ch);
        return emoji;
    }

    public static Emojicon fromChars(String chars) {
        Emojicon emoji = new Emojicon();
        emoji.emoji = chars;
        return emoji;
    }

    public Emojicon(String emoji) {
        this.emoji = emoji;
    }

    public char getValue() {
        return value;
    }

    public int getIcon() {
        return icon;
    }

    public String getEmoji() {
        return emoji;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Emojicon && emoji.equals(((Emojicon) o).emoji);
    }

    @Override
    public int hashCode() {
        return emoji.hashCode();
    }

    public static final String newString(int codePoint) {
    	// Character.charCount 指定字符是否是等于或大于0x10000的，那么该方法返回2。否则，该方法返回1。
        if (Character.charCount(codePoint) == 1) {
            return String.valueOf(codePoint);
        } else {
        	//指定字符（Unicode代码点）转换成UTF-16表示存储在一个char数组。
            return new String(Character.toChars(codePoint));
        }
    }
}
