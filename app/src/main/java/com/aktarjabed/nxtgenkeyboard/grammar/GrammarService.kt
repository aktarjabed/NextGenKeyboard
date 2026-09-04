package com.aktarjabed.nxtgenkeyboard.grammar

interface GrammarService {
    suspend fun check(text: String, lang: String = "en"): List<GrammarIssue>
}