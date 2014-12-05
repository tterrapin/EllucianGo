//  Copyright (c) 2012-2014 Ellucian. All rights reserved.

window.EllucianMobile = (function() {
	internal = {
		_readyFlag: false,
		_callWhenReady: [],
		_isIOsValue: undefined,
		_isIOs: function() {
			if (!internal._isIOsValue) {
				var ua = navigator.userAgent
				internal._isIOsValue = ua.search(/applewebkit/i) >= 0 &&
					(ua.search(/iphone/i) >= 0 || ua.search(/ipad/i) >= 0 || ua.search(/ipod/i) >= 0)
			}

			return internal._isIOsValue
		},
		_isReady: function() {
			return !internal._isIOs() || internal._readyFlag
		},
		_call: function(theFunction) {
			internal._isReady() ? theFunction() : internal._callWhenReady.push(theFunction)
		},
		_ready: function() {
			internal._readyFlag = true
			while (queued = internal._callWhenReady.shift()) {
				queued(internal)
			}
		},
		_log: function(message) {
			typeof EllucianMobileDevice != 'undefined' && EllucianMobileDevice.log(message)
		},
		_openMenu: function(name, type) {
			typeof EllucianMobileDevice != 'undefined' && EllucianMobileDevice.openMenu(name, type)
		},
		_refreshRoles: function() {
			typeof EllucianMobileDevice != 'undefined' && EllucianMobileDevice.refreshRoles()
		},
		_reloadWebModule: function() {
			typeof EllucianMobileDevice != 'undefined' && EllucianMobileDevice.reloadWebModule()
		}
	}

	public = {
		_ellucianMobileInternalReady: function() {
			internal._ready()
		},

		log: function(message) {
			internal._call(function() { internal._log(message) });
		},

		openMenu: function(name, type) {
			internal._call(function() { internal._openMenu(name, type) });
		},

		refreshRoles: function() {
			internal._call(internal._refreshRoles)
		},

		reloadWebModule: function() {
			internal._call(internal._reloadWebModule)
		}
	}

	return public
})();
