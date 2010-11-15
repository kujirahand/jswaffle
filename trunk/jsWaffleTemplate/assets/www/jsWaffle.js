/**
 * @projectDescription JavaScript Library for Android
 * 
 * @author	kujirahand.com (http://kujirahand.com)
 * @version	0.1
 * @see http://d.aoikujira.com/jsWaffle/wiki/
 */

/**
 * jsWaffle Default Instance
 * @type {jsWaffle}
 */
var droid = (function(self){
	if (typeof(self.jsWaffle) != 'undefined') return;
	// helper
	if (typeof(self.$) == 'undefined') {
		self.$ = function (id) { return document.getElementById(id); }
	}
	// support for none android device
	if (typeof(self._DroidWaffle) == 'undefined') {
		// return dummy action
		self._DroidWaffle = _DroidWaffle_getDummyFunctions();
	}
	// global temporary object
	var DroidWaffle = self.DroidWaffle = {
		x:0, y:0, z:0, // for accel
		_shake_fn_user : null, // for shake
		_menu_item_fn : [], // for menu
		// for callback function list
		_callback_list : [], 
		getCallbackId : function () {
			return this._callback_list.length;
		},
		setCallback : function (id, callback) {
			this._callback_list[id] = callback;
		},
		getCallback : function (id) {
			var f = this._callback_list[id];
			return f;
		},
		___ : 0
	};
	// local _DroidWaffle shortcut
	var _w = _DroidWaffle;
	/**
	 * jsWaffle Class
	 * 
	 * @classDescription This class defineds jsWaffle functions
	 * @return {jsWaffle}
	 * @type {Object}
	 * @constructor
	 */
	var jsWaffle = self.jsWaffle = function () {};
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
	 * @param {boolean} accuracy_fine
	 * @return {Integer} watchId
	 */
	jsWaffle.prototype.getCurrentPosition = function (onSuccess, onError, accuracy_fine) {
		// set user event
		DroidWaffle._geolocation_fn_ok_user = onSuccess;
		DroidWaffle._geolocation_fn_ng_user = onError;
		if (accuracy_fine == undefined) accuracy_fine = true;
		// register callback function
		return _w.geolocation_getCurrentPosition(
			"DroidWaffle._geolocation_fn_ok",
			"DroidWaffle._geolocation_fn_ng",
			accuracy_fine
		);
	};
	/**
	 * watchPosition (GPS)
	 * @alias DroidWaffle.geolocation.watchPosition
	 * @param {Function} onSuccess
	 * @param {Function} onError
	 * @param {boolean} accuracy_fine
	 * @return {Integer} watchId
	 */
	jsWaffle.prototype.watchPosition = function (onSuccess, onError, accuracy_fine) {
		// set user event
		DroidWaffle._geolocation_fn_ok_user = onSuccess;
		DroidWaffle._geolocation_fn_ng_user = onError;
		if (accuracy_fine == undefined) accuracy_fine = true;
		return _w.geolocation_watchPosition(
			"DroidWaffle._geolocation_fn_ok",
			"DroidWaffle._geolocation_fn_ng",
			accuracy_fine
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
		if (DroidWaffle._geolocation_fn_ok_user) {
			DroidWaffle._geolocation_fn_ok_user(lat, lon, alt);
		}
	};
	DroidWaffle._geolocation_fn_ng = function (err) {
		if (DroidWaffle._geolocation_fn_ng_user) {
			DroidWaffle._geolocation_fn_ng_user(err);
		}
	};
	DroidWaffle._geolocation_fn_ok_gokan = function (lat, lon, alt) { // for HTML5 geolocation event
		var position = {
			"coords" : {
				"latitude"	: lat, 
				"longitude"	: lon,
				"altitude"	: alt
			}
		};
		if (DroidWaffle._geolocation_fn_ok_user) {
			DroidWaffle._geolocation_fn_ok_user(position);
		}
	};
	// emulate HTML5 geolocation (for Android 1.6)
	if (typeof(navigator.geolocation) == "undefined") {
		navigator.geolocation = {
			getCurrentPosition : function (ok_f, ng_f, opt) {
				// set user event
				DroidWaffle._geolocation_fn_ok_user = ok_f;
				DroidWaffle._geolocation_fn_ng_user = ng_f;
				var accuracy_fine = true;
				if (typeof(opt) == "object") {
					accuracy_fine = opt.enableHighAccuracy;
				}
				// register callback function
				return _w.geolocation_getCurrentPosition(
					"DroidWaffle._geolocation_fn_ok_gokan",
					"DroidWaffle._geolocation_fn_ng",
					accuracy_fine
				);
			},
			watchPosition : function (ok_f, ng_f, opt) {
				// set user event
				DroidWaffle._geolocation_fn_ok_user = ok_f;
				DroidWaffle._geolocation_fn_ng_user = ng_f;
				var accuracy_fine = true;
				if (typeof(opt) == "object") {
					accuracy_fine = opt.enableHighAccuracy;
				}
				// register callback function
				return _w.geolocation_watchPosition(
					"DroidWaffle._geolocation_fn_ok_gokan",
					"DroidWaffle._geolocation_fn_ng",
					accuracy_fine
				);
			},
			clearWatch : function (watchid) {
				_w.geolocation_clearWatch(watchid);
			}
		};
	}
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
	 * storage set
	 * @param {String} key
	 * @param {String} value
	 */
	jsWaffle.prototype.pref_set = function (key, value) { _w.preferencePut(key, value); };
	/**
	 * storage get
	 * @param {String} key
	 * @param {String} defValue
	 */
	jsWaffle.prototype.pref_get = function (key, defValue) { return _w.preferenceGet(key, defValue); };
	/**
	 * storage remove
	 * @param {String} key
	 */
	jsWaffle.prototype.pref_remove = function (key) { return _w.preferenceRemove(key, defValue); };
	/**
	 * storage clear
	 */
	jsWaffle.prototype.pref_clear = function () { return _w.preferenceClear(); };
	
	// emurate localStorage for Android 1.6
	if (typeof(window.localStorage) == "undefined") {
		window.localStorage = {
			getItem    : function (key, defvalue) {
				if (defvalue == undefined) defvalue = null;
				return _w.localStorage_get(key, defvalue);
			},
			setItem    : function (key, value) { return _w.localStorage_put(key, value); },
			removeItem : function (key) { return _w.localStorage_remove(key, defValue); },
			clear      : function () { _w.localStorage_clear(); },
			___ : 0
		};
	}
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
	 * @param {String}url (http/https/market/tel/sms/geo/mailto/file/camera/video)
	 * @return {Boolean} result
	 */
	jsWaffle.prototype.startIntent = function (url) {
		return _w.startIntent(url);
	};
	/**
	 * Start Intent with FullScreen
	 * @param {Object} url (ex) file:///assets/www/full.html
	 */
	jsWaffle.prototype.startIntentFullScreen = function (url) {
		return _w.startIntentFullScreen(url);
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
	/**
	 * Scan Barcode
	 * require "com.google.zxing.client.android"
	 * @see http://code.google.com/p/zxing/
	 * @param {Object} callback_fn
	 * @param {String} mode (AUTO|QR_CODE_MODE|ONE_D_MODE|DATA_MATRIX_MODE)
	 * @param {boolean} show_help
	 * @return {boolean} Scanner exisits?
	 */
	jsWaffle.prototype.scanBarcode = function (callback_fn, mode, show_help) {
		// params
		DroidWaffle._scanbarcode_fn = callback_fn;
		if (typeof(mode) != "string") { mode = "AUTO"; }
		// execute
		var b = _w.scanBarcode("DroidWaffle._scanbarcode_onResult", mode);
		if (b) return true;
		// show help
		if (!show_help) return false;
		var show_link = confirm("You need Barcode Scanner. Download?");
		if (show_link) {
			droid.startIntent("market://search?q=pname:com.google.zxing.client.android");
		}
	};
	DroidWaffle._scanbarcode_onResult = function (contents) {
		var f = DroidWaffle._scanbarcode_fn;
		if (typeof(f) == "function") {
			f(contents);
		}
	};
	/**
	 * Scan QRCode
	 * @see getBarcode
	 */
	jsWaffle.prototype.scanQRCode = function (callback_fn, show_help) {
		return this.scanBarcode(callback_fn, "QR_CODE_MODE", show_help);
	};
	
	/**
	 * finish app
	 */
	jsWaffle.prototype.finish = function() {
		_w.finish();
	};
	jsWaffle.prototype.quit = function() {
		_w.finish();
	};
	
	/**
	 * Set MenuItem
	 * (ex) iconName =ic_menu_edit/ic_menu_gallery/ic_menu_help/ic_menu_info_details/ic_menu_manage/ic_menu_preferences
	 * @see http://www.taosoftware.co.jp/blog/2008/11/android_5.html
	 * @param {Integer} itemNo (0-5)
	 * @param {boolean} visible
	 * @param {String} title
	 * @param {String} iconName (resourceName])
	 * @param {Function} callback_fn
	 */
	jsWaffle.prototype.setMenuItem = function (itemNo, visible, title, iconName, callback_fn) {
		_w.setMenuItem(itemNo, visible, title, iconName);
		DroidWaffle._menu_item_fn[itemNo] = callback_fn;
		_w.setMenuItemCallback("DroidWaffle._menu_onSelected");
	};
	DroidWaffle._menu_onSelected = function (itemNo) {
		var f = DroidWaffle._menu_item_fn[itemNo];
		if (typeof(f) == "function") {
			f();
		}
	};
	/**
	 * Dialog Yes or No
	 * @param {String} caption
	 * @param {String} message
	 * @param {Function} callback
	 */
	jsWaffle.prototype.dialogYesNo = function(caption, message, callback_fn){
		var tag = DroidWaffle.getCallbackId();
		_w.dialogYesNo(caption, message, "DroidWaffle._dialogYesNo_callback", tag);
		DroidWaffle.setCallback(tag, callback_fn);
	};
	DroidWaffle._dialogYesNo_callback = function(answer, tag) {
		var f = DroidWaffle.getCallback(tag);
		if (is_function(f)) { f(answer); }
	};
	/**
	 * Select dialog
	 * @param {String} caption
	 * @param {Array} items
	 * @param {Function} callback
	 */
	jsWaffle.prototype.selectList = function(caption, items, callback_fn){
		var tag = DroidWaffle.getCallbackId();
		_w.selectList(caption, items.join(";;;"), "DroidWaffle._selectList_callback", tag);
		DroidWaffle.setCallback(tag, callback_fn);
	};
	DroidWaffle._selectList_callback = function(answer, tag) {
		var f = DroidWaffle.getCallback(tag);
		if (is_function(f)) { f(answer); }
	};
	
	/**
	 * activity event
	 * @param {Function} callback_fn
	 */
	jsWaffle.prototype.registerActivityOnStart = function(callback_fn) {
		DroidWaffle._registerActivityOnStart_user = callback_fn;
		_w.registerActivityOnStart("DroidWaffle._registerActivityOnStart_callback");
	};
	DroidWaffle._registerActivityOnStart_callback = function() {
		if(is_function(DroidWaffle._registerActivityOnStart_user)) {
			DroidWaffle._registerActivityOnStart_user();
		}
	};
	jsWaffle.prototype.registerActivityOnStop = function(callback_fn) {
		DroidWaffle._registerActivityOnStop_user = callback_fn;
		_w.registerActivityOnStop("DroidWaffle._registerActivityOnStop_callback");
	};
	DroidWaffle._registerActivityOnStop_callback = function() {
		if(is_function(DroidWaffle._registerActivityOnStop_user)) {
			DroidWaffle._registerActivityOnStop_user();
		}
	};
	jsWaffle.prototype.registerActivityOnResume = function(callback_fn) {
		DroidWaffle._registerActivityOnResume_user = callback_fn;
		_w.registerActivityOnResume("DroidWaffle._registerActivityOnResume_callback");
	};
	DroidWaffle._registerActivityOnResume_callback = function() {
		if(is_function(DroidWaffle._registerActivityOnResume_user)) {
			DroidWaffle._registerActivityOnResume_user();
		}
	};
	
	/**
	 * write DDMS log console
	 * @param {String} msg
	 */
	jsWaffle.prototype.log = function(msg){
		_w.log(msg);
	};
	jsWaffle.prototype.log_error = function(msg){
		_w.log_error(msg);
	};
	jsWaffle.prototype.log_warn = function(msg){
		_w.log_warn(msg);
	};
	
	return (new jsWaffle());
	
	function is_function(f) {
		return (typeof(f) == "function");
	}
	
	//-----------------------------------
	// dummy function for PC Browser
	//-----------------------------------
	function _DroidWaffle_getDummyFunctions() {
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
					clearInterval(this._timerId);
				} else {
					this._timerId = setInterval(function(){
						var x = Math.random() * 2 -2;
						var y = Math.random() * 2 -2;
						var z = Math.random() * 2 -2;
						eval(fn+"("+x+","+y+","+z+")");
					},1000);
				}
			},
			setShakeCallback : function(fn, freq){
				this._timerId = setInterval(function(){
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
			setMenuItem : function(){},
			setMenuItemCallback : function(){},
			scanBarcode : function(){},
			dialogYesNo:function(msg, f, tag){ var a = confirm(msg); f(a,tag); },
			___ : 0
		};
	}
})(this);


