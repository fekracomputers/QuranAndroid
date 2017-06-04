package com.fekracomputers.quran.Models;

/**
 * Model class for Aya tafseer
 */
public class AyaTafseer {
    public int soraID , ayaID  ;
    public String tafseer , ayaText;
    /**
     * Constructor to create aya tafser
     * @param soraID tafseer sora ID
     * @param ayaID tafseer aya ID
     * @param tafseer tafseer text
     */
    public AyaTafseer(int soraID, int ayaID, String tafseer , String ayaText)
    {
        this.soraID = soraID ;
        this.ayaID = ayaID ;
        this.tafseer = tafseer ;
        this.ayaText = ayaText ;
    }

}
