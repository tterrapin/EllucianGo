//  Copyright (c) 2012-2014 Ellucian. All rights reserved.

if (!window.console) window.console = {log:function () {}};

window.EllucianMobile = (function() {
	internal = {
		_readyFlag: false,
		_callWhenReady: [],
		_isIOsValue: typeof window._isIOsValue != 'undefined' ? window._isIOsValue : undefined,
		_isIOs: function() {
			if (!internal._isIOsValue) {
				var ua = navigator.userAgent
				internal._isIOsValue = ua.search(/applewebkit/i) >= 0 &&
					(ua.search(/iphone/i) >= 0 || ua.search(/ipad/i) >= 0 || ua.search(/ipod/i) >= 0)
				console.log("_isIos: " + internal._isIOsValue)
			}

			return internal._isIOsValue
		},
		_isReady: function() {
			var result = !internal._isIOs() || internal._readyFlag
			console.log("_isReady: " + result)
			return result
		},
		_call: function(theFunction) {
			if (internal._isReady()) {
				console.log("_call: calling function")
				theFunction()
			} else {
				console.log("_call: queing function call")
				internal._callWhenReady.push(theFunction)
			}
		},
		_ready: function() {
			console.log("_ready: Device is ready")
			internal._readyFlag = true
			while (queued = internal._callWhenReady.shift()) {
				console.log("_ready: calling queued function")
				queued(internal)
			}
		},
		_log: function(message) {
			console.log("_log: message: " + message)
			typeof EllucianMobileDevice != 'undefined' && EllucianMobileDevice.log(message)
		},
		_openMenu: function(name, type) {
			console.log("_openMenu: name: " + name + " type: " + type)
			typeof EllucianMobileDevice != 'undefined' && EllucianMobileDevice.openMenu(name, type)
		},
		_refreshRoles: function() {
			console.log("_refreshRoles: called")
			typeof EllucianMobileDevice != 'undefined' && EllucianMobileDevice.refreshRoles()
		},
		_reloadWebModule: function() {
			console.log("_reloadWebModule: called")
			typeof EllucianMobileDevice != 'undefined' && EllucianMobileDevice.reloadWebModule()
		}
	}

	public = {
		_ellucianMobileInternalReady: function() {
			console.log("_ellucianMobileInternalReady: called")
			internal._ready()
		},

		log: function(message) {
			console.log("log: called")
			internal._call(function() { internal._log(message) });
		},

		openMenu: function(name, type) {
			console.log("openMenu: called")
			internal._call(function() { internal._openMenu(name, type) });
		},

		refreshRoles: function() {
			console.log("refreshRoles: called")
			internal._call(internal._refreshRoles)
		},

		reloadWebModule: function() {
			console.log("reloadWebModule: called")
			internal._call(internal._reloadWebModule)
		}
	}

	return public
})();
