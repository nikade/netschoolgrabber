package `in`.edak.netschool.parse

import `in`.edak.selenium.clickSelectOption
import `in`.edak.selenium.findSpanClick
import `in`.edak.selenium.getOrWaitElementXPath
import `in`.edak.selenium.typeToField
import org.openqa.selenium.remote.RemoteWebDriver
import org.tinylog.kotlin.Logger

class Login(private val wd: RemoteWebDriver, private val url: String) {
    private fun needLogin(waitMillis: Long = 1000): Boolean {
        return wd.getOrWaitElementXPath("//span[text()='Выход']", waitMillis) == null
    }

    fun login(loginName: String, password: String, city: String, schoolType: String, school: String) {
        //wd.navigate().refresh()
        wd.get(url)
        Thread.sleep(15000L)
        // <span class="hidden-scr-sm">Выход</span>
        //div[text()='Введите сообщение']/parent::*//div[2]
        if (!needLogin()) return // already login nothing to do
        if (loginContinue()) return // continue, already login
        Logger.info("need login")
        //<a href="https://netschool.edu22.info/about.html" class="btn red">Вход для учащихся младше 14 лет</a>
        wd.getOrWaitElementXPath("//a[text()='Вход для учащихся младше 14 лет']")?.click()
        Thread.sleep(2000)
        Logger.info("fill form")
        //<select id="provinces" name="pid">
        wd.clickSelectOption("//select[@id='provinces']", city)
        Thread.sleep(2000)
        //<select id="funcs" name="sft">
        wd.clickSelectOption("//select[@id='funcs']", schoolType)
        Thread.sleep(2000)
        //<select id="schools" name="scid">
        wd.clickSelectOption("//select[@id='schools']", school)
        Thread.sleep(2000)

        //<input name="UN" placeholder="Пользователь" type="text" class="control-input col-md-6">
        wd.typeToField("UN", loginName)
        Thread.sleep(2000)
        //<input name="PW" placeholder="Пароль" type="password" class="control-input col-md-6">
        wd.typeToField("PW", password)
        Thread.sleep(2000)

        Logger.info("click login button")
        //<span class="button-login-title">Войти </span>
        wd.findSpanClick("Войти ")
        Logger.info("wait 30 sec")
        Thread.sleep(30000)
        Logger.info("check continue")
        loginContinue()
        Thread.sleep(10000)
        Logger.info("check login")
        if (needLogin()) throw Exception("Login failed")
    }

    private fun loginContinue(waitMillis: Long = 1000): Boolean {
        // <div class="alert alert-warning" role="alert"><b>Внимание!</b><br>В настоящий момент
        return if (wd.getOrWaitElementXPath("//div[contains(text(), 'В настоящий момент в ОО')]", 1000) != null) {
            //<button title="Продолжить" type="button" class="btn btn-default" onclick="if(isButtonsLock()) {return;} doContinue();;return false;"><span class="glyphicon glyphicon-new-window"></span> <span>Продолжить</span></button>
            (wd.getOrWaitElementXPath("//button/span[text()='Продолжить']", waitMillis)
                    ?: throw Exception("Could not find continue  button on continue page")).click()
            true
        } else false
    }
}