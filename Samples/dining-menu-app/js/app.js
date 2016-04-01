$(function() {

    //    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
    //    USE QUERYSTRING TO CHANGE WHAT'S VISIBLE
    //    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
    var getQueryString = function (field, url) {
        var href = url ? url : window.location.href;
        var reg = new RegExp( '[?&]' + field + '=([^&#]*)', 'i' );
        var string = reg.exec(href);
        return string ? string[1] : null;
    };

    // Check for Ellucian Mobile, hide header and footer if true
    var em = getQueryString('ellucian-mobile');
    if(em == "true") {
        $("header").css("display", "none");
        $("footer").css("display", "none");
    }

    //    xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

    //xxxxxxxxxxxxxxxx [    Drag to swipe function    ] xxxxxxxxxxxxxxxx
        //new Dragend($("#container").get(0), {
        $(function() {
            var container = $("#container");
            if (container.size() > 0) {
                container.dragend({
	                afterInitialize: function() {
	                    $("#container").css("visibility", "visible");

	                    // use querystring value to jump to page
	                    var pg = getQueryString("pagenum");
	                    if(pg != null) {
	                        $("#container").dragend({
	                            jumpToPage: pg
	                        });

	                        // set active nav dot
	                        $("#dot-nav nav a:nth-child(" + pg + ")").removeClass("normal-dot");
	                        $("#dot-nav nav a:nth-child(" + pg + ")").addClass('current-dot');
	                    }
	                    else {

	                        // set page navigation indicator to 1st page as a default
	                        $("#dot-nav nav a:nth-child(1)").removeClass("normal-dot");
	                        $("#dot-nav nav a:nth-child(1)").addClass('current-dot');
	                    }
	                },

	                onSwipeEnd: function() {

	                    // update dot navigation when swiping pages
	                    var thisPage = this.page + 1;
	                    $("#dot-nav nav a").removeClass('current-dot');
	                    $("#dot-nav nav a").addClass('normal-dot');

	                    // set active dot
	                    $("#dot-nav nav a:nth-child(" + thisPage + ")").removeClass("normal-dot");
	                    $("#dot-nav nav a:nth-child(" + thisPage + ")").addClass('current-dot');
	                }
	            });
	        }
        });

        //xxxxxxxxxxxxxxxx [    Toggle Left Nav    ] xxxxxxxxxxxxxxxx
        $("#page-header .menu, .embedded-menu-arrow").on("click", function() {
            var ln = $("#left-nav");
            if(!ln.attr("class")) {
                ln.addClass("showLeftNav");										// show nav
                $("#container").css("margin-left", "80px");						// adjust content to fit nav
                $("#menu-content-container").css("margin-left", "80px");			// menu screen
                $("#home-content-container").css("margin-left", "80px");			// home screen
                $("#about-content-container").css("margin-left", "80px");		// about screen
				$('.embedded-menu-arrow').addClass('open');
            }
            else {
                $("#left-nav").removeClass("showLeftNav");
                $("#container").css("margin-left", "0");
                $("#home-content-container").css("margin-left", "0");
                $("#menu-content-container").css("margin-left", "0");
                $("#about-content-container").css("margin-left", "0");
				$('.embedded-menu-arrow').removeClass('open');
            }
        });


        if (EllucianMobile.isEmbeddedInEllucianMobile) {

			// switch css class on restaurant headers
			if(document.querySelector(".restaurant")) {
				var restaurants = document.getElementsByClassName("restaurant");
				var i = restaurants.length;
				while(i--) {
					restaurants[i].setAttribute("class", "restaurant-embedded");
				}
			}
        }
});
