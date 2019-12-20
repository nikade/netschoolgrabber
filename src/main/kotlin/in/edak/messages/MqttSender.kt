package `in`.edak.messages

import org.eclipse.paho.client.mqttv3.IMqttClient
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage


class MqttSender(
    brokerUrl: String,
    clientId: String,
    private val usermameProp: String?,
    private val passwordProp: String?
) {
    var publisher = MqttClient(brokerUrl, clientId).apply {
        connect(
            MqttConnectOptions().also {
                it.isAutomaticReconnect = true
                it.isCleanSession = true
                it.connectionTimeout = 10
                if(usermameProp != null) it.userName = usermameProp
                if(passwordProp != null) it.password = passwordProp.toCharArray()
            }
        )
    }

    fun getTopic(queue: String): Topic {
        return MqttTopic(this.publisher,queue)
    }

    class MqttTopic(val publisher: IMqttClient, val queue: String): Topic {
        override fun send(msg: String) {
            publisher.publish(
                queue,
                MqttMessage(msg.toByteArray(Charsets.UTF_8)).apply {
                   qos = 2 // exacly one
                }
            )
        }
    }
}