function resizeMe()	{
	alert('loaded');
}

function InitElements()  {
        for(var a=0; a<document.all.length; a++)  {
                if(document.all[a].tagName == 'DIV' && document.all[a].className.toLowerCase()=='namespacechild') {
                        document.all[a].style.display = 'none';
                }
                if(document.all[a].tagName=='IMG')  {
                        document.all[a].style.display='inline';
                }
        }
}

function gvResolve(param) {
	var lmSpan = param

	if (lmSpan.style.display == "block") {
		lmSpan.style.display = "none"
	}else{
		lmSpan.style.display = "block"
	}

		
}

function gvResolve2(param) {
	var lmSpan = param
	if (lmSpan.style.display == "block") {
		setCookie("TeamMenuDisplay", "none", "Mon, 01-Jan-2001 00:00:00 GMT", "/")
		lmSpan.style.display = "none"
	}else{
		setCookie("TeamMenuDisplay", "block", "Mon, 01-Jan-2001 00:00:00 GMT", "/")
		lmSpan.style.display = "block"
	}

		
}

function gvResolve2a(param) {
	var lmSpan = param
	if (lmSpan.style.display == "block") {
		setCookie("ProdMenuDisplay", "none", "Mon, 01-Jan-2001 00:00:00 GMT", "/")
		lmSpan.style.display = "none"
	}else{
		setCookie("ProdMenuDisplay", "block", "Mon, 01-Jan-2001 00:00:00 GMT", "/")
		lmSpan.style.display = "block"
	}

		
}

function gvResolveX2(param, whichArrow, path) {
	var lmSpan = param
	var changeArrow = whichArrow
        var rootpath = path
	if (lmSpan.style.display == "block") {
		lmSpan.style.display = "none";
		changeArrow.src = rootpath + "Plus.jpg";	
	}else{
		lmSpan.style.display = "block";
		changeArrow.src = rootpath + "Minus.jpg";
	}

		
}

function setStatus(param)	{
	var whatMSG = param;
	window.status = whatMSG;
}

function BTN_preloadImages() { //v1.2
  if (document.images) {
    var imgFiles = BTN_preloadImages.arguments;
    var preloadArray = new Array();
    for (var i=0; i<imgFiles.length; i++) {
      preloadArray[i] = new Image;
      preloadArray[i].src = imgFiles[i];
    }
  }
}


function BTN_swapImage() {
  var i,j=0,objStr,obj,swapArray=new Array,oldArray=document.BTN_swapImgData;
  for (i=0; i < (BTN_swapImage.arguments.length-2); i+=3) {
    objStr = BTN_swapImage.arguments[(navigator.appName == 'Netscape')?i:i+1];
    if ((objStr.indexOf('document.layers[')==0 && document.layers==null) ||
        (objStr.indexOf('document.all[')   ==0 && document.all   ==null))
      objStr = 'document'+objStr.substring(objStr.lastIndexOf('.'),objStr.length);
    obj = eval(objStr);
    if (obj != null) {
      swapArray[j++] = obj;
      swapArray[j++] = (oldArray==null || oldArray[j-1]!=obj)?obj.src:oldArray[j];
      obj.src = BTN_swapImage.arguments[i+2];
  } }
  document.BTN_swapImgData = swapArray; //used for restore
}

function BTN_swapImgRestore() { 
  if (document.BTN_swapImgData != null)
    for (var i=0; i<(document.BTN_swapImgData.length-1); i+=2)
      document.BTN_swapImgData[i].src = document.BTN_swapImgData[i+1];
}


//cookie code
function setCookie (name, value, expires, path, domain, secure) {
	document.cookie = name + "=" + escape(value) +
    	((expires) ? "; expires=" + expires : "") +
        ((path) ? "; path=" + path : "") +
        ((domain) ? "; domain=" + domain : "") +
        ((secure) ? "; secure" : "");
}
	
function getCookie(name) {
	var cookie = " " + document.cookie;
	var search = " " + name + "=";
	var setStr = null;
	var offset = 0;
	var end = 0;
	if (cookie.length > 0) {
		offset = cookie.indexOf(search);
		if (offset != -1) {
			offset += search.length;
			end = cookie.indexOf(";", offset)
			if (end == -1) {
				end = cookie.length;
			}
			setStr = unescape(cookie.substring(offset, end));
		}
	}
	return(setStr);
}
	
function getVal(param)	{
	var myVal = getCookie(param);
	alert(myVal);
}


function toggleInfoDisplay (param)	{
	var myVal = getCookie(param);
		if (myVal == "none") {
			setCookie(param, "block", "Mon, 01-Jan-2001 00:00:00 GMT", "/")
			toggleInfoBTN.innerText = "Hide Page Info"
			pageInfo.style.display = "block"
		}else{
			if (myVal == "block") {
				setCookie(param, "none", "Mon, 01-Jan-2001 00:00:00 GMT", "/")
				toggleInfoBTN.innerText = "Show Page Info"
				pageInfo.style.display = "none"
				}
		}

}

function toggleTickerDisplay (param)	{
			setCookie("TickerTickler", param, "Mon, 01-Jan-2001 00:00:00 GMT", "/")
			top.location=param
}


function cookieINIT (param)	{
	var cookieVal = getCookie(param);
	if (cookieVal == null) {
		setCookie(param, "block", "Mon, 01-Jan-2001 00:00:00 GMT", "/")
	}

}

function setCorrectBTNText(param)	{
	var myVal = getCookie(param);
		if (myVal == "none") {
			document.write("Show Page Info")
		}else{
		if (myVal == "block") {
			document.write("Hide Page Info")
			}
		}
}

function checkDefaultPage (param)	{
	var myVal = getCookie(param);
	if (myVal == null) {
		setCookie(param, "default_0.asp", "Mon, 01-Jan-2001 00:00:00 GMT", "/")
		top.location="default_0.asp"
	}
		if (myVal == "default_0.asp") {
			top.location="default_0.asp"
		}else{
		if (myVal == "default_1.asp") {
			top.location="default_1.asp"
		}else{
		setCookie(param, "default_0.asp", "Mon, 01-Jan-2001 00:00:00 GMT", "/")
		}
		}
}


//end cookie