

// css overrides to be injected when embedded in Ellucian Mobile
if (EllucianMobile.isEmbeddedInEllucianMobile) {
	var css = '\
		h1#home, \
		.restaurant, .restaurant-embedded, .int-page-title {\
			background-color: ' + EllucianMobile.accentColor() + ';\
		}\
		h1#home, .int-page-title h1 {\
			color:' + EllucianMobile.subheaderTextColor() + ';\
			padding:10px 20px 10px 20px;\
		}\
		.name {\
			color:' + EllucianMobile.subheaderTextColor() + ';\
		}\
		.restaurant {\
			top:0px;\
		}\
		#dot-nav {\
			bottom:0px;\
		}\
		table {\
			margin-top:0px;\
			margin-bottom:0px;\
		}\
		#about h1 {\
			color: ' + EllucianMobile.primaryColor() + ';\
		}\
		header {\
			display: none;\
		}\
		footer {\
			display: none;\
		}\
		.menu-bar, .menu-bar-home {\
			display: block;\
		}\
		'
	EllucianMobile.addCssToHead(css)
}
