package com.aktarjabed.nxtgenkeyboard.grammar

class OfflineGrammarService : GrammarService {

    override suspend fun check(text: String, lang: String): List<GrammarIssue> {
        if (text.isBlank()) return emptyList()

        val issues = mutableListOf<GrammarIssue>()

        Regex("""(\p{L}+)\s+\1""", RegexOption.IGNORE_CASE)
            .findAll(text)
            .forEach { match ->
                issues += GrammarIssue(
                    start = match.range.first,
                    length = match.value.length,
                    message = "Repeated word: ${match.groupValues[1]}",
                    replacements = listOf(match.groupValues[1])
                )
            }

        Regex("""\s+([.,!?;:])""")
            .findAll(text)
            .forEach { match ->
                issues += GrammarIssue(
                    start = match.range.first,
                    length = match.value.length,
                    message = "Remove space before punctuation",
                    replacements = listOf(match.groupValues[1])
                )
            }

        Regex("""(?<=[.!?]\s)(\p{L})""")
            .findAll(text)
            .forEach { match ->
                val ch = match.groupValues[1]
                if (ch != ch.uppercase()) {
                    issues += GrammarIssue(
                        start = match.range.first,
                        length = 1,
                        message = "Capitalize first letter",
                        replacements = listOf(ch.uppercase())
                    )
                }
            }

        Regex("""([.,!?])(?=\p{L})""")
            .findAll(text)
            .forEach { match ->
                issues += GrammarIssue(
                    start = match.range.first,
                    length = 1,
                    message = "Add space after punctuation",
                    replacements = listOf(match.groupValues[1] + " ")
                )
            }

        return issues
            .distinctBy { Triple(it.start, it.length, it.message) }
            .sortedBy { it.start }
    }
}