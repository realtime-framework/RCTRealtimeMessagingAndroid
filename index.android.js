/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 */
'use strict';

var React = require('react-native');
var module = require('RCTRealtimeMessagingAndroid');
var RCTRealtimeMessaging = new module();


var messages = [];

var {
  AppRegistry,
  Image,
  StyleSheet,
  Text,
  Navigator,
  TextInput,
  ScrollView,
  TouchableHighlight,
  ListView,
  View
} = React;


var RCTRealtimeMessagingAndroid = React.createClass({ 

  doConnect: function(){
    this._log('Trying to connect!');

    RCTRealtimeMessaging.RTEventListener("onConnected",this._onConnected),
    RCTRealtimeMessaging.RTEventListener("onDisconnected",this._onDisconnected),
    RCTRealtimeMessaging.RTEventListener("onSubscribed",this._onSubscribed),
    RCTRealtimeMessaging.RTEventListener("onUnSubscribed",this._onUnSubscribed),
    RCTRealtimeMessaging.RTEventListener("onException",this._onException),
    RCTRealtimeMessaging.RTEventListener("onMessage",this._onMessage),
    RCTRealtimeMessaging.RTEventListener("onPresence",this._onPresence);

    RCTRealtimeMessaging.RTConnect(
    {
      appKey:this.state.appKey,
      token:this.state.token,
      connectionMetadata:this.state.connectionMetadata,
      clusterUrl:this.state.clusterUrl,
      projectId:'<YOUR-GOOGLE-PROJECT-NUMBER>'
    });
  },

  componentDidMount: function(){
    RCTRealtimeMessaging.RTPushNotificationListener(this._onNotification);
  },

  componentWillUnmount: function() {
    RCTRealtimeMessaging.RTDisconnect();
  },

  doDisconnect:function(){
    RCTRealtimeMessaging.RTDisconnect();
  },

  doSubscribe: function(){
    RCTRealtimeMessaging.RTSubscribeWithNotifications(this.state.channel, true);
  },

  doUnSubscribe: function(){
    RCTRealtimeMessaging.RTUnsubscribe(this.state.channel);
  },

  doSendMessage: function(){
    RCTRealtimeMessaging.RTSendMessage(this.state.message, this.state.channel);
  },

  doPresence: function(){
    RCTRealtimeMessaging.RTPresence(
       this.state.channel
     );
  },

  _onException: function(exceptionEvent){
    this._log("Exception:" + exceptionEvent.error);
  },

  _onConnected: function()
  {
    this._log("Connected");
  },


  _onDisconnected: function(){
    this._log("Disconnected");
  },

  _onSubscribed: function(subscribedEvent)
  {
    this._log("Subscribed channel " + subscribedEvent.channel);
  },

  _onUnSubscribed: function(unSubscribedEvent)
  {
    this._log("Unsubscribed channel " + unSubscribedEvent.channel);
  },

  _onNotification: function(data)
  { 
    this._log("Received notification: " + JSON.stringify(data));  
  },

  _onMessage: function(messageEvent)
  { 
    this._log("Received message: ["+messageEvent.message+"] on channel [" + messageEvent.channel+"]");  
  },
  
  _onPresence: function(presenceEvent){
    if (presenceEvent.error) {
      this._log("Error getting presence: " + presenceEvent.error);
    }else
    {
      this._log("Presence data: " + JSON.stringify(presenceEvent.result));
    };    
  },


  getInitialState: function() {
    return {
      clusterUrl: "http://ortc-developers.realtime.co/server/2.1/",
      token: "SomeAuthenticatedToken",
      appKey: "<YOUR-REALTIME-APPKKEY>",
      channel: "yellow",
      connectionMetadata: "clientConnMeta",
      message: "This is the message",
      topWidth:0,
      dataSource: new ListView.DataSource({
        rowHasChanged: (row1, row2) => row1 !== row2,
      }),
    };
  },

  _renderRow: function(rowData: string, sectionID: number, rowID: number) {
    return (
      <TouchableHighlight>
        <View>
          <View style={styles.row}>
            <Text style={styles.text}>
              {rowData}
            </Text>
          </View>
          <View style={styles.separator} />
        </View>
      </TouchableHighlight>
    );
  },

  _log: function(message: string)
  {
    var time = this.getFormattedDate();
    time += " - " + message
    var temp = [];
    temp[0] = time;

    for (var i = 0; i < messages.length; i++) {
      temp[i+1] =  messages[i];
    };
    messages = temp;

    this.setState({
      dataSource: this.getDataSource(messages)
    });
  },

  getFormattedDate: function() {
    var date = new Date();
    var str = date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
    return str;
  },

  getDataSource: function(messages: Array<any>): ListView.DataSource {
    return this.state.dataSource.cloneWithRows(messages);
  },

  render: function() {
    return (
      <ScrollView style={styles.container}>

        <Text clusterUrl = {this.state.clusterUrl} >
            clusterUrl:
        </Text>
        <TextInput
          style={styles.textInput}
          placeholder={this.state.clusterUrl}
          onChangeText={(text) => this.setState({server: text})}
        />

        <View style={styles.custom}>
          <View style={styles.margin}>
            <Text server = {this.state.server} >
                Authentication Token:
            </Text>
            <TextInput
              style={styles.halfTextInput}
              placeholder={this.state.token}
              onChangeText={(text) => this.setState({token: text})}
            />
     
            <Text server = {this.state.server} >
                Application Key:
            </Text>
            <TextInput
              style={styles.halfTextInput}
              placeholder={this.state.appKey}
              onChangeText={(text) => this.setState({appKey: text})}
            />
          </View>

          <View style={styles.margin}>
            <Text server = {this.state.server} >
                Channel:
            </Text>
            <TextInput
              style={styles.halfTextInput}
              placeholder={this.state.channel}
              onChangeText={(text) => this.setState({channel: text})}
            />
          
            <Text server = {this.state.server} >
                Connection Metadata:
            </Text>
            <TextInput
              style={styles.halfTextInput}
              placeholder={this.state.connectionMetadata}
              onChangeText={(text) => this.setState({connectionMetadata: text})}
            />
          </View>

        </View>
        <Text server = {this.state.server} >
            Message:
        </Text>
        <TextInput
          style={styles.textInput}
          placeholder={this.state.message}
          onChangeText={(text) => this.setState({message: text})}
        />
        
        <View style={styles.rowView}>

          <TouchableHighlight style={styles.button} onPress={this.doConnect}>
            <View style={styles.tryAgain}>
              <Text style={styles.tryAgainText}>Connect</Text>
            </View>
          </TouchableHighlight>

          <TouchableHighlight style={styles.button} onPress={this.doDisconnect}>
            <View style={styles.tryAgain}>
              <Text style={styles.tryAgainText}>Disconnect</Text>
            </View>
          </TouchableHighlight>

          <TouchableHighlight style={styles.button} onPress={this.doSubscribe}>
            <View style={styles.tryAgain}>
              <Text style={styles.tryAgainText}>Subscribe</Text>
            </View>
          </TouchableHighlight>

          <TouchableHighlight style={styles.button} onPress={this.doUnSubscribe}>
            <View style={styles.tryAgain}>
              <Text style={styles.tryAgainText}>Unsubscribe</Text>
            </View>
          </TouchableHighlight>

          <TouchableHighlight style={styles.button} onPress={this.doSendMessage}>
            <View style={styles.tryAgain}>
              <Text style={styles.tryAgainText}>Send</Text>
            </View>
          </TouchableHighlight>

          <TouchableHighlight style={styles.button} onPress={this.doPresence}>
            <View style={styles.tryAgain}>
              <Text style={styles.tryAgainText}>Presence</Text>
            </View>
          </TouchableHighlight>

        </View>
        <ListView
          style={styles.list}
          dataSource={this.state.dataSource}
          renderRow={this._renderRow}
        />
      </ScrollView>

    )}
  });

