package org.ld.utils;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Description: 国际化工具类
 */
@SuppressWarnings("unused")
public class I18N {

    /**
     * error_strings.properties
     */
    public static String getErrorMsg(String key, Object... args) {
        return getLocalResource(Locale.getDefault(), "values/error_strings", key, args);
    }

    public static String getLocalResource(Locale locale, String baseName, String key, Object... args) {
        return Optional.of(ResourceBundle.getBundle(baseName, locale))
                .map(rb -> rb.getString(key))
                .map(message ->
                        args != null && args.length > 0
                        ? MessageFormat.format(message, args)
                        : message)
                .orElse(null);
    }
}
