package `in`.edak.netschool.parse

import `in`.edak.selenium.getOrWaitElementXPath
import org.openqa.selenium.Cookie
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteExecuteMethod
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.remote.html5.RemoteLocalStorage
import org.tinylog.kotlin.Logger
import java.io.Closeable
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

class Grabber(
        seleniumRemoteUrl: String,
        netSchoolUrl: String,
        val cookies: MutableSet<Cookie>,
        val storage: MutableMap<String,String>,
        browser: String
): Closeable {
    val webDriver = RemoteWebDriver(
            URL(seleniumRemoteUrl),
            when(browser) {
                "FIREFOX" -> DesiredCapabilities.firefox()
                "CHROME" -> DesiredCapabilities.chrome()
                else -> throw Exception("Unknown browser $browser")
            })
    private val navigate = Navigate(webDriver)
    private val localStorage = RemoteLocalStorage(RemoteExecuteMethod(webDriver))

    companion object {
        const val REFRESH_PERIOD = 10*60*1000L
    }

    init {
        webDriver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS)
        webDriver.get(netSchoolUrl)
        if(cookies.isNotEmpty() || storage.isNotEmpty()) {
            webDriver.manage().deleteAllCookies()
            cookies.forEach { webDriver.manage().addCookie(it) }
            localStorage.clear()
            storage.forEach { localStorage.setItem(it.key,it.value) }

            webDriver.get(netSchoolUrl)
        }

    }

    fun saveCookies() {
        cookies.clear()
        cookies.addAll(webDriver.manage().cookies)

        storage.clear()
        localStorage.keySet().forEach {key ->
            storage[key] = localStorage.getItem(key)
        }
    }

    override fun close() {
        //<span class="hidden-scr-sm">Выход</span>
        webDriver.getOrWaitElementXPath("//span[text()='Выход']")?.click()
        Thread.sleep(1000)
        webDriver.getOrWaitElementXPath("//button[text()='Да']")?.click()
        Thread.sleep(5000)
        webDriver.quit()
    }
}