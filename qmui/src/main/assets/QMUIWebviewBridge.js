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

    function _fetchQueueFromNative(){
        var messageQueueString = JSON.stringify(sendingMessageQueue);
        sendingMessageQueue = [];
        return messageQueueString;
    }

    function _handleResponseFromNative(responseStr){
        var response = JSON.parse(responseStr);
        if(response.id){
            var responseCallback = responseCallbacks[response.id];
            if(responseCallback){
                responseCallback(response.data);
                delete responseCallbacks[response.id];
            }
        }
    }

    var QMUIBridge = window.QMUIBridge = {
        send: send,
        _fetchQueueFromNative: _fetchQueueFromNative,
        _handleResponseFromNative: _handleResponseFromNative
    };

    var readyEvent = doc.createEvent('Events');
    readyEvent.initEvent('QMUIBridgeReady');
    readyEvent.bridge = QMUIBridge;
    doc.dispatchEvent(readyEvent);
})()