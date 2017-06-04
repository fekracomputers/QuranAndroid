package com.nourayn.quran.Models;

/**
 * Model class for page
 */
public class Page {

    public int pageNumber , jozaNumber , hezbNumber ;
    public String soraName , soraNameEnglish ;

    /**
     * Constructor to create page
     * @param pageNumber Page number
     * @param jozaNumber Page part number
     * @param soraName Page sora name
     * @param hezbNumber Page hezb number
     * @param soraNameEnglish Sorah name in english
     */
    public Page(int pageNumber , int jozaNumber , int hezbNumber ,String soraName , String soraNameEnglish )
    {
        this.pageNumber = pageNumber ;
        this.jozaNumber = jozaNumber ;
        this.hezbNumber = hezbNumber ;
        this.soraName = soraName ;
        this.soraNameEnglish = soraNameEnglish ;
    }

}
