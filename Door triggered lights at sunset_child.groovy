/**
 *  Door triggered lights at sunset
 *
 *
 */
definition(
    name: "DoorTriggeredLightsSunsetChild",
    namespace: "scottdog123",
    author: "Curt Scott",
    parent: "scottdog123:DoorTriggeredLightsSunsetParent",
    description: "Turns on lights when door opens after sunset for x minutes",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/rise-and-shine.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/rise-and-shine@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/rise-and-shine@2x.png")

preferences {
  section("Lights") {
    input "switches", "capability.switch", title: "Which light to turn on?", multiple: true
    input "disable_switch", "capability.switch", title: "Disable Switch", required: false
    input "doorSensor", "capability.contactSensor", title: "Which Door to trigger on?", multiple: true
    //input "motionSensor", "capability.motionSensor", title: "Which Motion Sensor?", required: false
    input "motionSensor", "capability.switch", title: "Which Motion Sensor?", required: false
    input "Delay", "number", title: "Delay(min) before turning OFF light"
  }
}

def installed() {
  initialize()
}

def updated() {
  unsubscribe()
  initialize()
}

def initialize() {
  subscribe(doorSensor, "contact.open", motionOrDoorEvent)
  //subscribe(motionSensor, "motion.active", motionOrDoorEvent)
  subscribe(motionSensor, "switch.on", motionOrDoorEvent)
  subscribe(location, "sunriseTime", sunRiseHandler)
  subscribe(location, "sunsetTime", sunSetHandler)
  def SunriseAndSunset = getSunriseAndSunset()
  if (now() >= SunriseAndSunset.sunrise.time && now() < SunriseAndSunset.sunset.time) {
    state.nightTime = 0
    log.debug "start daymode"
  }
  else {
    state.nightTime = 1
    log.debug "start nightmode"
  }
}

def lightOffHandler() {
  // log.debug "turning off lights"
  switches.off()
}

def motionOrDoorEvent(evt) {
  if (disable_switch && disable_switch.currentValue("switch") == "on") {
    log.debug "disable_switch on"
    return
  }
  if (state.nightTime == 1) {
    switches.on()
    // log.debug "door open"
    // sendPush("Door open: ${evt.name}")
    runIn(Delay*60, lightOffHandler, [overwrite: true])
  }
}

def sunRiseHandler(evt) {
  state.nightTime = 0
  // log.debug "Day Time"
}

def sunSetHandler(evt) {
  state.nightTime = 1
  // log.debug "Night Time"
}
