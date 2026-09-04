package com.aktarjabed.nxtgenkeyboard.grammar

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class OnlineGrammarService : GrammarService {

    override suspend fun check(text: String, lang: String): List<GrammarIssue> {
        return withContext(Dispatchers.IO) {
            val language = when (lang) {
                "hi" -> "hi-IN"
                "bn" -> "bn"
                else -> "en-US"
            }

            val url = URL("https://api.languagetool.org/v2/check")
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.connectTimeout = 5000
                conn.readTimeout = 8000
                conn.setRequestProperty(
                    "Content-Type",
                    "application/x-www-form-urlencoded"
                )

                val body = "language=$language&text=" + URLEncoder.encode(text, "UTF-8")
                conn.outputStream.use { it.write(body.toByteArray()) }

                if (conn.responseCode != 200) {
                    throw RuntimeException("Grammar API error: ${conn.responseCode}")
                }

                val json = conn.inputStream.bufferedReader().use { it.readText() }
                val matches = JSONObject(json).optJSONArray("matches") ?: JSONArray()
                val issues = mutableListOf<GrammarIssue>()

                for (i in 0 until matches.length()) {
                    val m = matches.getJSONObject(i)
                    val offset = m.getInt("offset")
                    val length = m.getInt("length")
                    val message = m.getString("message")

                    val replacements = mutableListOf<String>()
                    val reps = m.optJSONArray("replacements")
                    if (reps != null) {
                        for (j in 0 until reps.length()) {
                            replacements.add(reps.getJSONObject(j).optString("value"))
                        }
                    }

                    issues.add(
                        GrammarIssue(
                            start = offset,
                            length = length,
                            message = message,
                            replacements = replacements
                        )
                    )
                }

                issues
            } finally {
                conn.disconnect()
            }
        }
    }
}