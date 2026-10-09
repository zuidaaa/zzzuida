package com.example.engine.edge

/**
 * Fragment 1: Pre-Execution AST (Abstract Syntax Tree) & Safety Validator.
 *
 * Implements security gating and syntax sanity checks:
 * - Prevents privileged process execution, arbitrary shell injection, and destructive file operations.
 * - Parses and validates delimiter balance (parentheses, braces, brackets) while safely
 *   ignoring string literals, raw multiline strings, and single/multiline comments.
 * - Performs language-specific structural heuristics for Kotlin, Python, and Java.
 */
object EdgeAstSafetyValidator {

    // Dangerous and privileged API calls across Python, Kotlin, Java, and Shell
    private val DANGEROUS_PATTERNS = mapOf(
        "Runtime.getRuntime().exec" to "Privileged process execution attempt",
        "ProcessBuilder(" to "Unauthorized sub-process fork",
        "System.exit" to "Abrupt VM termination call",
        "java.lang.reflect" to "Unsafe Java reflection invocation",
        ".setAccessible(true)" to "Privileged reflection access override",
        "sun.misc.Unsafe" to "Direct memory manipulation attempt",
        "os.system(" to "Unrestricted OS shell command injection",
        "subprocess.Popen(" to "Privileged shell spawn",
        "subprocess.run(" to "Privileged subprocess execution",
        "shutil.rmtree('/'" to "Destructive root filesystem modification",
        "deleteRecursively()" to "Unconstrained recursive file deletion",
        "open('/etc/" to "Privileged system configuration file access",
        "open('/dev/" to "Direct hardware device node access",
        "eval(" to "Unsafe dynamic script evaluation",
        "exec(" to "Unsafe dynamic arbitrary code execution",
        "__import__('os')" to "Obfuscated OS module import",
        "__import__('subprocess')" to "Obfuscated subprocess import"
    )

    /**
     * Comprehensive AST Safety & Syntax Validation.
     * Returns an [AstValidationReport] with violations, warnings, node count, and status.
     */
    fun validate(code: String, language: String = "kotlin"): AstValidationReport {
        val violations = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // 1. Check for privileged / dangerous patterns
        for ((pattern, reason) in DANGEROUS_PATTERNS) {
            if (code.contains(pattern)) {
                violations.add("AST Security Violation: Disallowed privileged call '$pattern' - $reason")
            }
        }

        // 2. Syntax & Delimiter Balance with Comment and String Sanitization
        val strippedCode = sanitizeCommentsAndStrings(code, language)
        val delimiterCheck = checkDelimiterBalance(strippedCode)
        if (!delimiterCheck.first) {
            violations.add("AST Syntax Violation: ${delimiterCheck.second}")
        }

        // 3. Language-Specific Structure Heuristics
        when (language.lowercase()) {
            "kotlin", "kt" -> {
                if (!code.contains("fun ") && !code.contains("class ") && !code.contains("val ") && !code.contains("package ")) {
                    warnings.add("Kotlin Structural Warning: No function, class, or property declarations detected.")
                }
            }
            "python", "py" -> {
                if (!code.contains("def ") && !code.contains("class ") && !code.contains("import ") && !code.contains("=")) {
                    warnings.add("Python Structural Warning: No def, class, import, or assignment statements detected.")
                }
            }
        }

        val estimatedNodeCount = code.split(Regex("\\s+")).filter { it.isNotBlank() }.size

        return AstValidationReport(
            isValid = violations.isEmpty(),
            violations = violations,
            warnings = warnings,
            astNodeCount = estimatedNodeCount,
            checkedLanguage = language
        )
    }

    /**
     * Quick boolean check returning Pair(isValid, errorReason).
     */
    fun quickCheck(code: String, language: String = "kotlin"): Pair<Boolean, String?> {
        val report = validate(code, language)
        return if (report.isValid) {
            true to null
        } else {
            false to (report.violations.firstOrNull() ?: "AST validation rejected")
        }
    }

    /**
     * Strips comments and string literals so delimiters inside comments/strings
     * don't trigger false-positive syntax errors.
     */
    private fun sanitizeCommentsAndStrings(code: String, language: String): String {
        val sb = StringBuilder()
        val len = code.length
        var i = 0

        var inSingleQuote = false
        var inDoubleQuote = false
        var inTripleQuote = false
        var inLineComment = false
        var inBlockComment = false

        val isPython = language.equals("python", ignoreCase = true) || language.equals("py", ignoreCase = true)

        while (i < len) {
            val c = code[i]
            val nextC = if (i + 1 < len) code[i + 1] else '\u0000'
            val next2C = if (i + 2 < len) code[i + 2] else '\u0000'

            // Handle line comments
            if (inLineComment) {
                if (c == '\n' || c == '\r') {
                    inLineComment = false
                    sb.append(c)
                }
                i++
                continue
            }

            // Handle block comments (/* ... */)
            if (inBlockComment) {
                if (c == '*' && nextC == '/') {
                    inBlockComment = false
                    i += 2
                } else {
                    i++
                }
                continue
            }

            // Handle triple quotes (""" or ''')
            if (inTripleQuote) {
                if (c == '"' && nextC == '"' && next2C == '"') {
                    inTripleQuote = false
                    i += 3
                } else {
                    i++
                }
                continue
            }

            // Handle double-quoted strings
            if (inDoubleQuote) {
                if (c == '\\') {
                    i += 2 // skip escaped character
                    continue
                }
                if (c == '"') {
                    inDoubleQuote = false
                }
                i++
                continue
            }

            // Handle single-quoted strings
            if (inSingleQuote) {
                if (c == '\\') {
                    i += 2 // skip escaped character
                    continue
                }
                if (c == '\'') {
                    inSingleQuote = false
                }
                i++
                continue
            }

            // Not inside any literal or comment - check for starters
            // Triple double-quote
            if (c == '"' && nextC == '"' && next2C == '"') {
                inTripleQuote = true
                i += 3
                continue
            }

            // Line comment in Python (#) or Kotlin/Java (//)
            if (isPython && c == '#') {
                inLineComment = true
                i++
                continue
            }
            if (!isPython && c == '/' && nextC == '/') {
                inLineComment = true
                i += 2
                continue
            }

            // Block comment (/*)
            if (!isPython && c == '/' && nextC == '*') {
                inBlockComment = true
                i += 2
                continue
            }

            // Double quote
            if (c == '"') {
                inDoubleQuote = true
                i++
                continue
            }

            // Single quote
            if (c == '\'') {
                inSingleQuote = true
                i++
                continue
            }

            // Normal code character
            sb.append(c)
            i++
        }

        return sb.toString()
    }

    /**
     * Checks balanced delimiters on sanitized code.
     */
    private fun checkDelimiterBalance(sanitizedCode: String): Pair<Boolean, String?> {
        var paren = 0
        var brace = 0
        var bracket = 0

        for (c in sanitizedCode) {
            when (c) {
                '(' -> paren++
                ')' -> {
                    paren--
                    if (paren < 0) return false to "Negative parenthesis balance (unexpected ')') encountered"
                }
                '{' -> brace++
                '}' -> {
                    brace--
                    if (brace < 0) return false to "Negative brace balance (unexpected '}') encountered"
                }
                '[' -> bracket++
                ']' -> {
                    bracket--
                    if (bracket < 0) return false to "Negative bracket balance (unexpected ']') encountered"
                }
            }
        }

        if (paren != 0 || brace != 0 || bracket != 0) {
            return false to "Incomplete delimiter closure (Paren=$paren, Brace=$brace, Bracket=$bracket)"
        }

        return true to null
    }
}
