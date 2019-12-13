package `in`.edak.netschool.props

data class MqttProps (
    val brokerUrl: String,
    val clientId: String,
    val clientUser: String,
    val clientPassword: String,
    val queue: String
)