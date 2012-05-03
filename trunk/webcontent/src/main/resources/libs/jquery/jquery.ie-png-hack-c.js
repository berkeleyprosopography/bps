/*
 * jQuery IEPngHack plugin method c
 * Version c.1.00  (04/05/2007)
 * @requires jQuery v1.1.1
 *
 * Examples at: http://khurshid.com/jquery/iepnghack/
 * Copyright (c) 2007 Khurshid M.
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl.html
 */
 /**
  *
  * @example
  *
  * $('img[@src$=.png], #panel').IEPNGHack();
  *
  * @apply hack to all png images and #panel which icluded png img in its css
  *
  * @name IEPNGHack
  * @type jQuery
  * @cat Plugins/Image
  * @return jQuery
  * @author jQuery Community
  */
 
(function($) {
	
	$.IEPNGHack = {pixel:'pixel.gif'};
	$.ltIE7 = $.browser.msie && /MSIE\s(5\.5|6\.)/.test(navigator.userAgent);
	$.fn.IEPNGHack = $.ltIE7 ? function() {
    	return this.each(function() {
			var $$ = $(this);
			if ($$.is('img')) { /* hack image tags present in dom */
				var filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(enabled=true,sizingMethod=crop,src='"+$$.attr('src')+"')";
				$$.css({filter:filter, width:$$.width(), height:$$.height()})
				  .attr({src:$.IEPNGHack.pixel})
				  .positionFix();
			} else { /* hack png css properties present inside css */
				var image = $$.css('backgroundImage');
				if (image.match(/^url\(["'](.*\.png)["']\)$/i)) {
					image = RegExp.$1;
					var filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(enabled=true,sizingMethod=crop,src='"+image+"')";
					$$.css({backgroundImage:'none', filter:filter})
					  .positionFix();
				}
			}
		});
	} : function() { return this; };
	// position relatively
	$.fn.positionFix = function() {
		return this.each(function() {
			var $$ = $(this);
			var position = $$.css('position');
			if (position != 'absolute' && position != 'relative') {
				$$.css('position', 'relative');
			}
		});
	};

})(jQuery);