package ru.intertrust.cm.core.gui.impl.client.util;

import com.google.gwt.regexp.shared.RegExp;

/**
 * Класс утилит для работы с цветом.<br>
 * <br>
 * Created by Myskin Sergey on 13.01.2021.
 */
public abstract class ColorUtils {

    private static final String HEX_COLOR_CODE_PATTERN = "^[a-fA-F0-9]{6}$";
    private static RegExp hexColorCodeRegExp = RegExp.compile(HEX_COLOR_CODE_PATTERN);

    private ColorUtils() {
    }

    /**
     * Инвертирует цвет на противоположный в формате 6-символьного HEX-кода
     *
     * @param hex цвет для инвертирования в формате 6-символьного HEX-кода
     * @return инвертированный цвет в формате 6-символьного HEX-кода
     */
    public static String invertColor(String hex) {
        return invertColor(hex, false);
    }

    /**
     * Инвертирует цвет на противоположный в формате 6-символьного HEX-кода
     *
     * @param hex цвет для инвертирования в формате 6-символьного HEX-кода
     * @param bw  признак того, инвертировать ли цвет на четко противоположный или в черно-белой градации (в последнем случае результатом будет черный, либо белый)
     * @return инвертированный цвет в формате 6-символьного HEX-кода
     */
    public static String invertColor(String hex, boolean bw) {
        final int[] rgb = hexToRgb(hex);
        if (bw) {
            return invertColorFromRgbToHexBW(rgb);
        } else {
            return invertColorFromRgbToHex(rgb);
        }
    }

    /**
     * Конвертирует цвет в HEX-формате (3 или 6 символьный код цвета) в формат RGB
     *
     * @param hex 3х или 6ти-символьный HEX-код цвета для конвертации
     * @return цвет в RGB-формате: массив, где [0] = R, [1] = G, [2] = B
     */
    private static int[] hexToRgb(String hex) {
        if (hex.indexOf('#') == 0) {
            hex = hex.substring(1);
        }
        // convert 3-digit hex to 6-digits.
        if (hex.length() == 3) {
            hex = "" + hex.charAt(0) +
                    hex.charAt(0) +
                    hex.charAt(1) +
                    hex.charAt(1) +
                    hex.charAt(2) +
                    hex.charAt(2);
        }
        if (hex.length() != 6) {
            throw new RuntimeException("Invalid HEX color.");
        }
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);

        int[] rgb = new int[3];

        rgb[0] = r;
        rgb[1] = g;
        rgb[2] = b;

        return rgb;
    }

    /**
     * Инвертирует цвет на противоположный (в градациях черный-белый) в формате 6-символьного HEX-кода
     *
     * @param rgb цвет в формате GRB: массив, где [0] = R, [1] = G, [2] = B
     * @return инвертированный цвет (черный или белый) в формате 6-символьного HEX-кода
     */
    private static String invertColorFromRgbToHexBW(int[] rgb) {
        int r = rgb[0];
        int g = rgb[1];
        int b = rgb[2];

        if ((r * 0.299 + g * 0.587 + b * 0.114) > 186) {
            return "000000";
        } else {
            return "FFFFFF";
        }
    }

    /**
     * Инвертирует цвет на противоположный в формате 6-символьного HEX-кода
     *
     * @param rgb цвет в формате GRB: массив, где [0] = R, [1] = G, [2] = B
     * @return инвертированный цвет в формате 6-символьного HEX-кода
     */
    private static String invertColorFromRgbToHex(int[] rgb) {
        int r = rgb[0];
        int g = rgb[1];
        int b = rgb[2];

        // invert color components
        int ri = 255 - r;
        int gi = 255 - g;
        int bi = 255 - b;

        String riHexStr = Integer.toString(ri, 16);
        String giHexStr = Integer.toString(gi, 16);
        String biHexStr = Integer.toString(bi, 16);

        riHexStr = riHexStr.length() == 1 ? '0' + riHexStr : riHexStr;
        giHexStr = giHexStr.length() == 1 ? '0' + giHexStr : giHexStr;
        biHexStr = biHexStr.length() == 1 ? '0' + biHexStr : biHexStr;

        String iHexStr = riHexStr + giHexStr + biHexStr;
        return iHexStr;
    }

    /**
     * Проверяет HEX-код цвета на правильность формата:<br>
     * <ol>
     *     <li>длина строки - 6 символов</li>
     *     <li>может содержать только цифры и буквы в диапазоне A-F без учета регистра</li>
     * </ol>
     *
     * @param hexColorCode HEX-код цвета для проверки
     * @return true - валидный формат; false - нет
     */
    public static boolean validateHexColorCode6symbolFormat(String hexColorCode) {
        final boolean result = hexColorCodeRegExp.test(hexColorCode);
        return result;
    }

}
