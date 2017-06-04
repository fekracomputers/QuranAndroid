package com.fekracomputers.quran.Models;

/**
 * Created by nosier on 11/26/2016.
 */

public class Bookmark_Tafseer {


    public int bookmarkID ;
    public int page ;
    public String dateAndTime ;
    public Page pageInfo ;

    /**
     * Class constructor for bookmark
     * @param page page number
     * @param dateAndTime last time bookmark
     * @param pageInfo
     */
    public Bookmark_Tafseer(int bookmarkID , int page , String dateAndTime , Page pageInfo )
    {
        this.bookmarkID = bookmarkID ;
        this.page = page ;
        this.dateAndTime = dateAndTime ;
        this.pageInfo = pageInfo ;
    }
}
