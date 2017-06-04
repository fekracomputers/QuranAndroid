package com.nourayn.quran.Models;

/**
 * Model class for Quarter
 */
public class Quarter {

    public String soraName , soraNameEnglish , firstVerseText ;
    public int soraid ,startPageNumber , HezbNumber , partNumber , ayaFirstNumber , counter , joza;

    /**
     * Constructor to create Quarter
     * @param soraName Sora name
     * @param soraid Sora ID
     * @param startPageNumber Start page number
     * @param HezbNumber Quarter number
     * @param partNumber Part number
     * @param firstVerseText First verse text
     */
    public Quarter(String soraName , String soraNameEnglish , int soraid, int startPageNumber,
                   int HezbNumber, int partNumber, String firstVerseText , int ayaFirstNumber , int counter , int joza)
    {
        this.soraName = soraName ;
        this.soraNameEnglish = soraNameEnglish ;
        this.soraid = soraid ;
        this.startPageNumber = startPageNumber ;
        this.HezbNumber = HezbNumber ;
        this.partNumber = partNumber ;
        this.firstVerseText = firstVerseText ;
        this.ayaFirstNumber = ayaFirstNumber ;
        this.counter = counter;
        this.joza = joza ;

    }

}
