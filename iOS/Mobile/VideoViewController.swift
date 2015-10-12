//
//  VideoViewController.swift
//  Mobile
//
//  Created by Jason Hocker on 8/21/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation
import AVKit
import AVFoundation

class VideoViewController : UIViewController, UIGestureRecognizerDelegate {
    
    var module : Module?
    
    @IBOutlet var imageView: UIImageView!
    @IBOutlet var textBackgroundView: UIView!
    @IBOutlet var label: UILabel!
    @IBOutlet var videoView: UIView!
    @IBOutlet var mediaPlayButton: UIView!
    
    var assetUrl : NSURL?
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        self.sendView("Video", forModuleNamed: module!.name)
    }
    
    override func viewDidLoad() {
        
        let hud = MBProgressHUD.showHUDAddedTo(self.view, animated: true)
        hud.labelText = NSLocalizedString("Loading", comment: "loading message while waiting for data to load")
        
        
        self.title = module!.name
        if let labelText = self.module?.propertyForKey("description") where labelText.characters.count > 0 {
            label.text = labelText
        } else {
            textBackgroundView.hidden = true
            label.hidden = true
        }
        
        if let urlString = module?.propertyForKey("video") {
            assetUrl = NSURL(string: urlString)
            copyImageToBackground()
        }
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue,
        sender: AnyObject?) {
            sendEventToTracker1WithCategory(kAnalyticsCategoryUI_Action, withAction: kAnalyticsActionButton_Press, withLabel: "Play button pressed", withValue: nil, forModuleNamed: module!.name)
            let destination = segue.destinationViewController as!
            AVPlayerViewController
            if let url = self.assetUrl {
                do {
                    let player = AVPlayer(URL: url)
                    let audioSession = AVAudioSession.sharedInstance()
                    try audioSession.setCategory(AVAudioSessionCategoryPlayback)
                    destination.player = player
                    player.play()
                } catch let error {
                    print(error)
                }
            }
    }
    
    private func copyImageToBackground() {
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0)) {
            let asset: AVAsset = AVAsset(URL: self.assetUrl!)
            let imageGenerator = AVAssetImageGenerator(asset: asset);
            let time = CMTimeMake(0, 600)
            do {
                let imageRef = try imageGenerator.copyCGImageAtTime(time, actualTime: nil)
                let image = UIImage(CGImage: imageRef)
                dispatch_async(dispatch_get_main_queue()) {
                    self.mediaPlayButton.hidden = false
                    MBProgressHUD.hideHUDForView(self.view, animated: true)
                    self.imageView.image = image
                    
                    self.addGestureRecognizer()
                }
            } catch let error as NSError {
                dispatch_async(dispatch_get_main_queue()) {
                    
                    MBProgressHUD.hideHUDForView(self.view, animated: true)
                    let alertController = UIAlertController(title: NSLocalizedString("Error Loading Video", comment: "title when error loading video"), message: error.localizedDescription, preferredStyle: .Alert)
                    
                    let OKAction = UIAlertAction(title: "OK", style: .Default, handler: nil)
                    alertController.addAction(OKAction)
                    
                    self.presentViewController(alertController, animated: true, completion: nil)
                }
            } catch {
                
            }
        }
    }
    
    func addGestureRecognizer() {
        let recognizer = UITapGestureRecognizer(target: self, action:Selector("play:"))
        recognizer.delegate = self
        self.videoView.addGestureRecognizer(recognizer)
    }
    
    func play(recognizer: UITapGestureRecognizer) {
        self.performSegueWithIdentifier("play", sender: nil)
    }
}
