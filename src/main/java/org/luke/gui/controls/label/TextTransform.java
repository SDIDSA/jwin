package org.luke.gui.controls.label;

import java.util.function.UnaryOperator;

/**
 * The {@code TextTransform} class provides various text transformation operations.
 * It implements the {@link UnaryOperator} interface for applying transformations to text.
 * <p>
 * The class includes transformations such as uppercase, lowercase, capitalize,
 * hide email, and hide phone, among others.
 * </p>
 * <p>
 * Instances of this class are immutable and can be used to transform text strings.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * String originalText = "example text";
 * String transformedText = TextTransform.UPPERCASE.apply(originalText);
 * </pre>
 * </p>
 * <p>
 * The transformations provided are:
 * <ul>
 *     <li>{@link #NONE}: No transformation, returns the input text as is.</li>
 *     <li>{@link #UPPERCASE}: Converts the input text to uppercase.</li>
 *     <li>{@link #LOWERCASE}: Converts the input text to lowercase.</li>
 *     <li>{@link #CAPITALIZE}: Capitalizes the first letter of each word in the input text.</li>
 *     <li>{@link #CAPITALIZE_PHRASE}: Capitalizes the first letter of the entire input text.</li>
 *     <li>{@link #HIDE_EMAIL}: Hides characters in the email address after the '@' symbol.</li>
 *     <li>{@link #HIDE_PHONE}: Hides characters in the phone number, leaving the last four digits visible.</li>
 * </ul>
 * </p>
 * 
 * @author SDIDSA
 */
public abstract class TextTransform implements UnaryOperator<String> {

    private TextTransform() {
        // Private constructor to prevent external instantiation
    }

    /**
     * No transformation, returns the input text as is.
     */
    public static final TextTransform NONE = new TextTransform() {
        @Override
        public String apply(String param) {
            return param;
        }
    };

    /**
     * Converts the input text to uppercase.
     */
    public static final TextTransform UPPERCASE = new TextTransform() {
        @Override
        public String apply(String param) {
            return param.toUpperCase();
        }
    };

    /**
     * Converts the input text to lowercase.
     */
    public static final TextTransform LOWERCASE = new TextTransform() {
        @Override
        public String apply(String param) {
            return param.toLowerCase();
        }
    };

    /**
     * Capitalizes the first letter of each word in the input text.
     */
    public static final TextTransform CAPITALIZE = new TextTransform() {
        @Override
        public String apply(String param) {
            StringBuilder res = new StringBuilder();

            for (String word : param.split(" ")) {
                if (!res.isEmpty()) {
                    res.append(' ');
                }
                res.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase());
            }

            return res.toString();
        }
    };

    /**
     * Capitalizes the first letter of the entire input text.
     */
    public static final TextTransform CAPITALIZE_PHRASE = new TextTransform() {
        @Override
        public String apply(String param) {
            StringBuilder res = new StringBuilder();

            if (!param.isEmpty()) {
                res.append(Character.toUpperCase(param.charAt(0)));
                res.append(param.substring(1).toLowerCase());
            }

            return res.toString();
        }
    };

    /**
     * Hides characters in the email address after the '@' symbol.
     */
    public static final TextTransform HIDE_EMAIL = new TextTransform() {
        @Override
        public String apply(String param) {
            boolean found = false;
            StringBuilder sb = new StringBuilder();
            for (char c : param.toCharArray()) {
                if (c == '@') {
                    found = true;
                }
                sb.append(found ? c : '*');
            }
            return sb.toString();
        }
    };

    /**
     * Hides characters in the phone number, leaving the last four digits visible.
     */
    public static final TextTransform HIDE_PHONE = new TextTransform() {
        @Override
        public String apply(String param) {
            if (param == null) {
                return "";
            }
            StringBuilder sb = new StringBuilder();

            int count = 0;
            for (int i = param.length() - 1; i >= 0; i--) {
                char c = param.charAt(i);
                sb.insert(0, count >= 4 ? "*" : c);
                count += Character.isDigit(c) ? 1 : 0;
            }

            return sb.toString();
        }
    };
}
