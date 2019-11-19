#import <Cordova/CDV.h>
#import <Cordova/CDVPlugin.h>

@interface PhotoViewer : CDVPlugin <UIDocumentInteractionControllerDelegate, UIScrollViewDelegate> {
    // Member variables go here.
    Boolean isOpen;
    UIScrollView *fullView;
    UIImageView *imageView;
    UIButton *closeBtn;
    UILabel *imageLabel;
    BOOL showCloseBtn;
    BOOL copyToReference;
    NSDictionary *headers;
}

@property (nonatomic, strong) UIDocumentInteractionController *docInteractionController;
@property (nonatomic, strong) NSMutableArray *documentURLs;

- (void)show:(CDVInvokedUrlCommand*)command;
@end