package com.fekracomputers.quran.Models;

/**
 * Model class for book mark
 */
public class Bookmark {

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
    public Bookmark(int bookmarkID , int page , String dateAndTime , Page pageInfo )
    {
        this.bookmarkID = bookmarkID ;
        this.page = page ;
        this.dateAndTime = dateAndTime ;
        this.pageInfo = pageInfo ;
    }

}
