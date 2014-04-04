// Listen for any changes to the URL of any tab.

chrome.commands.onCommand.addListener(function (command) {
    chrome.tabs.executeScript(null, { file: "jquery.min.js" }, function() {
        chrome.tabs.executeScript(null, { file: "content.js" });
    });

})