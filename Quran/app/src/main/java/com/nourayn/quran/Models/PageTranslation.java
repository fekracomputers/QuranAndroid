package com.nourayn.quran.Models;

import java.util.List;

/**
 * Model class for page translate
 */
public class PageTranslation {
    public String soraName ;
    public int soraID , pageNumber , jozaNumber ;
    public List<AyaTranslation> ayaTranslation;

    /**
     * Constructor class for page translate
     * @param soraName Sora name
     * @param soraID sora ID
     * @param pageNumber Page number
     * @param jozaNumber Joza number
     * @param ayaTranslation Aya translation text
     */
    public PageTranslation(String soraName, int soraID, int pageNumber, int jozaNumber, List<AyaTranslation> ayaTranslation)
    {
        this.soraName = soraName ;
        this.soraID = soraID ;
        this.pageNumber = pageNumber ;
        this.jozaNumber = jozaNumber ;
        this.ayaTranslation = ayaTranslation;
    }

}
