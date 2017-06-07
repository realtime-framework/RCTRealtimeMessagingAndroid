//  Created by Joao Caixinha on 02/04/15.
//  Copyright (c) 2015 Realtime. All rights reserved.
//

/**
 * @providesModule RCTRealtimeMessagingAndroid
 * @flow
 */

'use strict';

import React, { Component } from 'react';
import { NativeModules } from 'react-native';
var RCTDeviceEventEmitter = require('RCTDeviceEventEmitter');
var ortcClient = NativeModules.RealtimeMessagingAndroid;
var RTEvents = {};
var instances = 0;

class RCTRealtimeMessagingAndroid extends React.Component {
	id: String;
	
	RCTRealtimeMessagingAndroid(){
		this.id = instances++;
	}
	constructor(props) {
    super(props);
		this.id = instances++;
	}

	RTConnect(config){
		ortcClient.connect(config, this.id);
	}
	
	RTDisconnect(){
		ortcClient.disconnect(this.id);
	}

	RTSubscribe(channel, subscribeOnReconnect: boolean){
		ortcClient.subscribe(channel, subscribeOnReconnect, this.id);
	}

	RTSubscribeWithFilter(channel, subscribeOnReconnect: boolean, filter){
		ortcClient.subscribeWithFilter(channel, subscribeOnReconnect, filter, this.id);
	}

	RTSubscribeWithBuffer(channel, subscriberId){
		ortcClient.subscribeWithBuffer(channel, subscriberId, this.id);
	}

	RTSubscribeWithOptions(options){
		ortcClient.subscribeWithOptions(options, this.id);
	}

	RTSubscribeWithNotifications(channel, subscribeOnReconnect: boolean){
		ortcClient.subscribeWithNotifications(channel, subscribeOnReconnect, this.id);
	}

	RTUnsubscribe(channel){
		ortcClient.unsubscribe(channel, this.id);
	}

	RTPublishMessage(channel, message, ttl, callBack){
		ortcClient.publish(channel, message, ttl, this.id, callBack);
	}

	RTSendMessage(message, channel){
		ortcClient.sendMessage(message, channel, this.id);
	}

	RTEnablePresence(aPrivateKey, channel, aMetadata:boolean){
		ortcClient.enablePresence(aPrivateKey, channel, aMetadata, this.id);
	}

	RTDisablePresence(aPrivateKey, channel){
		ortcClient.disablePresence(aPrivateKey, channel, this.id);
	}

	RTPresence(channel){
		ortcClient.presence(channel, this.id);
	}

	RTIsSubscribed(channel, callBack: Function){
		ortcClient.isSubscribed(channel, this.id, callBack);
	}

	// RTSaveAuthentication(url, isCluster, authenticationToken, authenticationTokenIsPrivate, applicationKey, timeToLive, privateKey, permissions, callBack: Function){
	// 	ortcClient.saveAuthentication(url, isCluster, authenticationToken, authenticationTokenIsPrivate, applicationKey, timeToLive, privateKey, permissions, this.id, callBack);
	// }

	RTGetHeartbeatTime(callBack: Function){
		ortcClient.getHeartbeatTime(this.id, callBack);
	}

	RTSetHeartbeatTime(newHeartbeatTime){
		ortcClient.setHeartbeatTime(newHeartbeatTime, this.id);
	}

	RTGetHeartbeatFails(callBack: Function){
		ortcClient.getHeartbeatFails(this.id, callBack);
	}

	RTSetHeartbeatFails(newHeartbeatFails){
		ortcClient.setHeartbeatFails(newHeartbeatFails, this.id);
	}

	RTIsHeartbeatActive(callBack: Function){
		ortcClient.isHeartbeatActive(this.id, callBack);
	}

	RTEnableHeartbeat(){
		ortcClient.enableHeartbeat(this.id);
	}

	RTDisableHeartbeat(){
		ortcClient.disableHeartbeat(this.id);
	}

	/*
		Events list
		- onConnected
		- onDisconnect
		- onReconnect
		- onReconnecting
		- onSubscribed
		- onUnSubscribed
		- onExcption
		- onMessage
		- onPresence
		- onDisablePresence
		- onEnablePresence
	*/

	RTPushNotificationListener(callBack: Function){
		require('RCTDeviceEventEmitter').addListener(
			  'onPushNotification',
			  callBack
			);
		ortcClient.checkForNotifications();
	};


	RTEventListener(notification, callBack: Function){
		var modNotification = String(this.id) + '-' + notification;
		var channelExists = RTEvents[modNotification];
		if (channelExists){
			this.RTRemoveEventListener(notification);
		}

		RTEvents[modNotification] = (
			require('RCTDeviceEventEmitter').addListener(
			  modNotification,
			  callBack
			)
		);
	};

	RTRemoveEventListener(notification)
	{
		var modNotification = String(this.id) + '-' + notification;
		RTEvents[modNotification].remove(),
		delete RTEvents[modNotification];
	};
}

module.exports = RCTRealtimeMessagingAndroid;
