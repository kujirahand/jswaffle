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
		self.$ = function (id) {
			var dom = document.getElementById(id);
			dom.hide = function() {
				this.style.display = "none";
			};
			dom.show = function() {
				this.style.display = "block";
			};
			return dom;
		}
	}
	// support for none android device
	var dummy_fn = function() { return false; };
	if (typeof(self._DroidWaffle) == 'undefined') {
		// return dummy action
		self._DroidWaffle = _DroidWaffle_getDummyFunctions();
	}
	// local _DroidWaffle shortcut
	var _w = _DroidWaffle;
	
	if (typeof(self._gps) == 'undefined') {
		self._gps = { "watchPosition":dummy_fn, "getCurrentPosition":dummy_fn, "clearWatch":dummy_fn };
		self._gps = {
			watchId : 0,
			watchPosition : function(fnOK, fnNG){
				return watchId = setInterval(function(){
					eval(fnOK+"(0,0,0)");
				}, 1000);
			},
			clearWatch : function(watchId){
				clearInterval(watchId);
			},
			getCurrentPosition : function(fnOK, fnNG){
				watchId = setInterval(function(){
					clearInterval(watchId);
					eval(fnOK+"(0,0,0)");
				}, 1000);
			}
		};
	}
	if (typeof(self._accel) == 'undefined') {
		self._accel = {
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
			}
		};
	}
	
	//todo: _storage object
	
	// global temporary object
	var DroidWaffle = self.DroidWaffle = {
		_shake_fn_user : null, // for shake
		_menu_item_fn : [], // for menu
		// for callback function list
		_callback_id   : 0,
		_callback_list : [], 
		getCallbackId : function () {
			this._callback_id++;
			return this._callback_id;
		},
		setCallback : function (id, callback) {
			this._callback_list[id] = callback;
		},
		getCallback : function (id) {
			var f = this._callback_list[id];
			return f;
		},
		onPageFinished : function(url){
			if (typeof(droid.onPageFinished) == "function") {
				droid.onPageFinished(url);
			}
		},
		___ : 0
	};
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
	 * @return {Number} watchId
	 */
	jsWaffle.prototype.watchAccel = function (callback_fn) {
		DroidWaffle._accel_fn = callback_fn;
		if (callback_fn == undefined || callback_fn == "" || callback_fn == null) {
			clearAccel();
			return -1;
		}
		return _accel.setAccelCallback("DroidWaffle._watchSensor");
	};
	/**
	 * clear sensor
	 * @param {Number} watchId
	 */
	jsWaffle.prototype.clearAccel = function (watchId) {
		if (watchId == undefined) {
			_accel.clearAccelAll();
			return;
		}
		_accel.clearAccel(watchId);
	};
	
	if (typeof(window.ondevicemotion) == "undefined") {
		// TODO: emulate safari ondevicemotion(?)
	}
	
	/**
	 * watch Shake device
	 * @param {Function} shake_begin_callback_fn
	 * @param {Function} shake_end_callback_fn
	 * @param {Number} shake_begin_freq (default = 20)
	 * @param {Number} shake_end_freq (default = 8)
	 * @return {Number} watchId
	 */
	jsWaffle.prototype.watchShake = function (shake_begin_callback_fn, shake_end_callback_fn, shake_begin_freq, shake_end_freq) {
		if (shake_begin_freq == undefined) shake_begin_freq = 20;
		if (shake_end_freq == undefined) shake_end_freq = 8;
		if (shake_begin_callback_fn == null || shake_begin_callback_fn == undefined || shake_begin_callback_fn == '') {
			_accel.clearAccelAll();
			return;
		}
		DroidWaffle._shake_fn_user = shake_begin_callback_fn;
		DroidWaffle._shake_end_fn_user = shake_end_callback_fn;
		_accel.setShakeCallback("DroidWaffle._shake_callback", "DroidWaffle._shake_end_callback", shake_begin_freq, shake_end_freq);
	};
	// for sensor
	DroidWaffle._watchSensor = function (accelX, accelY, accelZ) {
		DroidWaffle._accel_fn(accelX, accelY, accelZ);
	};
	DroidWaffle._shake_callback = function () {
		if (DroidWaffle._shake_fn_user) DroidWaffle._shake_fn_user();
	};
	DroidWaffle._shake_end_callback = function () {
		if (DroidWaffle._shake_end_fn_user) DroidWaffle._shake_end_fn_user();
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
		return _gps.getCurrentPosition(
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
		return _gps.watchPosition(
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
		_gps.clearWatch(watchid);
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
				return _gps.getCurrentPosition(
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
				return _gps.watchPosition(
					"DroidWaffle._geolocation_fn_ok_gokan",
					"DroidWaffle._geolocation_fn_ng",
					accuracy_fine
				);
			},
			clearWatch : function (watchid) {
				_gps.clearWatch(watchid);
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
	 * check file exists
	 * @param {String} path
	 */
	jsWaffle.prototype.fileExists = function (path) {
		return _w.fileExists(path);
	};
	/**
	 * create directories
	 * @param {String} path
	 */
	jsWaffle.prototype.mkdir = function (path) {
		return _w.mkdir(path);
	};
	/**
	 * delete file
	 * @param {String} path
	 */
	jsWaffle.prototype.deleteFile = function (path) {
		return _w.deleteFile(path);
	};
	/**
	 * fileSize
	 * @param {String} path
	 */
	jsWaffle.prototype.fileSize = function (path) {
		return _w.fileSize(path);
	};
	/**
	 * copy file from assets to sdcard
	 * @param {String} assetsName
	 * @param {String} savepath
	 */
	jsWaffle.prototype.copyAssetsFile = function (assetsName, savepath) {
		return _w.copyAssetsFile(assetsName, savepath);
	};
	/**
	 * copy separated file from assets to sdcard
	 * @param {String} assetsName
	 * @param {String} savepath
	 */
	jsWaffle.prototype.mergeSeparatedAssetsFile = function (assetsName, savepath) {
		return _w.mergeSeparatedAssetsFile(assetsName, savepath);
	};
	/**
	 * storage set
	 * @param {String} key
	 * @param {String} value
	 */
	jsWaffle.prototype.pref_set = function (key, value) { _storage.preferencePut(key, value); };
	/**
	 * storage get
	 * @param {String} key
	 * @param {String} defValue
	 */
	jsWaffle.prototype.pref_get = function (key, defValue) { return _storage.preferenceGet(key, defValue); };
	/**
	 * storage remove
	 * @param {String} key
	 */
	jsWaffle.prototype.pref_remove = function (key) { return _storage.preferenceRemove(key, defValue); };
	/**
	 * storage clear
	 */
	jsWaffle.prototype.pref_clear = function () { return _storage.preferenceClear(); };
	
	// emurate localStorage for Android 1.6
	if (typeof(window.localStorage) == "undefined") {
		window.localStorage = {
			getItem    : function (key, defvalue) {
				if (defvalue == undefined) defvalue = null;
				return _w.localStorage_get(key, defvalue);
			},
			setItem    : function (key, value) { return _storage.localStorage_put(key, value); },
			removeItem : function (key) { return _storage.localStorage_remove(key, defValue); },
			clear      : function () { _storage.localStorage_clear(); },
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
		var db = _db.openDatabase(dbname);
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
		var tag = DroidWaffle.getCallbackId();
		DroidWaffle.setCallback("executeSql_ok" + tag, fn_ok);
		DroidWaffle.setCallback("executeSql_ng" + tag, fn_ng);
		_db.executeSql(db, sql, "DroidWaffle._db.ok", "DroidWaffle._db.ng", tag);
	};
	DroidWaffle._db = {};
	DroidWaffle._db.ok = function (result, tag) {
		var f = DroidWaffle.getCallback("executeSql_ok" + tag);
		f(result);
	};
	DroidWaffle._db.ng = function (err, tag) {
		var f = DroidWaffle.getCallback("executeSql_ng" + tag);
		f(err);
	};
	/**
	 * executeSqlSync
	 * @param {DBHelper}db
	 * @param {String}sql
	 * @return {Array}query result
	 */
	jsWaffle.prototype.executeSqlSync = function (db, sql) {
		var r = _db.executeSqlSync(db, sql);
		if (r != null) {
			return eval("("+r+")");
		}
		return r;
	};
	jsWaffle.prototype.getDatabaseError = function(db){
		var r = _db.getDatabaseError(db, sql);
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
	 * Start Intent for Result
	 * @param {String} url
	 * @param {Function} callback
	 */
	jsWaffle.prototype.startIntentForResult = function (url, callback) {
		// register user function
		var id = DroidWaffle.getCallbackId();
		DroidWaffle.setCallback(id, callback);
		// set proxy function
		DroidWaffle._startIntentForResult_callback = function (request_code, result_code) {
			var f = DroidWaffle.getCallback(request_code);
			if (typeof(f) == "function") {
				f(result_code);
			}
		};
		return _w.startIntentForResult(url, "DroidWaffle._startIntentForResult_callback", id);
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
	 * save screen capture 
	 * @param {String} filename
	 * @param {String} format (png or jpeg)
	 */
	jsWaffle.prototype.snapshotToFile = function (filename, format) {
		if (format == undefined) { format = "png"; }
		return _w.snapshotToFile(filename, format);
	};
	
	/**
	 * http get
	 * @param {String} url
	 * @return {String} get data
	 */
	jsWaffle.prototype.httpGet = function (url) {
		return _w.httpGet(url);
	};
	/**
	 * http post
	 * @param {String} url
	 * @param {String} json
	 * @return {String} result
	 */
	jsWaffle.prototype.httpPostJSON = function (url, json) {
		return _w.httpPostJSON(url, json);
	};
	/**
	 * http download
	 * @param {String} url
	 * @param {String} filename
	 * @param {Function} callback
	 */
	jsWaffle.prototype.httpDownload = function (url, filename, callback) {
		DroidWaffle._httpDownload = function(ok) {
			if(typeof(callback) == "function") {
				callback(ok);
			}
		};
		_w.httpDownload(url, filename, "DroidWaffle._httpDownload");
	};
	
	/**
	 * Get Res Strings
	 * 国際化のためのリソース文字列(values-xx/strings.xml)の値を取得する
	 * @param {String} id
	 * @return {String} Get String from resource
	 */
	jsWaffle.prototype.getResString = function (id) {
		return _w.getResString(id);
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
	 * @param {String} title
	 * @param {String} message
	 * @return {Boolean}
	 */
	jsWaffle.prototype.dialogYesNo = function(title, message){
		_w.setPromptType(0x10, title);
		var r = prompt(message);
		_w.setPromptType(0, "prompt");
		return (r == "true");
	};
	/**
	 * list dialog
	 * @param {String} title
	 * @param {Array} items
	 * @return {String} selected item
	 */
	jsWaffle.prototype.dialogList = function(title, items){
		_w.setPromptType(0x11, title);
		var r = prompt("", items.join(";;;"));
		_w.setPromptType(0, "prompt");
		return r;
	};
	/**
	 * Checkbox list dialog
	 * @param {String} title
	 * @param {Array} items
	 * @return {Array} selected item
	 */
	jsWaffle.prototype.dialogCheckboxList = function(title, items){
		_w.setPromptType(0x12, title);
		var r = prompt("", items.join(";;;"));
		_w.setPromptType(0, "prompt");
		return r.split(";;;");
	};
	/**
	 * Date picker dialog
	 * @param {Date} defaultDate
	 * @return {Date}
	 */
	jsWaffle.prototype.dialogDatePicker = function(defaultDate){
		_w.setPromptType(0x13, "");
		if (typeof(defaultDate) != "object") {
			defaultDate = new Date();
		}
		var d = defaultDate;
		var r = prompt("", new Array(d.getFullYear(), d.getMonth(), d.getDate()).join(","));
		var a = r.split(",");
		d = new Date(a[0], a[1], a[2]);
		_w.setPromptType(0, "prompt");
		return d;
	};
	/**
	 * Time picker dialog
	 * @param {Integer} hours
	 * @param {Integer} minutes
	 * @return {String} time format "hh:nn"
	 */
	jsWaffle.prototype.dialogTimePicker = function(hours, minutes){
		if (hours == undefined || minutes == undefined) {
			var d = new Date();
			hours = d.getHours();
			minutes = d.getMinutes();
		}
		_w.setPromptType(0x14, "");
		var r = prompt("", hours + ":" + minutes);
		_w.setPromptType(0, "prompt");
		return r;
	};
	/**
	 * Seekbar dialog
	 * @param {String} title
	 * @param {Integer} min
	 * @param {Integer} max
	 * @param {Integer} defaltValue
	 * @return {Integer} value
	 */
	jsWaffle.prototype.dialogSeekbar = function(title,min,max,defaltValue){
		if (typeof(min) != "number") min = 0;
		if (typeof(max) != "number") max = 100;
		if (typeof(defaltValue) != "number") defaltValue = Math.floor((max - min) / 2);
		_w.setPromptType(0x15, title);
		var r = prompt("", min + "," + max + "," + defaltValue);
		_w.setPromptType(0, "prompt");
		return parseInt(r);
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
			getWaffleVersion : function () { return "x.xx"; },
			log : function (msg) { console.log(msg); },
			beep : function() {},
			vibrate : function() {},
			ring : function(){},
			makeToast : function(){},
			createPlayer : function(){ return {start:function(){}}; },
			saveText : function (){},
			loadText : function() {},
			startIntent : function(){},
			openDatabase : function(fname){
				// for webkit
				if (typeof(window.openDatabase) == "function") {
					var db = window.openDatabase(fname, "","emu db", 1024*1024*8);
					return db;
				} else {
					return {};
				}
			},
			executeSql : function(db, sql, ok_str, ng_str, tag) {
				var ok = eval(ok_str);
				var ng = eval(ng_str);
				if (typeof(ng) !== "function") { ng = function(){}; }
				// for webkit
				if (typeof(window.openDatabase) == "function") {
					db.transaction(function(tx){
						tx.executeSql(sql, [],
								function(tx,res){
									var r = [];
									for (var i = 0; i < res.rows.length; i++) {
										r.push(res.rows.item(i));
									}
									ok(r, tag);
								},
								function(tx,err){ ng(err, tag);}
						);
					});
				} else {
					if (typeof(ok) == "function") { ok(); }
				}
			},
			executeSqlSync : function (db, sql) {
				return null;
			},
			setMenuItem : function(){},
			setMenuItemCallback : function(){},
			scanBarcode : function(){},
			dialogYesNo:function(msg, f, tag){ var a = confirm(msg); f(a,tag); },
			fileExists : function(){ return false; },
			mkdir : function () { return true; },
			deleteFile : function() { return true; },
			setPromptType : function () {},
			getResString: function(name) { return name; },
			mergeSeparatedAssetsFile : function() { return false; },
			snapshotToFile: function(fname) { return false; },
			httpGet: function(){ return "hoge"; },
			httpPostJSON: function(){ return "hoge"; },
			___ : 0
		};
	}
})(this);


