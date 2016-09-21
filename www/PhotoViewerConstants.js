module.exports = {
    Action: {
        NONE: 0,
        DOWNLOAD: 1,
        SHARE: 2,
        COPY_LINK: 3,
        COPY_DATA: 4
    },
    ShowAsAction: {
        NEVER: 0,                // Never show this item as a button in an Action Bar.
        IF_ROOM: 1,              // Show this item as a button in an Action Bar if the system decides there is room for it.
        ALWAYS: 2,               // Always show this item as a button in an Action Bar. * Use sparingly!
        WITH_TEXT: 4,            // When this item is in the action bar, always show it with a text label even if * it also has an icon specified.
        COLLAPSE_ACTION_VIEW: 8  // This item's action view collapses to a normal menu item. When expanded, the action view temporarily takes over a larger segment of its container.
    }
};