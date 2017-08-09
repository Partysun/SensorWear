import React from 'react';
import { 
  StyleSheet,
  Text,
  View,
  Button,
  NativeModules,
  DeviceEventEmitter,
  ToastAndroid
} from 'react-native';

const TOGGLE_ALARM_EVENT = 'toggleAlarm';
const TOGGLE_SIGMOTION_EVENT = 'sigMotion';
const TOGGLE_STEPS_EVENT = 'steps';

const WelcomeScreen = () => {
  return (
    <View>
      <Text style={styles.welcomeMessage}>
        Alarm system is not activated on the wear device yet.
      </Text>
    </View>
  )
}

const SensorAnalyticScreen = ({sigMotion}) => (
  <View>
    <Text style={styles.alarmMessage}>
      Alarm system is activated
    </Text>
    {sigMotion && 
    <Text style={styles.alarmMessage}>
       Detect Significant Motion!
    </Text>}
  </View>  
)

export default class App extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      isAlarm: false,
      sigMotion: false,
      steps: 0
    };
    this.toggleAlarm = this.toggleAlarm.bind(this);
    this.toggleSigMotion = this.toggleSigMotion.bind(this);
    this.toggleWatchAlarm = this.toggleWatchAlarm.bind(this);
    this.stopStepCounter = this.stopStepCounter.bind(this);
    this.steps = this.steps.bind(this);
  };

  componentWillMount() {
    NativeModules.AndroidWearCommunication.resetWatch();
    DeviceEventEmitter.addListener(TOGGLE_SIGMOTION_EVENT, this.toggleSigMotion);
    DeviceEventEmitter.addListener(TOGGLE_ALARM_EVENT, this.toggleAlarm);
    DeviceEventEmitter.addListener(TOGGLE_STEPS_EVENT, this.steps);
  }

  componentWillUnmount() {
    DeviceEventEmitter.removeListener(TOGGLE_SIGMOTION_EVENT, this.toggleSigMotion);
    DeviceEventEmitter.removeListener(TOGGLE_ALARM_EVENT, this.toggleAlarm);
    DeviceEventEmitter.removeListener(TOGGLE_STEPS_EVENT, this.steps);
  }

  toggleWatchAlarm() {
    NativeModules.AndroidWearCommunication.toggleWatchAlarm();
  }

  stopStepCounter() {
    NativeModules.AndroidWearCommunication.stopStepCounter();
  }

  toggleSigMotion() {
    ToastAndroid.show('Detect Signigicant Motion!', ToastAndroid.SHORT);
    const currentSigMotion = this.state.sigMotion;
    this.setState({
      sigMotion: !currentSigMotion
    });
  }

  toggleAlarm({isAlarm}) {
    const alarm = (isAlarm === 'true');
    this.setState({isAlarm: alarm});
  }

  steps({steps}) {
    this.setState({steps: parseInt(steps, 10)});
  }
  
  render() {
    const {isAlarm, sigMotion, steps} = this.state;
    return (
      <View style={styles.container}>
        <View style={styles.analyticContainer}>
          {isAlarm ? <SensorAnalyticScreen sigMotion={sigMotion} /> : <WelcomeScreen />}
          {sigMotion && <Text>{steps}</Text>}
        </View>
        <View style={styles.actionContainer}>
          {sigMotion && <Button
            title="Stop Step Counter"
            onPress={this.stopStepCounter}
            style={styles.button}
          />}
          <Button
            title="Toggle Alarm System"
            onPress={this.toggleWatchAlarm}
            style={styles.button}
          />
        </View>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5FCFF'
  },
  label: {
    fontSize: 60,
  },
  actionContainer: {
    padding: 35
  },
  analyticContainer: {
    flex: 1,
    padding: 35
  },
  button: {
    justifyContent: 'center',
    alignItems: 'center',
    fontSize: 90,
  },
  welcomeMessage: {
    fontSize: 18,
    color: 'white',
    backgroundColor: 'black',
    padding: 10
  },
  alarmMessage: {
    fontSize: 18,
    color: 'white',
    backgroundColor: 'orange',
    padding: 10
  }
});
