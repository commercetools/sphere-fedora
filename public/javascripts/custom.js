$(document).ready(function(){
	//FLEXISLIDER
	jQuery('.flexslider').flexslider({
		animation: "slide",
		start: function(slider){
		  $('body').removeClass('loading');
		}
	});
	
	//JCAROUSEL
	jQuery('.first-and-second-carousel').jcarousel();
	
	
	//SLIDE TOGGLE
	jQuery(".minicart_link").toggle(function() {
		 $('.cart_drop').slideDown(300);	
		 }, function(){
		 $('.cart_drop').slideUp(300);		 
	});	

	//SUB MENU

	jQuery("ul.departments > li.menu_cont > a").toggle(function(){
		$(this).addClass('active');
		$(this).siblings('.side_sub_menu').slideDown(300);
		}, function(){
		$(this).removeClass('active');
		$(this).siblings('.side_sub_menu').slideUp(300);
	});

	jQuery("ul.departments > li.menu_cont > a.active").siblings('.side_sub_menu').slideDown(300);
	
	//FORM ELEMENTS
	jQuery("select").uniform(); 
	
	
	//SHORTCODES
	//Toggle Box
	jQuery(".toggle_box > li:first-child .toggle_title, .toggle_box > li:first-child .toggle_content").addClass('active');
	jQuery(".toggle_box > li > a.toggle_title").toggle(function(){
														
		$(this).addClass('active');
		$(this).siblings('.toggle_content').slideDown(300);
		}, function(){
		$(this).removeClass('active');
		$(this).siblings('.toggle_content').slideUp(300);	
	});	
	
	//TWITTER FEED    //replace "rohithpaul" with your Twitter ID
	$('.twitter_feed').jTweetsAnywhere({
		username: 'rohithpaul',
		count: 1
	});
	
    //ZOOM
    if ($ && $.fn.zoom) {
        $('.image-zoom').zoom();
    }

    //REDIRECT ON SELECT
    $('.browse-url-selector').change(function () {
        var url = $(this).find("option:selected").data("url");
        window.location.href = url;
    });

    //ABBREVIATE DESCRIPTION
    $('.product-item .product_info .description').ellipsis();

    //TOGGLE REGISTRATION FORM IN CHECKOUT PAGE
    $('#checkout-step-login input[name=checkout_method]').change(function() {
        $('.checkout_steps .checkout-step-register').slideToggle(300);
        $('.checkout_steps .checkout-step-guest').slideToggle(500);
    });

    //TOGGLE LANGUAGE LIST
    $('#language_more').on('click', function(e) {
        e.preventDefault();
        $('header .language_switch li').slideToggle();
    });

    // CHANGE PRODUCT PICTURE
    $('.product-image-small').click(function(e) {
        e.preventDefault();
        $('#product-image-zoom img').attr("src", $(this).data("pic"));
    });

    // Toggle payment form between credit card and direct debit
    $('.checkout .paymenttype').click(function(e) {
        $(this).addClass('btn-primary disabled');
        if ($(this).val() == 'elv') {
            $('#payment-form-elv').show();
            $('#payment-form-cc').hide();
        } else {
            $('#payment-form-elv').hide();
            $('#payment-form-cc').show();
        }
    });

});