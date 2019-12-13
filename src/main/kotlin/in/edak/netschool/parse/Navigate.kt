package `in`.edak.netschool.parse

import `in`.edak.selenium.getOrWaitElementXPathOrExpception
import org.openqa.selenium.remote.RemoteWebDriver

class Navigate(private val wd: RemoteWebDriver) {
    fun gotoDairy() {
        //<a href="JavaScript:SetSelectedTab(30, '/angular/school/studentdiary/')" onclick="SetSelectedTab(30, '/angular/school/studentdiary/');return false;">Дневник</a>
        wd.getOrWaitElementXPathOrExpception(
                xpath = "//a[@href and @onclick and text()='Дневник']",
                errorIfNotFound = "Could not find menu diary")
                .click()
    }

    fun ping() {
        wd.getOrWaitElementXPathOrExpception(
                xpath = "//div",
                errorIfNotFound = "WebDriver refresher - could not get div")
    }
}