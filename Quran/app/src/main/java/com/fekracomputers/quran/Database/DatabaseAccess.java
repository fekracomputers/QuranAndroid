package com.fekracomputers.quran.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.RectF;
import android.os.Environment;
import android.util.Log;

import com.fekracomputers.quran.Models.Aya;
import com.fekracomputers.quran.Models.AyaTafseer;
import com.fekracomputers.quran.Models.AyaTranslation;
import com.fekracomputers.quran.Models.Bookmark;
import com.fekracomputers.quran.Models.Page;
import com.fekracomputers.quran.Models.PageTafseer;
import com.fekracomputers.quran.Models.PageTranslation;
import com.fekracomputers.quran.Models.Quarter;
import com.fekracomputers.quran.Models.QuarterPage;
import com.fekracomputers.quran.Models.Reader;
import com.fekracomputers.quran.Models.Sora;
import com.fekracomputers.quran.Models.TranslationBook;
import com.fekracomputers.quran.Utilities.AppConstants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.widget.Toast;

/**
 * A class for access quran databases
 */
public class DatabaseAccess {
    public static final String
            MAIN_DATABASE = Environment.getExternalStorageDirectory().getAbsolutePath() + AppConstants.Paths.MAIN_DATABASE_PATH,
            TAFSEER_DATABASE = Environment.getExternalStorageDirectory().getAbsolutePath() + AppConstants.Paths.TAFSEER_DATABASE_PATH,
            TRANSLATION_DATABASE = Environment.getExternalStorageDirectory().getAbsolutePath() + AppConstants.Paths.MAIN_DATABASE_PATH,
            SELECTION_DATABASE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/quran_fekra_computers/quranpages_" + AppPreference.getScreenResolution() + "/quranpages.sqlite";

    /**
     * Empty constructor for database class
     */
    public DatabaseAccess() {
    }

    /**
     * Function to open connection with database
     *
     * @param path database path in mobile
     * @return database object to start queries
     */
    public SQLiteDatabase openDB(String path) {

        Log.d("DATABASE", path);

        SQLiteDatabase db ;
        try {
            db = SQLiteDatabase.openDatabase(path, null, 0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return db;

    }

    public String getSoraFirstAya(int soraNumber){
       SQLiteDatabase sqLiteDatabase =  openDB(MAIN_DATABASE);
        String sql ;
        if(soraNumber!=9){
            sql = "select `text` from `aya` where `soraid`="+soraNumber+"  limit 1 OFFSET 1;";
        }else{
            sql = "select `text` from `aya` where `soraid`="+soraNumber+" limit 1";
        }
        Cursor cursor = sqLiteDatabase.rawQuery(sql,null);
        String aya = "";
        if(cursor.moveToFirst()){
            aya+=cursor.getString(0);
        }
        cursor.close();
        closeDB(sqLiteDatabase);
        return aya;
    }

    /**
     * Function to close connection with database
     *
     * @param db database object you would to close
     */
    public void closeDB(SQLiteDatabase db) {
        if (db != null)
            db.close();
    }

    /**
     * Function to get all Qura'n by sora and other some information
     *
     * @return List of Sora
     */
    public List<Sora> getAllSora() {
        List<Sora> allQuranBySora = new ArrayList<Sora>();
        SQLiteDatabase db = openDB(MAIN_DATABASE);
        String sql = "select a.name, a.name_english, count(b.ayaid), joza, MIN(b.page), a.place " +
                "from sora a, aya b where b.soraid = a.soraid group by a.name order by a.soraid;";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            allQuranBySora.add(new Sora(cursor.getString(0),
                    cursor.getString(1),
                    (cursor.getInt(2) - 1),
                    cursor.getInt(4),
                    cursor.getInt(3),
                    cursor.getInt(5)));
            cursor.moveToNext();
        }
        cursor.close();
        closeDB(db);
        return allQuranBySora;
    }

    /**
     * Function to get all Qura'n by Quarter and other some information
     *
     * @return List of Quraters
     */
    public List<Quarter> getAllQuarters() {

        int counter = 1;
        List<Quarter> allQuranBySora = new ArrayList<Quarter>();
        SQLiteDatabase db = openDB(MAIN_DATABASE);
        String sql = "select b.name , b.name_english , a.soraid , a.page , a.text , a.hezb , a.quarter , a.joza , a.ayaid from aya a" +
                " , sora b where quarterstart = 1 and b.soraid = a.soraid order by a.soraid , a.ayaid ";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            allQuranBySora.add(new Quarter(cursor.getString(0),
                    cursor.getString(1),
                    cursor.getInt(2),
                    cursor.getInt(3),
                    cursor.getInt(5),
                    cursor.getInt(6),
                    cursor.getString(4),
                    cursor.getInt(8),
                    cursor.getInt(6) == 1 ? counter++ : 0,
                    cursor.getInt(7)));
            cursor.moveToNext();
        }
        cursor.close();
        closeDB(db);
        return allQuranBySora;
    }


