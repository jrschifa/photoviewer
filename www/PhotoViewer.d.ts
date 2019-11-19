declare var exec: any;
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
declare const ACTION: {
    NONE: number;
    DOWNLOAD: number;
    SHARE: number;
    COPY_LINK: number;
    COPY_DATA: number;
};
declare const SHOW_AS_ACTION: {
    NEVER: number;
    IF_ROOM: number;
    ALWAYS: number;
    WITH_TEXT: number;
    COLLAPSE_ACTION_VIEW: number;
};
declare class PhotoViewer {
    Action: {
        NONE: number;
        DOWNLOAD: number;
        SHARE: number;
        COPY_LINK: number;
        COPY_DATA: number;
    };
    ShowAsAction: {
        NEVER: number;
        IF_ROOM: number;
        ALWAYS: number;
        WITH_TEXT: number;
        COLLAPSE_ACTION_VIEW: number;
    };
    static show(options?: IPhotoViewerOptions): void;
}
//# sourceMappingURL=PhotoViewer.d.ts.map