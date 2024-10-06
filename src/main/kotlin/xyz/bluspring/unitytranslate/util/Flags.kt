package xyz.bluspring.unitytranslate.util

object Flags {
    /**
     * Prints any translation HTTP exceptions to the log.
     */
    val PRINT_HTTP_ERRORS = System.getProperty("unitytranslate.printHttpErrors", "false") == "true"

    /**
     * Allows LibreTranslate to pipe its console output to the log.
     */
    val ENABLE_LOGGING = System.getProperty("unitytranslate.enableLogging", "false") == "true"
}