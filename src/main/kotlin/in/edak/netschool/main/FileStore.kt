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

        val result = items.filter {item ->
            item.getScoreStr().let {
                it != null && !lines.contains(it)
            }
        }
        File(fileName).writeText(result.map(Dairy.DairyItem::getScoreStr).joinToString("\n"), charset)
        return result
    }
}