package com.nourayn.quran.Models;

import java.util.List;

/**
 * Model class for page tafser
 */
public class PageTafseer {
    public String soraName , soraNameEnglish ;
    public int  soraID , pageNumber , jozaNumber ;
    public List<AyaTafseer> ayaTafseers ;

    /**
     * Constructor for page tafser class
     * @param soraName Sora name
     * @param soraID Sora ID
     * @param pageNumber Page number
     * @param jozaNumber Part number
     * @param ayaTafseers aya number
     */
    public PageTafseer(String soraName ,String soraNameEnglish , int soraID, int pageNumber, int jozaNumber, List<AyaTafseer> ayaTafseers)
    {
        this.soraName = soraName ;
        this.soraNameEnglish = soraNameEnglish ;
        this.soraID = soraID ;
        this.pageNumber = pageNumber ;
        this.jozaNumber = jozaNumber ;
        this.ayaTafseers = ayaTafseers ;
    }

}
