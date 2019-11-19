"use strict";
// @ts-ignore
var exec = require('cordova/exec');
var ACTION = {
    NONE: 0,
    DOWNLOAD: 1,
    SHARE: 2,
    COPY_LINK: 3,
    COPY_DATA: 4
};
var SHOW_AS_ACTION = {
    NEVER: 0,
    IF_ROOM: 1,
    ALWAYS: 2,
    WITH_TEXT: 4,
    COLLAPSE_ACTION_VIEW: 8,
};
var PhotoViewer = /** @class */ (function () {
    function PhotoViewer() {
        this.Action = ACTION;
        this.ShowAsAction = SHOW_AS_ACTION;
    }
    PhotoViewer.show = function (options) {
        if (options === void 0) { options = {
            url: '',
            title: '',
            subtitle: '',
            maxWidth: 0,
            maxHeight: 0,
            menu: [],
            share: false,
            closeButton: true,
            copyToReference: false,
            headers: '',
            picasso: {
                fit: true,
                centerInside: true,
                centerCrop: false
            }
        }; }
        if (!options.url) {
            // Do nothing
            return;
        }
        var args = [
            options.url,
            options.title,
            options.subtitle,
            options.maxWidth,
            options.maxHeight,
            options.menu,
            options.share,
            options.closeButton,
            options.copyToReference,
            options.headers,
            options.picasso
        ];
        exec(function () { }, function () { }, "PhotoViewer", "show", args);
    };
    return PhotoViewer;
}());
// @ts-ignore
module.exports = PhotoViewer;
