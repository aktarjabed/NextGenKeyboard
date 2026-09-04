package com.aktarjabed.nxtgenkeyboard.grammar

data class GrammarIssue(
    val start: Int,
    val length: Int,
    val message: String,
    val replacements: List<String>
)