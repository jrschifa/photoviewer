var argscheck = require('cordova/argscheck'),
    exec = require('cordova/exec'),
    constants = require('./PhotoViewerConstants'),
    PhotoViewer = function() {};

// Tack on the PhotoViewer Constants to the base PhotoViewer plugin.
for (var key in constants) {
    if (!PhotoViewer.hasOwnProperty(key))
        PhotoViewer.prototype[key] = constants[key];
}

PhotoViewer.prototype.show = function(options, successHandler, errorHandler) {
    argscheck.checkArgs('OfF', 'PhotoViewer.show', arguments);
    options = options || {};
    var getValue = argscheck.getValue;

    var url = getValue(options.url, null),
        title = getValue(options.title, null),
        subtitle = getValue(options.subtitle, null),
        maxWidth = getValue(options.maxWidth, 0),
        maxHeight = getValue(options.maxHeight, 0),
        menu = getValue(options.menu, []),
        args = [url, title, subtitle, maxWidth, maxHeight, menu];

    exec(successHandler, errorHandler, "PhotoViewer", "show", args);
};

var photoViewer = new PhotoViewer();

if (!window.PhotoViewer) window.PhotoViewer = photoViewer;
if (typeof module != 'undefined' && module.exports) module.exports = photoViewer;
