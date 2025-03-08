#import <AppKit/AppKit.h>
#import <Cocoa/Cocoa.h>
// #import <JavaNativeFoundation/JavaNativeFoundation.h>
#include <jni.h>

JNIEXPORT void JNICALL Java_processing_core_ThinkDifferent_hideMenuBar
(JNIEnv *env, jclass clazz, jboolean visible, jboolean kioskMode)
{
    NSApplicationPresentationOptions options =
            NSApplicationPresentationHideDock | NSApplicationPresentationHideMenuBar;
    [NSApp setPresentationOptions:options];
}

JNIEXPORT void JNICALL Java_processing_core_ThinkDifferent_showMenuBar
(JNIEnv *env, jclass clazz, jboolean visible, jboolean kioskMode)
{
    [NSApp setPresentationOptions:0];
}

JNIEXPORT void JNICALL Java_processing_core_ThinkDifferent_activateIgnoringOtherApps
(JNIEnv *env, jclass klass)
{
    [NSApp activateIgnoringOtherApps:true];
}

JNIEXPORT void JNICALL Java_processing_core_ThinkDifferent_activate
(JNIEnv *env, jclass klass)
{
    [NSApp activate];
}

JNIEXPORT jfloat JNICALL Java_processing_core_ThinkDifferent_getScaleFactor
(JNIEnv *env, jclass cls) {
    NSArray *screens = [NSScreen screens];
    if ([screens count] > 0) {
        NSScreen *mainScreen = [screens objectAtIndex:0];
        NSDictionary *description = [mainScreen deviceDescription];
        NSNumber *scaleFactor = [description objectForKey:NSDeviceResolution];
        return [scaleFactor floatValue];
    }
    return 1.0;
}