//
//  ViewController.swift
//  SensorStore
//
//  Created by Joseph Noor on 9/26/16.
//  Copyright © 2016 Joseph Noor. All rights reserved.
//

import UIKit

class ViewController: UIViewController {

    static var iterations = 0
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
        
        Timer.scheduledTimer(withTimeInterval: 0.1, repeats: true) { (timer) in
            
            if ViewController.iterations == 5 {
                self.close()
                self.read()
                ViewController.iterations += 1
                return
            }
            
            if ViewController.iterations > 5 {
                return
            }
            
            print("writing \(ViewController.iterations)")
            ViewController.iterations += 1
            self.sendMessage()
        }
    }
    
    func read() {
        SS_read_all()
    }
    
    func setup() {
        if let fileURL = try? FileManager.default.url(for: .documentDirectory, in: .userDomainMask, appropriateFor: nil, create: false) {
            
            let dataURL = fileURL.appendingPathComponent("log.txt")
            let bytes = (dataURL.path as NSString).utf8String
            let databuffer = UnsafeMutablePointer<Int8>(mutating: bytes)
            
            let indexURL = fileURL.appendingPathComponent("index.txt")
            let bytes2 = (indexURL.path as NSString).utf8String
            let indexbuffer = UnsafeMutablePointer<Int8>(mutating: bytes2)
            
            let dataOffsetURL = fileURL.appendingPathComponent("logoffset.txt")
            let bytes3 = (dataOffsetURL.path as NSString).utf8String
            let logoffsetbuffer = UnsafeMutablePointer<Int8>(mutating: bytes3)
            
            SS_setup(databuffer, indexbuffer, logoffsetbuffer)
        }
    }

    func sendMessage() {
        var ptr: UnsafeMutablePointer<Int8>? = UnsafeMutablePointer<Int8>(mutating: ("This is a long ass-string" as NSString).utf8String)
        switch ViewController.iterations {
        case 1:
            ptr = UnsafeMutablePointer<Int8>(mutating: ("This is a primary ass-string" as NSString).utf8String)
        case 2:
            ptr = UnsafeMutablePointer<Int8>(mutating: ("This is a secondary ass-string" as NSString).utf8String)
        case 3:
            ptr = UnsafeMutablePointer<Int8>(mutating: ("Suck my ass-string" as NSString).utf8String)
        case 4:
            ptr = UnsafeMutablePointer<Int8>(mutating: ("Sup sexy beeeeeeeetches" as NSString).utf8String)
        case 5:
            ptr = UnsafeMutablePointer<Int8>(mutating: ("Working feels good." as NSString).utf8String)
        default:
            break
        }
        SS_write(Int32(ViewController.iterations), ptr)
    }
    
    func close() {
        SS_close()
    }
}

//MARK: SQLite Stuffs
extension ViewController {
    func SQLiteTest() {
        //        if let fileURL = try? FileManager.default.url(for: .documentDirectory, in: .userDomainMask, appropriateFor: nil, create: false).appendingPathComponent("test.sqlite") {
        //            var db: OpaquePointer? = nil
        //            if sqlite3_open(fileURL.path, &db) != SQLITE_OK {
        //                print("Error opening SQLite DB")
        //            } else {
        //                print("Success!!!", SQLITE_VERSION, SQLITE_VERSION_NUMBER)
        //            }
        //            runExampleInC()
        //        }
    }
}

