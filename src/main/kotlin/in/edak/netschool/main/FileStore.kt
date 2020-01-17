package `in`.edak.netschool.main

import `in`.edak.netschool.parse.Dairy
import java.io.File
import java.io.FileNotFoundException

class FileStore(val fileName: String) {
    companion object {
        val charset = Charsets.UTF_8
    }

    fun saveScoresAndNetFilter(items: List<Dairy.DairyItem>): List<Dairy.DairyItem> {
        val lines = try {
            File(fileName).readLines(charset)
        } catch (e: FileNotFoundException) {
            listOf<String>()
        }

        val toFile = mutableListOf<String>()
        val result = mutableListOf<Dairy.DairyItem>()
        items.forEach { item ->
            item.scores.forEach {score ->
                val scoreLine = "${item.date.time}#${item.discipline}#${item.lessonNo}#${score.score}#${score.scoreReason}"
                if(!lines.contains(scoreLine)) {
                    toFile.add(scoreLine)
                    result.add(
                            Dairy.DairyItem(
                                    item.date,
                                    item.dateStr,
                                    item.lessonNo,
                                    item.discipline,
                                    item.lessonBeg,
                                    item.lessonEnd,
                                    item.homeWork,
                                    item.isHomeWorkAttachment,
                                    listOf(score)
                            )
                    )
                }

            }
        }
        if(toFile.isNotEmpty()) {
            File(fileName).appendText(toFile.joinToString("\n") + "\n", charset)
        }
        return result
    }
}