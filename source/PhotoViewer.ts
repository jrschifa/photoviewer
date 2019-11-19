// @ts-ignore
var exec = require('cordova/exec');

interface IPhotoViewerOptions {
    url?: string;
    title?: string;
    subtitle?: string;
    maxWidth?: number;
    maxHeight?: number;
    menu?: Array<any>;
    share?: boolean;
    closeButton?: boolean;
    copyToReference?: boolean;
    headers?: string;
    picasso?: {
        fit?: boolean;
        centerInside?: boolean;
        centerCrop?: boolean;
    };
}

const ACTION = {
    NONE: 0,
    DOWNLOAD: 1,
    SHARE: 2,
    COPY_LINK: 3,
    COPY_DATA: 4
}

const SHOW_AS_ACTION = {
    NEVER: 0,                // Never show this item as a button in an Action Bar.
    IF_ROOM: 1,              // Show this item as a button in an Action Bar if the system decides there is room for it.
    ALWAYS: 2,               // Always show this item as a button in an Action Bar. * Use sparingly!
    WITH_TEXT: 4,            // When this item is in the action bar, always show it with a text label even if * it also has an icon specified.
    COLLAPSE_ACTION_VIEW: 8, // This item's action view collapses to a normal menu item. When expanded, the action view temporarily takes over a larger segment of its container.
}

class PhotoViewer {
    public Action = ACTION;
    public ShowAsAction = SHOW_AS_ACTION;

    public static show(options: IPhotoViewerOptions = {
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
    }) {
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
    
        exec(function() {}, function() {}, "PhotoViewer", "show", args);
    }
}

// @ts-ignore
module.exports = PhotoViewer;