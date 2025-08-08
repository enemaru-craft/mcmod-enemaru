package com.enemaru.talkingclouds.utils;

import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TextProcessor {

    public static List<Text> splitText(Text text, int maxLineLength, boolean enableFormattingCodes) {
        List<Text> splitText = new ArrayList<>();

        text.visit((style, asString) -> {
            var textStyle = enableFormattingCodes ? style : Style.EMPTY;
            splitText.addAll(TextProcessor.splitStringToLines(
                    asString.trim(), maxLineLength, enableFormattingCodes, textStyle));
            return Optional.empty();
        }, Style.EMPTY);

        return splitText;
    }

    public static List<Text> splitStringToLines(String text, int maxLineLength, boolean enableFormattingCodes, Style textStyle) {
        List<Text> splitText = new ArrayList<>();

        if (enableFormattingCodes) {
            text = text.replace("\\u00A7", String.valueOf(Formatting.FORMATTING_CODE_PREFIX))
                    .replace("\\n", "\n")
                    .replaceAll("(?<=^|[^\\\\])&", String.valueOf(Formatting.FORMATTING_CODE_PREFIX));
        }

        var newLine = new StringBuilder();
        var builder = new StringBuilder(text);

        var codePoints = builder.codePoints().toArray();
        int space = ' ';
        int wrap = '\n';
        int colorCode = Formatting.FORMATTING_CODE_PREFIX;
        Formatting currentColor = Formatting.RESET;
        List<Formatting> currentModifiers = new ArrayList<>();
        int lastSpaceIndex = 0;
        int codesOffset = 0;

        for (int i = 0; i < codePoints.length; i++) {
            var point = codePoints[i];
            newLine.appendCodePoint(point);
            if (point == space) {
                lastSpaceIndex = newLine.length() - 1;
            }
            if (point == wrap) {
                splitText.add(createStyledText(newLine.toString().trim(), textStyle));
                newLine.setLength(0);
                lastSpaceIndex = 0;
                continue;
            }
            if (point == colorCode && i + 1 < codePoints.length) {
                codesOffset += 2;
                var formatting = Formatting.byCode((char) codePoints[i + 1]);
                if (formatting != null) {
                    if (!formatting.isModifier()) {
                        currentColor = formatting;
                        currentModifiers.clear();
                    } else {
                        currentModifiers.add(formatting);
                    }
                }
            }
            var maxLength = maxLineLength + codesOffset;
            if (newLine.length() >= maxLength) {
                if (lastSpaceIndex > maxLength * 0.6) {
                    var trailing = newLine.substring(lastSpaceIndex + 1, newLine.length());
                    newLine.setLength(lastSpaceIndex);

                    splitText.add(createStyledText(newLine.toString().trim(), textStyle));
                    newLine.setLength(0);
                    newLine.append(TextProcessor.getStringWithColorCodes(currentColor, currentModifiers));
                    newLine.append(trailing);
                } else {
                    if (i < codePoints.length - 1) {
                        var nextCodePoint = codePoints[i + 1];
                        if (nextCodePoint != space) {
                            newLine.append('-');
                        }
                    }

                    splitText.add(createStyledText(newLine.toString().trim(), textStyle));
                    newLine.setLength(0);
                    newLine.append(TextProcessor.getStringWithColorCodes(currentColor, currentModifiers));
                }
                lastSpaceIndex = 0;
                codesOffset = 0;
            }
        }

        if (!newLine.isEmpty()) {
            splitText.add(createStyledText(newLine.toString().trim(), textStyle));
        }

        return splitText;
    }

    private static String getStringWithColorCodes(Formatting color, List<Formatting> modifiers) {
        var builder = new StringBuilder();
        builder.append(Formatting.FORMATTING_CODE_PREFIX).append(color.getCode());
        for (Formatting modifier : modifiers) {
            builder.append(Formatting.FORMATTING_CODE_PREFIX).append(modifier.getCode());
        }
        return builder.toString();
    }

    public static Text createStyledText(String string, Style style) {
        return Text.literal(string).setStyle(style);
    }
}
