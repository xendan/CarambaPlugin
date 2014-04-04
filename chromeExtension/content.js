var inited;
if (inited == undefined) {
    inited = true;
} else {
    console.log("toggled is " + toggled);
    var toggled = !toggled;
    var allIds = '';
    var mynamespace = 'click.mylistener';

    function notSkiped(id) {
        return id.indexOf('_Label') == -1
            && id.indexOf('_TextArea') == -1
    }

    $('span').each(function () {

            if (notSkiped(this.id)) {
                if (toggled) {
                    $(this).css({"border-color": "#FF0000",
                        "border-width": "2px",
                        "border-style": "solid"});
                    allIds += this.id + '\n';
                } else {
                    $(this).css({"border": "none"});
                }

                if (toggled) {
                    $(this).bind(mynamespace, function () {
                        chrome.runtime.sendMessage('cgcopoafcolebdihohmbaacgjhgagdgm',
                            {getTargetData: allIds + 'SELECTED:' + this.id + '\n'},
                            function (response) {
                            });
                    });
                }
                else {
                    $(this).unbind(mynamespace);
                }
            }
        }
    );
    if (toggled) {
        $('span').each(function () {
                if (notSkiped(this.id) && allIds.indexOf(this.id + ".") != -1) {
                    $(this).unbind(mynamespace);
                    $(this).css({"border": "none"});
                }
            }
        )
    }
}

