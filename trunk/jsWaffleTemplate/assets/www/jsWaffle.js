/**
 * @projectDescription JavaScript Library for Android
 * 
 * @author	kujirahand.com (http://kujirahand.com)
 * @version	0.1
 * @see http://d.aoikujira.com/jsWaffle/wiki/
 */
(function(){
	if (typeof(jsWaffle) != 'undefined') return;
	// helper
	if (typeof($) == 'undefined') {
		$ = function (id) { return document.getElementById(id); }
	}
	// support for none android device
	if (typeof(_DroidWaffle) == 'undefined') {
		// return dummy action
		_DroidWaffle = _DroidWaffle_getDummyFunctionsn();
	}
	// global temporary object
	DroidWaffle = {
		x:0, y:0, z:0,
		_shake_fn_user : null
	};
	// _DroidWaffle shortcut
	var _w = _DroidWaffle;
	/**
	 * jsWaffle Class
	 * 
	 * @classDescription This class defineds jsWaffle functions
	 * @return {jsWaffle}
	 * @type {Object}
	 * @constructor
	 */
	jsWaffle = function () {};
	/**
	 * get version info
	 * @memberOf jsWaffle
	 * @method
	 * @alias jsWaffle.getWaffleVersion
	 * @return {String} version info
	 */
	jsWaffle.prototype.getWaffleVersion = function() {
		return _w.getWaffleVersion();
	};
	/**
	 * beep method
	 * @memberOf jsWaffle
	 * @method
	 * @alias jsWaffle.beep
	 * @return {void}
	 */
	jsWaffle.prototype.beep = function () { _w.beep(); };
	/**
	 * vibrate method
	 * @param {Integer} msec
	 * @return {void}
	 */
	jsWaffle.prototype.vibrate = function (msec) {
		if (msec == undefined) msec = 500;
		_w.vibrate(msec);
	};
	/**
	 * ring method
	 * @return {void}
	 */
	jsWaffle.prototype.ring = function () { _w.ring(); };
	/**
	 * show message in toast
	 * @param {String} msg
	 */
	jsWaffle.prototype.makeToast = function (msg) {
		_w.makeToast(msg);
	};
	/**
	 * watch ACCELEROMETER
	 * @param {Function} callback_fn
	 */
	jsWaffle.prototype.watchAccel = function (callback_fn) {
		DroidWaffle._accel_fn = callback_fn;
		_w.setAccelCallback("DroidWaffle._watchSensor");
	};
	/**
	 * clear sensor
	 */
	jsWaffle.prototype.clearAccel = function () {
		_w.setAccelCallback("");
	};
	/**
	 * watch Shake device
	 * @param {Function} callback_fn
	 * @param {double} freq
	 */
	jsWaffle.prototype.watchShake = function (callback_fn, freq) {
		if (freq == undefined) { freq = 20; }
		DroidWaffle._shake_fn_user = callback_fn;
		_w.setShakeCallback("DroidWaffle._shake_fn", freq);
	};
	// for sensor
	DroidWaffle._watchSensor = function (accelX, accelY, accelZ) {
		DroidWaffle.x = accelX;
		DroidWaffle.y = accelY;
		DroidWaffle.z = accelZ;
		DroidWaffle._accel_fn(accelX, accelY, accelZ);
	};
	DroidWaffle._shake_fn = function () {
		if (DroidWaffle._shake_fn_user) DroidWaffle._shake_fn_user();
	};
	
	/**
	 * get current position (GPS)
	 * @param {Function} onSuccess
	 * @param {Function} onError
	 * @return {Integer} watchId
	 */
	jsWaffle.prototype.getCurrentPosition = function (onSuccess, onError) {
		// set user event
		DroidWaffle._geolocation_fn_ok_user = onSuccess;
		DroidWaffle._geolocation_fn_ng_user = onError;
		// register callback function
		return _w.geolocation_getCurrentPosition(
			"DroidWaffle._geolocation_fn_ok",
			"DroidWaffle._geolocation_fn_ng"
		);
	};
	/**
	 * watchPosition (GPS)
	 * @alias DroidWaffle.geolocation.watchPosition
	 * @param {Function} onSuccess
	 * @param {Function} onError
	 * @return {Integer} watchId
	 */
	jsWaffle.prototype.watchPosition = function (onSuccess, onError) {
		return _w.geolocation_watchPosition(
			"DroidWaffle._geolocation_fn_ok",
			"DroidWaffle._geolocation_fn_ng"
		);
	};
	/**
	 * clear watchPosion function
	 * @alias DroidWaffle.geolocation.clearWatch
	 * @param {Integer} watchid
	 */
	jsWaffle.prototype.clearWatchPosition = function (watchid) {
		_w.geolocation_clearWatch(watchid);
	};
	// for getPosition/watchPosition/clearPosition
	DroidWaffle._geolocation_fn_ok = function (lat, lon, alt) {
		/*
		var position = {
			"coords" : {
				"latitude"	: lat, 
				"longitude"	: lon,
				"altitude"	: alt
			}
		};
		*/
		if (DroidWaffle._geolocation_fn_ok_user) {
			DroidWaffle._geolocation_fn_ok_user(lat, lon, alt);
		}
	};
	DroidWaffle._geolocation_fn_ng = function (err) {
		if (DroidWaffle._geolocation_fn_ng_user) {
			DroidWaffle._geolocation_fn_ng_user(err);
		}
	};
	/**
	 * save text file
	 * @param {String} filename
	 * @param {String} text
	 */
	jsWaffle.prototype.saveText = function (filename, text) {
		_w.saveText(filename, text);
	};
	/**
	 * load text file
	 * @param {String} filename
	 * @return {String} text
	 */
	jsWaffle.prototype.loadText = function (filename) {
		return _w.loadText(filename);
	};
	/**
	 * get file list
	 * @param {String} path (if undefined then default data area)
	 * @return {Array} file list
	 */
	jsWaffle.prototype.fileList = function (path/*=undefined*/) {
		var s = _w.fileList(path);
		return s.split(";");
	};
	/**
	 * sound play
	 * @param {String} filename
	 * @return {MediaPlayer}
	 */
	jsWaffle.prototype.playSound = function (filename) {
		var i = _w.createPlayer(filename);
		i.start(); // or _w.playPlayer(i);
		return i;
	};
	/**
	 * stop sound
	 * @param {MediaPlayer}
	 */
	jsWaffle.prototype.stopSound = function (playerObj) {
		playerObj.stop(); // or _w.stopPlayer(playerObj);
	};
	/**
	 * open database
	 * @paran {String} dbname
	 * @return {Object}dbObj
	 */
	jsWaffle.prototype.openDatabase = function (dbname) {
		var db = _w.openDatabase(dbname);
		return db;
	};
	/**
	 * executeSql
	 * @param {DBHelper}db
	 * @param {String}sql
	 * @param {Function}fn_ok
	 * @param {Function}fn_ng
	 */
	jsWaffle.prototype.executeSql = function (db, sql, fn_ok, fn_ng) {
		var o = DroidWaffle._db = {};
		o.fn_ok = fn_ok;
		o.fn_ng = fn_ng;
		o.ok = function (result) {
			if(typeof(o.fn_ok) == "function") { o.fn_ok(result); }
		};
		o.ng = function (err) {
			if(typeof(o.fn_ng) == "function") { o.fn_ng(err); }
		};
		_w.executeSql(db, sql, "DroidWaffle._db.ok", "DroidWaffle._db.ng");
	};
	/**
	 * Start Intent (ex) mailto:hoge@example.com?subject=xxx&body=xxx
	 * @param {String}url (http/https/tel/sms/geo/mailto/file/camera/video)
	 * @return {Boolean} result
	 */
	jsWaffle.prototype.startIntent = function (url) {
		return _w.startIntent(url);
	};
	/**
	 * Custom Intent
	 * @param {String}action
	 * @param {String}uri
	 * @return {Intent}
	 */
	jsWaffle.prototype.newIntent = function (action, uri) {
		return _w.newItent(action, uri);
	};
	jsWaffle.prototype.intent_putExtra = function (intent, name, value) {
		_w.intent_putExtra(intent, name, value);
	};
	jsWaffle.prototype.intent_startActivity = function (intent) {
		_w.intent_startActivity(intent);
	};
	
	//-----------------------------------
	// dummy function for PC Browser
	//-----------------------------------
	function _DroidWaffle_getDummyFunctionsn() {
		return {
			getWaffleVersion : function () { return 0; },
			log : function (msg) { console.log(msg); },
			beep : function() {},
			vibrate : function() {},
			ring : function(){},
			makeToast : function(){},
			createPlayer : function(){ return {start:function(){}}; },
			_timerId : 0,
			setAccelCallback : function(fn){
				if (fn == "") {
					clearInterval(_timerId);
				} else {
					_timerId = setInterval(function(){
						var x = Math.random() * 2 -2;
						var y = Math.random() * 2 -2;
						var z = Math.random() * 2 -2;
						eval(fn+"("+x+","+y+","+z+")");
					},1000);
				}
			},
			setShakeCallback : function(fn, freq){
				_timerId = setInterval(function(){
					eval(fn+"()");
				},1000);
			},
			geolocation_watchPosition : function(fnOK, fnNG){
				return setInterval(function(){
					eval(fnOK+"(0,0,0)");
				}, 1000);
			},
			geolocation_clearWatch : function(watchId){
				clearInterval(watchId);
			},
			geolocation_getCurrentPosition : function(fnOK, fnNG){
				setTimeout(function(){
					eval(fnOK+"(0,0,0)");
				}, 1000);
			},
			saveText : function (){},
			loadText : function() {},
			startIntent : function(){},
			openDatabase : function(){ return {} },
			executeSql : function(db, sql, ok, ng) { if(typeof(ok)=="function") { ok(); } },
			___ : 0
		};
	}
	
})();

/**
 * jsWaffle Default Instance
 * @type {jsWaffle}
 */
var droid = new jsWaffle();

