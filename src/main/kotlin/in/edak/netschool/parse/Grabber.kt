package `in`.edak.netschool.parse

import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteWebDriver
import org.tinylog.kotlin.Logger
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

class Grabber(
        seleniumRemoteUrl: String,
        netSchoolUrl: String
) {
    val webDriver = RemoteWebDriver(URL(seleniumRemoteUrl), DesiredCapabilities.chrome()) // DesiredCapabilities.firefox())
    private val navigate = Navigate(webDriver)

    companion object {
        const val REFRESH_PERIOD = 10*60*1000L
    }

    init {
        webDriver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS)
        webDriver.get(netSchoolUrl)
        scheduleWebDriverRefresher(REFRESH_PERIOD)
    }

    fun close() {
        webDriver.close()
    }

    private fun scheduleWebDriverRefresher(periodMillis: Long) {
        Timer().schedule(periodMillis, periodMillis) {
            try {
                Logger.info("refresh start")
                synchronized(webDriver) {
                    navigate.ping()
                }
                Logger.info("refresh done")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}