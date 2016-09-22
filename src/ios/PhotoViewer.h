#import <Cordova/CDV.h>
#import <Cordova/CDVPlugin.h>

@interface PhotoViewer : CDVPlugin <UIDocumentInteractionControllerDelegate>

@property (nonatomic, strong) UIDocumentInteractionController *docInteractionController;
@property (nonatomic, strong) NSMutableArray *documentURLs;

- (void)show: (CDVInvokedUrlCommand*)command;

@end
