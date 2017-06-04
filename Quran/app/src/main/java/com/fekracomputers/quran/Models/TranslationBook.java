package com.fekracomputers.quran.Models;

/**
 * Model class for translation
 */
public class TranslationBook {
    public int bookID;
    public String bookName, info;
    public int type;
    public boolean isDownloaded , downloading ;

    /**
     * Constructor for translation class
     *
     * @param bookID   Book number
     * @param bookName Book name
     * @param info     Information of book
     * @param type     Type of book translation or explanation
     */
    public TranslationBook(int bookID, String bookName, String info, int type , boolean isDownloaded ,boolean downloading) {
        this.bookID = bookID;
        this.bookName = bookName;
        this.info = info;
        this.type = type;
        this.isDownloaded = isDownloaded ;
        this.downloading = downloading ;
    }

}
