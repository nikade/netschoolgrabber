package `in`.edak.selenium

import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebElement
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.support.ui.Select

fun RemoteWebDriver.findSpanClick(text: String) {
    this.getOrWaitElementXPathOrExpception("//span[text()='${text}']",
            errorIfNotFound = "Could not find span with ${text}")
            .click()
}

fun RemoteWebDriver.typeToField(fieldName: String, text: String) {
    this.getOrWaitElementXPathOrExpception("//input[@name='${fieldName}']",
            errorIfNotFound = "Could not find ${fieldName} field")
            .sendKeys(text)
}

fun RemoteWebDriver.clickSelectOption(xpath: String, filter: (String) -> Boolean) {
    this.getOrWaitElementXPathOrExpception(xpath,
            errorIfNotFound = "Could not find $xpath select").let { element ->
        (Select(element).options.find { filter(it.text) }
                ?: throw Exception("Could not find option")).click()
    }
}

fun RemoteWebDriver.clickSelectOption(xpath: String, optionText: String) {
    this.clickSelectOption(xpath) { a -> a == optionText }
}

fun RemoteWebDriver.getOrWaitElementXPathOrExpception(
        xpath: String,
        timeOutMilis: Long = 10000,
        pause: Long = 500,
        errorIfNotFound: String): WebElement {
    return this.getOrWaitElementXPath(xpath, timeOutMilis, pause) ?: throw Exception(errorIfNotFound)
}

fun RemoteWebDriver.getOrWaitElementXPath(
        xpath: String,
        timeOutMilis: Long = 10000,
        pause: Long = 500
): WebElement? {
    (0..timeOutMilis / pause).forEach {
        val result = try {
            this.findElementByXPath(xpath)
        } catch (e: NoSuchElementException) {
            null
        }
        if (result != null) return result
        Thread.sleep(pause)
    }
    return null
}

fun WebElement.getSubElementXPathOrException(
        xpath: String,
        errorIfNotFound: String): WebElement {
    try {
        return this.findElement(By.xpath(xpath))
    } catch (e: NoSuchElementException) {
        throw java.lang.Exception(errorIfNotFound)
    }
}

fun WebElement.getSubElementXPath(
        xpath: String): WebElement? {
    return try {
        this.findElement(By.xpath(xpath))
    } catch (e: NoSuchElementException) {
        null
    }
}

fun WebElement.getSubElementsXPath(
        xpath: String): List<WebElement> {
    return this.findElements(By.xpath(xpath))
}
