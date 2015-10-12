//
//  AudioViewController.swift
//  Mobile
//
//  Created by Jason Hocker on 8/21/15.
//  Copyright © 2015 Ellucian Company L.P. and its affiliates. All rights reserved.
//

import Foundation
import AVFoundation
import MediaPlayer

class AudioViewController: UIViewController, AVAudioPlayerDelegate {
    
    var module : Module?
    
    var audioPlayer : AVPlayer?
    var sliderTimer : NSTimer?
    var mpArtwork : MPMediaItemArtwork?
    
    @IBOutlet var imageView: UIImageView!
    
    @IBOutlet var seeker: UISlider!
    @IBOutlet var playButton: UIButton!
    @IBOutlet var backButton: UIButton!
    @IBOutlet var forwardButton: UIButton!
    @IBOutlet var textTextView: UITextView!
    
    @IBOutlet var textLabelBackgroundView: UIView!
    @IBOutlet var textLabel: UILabel!
    @IBOutlet var play: UIButton!
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        sendView("Audio", forModuleNamed: module!.name)
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        UIApplication.sharedApplication().beginReceivingRemoteControlEvents()
        self.becomeFirstResponder()
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        UIApplication.sharedApplication().endReceivingRemoteControlEvents()
        self.resignFirstResponder()
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let urlString = module!.propertyForKey("audio")
        let url = NSURL(string: urlString)
        if let url = url {
            let asset = AVURLAsset(URL: url)
            let playerItem = AVPlayerItem(asset: asset)
            
            do {
                NSNotificationCenter.defaultCenter().addObserver(self, selector:Selector("itemDidFinishPlaying:"), name:AVPlayerItemDidPlayToEndTimeNotification, object: playerItem)
                
                sliderTimer = NSTimer .scheduledTimerWithTimeInterval(0.1, target: self, selector: Selector("updateSlider"), userInfo: nil, repeats: true)
                
                try AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback)
                try AVAudioSession.sharedInstance().setActive(true)
                
                audioPlayer = AVPlayer(playerItem: playerItem)
                audioPlayer?.currentItem?.addObserver(self, forKeyPath: "status", options: ([.New, .Initial]), context: nil)
                
               
                if let description = module!.propertyForKey("description") where description.characters.count > 0 {
                    
                    textLabel.text = description
                    textTextView.text = description
                    
                    textTextView.textColor = UIColor.whiteColor()
                    textTextView.font = UIFont.preferredFontForTextStyle(UIFontTextStyleBody)
                    
                    let labelTapGestureRecognizer = UITapGestureRecognizer(target: self, action: Selector("expandText:"))
                    labelTapGestureRecognizer.numberOfTapsRequired = 1
                    textLabelBackgroundView.addGestureRecognizer(labelTapGestureRecognizer)
                    
                    let textViewTapGestureRecognizer = UITapGestureRecognizer(target:self, action: Selector("expandText:"))
                    textViewTapGestureRecognizer.numberOfTapsRequired = 1
                    textTextView.addGestureRecognizer(textViewTapGestureRecognizer)
                    
                    
                } else {
                    textLabelBackgroundView.hidden = true
                    textLabel.hidden = true
                }

                if let imageUrl = self.module?.propertyForKey("image") {
                    NSURLSession.sharedSession().dataTaskWithURL(NSURL(string: imageUrl)!) { (data, response, error) in
                        if let data = data {
                            dispatch_async(dispatch_get_main_queue()) {
                                let image = UIImage(data: data)
                                if let image = image {
                                    self.imageView.image = image
                                    self.mpArtwork = MPMediaItemArtwork(image: image)
                                    self.updateNowPlaying()
                                }
                            }
                        }
                    }.resume()
                }
                
                self.title = module!.name
                self.seeker.thumbTintColor = UIColor.primaryColor()
                self.seeker.minimumTrackTintColor = UIColor.primaryColor()

                self.updateNowPlaying()
            } catch let error {
                print (error)
            }
            
        }
    }
    
    // MPMediaItemPropertyAlbumTitle
    // MPMediaItemPropertyAlbumTrackCount
    // MPMediaItemPropertyAlbumTrackNumber
    // MPMediaItemPropertyArtist
    // MPMediaItemPropertyArtwork
    // MPMediaItemPropertyComposer
    // MPMediaItemPropertyDiscCount
    // MPMediaItemPropertyDiscNumber
    // MPMediaItemPropertyGenre
    // MPMediaItemPropertyPersistentID
    // MPMediaItemPropertyPlaybackDuration
    // MPMediaItemPropertyTitle
    func updateNowPlaying() {
        
        let playbackDuration = CMTimeGetSeconds(audioPlayer!.currentItem!.duration)
        let playbackTime = CMTimeGetSeconds(audioPlayer!.currentItem!.currentTime())
        
        if let artwork = self.mpArtwork{
            MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = [ MPMediaItemPropertyArtwork : artwork, MPMediaItemPropertyTitle : module!.name, MPMediaItemPropertyPlaybackDuration:playbackDuration, MPNowPlayingInfoPropertyPlaybackRate: 1.0, MPNowPlayingInfoPropertyElapsedPlaybackTime: playbackTime ]
        
        } else {
             MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = [ MPMediaItemPropertyTitle : module!.name, MPMediaItemPropertyPlaybackDuration:playbackDuration, MPNowPlayingInfoPropertyPlaybackRate: 1.0, MPNowPlayingInfoPropertyElapsedPlaybackTime: playbackTime ]
        }
    }
    

    
    @IBAction func togglePlay(sender: AnyObject) {
        if let _ = audioPlayer {
            if isPlaying() {
                goPause(sender)
            } else {
                goPlay(sender)
            }
        }
    }
    
    func goPlay(sender: AnyObject) {
        if let player = audioPlayer {
            sendEventToTracker1WithCategory(kAnalyticsCategoryUI_Action, withAction: kAnalyticsActionButton_Press, withLabel: "Play button pressed", withValue: nil, forModuleNamed: self.module!.name)
            player.play()
            playButton.setImage(UIImage(named: "media_pause"), forState: .Normal)
            updateNowPlaying()
        }
    }
    
    func goPause(sender: AnyObject) {
        if let player = audioPlayer {
            player.pause()
            playButton.setImage(UIImage(named: "media_play"), forState: .Normal)
            updateNowPlaying()
        }
    }
    
    @IBAction func goBack(sender: AnyObject) {
        if let player = audioPlayer {
            let newTime = CMTimeMakeWithSeconds(0, 1)
            player.seekToTime(newTime)
            updateSlider()
        }
    }
    @IBAction func goForward(sender: AnyObject) {
        if let player = audioPlayer {
            let newTime = CMTimeMakeWithSeconds(durationInSeconds() + 1, 1)
            player.seekToTime(newTime)
            updateSlider()
        }
    }
    
    func isPlaying() -> Bool {
        if let player = audioPlayer {
            return player.currentItem != nil && player.rate != 0
        }
        return false
    }
    
    func updateSlider() {
        let duration = durationInSeconds()
        if duration > 0 {
            self.seeker.maximumValue = Float(duration)
            self.seeker.value = Float(currentTimeInSeconds())
            updateNowPlaying()
        } else {
            self.seeker.enabled = false
        }
    }
    
    func durationInSeconds() -> Float64 {
        if let currentItem = audioPlayer?.currentItem {
            return CMTimeGetSeconds(currentItem.duration)
        }
        return 0
    }
    
    func currentTimeInSeconds() -> Float64 {
        if let currentItem = audioPlayer?.currentItem {
            return CMTimeGetSeconds(currentItem.currentTime())
        }
        return 0
    }
    
    @IBAction func sliding(sender: AnyObject) {
        if let player = audioPlayer {
            
            let newTime = CMTimeMakeWithSeconds(Float64(seeker.value), 1)
            player.seekToTime(newTime)
        }
    }
    
    override func observeValueForKeyPath(keyPath: String?, ofObject object: AnyObject?, change: [String : AnyObject]?, context: UnsafeMutablePointer<Void>) {
        if self.audioPlayer?.currentItem!.status == .ReadyToPlay {
            let _ = self.audioPlayer?.currentItem?.duration
            
            playButton.enabled = true
            forwardButton.enabled = true
            backButton.enabled = true
            seeker.enabled = true
            
            updateSlider()
            
            if let currentItem = self.audioPlayer?.currentItem {
                currentItem.removeObserver(self, forKeyPath: "status")
            }
        } else if self.audioPlayer?.currentItem!.status == .Failed {
            let error = self.audioPlayer?.currentItem?.error?.localizedDescription
            let alertController = UIAlertController(title: NSLocalizedString("Error Loading Audio", comment: "title when error loading audio"), message: error, preferredStyle: .Alert)
            
            let OKAction = UIAlertAction(title: "OK", style: .Default, handler: nil)
            alertController.addAction(OKAction)
            
            if let currentItem = self.audioPlayer?.currentItem {
                currentItem.removeObserver(self, forKeyPath: "status")
            }
            
            self.presentViewController(alertController, animated: true, completion: nil)
        }
    }
    
    func itemDidFinishPlaying(sender: AnyObject) {
        if let player = audioPlayer {
            playButton.setImage(UIImage(named: "media_play"), forState: .Normal)
            let newTime = CMTimeMakeWithSeconds(0, 1)
            player.seekToTime(newTime)
            updateSlider()
        }
    }
    
    override func canBecomeFirstResponder() -> Bool {
        return true
    }
    
    override func remoteControlReceivedWithEvent(event: UIEvent?) {
        if let event = event {
            if event.type == UIEventType.RemoteControl {
                switch event.subtype {
                case .RemoteControlPlay:
                    goPlay(event)
                case .RemoteControlPause, .RemoteControlStop:
                    goPause(event)
                case .RemoteControlTogglePlayPause:
                   togglePlay(event)
                case .RemoteControlNextTrack:
                    goForward(event)
                case .RemoteControlPreviousTrack:
                    goBack(event)
                case .RemoteControlBeginSeekingBackward, .RemoteControlEndSeekingBackward, .RemoteControlBeginSeekingForward, .RemoteControlEndSeekingForward:
                    break
                default:
                    break
                }
            }
        }
    }
    
    func expandText(sender: AnyObject) {
        if textLabelBackgroundView.hidden { //shrink
            textLabel.hidden = false
            textLabelBackgroundView.hidden = false
            textTextView.hidden = true
        } else { //grow
            textLabel.hidden = true
            textLabelBackgroundView.hidden = true
            textTextView.hidden = false
        }
    }
}