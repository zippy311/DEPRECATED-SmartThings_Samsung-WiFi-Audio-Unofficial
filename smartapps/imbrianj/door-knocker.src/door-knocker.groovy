/**
 *  Door Knocker
 *
 *  Author: brian@bevey.org
 *  Date: 9/10/13
 *
 *  Let me know when someone knocks on the door, but ignore
 *  when someone is opening the door.
 */

definition(
    name: "Door Knocker",
    namespace: "imbrianj",
    author: "brian@bevey.org",
    description: "Alert if door is knocked, but not opened.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
  section("When Someone Knocks?") {
    input name: "knockSensor", type: "capability.accelerationSensor", title: "Where?"
  }

  section("But not when they open this door?") {
    input name: "openSensor", type: "capability.contactSensor", title: "Where?"
  }

  section("Knock Delay (defaults to 5s)?") {
    input name: "knockDelay", type: "number", title: "How Long?", required: false
  }

  section("Notifications") {
    input "sendPushMessage", "enum", title: "Send a push notification?", metadata: [values: ["Yes", "No"]], required: false
    input "phone", "phone", title: "Send a Text Message?", required: false
  }
}

def installed() {
  init()
}

def updated() {
  unsubscribe()
  init()
}

def init() {
  state.lastClosed = 0
  subscribe(knockSensor, "acceleration.active", handleEvent)
  subscribe(openSensor, "contact.closed", doorClosed)
}

def doorClosed(evt) {
  state.lastClosed = now()
}

def doorKnock() {
  if((openSensor.latestValue("contact") == "closed") &&
     (now() - (60 * 1000) > state.lastClosed)) {
    log.debug("${knockSensor.label ?: knockSensor.name} detected a knock.")
    send("${knockSensor.label ?: knockSensor.name} detected a knock.")
  }

  else {
    log.debug("${knockSensor.label ?: knockSensor.name} knocked, but looks like it was just someone opening the door.")
  }
}

def handleEvent(evt) {
  def delay = knockDelay ?: 5
  runIn(delay, "doorKnock")
}

private send(msg) {
  if(sendPushMessage != "No") {
    log.debug("Sending push message")
    sendPush(msg)
  }

  if(phone) {
    log.debug("Sending text message")
    sendSms(phone, msg)
  }

  log.debug(msg)
}