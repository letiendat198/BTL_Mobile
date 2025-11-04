package com.ptit.btl_mobile.model.lyrics

data class LrcLine(
    val timeInMillis: Long,
    val text: String
)

object LrcParser {
    fun parse(lrcContent: String): List<LrcLine> {
        val lines = mutableListOf<LrcLine>()
        val regex = Regex("""\[(\d{2}):(\d{2})\.(\d{2})\](.*)""")

        lrcContent.lines().forEach { line ->
            regex.find(line)?.let { match ->
                val (minutes, seconds, centiseconds, text) = match.destructured
                val timeInMillis = (minutes.toLong() * 60 * 1000) +
                        (seconds.toLong() * 1000) +
                        (centiseconds.toLong() * 10)

                if (text.isNotBlank()) {
                    lines.add(LrcLine(timeInMillis, text.trim()))
                }
            }
        }

        return lines.sortedBy { it.timeInMillis }
    }
}