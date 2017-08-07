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

const INCREASE_COUNTER_EVENT = 'increaseCounter';

export default class App extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      counter: 0
    };
    this.increaseLocalCounter = this.increaseLocalCounter.bind(this);
    this.increaseWatchCounter = this.increaseWatchCounter.bind(this);
  };

  componentWillMount() {
    DeviceEventEmitter.addListener(INCREASE_COUNTER_EVENT, this.increaseLocalCounter);
  }

  componentWillUnmount() {
    DeviceEventEmitter.removeListener(INCREASE_COUNTER_EVENT, this.increaseLocalCounter);
  }

  increaseLocalCounter() {
    ToastAndroid.show('Hop hop!', ToastAndroid.SHORT);
    const currentValue = this.state.counter;
    this.setState({
      counter: currentValue + 1
    });
  }

  increaseWatchCounter() {
    NativeModules.AndroidWearCommunication.increaseWatchCounter();
  }
  
  render() {
    return (
      <View style={styles.container}>
        <Text>{this.state.counter}.</Text>
        <View style={styles.buttonContainer}>
          <Button
            title="Increase Watch Counter"
            onPress={this.increaseWatchCounter}
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
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF'
  },
  label: {
    fontSize: 60,
  },
  counter: {
    fontSize: 140,
    color: 'black'
  },
  buttonContainer: {
    padding: 35
  },
  button: {
    fontSize: 90
  }
});
