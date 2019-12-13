package `in`.edak.netschool.main

import `in`.edak.messages.MqttSender
import `in`.edak.messages.Telega
import `in`.edak.messages.TelegaTopic
import `in`.edak.netschool.parse.Dairy
import `in`.edak.netschool.parse.Login
import `in`.edak.netschool.parse.Grabber
import `in`.edak.netschool.props.MqttProps
import `in`.edak.props.AllProps
import `in`.edak.props.MainProps
import `in`.edak.props.TelegaProps

import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.schedule
import org.tinylog.kotlin.Logger

object Main {
    @Throws(InterruptedException::class, IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        Logger.info("start")
        val dateFormat = SimpleDateFormat("dd.MM.yyyy")
        // load properties
        val allProps = AllProps("netschoolgrabber.properties")
        val telegaProps = allProps.getProps(TelegaProps::class, "telega.") as TelegaProps
        val mainProps = allProps.getProps(MainProps::class,"main.") as MainProps
        val mqttProps = allProps.getProps(MqttProps::class,"mqtt.") as MqttProps

        val telega = Telega(
                telegaProps.proxyHost,
                telegaProps.proxyPort?.toInt(),
                telegaProps.proxyUsername,
                telegaProps.proxyPassword)
        val telegaErrorTopic = TelegaTopic(telegaProps.token,telegaProps.chatId.toLong(),telega)

        val mqttSender = MqttSender(
                mqttProps.brokerUrl,
                mqttProps.clientId,
                mqttProps.clientUser,
                mqttProps.clientPassword)
        val mqttTopic = mqttSender.getTopic(mqttProps.queue)
        Logger.info("properties loaded")

        val fileStore = FileStore(mainProps.scoreFilename)

        Logger.info("start selenium init")
        val netSchoolGrabber = Grabber(mainProps.seleniumUrl, mainProps.netSchoolUrl)
        val login = Login(netSchoolGrabber.webDriver)
        val dairy = Dairy(netSchoolGrabber.webDriver)
        //val navigate = Navigate(netSchoolGrabber.webDriver)
        Logger.info("start selenium init done")

        Logger.info("start scheduler")
        Timer().schedule(
                1000,
                10*60*1000 // period 10 minutes
        ) {
            try {
                Logger.info { "start grab ${Date()}" }
                Logger.info("logging")
                login.login(
                        mainProps.username,
                        mainProps.password,
                        mainProps.city,
                        mainProps.schoolType,
                        mainProps.school)
                val currWeek = Utils.getCurrentWeekFrom1Sep()

                val lastThreeWeekLessons = (maxOf(1,currWeek-mainProps.weeksToCollect.toInt())..currWeek).map { week ->
                    Logger.info { "goto week $week" }
                    dairy.gotoWeek(week)
                    Thread.sleep(10000L)
                    Logger.info("extract dairy")
                    dairy.extractDairy()
                }.flatten()
                Logger.info("write to scorefile")
                val scoreToSend = fileStore.saveScoresAndNetFilter(lastThreeWeekLessons)
                scoreToSend.forEach {
                    val message = "${dateFormat.format(it.date)} ${it.discipline}\n*${it.score}* ${it.scoreReason}"
                    Logger.info("send message $message")
                    mqttTopic.send(message)
                }
                Logger.info("done ${Date()}")
            } catch (e: Exception) {
                e.printStackTrace()
                Logger.error(e)
                telegaErrorTopic.send("NetschoolGrabber "+(e.message ?: "Неизвестная ошибка"))
            }
        }
        Logger.info("join thread - sleep")
        Thread.currentThread().join()
    }
}