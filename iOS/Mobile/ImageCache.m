//
//  ImageCache.m
//  Mobile
//
//  Created by Alan McEwan on 11/20/12.
//  Copyright (c) 2012 Ellucian. All rights reserved.
//

#import "ImageCache.h"
#import "NSString+Hash.h"

@implementation ImageCache

static ImageCache *sharedImageCache = nil;

+ (ImageCache*) sharedCache
{
    if (sharedImageCache == nil) {
        sharedImageCache = [[ImageCache alloc] init];
    }
    return sharedImageCache;
}

- (id)init
{
    self = [super init];
    [self createCacheDirectory];
    return self;
}

- (void)createCacheDirectory
{
    _cachePath = nil;
    NSFileManager*fm = [NSFileManager defaultManager];
    
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSApplicationSupportDirectory, NSUserDomainMask, YES);
    if ([paths count])
    {
        NSString *bundleName =
        [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleIdentifier"];
        _cachePath = [[paths objectAtIndex:0] stringByAppendingPathComponent:bundleName];
        
        NSError* error = nil;
        [fm createDirectoryAtPath:_cachePath
      withIntermediateDirectories:YES
                       attributes:nil error:&error];
        if( error != nil )
            NSLog(@"unable to create directory: %@", error);
        
    }
}

//remove all images from cache
- (void)reset
{
    NSError *error = nil;
    NSFileManager *fm = [NSFileManager defaultManager];
    //remove directory and its contents
    [fm removeItemAtPath:_cachePath error:&error];
    if( error != nil )
        NSLog(@"Unable to clear cache: %@", error);
    
    //restore directory after deletion
    [self createCacheDirectory];
    
}

- (UIImage *) getImage:(NSString *)filename
{
    return [self getImage:filename fromCacheOnly:NO];
}

- (UIImage *) getCachedImage:(NSString *)filename
{
    return [self getImage:filename fromCacheOnly:YES];
}

- (UIImage *) getImage:(NSString*) filename fromCacheOnly:(BOOL)useCacheOnly
{
    
    NSString * encodedName = [filename sha1];
    NSURL *imageURL = [NSURL URLWithString: filename];
    
    // Generates a unique path to a resource representing the image
    NSString *uniquePath = [_cachePath stringByAppendingPathComponent: encodedName];

    // Check for file existence
    if(![[NSFileManager defaultManager] fileExistsAtPath: uniquePath] && !useCacheOnly)
    {
        // The file doesn't exist; we should get a copy of it
        
        // Fetch image
        NSData *data = [[NSData alloc] initWithContentsOfURL: imageURL];
        UIImage *image = [[UIImage alloc] initWithData: data];
        NSURL *fileURL = [NSURL fileURLWithPath:uniquePath];

        // Is it PNG or JPG/JPEG?
        // Running the image representation function writes the data from the image to a file
        NSError* error;
        if([filename rangeOfString: @".png" options: NSCaseInsensitiveSearch].location != NSNotFound)
        {
            [UIImagePNGRepresentation(image) writeToFile:uniquePath options:NSDataWritingAtomic error:&error];
            if(error != nil)
                NSLog(@"write error %@", error);
            [fileURL setResourceValue:[NSNumber numberWithBool:YES] forKey:NSURLIsExcludedFromBackupKey error:&error];
        }
        else if ( [filename rangeOfString: @".jpg" options: NSCaseInsensitiveSearch].location != NSNotFound ||
                     [filename rangeOfString: @".jpeg" options: NSCaseInsensitiveSearch].location != NSNotFound )
        {
            [UIImageJPEGRepresentation(image, 100) writeToFile: uniquePath atomically: YES];
            [fileURL setResourceValue:[NSNumber numberWithBool:YES] forKey:NSURLIsExcludedFromBackupKey error:&error];
        }
        
    }

    //retrieve it from disk
    return [self loadImage: uniquePath];
    
}

-(UIImage *) loadImage:(NSString *)fileName 
{
    UIImage * result = [UIImage imageWithContentsOfFile:[NSString stringWithFormat:@"%@", fileName]];
    
    return result;
}


@end
