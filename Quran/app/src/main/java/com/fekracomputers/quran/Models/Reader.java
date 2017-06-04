package com.fekracomputers.quran.Models;

/**
 * Model class for reader
 */
public class Reader {
    public int readerID , audioType ;
    public String readerName , readerNameEnglish , downloadUrl ;

    /**
     * Constructor for reader class
     * @param readerID Reader id
     * @param readerName Reader name
     * @param readerNameEnglish Reader name in english
     * @param audioType Audio type
     * @param downloadUrl Link to download or stream audio
     */
    public Reader(int readerID, String readerName, String readerNameEnglish, int audioType, String downloadUrl)
    {
        this.readerID = readerID;
        this.audioType = audioType;
        this.readerName = readerName;
        this.readerNameEnglish = readerNameEnglish;
        this.downloadUrl = downloadUrl;
    }

}