var styles = StyleSheet.create({
  container: {
    marginTop: 30,
    margin: 5,
    backgroundColor: '#FFFFFF',
  },
  list: {
    flexDirection: 'column',
    backgroundColor: '#F6F6F6',
    height:150,
  },
  rowView:{
    alignItems: 'stretch',
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent:'center',
  },
  button:{
    margin: 5,
  },
  margin:{
    
  },
  custom:{
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent:'space-between',
  },
  textInput:{
    height: 30,
    borderColor: 'gray',
    borderWidth: 1,
    borderRadius: 4,
    padding: 5,
    fontSize: 15,
  },

  halfTextInput:{
    height: 30,
    borderColor: 'gray',
    borderWidth: 1,
    borderRadius: 4,
    padding: 5,
    fontSize: 15,
    width: 153,
  },
  tryAgain: {
    backgroundColor: '#336699',
    padding: 13,
    borderRadius: 5,
  },
  tryAgainText: {
    color: '#ffffff',
    fontSize: 14,
    fontWeight: '500',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'center',
    padding: 10,
    backgroundColor: '#F6F6F6',
  },
  separator: {
    height: 1,
    backgroundColor: '#CCCCCC',
  },
  thumb: {
    width: 64,
    height: 64,
  },
  text: {
    flex: 1,
    fontSize: 13,
  },
});

AppRegistry.registerComponent('RCTRealtimeMessagingAndroid', () => RCTRealtimeMessagingAndroid);
