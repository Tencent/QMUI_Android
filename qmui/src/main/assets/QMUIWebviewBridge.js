(function(){
    var doc = document;
    if(window.QMUIBridge){
        return;
    }
    var messagingIframe = createIframe(doc);
    var sendingMessageQueue = [];
    var receivedMessageQueue = [];
    var messageHandlers = {};
    var QUEUE_HAS_MESSAGE = 'qmui://__QUEUE_MESSAGE__/';
    var responseCallbacks = {};
    var uuid = 1;

    function createIframe(doc) {
        var iframe = doc.createElement('iframe');
        iframe.style.display = 'none';
        doc.documentElement.appendChild(iframe);
        return iframe;
    }

    function send(data, callback) {
        if(!data){
            throw new Error("message == null")
        }
        var message = {
            data: data
        }
        if(callback){
            var callbackId = 'cb_' + (uuid++) + '_' + (new Date() - 0);
            responseCallbacks[callbackId] = callback;
            message.callbackId = callbackId;
        }
        sendingMessageQueue.push(message);
        messagingIframe.src = QUEUE_HAS_MESSAGE;
    }

    function isCmdSupport(cmd, callback){
        if(isCmdSupport.__cache && isCmdSupport.__cache.indexOf(cmd) >= 0){
            callback(true)
            return
        }
        getSupportedCmdList(function(data){
            if(data && data.length > 0){
                if(!isCmdSupport.__cache){
                    isCmdSupport.__cache = []
                }
                for(var i = 0; i < data.length; i++){
                    isCmdSupport.__cache.push(data[i])
                }
            }
            callback(isCmdSupport.__cache.indexOf(cmd) >= 0)
        })

    }

    function getSupportedCmdList(callback){
        if(getSupportedCmdList.__cache){
            callback(getSupportedCmdList.__cache)
            return
        }
        send({__cmd__: "getSupportedCmdList"}, function(data){
            getSupportedCmdList.__cache = data
            callback(data)
        })
    }

    function _fetchQueueFromNative(){
        var messageQueueString = JSON.stringify(sendingMessageQueue);
        sendingMessageQueue = [];
        return messageQueueString;
    }

    function _handleResponseFromNative(response){
        if(response && response.callbackId){
            var responseCallback = responseCallbacks[response.callbackId];
            if(responseCallback){
                responseCallback(response.data);
                delete responseCallbacks[response.callbackId];
            }
        }
    }

    var QMUIBridge = window.QMUIBridge = {
        send: send,
        isCmdSupport: isCmdSupport,
        getSupportedCmdList: getSupportedCmdList,
        _fetchQueueFromNative: _fetchQueueFromNative,
        _handleResponseFromNative: _handleResponseFromNative
    };

    var readyEvent = doc.createEvent('Events');
    readyEvent.initEvent('QMUIBridgeReady');
    readyEvent.bridge = QMUIBridge;
    doc.dispatchEvent(readyEvent);
})()