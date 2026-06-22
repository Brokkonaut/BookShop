package me.ibhh.BookShop;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class MessageFormatter {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?s|%%");
    private static final PlainTextComponentSerializer PLAIN_TEXT = PlainTextComponentSerializer.plainText();

    private final Component prefix;
    private final TextColor messageColor;

    public MessageFormatter(Component prefix, TextColor messageColor) {
        this.prefix = prefix == null ? Component.empty() : prefix;
        this.messageColor = messageColor;
    }

    public Component info(String message) {
        return info(Component.text(message == null ? "" : message));
    }

    public Component info(Component message) {
        return prefix.append(applyDefaultColor(message));
    }

    public Component error(String message) {
        return error(Component.text(message == null ? "" : message));
    }

    public Component error(Component message) {
        return prefix.append(Component.text("ERROR: ", NamedTextColor.RED)).append(applyDefaultColor(message));
    }

    public Component formatLegacyPlaceholders(String template, Object... arguments) {
        if (template == null) {
            return Component.empty();
        }

        Component result = Component.empty();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        int lastEnd = 0;
        int implicitIndex = 0;
        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                result = result.append(text(template.substring(lastEnd, matcher.start()), messageColor));
            }
            String match = matcher.group();
            if ("%%".equals(match)) {
                result = result.append(text("%", messageColor));
            } else {
                String explicitIndex = matcher.group(1);
                int argumentIndex = explicitIndex == null ? implicitIndex++ : Integer.parseInt(explicitIndex) - 1;
                result = result.append(toComponent(argumentIndex >= 0 && argumentIndex < arguments.length ? arguments[argumentIndex] : match));
            }
            lastEnd = matcher.end();
        }
        if (lastEnd < template.length()) {
            result = result.append(text(template.substring(lastEnd), messageColor));
        }
        return result;
    }

    public static Component text(String text, TextColor color) {
        Component component = Component.text(text == null ? "" : text);
        return color == null ? component : component.color(color);
    }

    public static String plain(Component component) {
        return component == null ? "" : PLAIN_TEXT.serialize(component);
    }

    public static String stripLegacyFormatting(String text) {
        return text == null ? "" : text.replaceAll("(?i)[§&][0-9A-FK-OR]", "");
    }

    public static NamedTextColor legacyColor(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        return switch (Character.toLowerCase(code.charAt(0))) {
            case '0' -> NamedTextColor.BLACK;
            case '1' -> NamedTextColor.DARK_BLUE;
            case '2' -> NamedTextColor.DARK_GREEN;
            case '3' -> NamedTextColor.DARK_AQUA;
            case '4' -> NamedTextColor.DARK_RED;
            case '5' -> NamedTextColor.DARK_PURPLE;
            case '6' -> NamedTextColor.GOLD;
            case '7' -> NamedTextColor.GRAY;
            case '8' -> NamedTextColor.DARK_GRAY;
            case '9' -> NamedTextColor.BLUE;
            case 'a' -> NamedTextColor.GREEN;
            case 'b' -> NamedTextColor.AQUA;
            case 'c' -> NamedTextColor.RED;
            case 'd' -> NamedTextColor.LIGHT_PURPLE;
            case 'e' -> NamedTextColor.YELLOW;
            case 'f' -> NamedTextColor.WHITE;
            default -> null;
        };
    }

    public static String legacyColorPrefix(String code) {
        return legacyColor(code) == null ? "" : "§" + Character.toLowerCase(code.charAt(0));
    }

    private Component applyDefaultColor(Component message) {
        Component safeMessage = message == null ? Component.empty() : message;
        return messageColor == null ? safeMessage : safeMessage.colorIfAbsent(messageColor);
    }

    private Component toComponent(Object argument) {
        if (argument == null) {
            return Component.empty();
        }
        if (argument instanceof Component component) {
            return messageColor == null ? component : component.colorIfAbsent(messageColor);
        }
        return text(String.valueOf(argument), messageColor);
    }
}
