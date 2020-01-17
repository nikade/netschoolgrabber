package `in`.edak.netschool.parse

import `in`.edak.selenium.clickSelectOption
import `in`.edak.selenium.getOrWaitElementXPath
import `in`.edak.selenium.getOrWaitElementXPathOrExpception
import org.openqa.selenium.By
import org.openqa.selenium.remote.RemoteWebDriver
import org.tinylog.kotlin.Logger
import java.time.LocalDate
import java.util.*

class Dairy(private val wd: RemoteWebDriver) {
    data class DairyItem(
            val date: Date,
            val dateStr: String,
            val lessonNo: Int,
            val discipline: String,
            val lessonBeg: String,
            val lessonEnd: String,
            val homeWork: String?,
            val isHomeWorkAttachment: Boolean,
            val scores: List<ScoreItem>
    )

    data class ScoreItem(
            val score: Int,
            val scoreReason: String
    )

    companion object {
        val months = mapOf(
                "января" to 1,
                "февраля" to 2,
                "марта" to 3,
                "апреля" to 4,
                "мая" to 5,
                "июня" to 6,
                "июля" to 7,
                "августа" to 8,
                "сентября" to 9,
                "октября" to 10,
                "ноября" to 11,
                "декабря" to 12
        )
        val dateRegexp = "(\\d{1,2})\\s(\\S+)\\s(\\d{4})".toRegex()
        val lessionTimeRegexp = "(\\d{2}:\\d{2})\\s-\\s(\\d{2}:\\d{2})".toRegex()
        val scoreRegexp = "ng-scope\\s(\\S+)".toRegex()
        val scoreTextMap = mapOf(
                "five" to 5,
                "four" to 4,
                "three" to 3,
                "two" to 2,
                "one" to 1
        )
        const val millisecInDay = 86400L * 1000L
    }

    fun gotoWeek(weekNo: Int) {
        //<select class="week_select ng-untouched ng-valid ng-not-empty ng-dirty ng-valid-parse select2-hidden-accessible"
        // ng-options="week.title for week in data.weekList" ng-model="data.week" ng-change="load()" tabindex="-1"
        // aria-hidden="true"><option label="1 неделя: 02.09.2019 - 08.09.2019" value="object:4">
        // 1 неделя: 02.09.2019 - 08.09.2019</option>

        // ng-model="ctrl.data.week"
        wd.clickSelectOption("//select[@ng-model='ctrl.data.week']") {
            //    clickSelectOption(wd, "//select[@ng-model='data.week']") {
            it.startsWith("$weekNo неделя:")
        }
    }

    fun prevWeek() {
        wd.getOrWaitElementXPath("//div[@class='button_prev']")!!.click()
    }

    fun nextWeek() {
        wd.getOrWaitElementXPath("//div[@class='button_next']")!!.click()
    }

