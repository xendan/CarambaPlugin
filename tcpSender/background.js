function onConnectedCallback() {
    console.log('In Connected Callback');
}
function str2ab(str) {
    var buf = new ArrayBuffer(str.length);
    var bufView = new Uint8Array(buf);
    for (var i = 0, strLen = str.length; i < strLen; i++) {
        bufView[i] = str.charCodeAt(i);
    }
    return buf;
}
function sendMessage(socketId, request) {
    chrome.socket.connect(socketId, 'localhost', 4455, function (result) {
        // We are now connected to the socket so send it some data
        chrome.socket.write(socketId, str2ab(request.getTargetData),
            function (sendInfo) {
                console.log("wrote " + sendInfo.bytesWritten);
            }
        );
        chrome.socket.destroy(socketId);
    });
}
chrome.runtime.onMessageExternal.addListener(
    function (request, sender, sendResponse) {
        chrome.socket.create('tcp', {}, function (createInfo) {
            sendMessage(createInfo.socketId, request)
        });

    });