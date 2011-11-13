<%-- 
    Document   : index
    Created on : 11.11.2011, 10:34:42
    Author     : muter
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>PrologIntegration</title>
        <style type="text/css">
            .messagePad{
                left: 0px;
                right: 0px;
                top: 0px;
                bottom: 30px;
                overflow-y: scroll;
                margin-bottom: 0px;
                padding-bottom: 0px;
                position: absolute;
            }
            body{
                position: static;
            }
            .messageQuery{
                left: 0px;
                right: 0px;
                bottom: 0px;
                height: 20px;
                position: absolute;
            }
            .queryString{
                width: 90%;
                border-style: none;
            }
            .message{
                width: 100%;
                margin-top: 0px;
                margin-bottom: 0px;
                padding-top: 0px;
                padding-bottom: 0px;
            }
            .messageLine{
                display: block;
                margin-top: 0px;
                margin-bottom: 0px;
                padding-top: 0px;
                padding-bottom: 0px;
            }
            *{
                background-color: black;
                color: rgb(187,240,187);
                font-family: 'Courier New', monospace;
                font-size: 10pt;
            }
            .info{
                color: rgb(187,187,187);
            }
            .warning{
                color: orange;
            }
            .error{
                color: red;
            }
        </style>
        <script type="text/javascript">
            var state = 'CALL';
            var url = '<%= request.getContextPath() + "/prolog" %>';
            var commandHistory = [], responseHistory = [];
            var currentIndex = 0, lastCommand = '';
            var KEY_UP = 38, KEY_DOWN = 40, KEY_RETURN = 13;

            function performRequest(text){
                lock();
                var messagePad = document.getElementById('msgs');
                var xmlhttp;
                if (window.XMLHttpRequest)
                    xmlhttp = new XMLHttpRequest();
                else
                    xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
                if(text.length > 0)
                    messagePad.appendChild(addLine('ownText', '>' + text));
                xmlhttp.onreadystatechange = function(){
                    if(xmlhttp.readyState === 4){
                        if(xmlhttp.status === 200){
                            state = xmlhttp.getResponseHeader('state');
                            try{
                                var messages = xmlhttp.responseXML.documentElement.getElementsByTagName('message');
                                for(var i = 0; i < messages.length; i++)
                                    messagePad.appendChild(addMessage(messages[i]));
                            }catch(e){};
                            if(state == 'CALL' || state == 'RECALL')
                                unlock();
                            else if(state == 'IMMIDIATE')
                                setTimeout(function(){performRequest('')}, 100);
                            currentIndex = getHistory().length;
                        }else
                            messagePad.appendChild(addLine('error', 'Сервер вернул статус ' + xmlhttp.status));
                        messagePad.scrollTop = messagePad.scrollHeight;
                    }
                };
                xmlhttp.open('GET', url + '?query=' + encodeURI(text), true);
                xmlhttp.send();
                messagePad.scrollTop = messagePad.scrollHeight;
            }

            function addMessage(message){
                var msgDiv = document.createElement('div');
                var msgType = message.getAttribute('type');
                if('error' == msgType){
                    msgDiv.setAttribute('class', 'message error');
                    msgDiv.setAttribute('className', 'message error');
                }else if('warning' == msgType){
                    msgDiv.setAttribute('class', 'message warning');
                    msgDiv.setAttribute('className', 'message warning');
                }else{
                    msgDiv.setAttribute('class','message info');
                    msgDiv.setAttribute('className', 'message info');
                }
                for(var node = message.firstChild; node; node = node.nextSibling){
                    if(node.nodeName == 'line'){
                        var msgP = document.createElement('p');
                        msgP.setAttribute('class', 'messageLine');
                        msgP.setAttribute('class', 'messageLine');
                        if(node.firstChild != null && node.firstChild.nodeValue != null)
                            msgP.appendChild(document.createTextNode(node.firstChild.nodeValue));
                        else
                            msgP.appendChild(document.createTextNode(' '));
                        msgDiv.appendChild(msgP);
                    }
                }
                return msgDiv;
            }

            function addLine(msgType, line){
                var msgDiv = document.createElement('div');
                if('error' == msgType){
                    msgDiv.setAttribute('class', 'message error');
                    msgDiv.setAttribute('className', 'message error');
                }else if('warning' == msgType){
                    msgDiv.setAttribute('class', 'message warning');
                    msgDiv.setAttribute('className', 'message warning');
                }else if('ownText' == msgType){
                    msgDiv.setAttribute('class','message ownText');
                    msgDiv.setAttribute('className','message ownText');
                }else{
                    msgDiv.setAttribute('class','message info');
                    msgDiv.setAttribute('className', 'message info');
                }
                var msgP = document.createElement('p');
                msgP.setAttribute('class', 'messageLine');
                msgP.setAttribute('class', 'messageLine');
                msgP.appendChild(document.createTextNode(line));
                msgDiv.appendChild(msgP);
                return msgDiv;
            }

            function processKey(event) {
                var field = document.getElementById('query');
                if(event.keyCode == KEY_UP){
                    if(currentIndex > 0){
                        if(currentIndex >= getHistory().length)
                            lastCommand = query.value;
                        field.value = getHistory()[--currentIndex];
                    }
                }else if(event.keyCode == KEY_DOWN){
                    if(currentIndex < getHistory().length - 1)
                        field.value = getHistory()[++currentIndex];
                    else if(currentIndex < getHistory().length){
                        field.value = lastCommand;
                        currentIndex++;
                    }
                }else if(event.keyCode == KEY_RETURN){
                    var text = field.value;
                    text = text.replace(/(^\s+)|(\s+$)/g, '');
                    if(!text == ''){
                        field.value = '';
                        getHistory().push(text);
                        currentIndex = getHistory().length;
                        lastCommand = '';
                        performRequest(text);
                    }
                }else
                    return true;
                return false;
            }

            function getHistory(){
                if(state == 'CALL')
                    return commandHistory;
                else
                    return responseHistory;
            }

            function unlock(){
                var field = document.getElementById('query');
                field.disabled = 0;
            }

            function lock(){
                var field = document.getElementById('query');
                field.disabled = true;
            }
        </script>
    </head>
    <body onload="document.getElementById('query').focus()" onclick="this.onload()">
        <div class="messagePad" id="msgs">
        </div>
        <div class="messageQuery">
            <label for="query" id="prompt">:-</label>
            <input id="query" type="text" class="queryString" onkeypress="return processKey(event)"/>
        </div>
    </body>
</html>