       //getAllQuartersPages
    public List<QuarterPage> getAllQuartersPages() {

        int counter = 1;
        List<QuarterPage> allQuranBySora1 = new ArrayList<QuarterPage>();
        SQLiteDatabase db = openDB(MAIN_DATABASE);
        String sql = "select a.page from aya a" +
                " , sora b where quarterstart = 1 and b.soraid = a.soraid order by a.soraid , a.ayaid ";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            allQuranBySora1.add(new QuarterPage (cursor.getInt(0)));
            cursor.moveToNext();
        }
        cursor.close();
        closeDB(db);
        return allQuranBySora1;
    }

    /**
     * Function to get page information
     *
     * @param page Page number
     * @return Page object contain all information you need
     */
    public Page getPageInfo(int page) {
        Page pageInfo = null;
        SQLiteDatabase db = openDB(MAIN_DATABASE);
        String sql = "select distinct a.page , a.joza , min(b.name) , min(b.name_english), a.hezb " +
                "from aya a , sora b where a.soraid = b.soraid and a.page = " + page + " group by a.page;";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            pageInfo = new Page(cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getInt(4),
                    cursor.getString(2),
                    cursor.getString(3));
            cursor.moveToNext();
        }
        cursor.close();
        closeDB(db);
        return pageInfo;
    }

    /**
     * Function to get the part start page
     *
     * @param partNumber Part Number
     * @return Page number
     */
    public int getPartStartPage(int partNumber) {
        int pageNumber = 0;
        SQLiteDatabase db = openDB(MAIN_DATABASE);
        String sql = "select page from aya where joza = " + partNumber + " limit 1 ;";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            pageNumber = cursor.getInt(0);
            cursor.moveToNext();
        }
        cursor.close();
        closeDB(db);
        return pageNumber;
    }

    /**
     * Function to get page first aya id
     *
     * @param page Page number
     * @return Aya id
     */
    public Aya getPageStartAyaID(int page) {
        Aya aya = null;
        try {
            SQLiteDatabase db = openDB(MAIN_DATABASE);
            String sql = "select ayaid , soraid from aya where page = " + page + " and ayaid <> 0 limit 1 ;";
            Cursor cursor = db.rawQuery(sql, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                aya = new Aya(cursor.getInt(1), cursor.getInt(0));
                cursor.moveToNext();
            }
            cursor.close();
            closeDB(db);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return aya;
    }

    /**
     * Function to get all ayat of page
     *
     * @param page Page number
     * @return List of ayat
     */
    public List<Aya> getPageAyat(int page) {
        List<Aya> pageAyat = new ArrayList<Aya>();
        SQLiteDatabase db = openDB(MAIN_DATABASE);
        String sql = "select soraid , ayaid from aya where page = " + page + " and ayaid  is not 0  order by soraid,ayaid ;";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            pageAyat.add(new Aya(page,
                    cursor.getInt(0),
                    cursor.getInt(1)));
            cursor.moveToNext();
        }
        cursor.close();
        closeDB(db);
        return pageAyat;

    }

    /**
     * Function to get one aya tafser
     *
     * @param soraID Sora id
     * @param ayaID  Aya id
     * @param book   Tafseer book number
     * @return Aya information with tafseer
     */
    public AyaTafseer getAyaTafseer(int soraID, int ayaID, int book, String ayaText) {

        AyaTafseer ayaTafseer = null;
        SQLiteDatabase db = openDB(TAFSEER_DATABASE + "/tafseer" + book + ".sqlite");
        String sql = "select tafseer from ayatafseer where soraid = " + soraID + " and ayaid = " + ayaID + " ;";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            ayaTafseer = new AyaTafseer(soraID,
                    ayaID, cursor.getString(0).equals("") ? "لا يوجد تفسير" : cursor.getString(0),
                    ayaText);
            cursor.moveToNext();
        }
        cursor.close();
        closeDB(db);
        return ayaTafseer;
    }


    /**
     * Function to get aya position
     *
     * @param soraID Sora id
     * @param ayaID  Aya id
     * @return Position of aya in database
     */
    public int getAyaPosition(int soraID, int ayaID) {
        int position = 0;
        try {
            SQLiteDatabase db = openDB(MAIN_DATABASE);
            String sql = "select count(*) from aya where soraid < " + soraID + " and ayaid <> 0 ;";
            Cursor cursor = db.rawQuery(sql, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                position = cursor.getInt(0);
                cursor.moveToNext();
            }
            cursor.close();
            closeDB(db);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return position + (ayaID - 1);
    }


    /**
     * Function to get aya from position
     *
     * @param position Position of aya
     * @return Aya object
     */
    public Aya getAyaFromPosition(int position) {
        Aya aya = null;
        try {
            SQLiteDatabase db = openDB(MAIN_DATABASE);
            String sql = "select a.name , a.name_english , b.soraid , b.ayaid , b.page , b.text  from sora a , aya b  where b.ayaid <> 0 and a.soraid = b.soraid  limit " + position + ",1 ;";
            Cursor cursor = db.rawQuery(sql, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                aya = new Aya(cursor.getInt(4), cursor.getInt(3), cursor.getInt(2), cursor.getString(5), cursor.getString(0), cursor.getString(1));
                cursor.moveToNext();
            }
            cursor.close();
            closeDB(db);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return aya;
    }

    /**
     * Function to get all page ayaat tafseer
     *
     * @param page Page number
     * @param book Tafseer book number
     * @return Page Information with tfaseer
     */
    public PageTafseer getPageTafseer(int page, int book) {

        PageTafseer pageTafseer = null;
        List<AyaTafseer> ayaTafseer = new ArrayList<AyaTafseer>();
        String soraName = null, soraNameEnglish = null, ayaText = null;
        int jozaID = -1;
        int soraId = -1;
        SQLiteDatabase db = openDB(MAIN_DATABASE);

        String sql = "select b.name ,  b.name_english , a.page , a.soraid , a.ayaid , a.joza , a.text " +
                "from aya a , sora b where page = " + page + " and b.soraid = a.soraid order by a.soraid  ;";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            if (cursor.getInt(4) != 0) {
                soraName = cursor.getString(0);
                soraNameEnglish = cursor.getString(1);
                jozaID = cursor.getInt(5);
                ayaText = cursor.getString(6);
                ayaTafseer.add(getAyaTafseer(cursor.getInt(3),
                        cursor.getInt(4),
                        book,
                        ayaText));
            }
            cursor.moveToNext();
        }
        pageTafseer = new PageTafseer(soraName,
                soraNameEnglish,
                soraId, page,
                jozaID,
                ayaTafseer);

        cursor.close();
        closeDB(db);
        return pageTafseer;
    }

    /**
     * Function to get aya page number
     *
     * @param soraID  Sora id
     * @param verseID Verse id
     * @return Page number to open
     */
    public int getAyaPage(int soraID, int verseID) {
        int page = 0;
        try {
            SQLiteDatabase db = openDB(MAIN_DATABASE);
            String sql = "select page from aya where ayaid = " + verseID + " and soraid = " + soraID + " ;";
            Cursor cursor = db.rawQuery(sql, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                page = cursor.getInt(0);
                cursor.moveToNext();
            }
            cursor.close();
            closeDB(db);
            return page;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return page;
    }

    /**
     * Function to get one aya translation
     *
     * @param soraID Sora ID
     * @param ayaID  Aya ID
     * @param book   TranslationBook book number
     * @return Aya information with translation
     */
    public AyaTranslation getAyaTranslation(int soraID, int ayaID, int book) {

        AyaTranslation ayaTranslation = null;
        SQLiteDatabase db = openDB(TRANSLATION_DATABASE);
        String sql = "select text from ayatext where soraid = " + soraID + " and ayaid = " + ayaID + " and  translationid = " + book + ";";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            ayaTranslation = new AyaTranslation(soraID,
                    ayaID,
                    cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        closeDB(db);
        return ayaTranslation;
    }

    /**
     * Function to get all page ayaat translation
     *
     * @param page Page number
     * @param book TranslationBook book number
     * @return Page information with translation
     */
    public PageTranslation PageTranslation(int page, int book) {

        PageTranslation pageTranslation = null;
        List<AyaTranslation> ayaTranslations = new ArrayList<AyaTranslation>();
        String soraName = "";
        int jozaID = -1;
        int soraId = -1;
        SQLiteDatabase db = openDB(TRANSLATION_DATABASE);

        String sql = "select b.name , a.page , a.soraid , a.ayaid , a.joza " +
                "from aya a , sora b where page = " + page + " and b.soraid = a.soraid order by a.soraid  ;";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            soraName = cursor.getString(0);
            jozaID = cursor.getInt(4);
            ayaTranslations.add(getAyaTranslation(cursor.getInt(2),
                    cursor.getInt(3),
                    book));
            cursor.moveToNext();
        }
        pageTranslation = new PageTranslation(soraName,
                soraId,
                page,
                jozaID,
                ayaTranslations);

        cursor.close();
        closeDB(db);
        return pageTranslation;
    }

    /**
     * Function to get last bookmark id
     *
     * @return Bookmark id
     */
    private int getLastBookmark() {
        try {
            SQLiteDatabase db = openDB(MAIN_DATABASE);
            String sql = "select bookmarkid from bookmarks order by bookmarkid desc limit 1 ;";
            Cursor cursor = db.rawQuery(sql, null);
            cursor.moveToFirst();
            int newBookmarkID = -1;
            if (!cursor.isAfterLast()) {
                newBookmarkID = cursor.getInt(0);
                cursor.moveToNext();
            }

            cursor.close();
            closeDB(db);
            return newBookmarkID;
        } catch (Exception e) {
            return -1;
        }

    }

    /**
     * Function to add new bookmark
     *
     * @param page Page to bookmark
     * @return Flag success of not
     */
    public boolean bookmark(int page) {

        try {
            SQLiteDatabase db = openDB(MAIN_DATABASE);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateandTime = sdf.format(new Date());
            ContentValues row = new ContentValues();
            row.put("page",page);
            row.put("bookmarkTime",currentDateandTime);
            //  String sql = "insert into `bookmarks` (`page` , `bookmarkTime`) values (" + page + " , '" + currentDateandTime + "')";
            // db.execSQL(sql);
            boolean addRow =  db.insert("bookmarks",null,row)>0;
            closeDB(db);
            return addRow;
        } catch (Exception e) {
            return false;
        }
    }



    /**
     * Function to remove bookmark
     *
     * @return Flag success of not
     */
    public Boolean removeBookmark(int page) {

        try {
            Log.d("BOOKMARKID", page + "");
            SQLiteDatabase db = openDB(MAIN_DATABASE);
            boolean deleteItem = db.delete("bookmarks","`page` ="+page,null)>0;
            //  String sql = "Delete from `bookmarks` where  " + bookmarkID + ";";
            //    db.execSQL(sql);
            closeDB(db);
            return deleteItem;
        } catch (Exception e) {
            return false;
        }
    }
       // page is bookmarked or not
    public boolean isPageBookmarked(int page){
        SQLiteDatabase sqLiteDatabase = openDB(MAIN_DATABASE);
        Cursor cursor = sqLiteDatabase.rawQuery("select `page` from `bookmarks` where `page`="+page+" limit 1",null);
        boolean isBookmarked = false;
        if(cursor.getCount()>0){
            isBookmarked = true;
        }
        cursor.close();
        closeDB(sqLiteDatabase);
        return isBookmarked;
    }









    /**
     * Function to update bookmark
     *
     * @param bookmarkID
     * @param page
     * @return Flag success of not
     */
    public Boolean updateBookmark(int bookmarkID, int page) {

        try {
            SQLiteDatabase db = openDB(MAIN_DATABASE);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String currentDateandTime = sdf.format(new Date());
            String sql = "update bookmarks set page = " + page + " , bookmarkTime = '" + currentDateandTime + "' " +
                    " where bookmarkId = " + bookmarkID + " ; ";
            db.execSQL(sql);
            closeDB(db);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Function to get all book marks
     *
     * @return List of bookmarks
     */
    public List<Bookmark> getAllBookmarks() {

        List<Bookmark> allBookmarks = new ArrayList<Bookmark>();
        SQLiteDatabase db = openDB(MAIN_DATABASE);
        String sql = "Select * from bookmarks ; ";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            allBookmarks.add(new Bookmark(cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getString(1),
                    getPageInfo(cursor.getInt(1))));
            cursor.moveToNext();
        }

        cursor.close();
        closeDB(db);
        return allBookmarks;
    }



    /**
     * Function to search in quran
     *
     * @return List of aya have same search text
     */
    public List<Aya> quranSearch(String searchText) {

        List<Aya> ayas = new ArrayList<Aya>();
        try{
            SQLiteDatabase db = openDB(MAIN_DATABASE);
            String sql = "select a.page ,  a.ayaid , a.searchtext , b.name , b.name_english , a.soraid from aya a, " +
                    "sora  b where b.soraid = a.soraid and a.ayaid <> 0 and  a.searchtext like '%" + searchText + "%' order by a.page ;";
            Cursor cursor = db.rawQuery(sql, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                ayas.add(new Aya(cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getInt(5),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4)));
                cursor.moveToNext();
            }

            cursor.close();
            closeDB(db);
        }catch (Exception e){
            e.printStackTrace();
        }

        return ayas;
    }

    /**
     * Function to get aya rectangle
     *
     * @param pageNumber Page number
     * @param suraID     Sura ID
     * @param ayaID      aya ID
     * @return Aya Rectangle
     */
    public synchronized List<RectF> getAyaRectsMap(int pageNumber, int suraID, int ayaID) {

        Log.d("DATABASE", pageNumber + "--" + suraID + "--" + ayaID);
        int lastLine = 0;
        int newLine = 0;
        boolean strat = true;
        boolean unionFlage = true;
        SQLiteDatabase db = openDB(SELECTION_DATABASE);
        String sqlRect = "select minx , maxx , miny , maxy , line from ayarects where page = " + pageNumber + " and" +
                " soraid = " + suraID + " and  ayaid = " + ayaID + " ;";
        RectF mainRect = new RectF();
        List<RectF> rects = null;
        Cursor cursor = db.rawQuery(sqlRect, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            newLine = cursor.getInt(4);
            if (strat) {
                rects = new ArrayList<RectF>();
                lastLine = newLine;
                strat = false;
            }
            if (newLine != lastLine) {
                rects.add(mainRect);
                mainRect = new RectF();
                lastLine = newLine;
            }

            mainRect.union(new RectF(cursor.getInt(0), cursor.getInt(2),
                    cursor.getInt(1), cursor.getInt(3)));
            unionFlage = true;

            cursor.moveToNext();

        }
        if (unionFlage && !strat) rects.add(mainRect);
        Log.d("Rects", "rect");
        cursor.close();
        closeDB(db);

        return rects;
    }

    /**
     * Function to get aya touched rectangle dimensions and info
     *
     * @param pageNumber Page number
     * @param positionX  Touch X position
     * @param positionY  Touch Y position
     * @return Aya Selection
     */
    public Aya getTouchedAya(int pageNumber, float positionX, float positionY) {
        int suraID = -1;
        int ayaID = -1;
        SQLiteDatabase db = openDB(SELECTION_DATABASE);
        String sqlPosition = "select soraid , ayaid from ayarects where page = " +
                "" + pageNumber + " and minx <= " + positionX + " and maxx >= " +
                positionX + " and miny <= " + positionY + " and maxy >= " + positionY + " ;";
        Cursor cursor = db.rawQuery(sqlPosition, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            suraID = cursor.getInt(0);
            ayaID = cursor.getInt(1);
            cursor.moveToNext();
        }

        cursor.close();
        closeDB(db);

        return new Aya(pageNumber, suraID, ayaID, getAyaRectsMap(pageNumber, suraID, ayaID));
    }


    /**
     * Function to get all translation books , info and status
     *
     * @return List of translations
     */
    public List<TranslationBook> getAllTranslations() {
        List<TranslationBook> translationBooks = new ArrayList<TranslationBook>();
        SQLiteDatabase db = openDB(MAIN_DATABASE);
        String sql = "select * from tafaseer ;";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            translationBooks.add(new TranslationBook(cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2) == null ? "" : cursor.getString(2),
                    cursor.getInt(3),
                    false,
                    false));
            cursor.moveToNext();
        }

        cursor.close();
        closeDB(db);
        return translationBooks;
    }

    /**
     * Function to get all readers
     *
     * @return All reader info
     */
    public List<Reader> getAllReaders() {
        List<Reader> readers = new ArrayList<Reader>();
        SQLiteDatabase db = openDB(MAIN_DATABASE);
        String sql = "select * from audio ; ";
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            readers.add(new Reader(cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3),
                    cursor.getString(4)));
            cursor.moveToNext();
        }

        cursor.close();
        closeDB(db);
        return readers;
    }


    /**
     * Function to get sura name
     *
     * @param suraID Sura id
     * @return Object of sura name
     */
    public Sora getSuraNameByID(int suraID) {

        Sora sora = null;
        try {
            SQLiteDatabase db = openDB(MAIN_DATABASE);
            String sql = "select * from sora where soraid = " + suraID + " ; ";
            Cursor cursor = db.rawQuery(sql, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                sora = new Sora(cursor.getString(1), cursor.getString(2));
                cursor.moveToNext();
            }
            cursor.close();
            closeDB(db);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sora;

    }



      //function page contain quarter
    public int getPageQuarter(int pageNumber) {
        int Quarter = 0;
        try {
            SQLiteDatabase db = openDB(MAIN_DATABASE);
            String sql = "select quarter from aya where page = " + pageNumber + " ;";
            Cursor cursor = db.rawQuery(sql, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Quarter = cursor.getInt(0);
                cursor.moveToNext();
            }
            cursor.close();
            closeDB(db);
            return Quarter;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Quarter;
    }
    //function if page contain hezb
    public int getPageHezb(int pageNumber) {
        int hezbNumber = 0;
        try {
            SQLiteDatabase db = openDB(MAIN_DATABASE);
            String sql = "select hezb from aya where page = " + pageNumber + " ;";
            Cursor cursor = db.rawQuery(sql, null);

            if(cursor.moveToLast()){
                hezbNumber = cursor.getInt(0);
            }
            cursor.close();
            closeDB(db);
            return hezbNumber;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hezbNumber;
    }

}