    fun extractDairy(): List<DairyItem> {
        // <div class="day_table">
        val result = mutableListOf<DairyItem>()
        val dayTableXpay = "//div[@class='day_table']"
        wd.findElementsByXPath(dayTableXpay).forEachIndexed { index, _ ->
            //val dayElement = wd.findElementByXPath("($dayTableXpay)[${index+1}]")
            val dayElementXpath = "($dayTableXpay)[${index + 1}]"
            //<span class="ng-binding">Понедельник, 16 декабря 2019 г.</span>

            var dateStr = ""
            (0..9).forEach {
                try {
                    dateStr = wd.findElements(By.xpath("$dayElementXpath//span[@class='ng-binding']"))[1].text
                } catch (e: org.openqa.selenium.StaleElementReferenceException) {
                    return@forEach
                }
            }
            if (dateStr.isBlank()) return@forEachIndexed
            val date = parseDate(dateStr)
            Logger.info { "Date $date" }

            //<tr ng-repeat-end="" ng-repeat="lesson in diaryDay.lessons"
            // ng-class="{'hidden-xs': !lesson.subjectName}" class="ng-scope">
            val lessonXpath = "$dayElementXpath//tr[@class='ng-scope']"
            wd.findElementsByXPath(lessonXpath).forEachIndexed lesson@{ lessonIndex, _ ->
                //<td class="num_subject ng-binding">6</td>
                //val lessionNo = getSubElementXPath(lessionElement,".//td[@class='num_subject ng-binding']"
                // )?.text?.toInt() ?: return@forEach
                val lessonNo = wd.getOrWaitElementXPath(
                        "($lessonXpath)[$lessonIndex+1]//td[@class='num_subject ng-binding']",
                        0
                )?.text?.toInt() ?: return@lesson

                //<a class="subject ng-binding ng-scope" ng-if="lesson.subjectName" title="Руcский язык">Руcский язык</a>
                //val discipline = getSubElementXPath(lessionElement,
                //        ".//a[@ng-if='lesson.subjectName']")?.text ?: return@forEach
                val discipline = wd.getOrWaitElementXPath(
                        "($lessonXpath)[$lessonIndex+1]//a[@ng-if='lesson.subjectName']",
                        0
                )?.text ?: return@lesson

                //<div class="time ng-binding ng-scope" ng-if="lesson.subjectName">08:00 - 08:40
                //						<!-- ngIf: lesson.room -->
                //					</div>
                /*val lessonTimeStr = getSubElementXPathOrException(
                        lessionElement,
                        ".//div[@ng-if='lesson.subjectName']",
                        "Could not find lession time").text*/
                val lessonTimeStr = wd.getOrWaitElementXPathOrExpception(
                        "($lessonXpath)[$lessonIndex+1]//div[@ng-if='lesson.subjectName']",
                        0,
                        errorIfNotFound = "Could not find lession time"
                ).text

                val lessionTimeMatch = lessionTimeRegexp.find(lessonTimeStr)
                        ?: throw Exception("Could not parse lession period")
                val lessionBeg = lessionTimeMatch.groupValues[1]
                val lessionEnd = lessionTimeMatch.groupValues[2]

                //<a href="#" ng-click="showAssignInfo(assign)" ng-repeat="assign in lesson.homeAssignments"
                // class="ng-binding ng-scope">Написать 5-6 предложений о любимом виде спорта, используя причастия и причастные обороты". Составить тест "Верно -неверно" из 7-8 вопросов по теме "Причастие"</a>
                val homeWork = wd.getOrWaitElementXPath(
                        "($lessonXpath)[$lessonIndex+1]//a[@ng-repeat='assign in lesson.homeAssignments']", 0)?.text
                // <i class="mdi mdi-paperclip"></i>
                val isHomeWorkAttachment = wd.getOrWaitElementXPath(
                        "($lessonXpath)[$lessonIndex+1]//i[@class='mdi mdi-paperclip']", 0) != null

                //<a href="#" ng-if="data.maxMark == 5" ng-repeat="assignWithMark in lesson.assignmentsWithMarks"
                // ng-click="showAssignInfo(assignWithMark)" ng-class="{one: assignWithMark.mark.mark == 1,
                // two: assignWithMark.mark.mark == 2, three: assignWithMark.mark.mark == 3,
                // four: assignWithMark.mark.mark == 4, five: assignWithMark.mark.mark == 5,
                // mark: assignWithMark.mark.dutyMark}" title="Ответ на уроке" class="ng-scope five">
                // </a>
                //<a href="#" ng-if="data.maxMark == 5" ng-repeat="assignWithMark in lesson.assignmentsWithMarks" ng-click="showAssignInfo(assignWithMark)" ng-class="{one: assignWithMark.mark.mark == 1, two: assignWithMark.mark.mark == 2, three: assignWithMark.mark.mark == 3, four: assignWithMark.mark.mark == 4, five: assignWithMark.mark.mark == 5, mark: assignWithMark.mark.dutyMark}" title="ÐžÑ‚Ð²ÐµÑ‚ Ð½Ð° ÑƒÑ€Ð¾ÐºÐµ" class="ng-scope five">
                val scores = wd.findElementsByXPath("($lessonXpath)[$lessonIndex+1]//a[@ng-repeat='assignWithMark in lesson.assignmentsWithMarks']")
                        .mapNotNull { scoreElement ->
                    val scoreReason = scoreElement.getAttribute("title")
                    val score = scoreElement.getAttribute("class")?.let { classText ->
                        scoreRegexp.find(classText)?.groupValues?.let { values ->
                            scoreTextMap.getOrDefault(values[1], 0)
                        }
                    }
                    if(score != null ) ScoreItem(score, scoreReason ?: "за \"что-то\"") else null
                }
                result.add(
                        DairyItem(
                                date = date,
                                dateStr = dateStr,
                                lessonNo = lessonNo,
                                discipline = discipline,
                                lessonBeg = lessionBeg,
                                lessonEnd = lessionEnd,
                                homeWork = homeWork,
                                isHomeWorkAttachment = isHomeWorkAttachment,
                                scores = scores
                        )
                )
            }
        }
        return result
    }

    private fun parseDate(dateStr: String): Date {
        dateRegexp.find(dateStr)?.let { match ->
            val month = months[match.groupValues[2]] ?: throw Exception("Could not parse month")
            return Date(LocalDate.of(match.groupValues[3].toInt(),
                    month,
                    match.groupValues[1].toInt()).toEpochDay() * millisecInDay)
        } ?: throw Exception("Could not parse dateStr=${dateStr}")

    }
}