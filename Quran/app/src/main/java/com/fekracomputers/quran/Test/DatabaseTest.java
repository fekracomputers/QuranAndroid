package com.fekracomputers.quran.Test;

import android.content.Context;
import android.widget.Toast;

import com.fekracomputers.quran.Database.DatabaseAccess;
import com.fekracomputers.quran.Models.AyaTafseer;
import com.fekracomputers.quran.Models.AyaTranslation;
import com.fekracomputers.quran.Models.Page;
import com.fekracomputers.quran.Models.PageTafseer;
import com.fekracomputers.quran.Models.PageTranslation;
import com.fekracomputers.quran.Models.Quarter;
import com.fekracomputers.quran.Models.Sora;

import java.util.List;

public class DatabaseTest {

    private Context context ;

    public DatabaseTest(Context context)
    {
        this.context = context ;
    }

    public void getAllSora()
    {
        long startTime = System.currentTimeMillis();
        List<Sora> Quran = new DatabaseAccess().getAllSora();
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        Toast.makeText(context , "getAllSora: List count :"+Quran.size() +" -- Time : " + elapsedTime +" ms" , Toast.LENGTH_LONG).show();
    }

    public void getAllQuarters()
    {
        long startTime = System.currentTimeMillis();
        List<Quarter> Quran = new DatabaseAccess().getAllQuarters();
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        Toast.makeText(context , "getAllQuarters: List count :"+Quran.size() +" -- Time : " + elapsedTime+" ms" , Toast.LENGTH_LONG).show();
    }

    public void getPageInfo()
    {
        long startTime = System.currentTimeMillis();
        Page page = new DatabaseAccess().getPageInfo(200);
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        Toast.makeText(context , "getPageInfo: Time : " + elapsedTime+" ms" , Toast.LENGTH_LONG).show();
    }

    public void getAyaTafseer()
    {
        long startTime = System.currentTimeMillis();
        AyaTafseer ayaTafseer = new DatabaseAccess().getAyaTafseer(2, 100, 1 , "");
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        Toast.makeText(context , "getAyaTafseer: Time : " + elapsedTime+" ms" , Toast.LENGTH_LONG).show();
    }

    public void PageTafseer()
    {
        long startTime = System.currentTimeMillis();
        PageTafseer pageTafseer = new DatabaseAccess().getPageTafseer(500, 1);
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        Toast.makeText(context , "PageTafseer: Time : " + elapsedTime+" ms" , Toast.LENGTH_LONG).show();
    }

    public void AyaTranslation()
    {
        long startTime = System.currentTimeMillis();
        AyaTranslation ayaTranslation = new DatabaseAccess().getAyaTranslation(5, 60, 3);
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        Toast.makeText(context , "AyaTranslation: Time : " + elapsedTime+" ms" , Toast.LENGTH_LONG).show();
    }

    public void PageTranslation()
    {
        long startTime = System.currentTimeMillis();
        PageTranslation ayaTranslation = new DatabaseAccess().PageTranslation(500, 2);
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        Toast.makeText(context , "PageTranslation: Time : " + elapsedTime+" ms" , Toast.LENGTH_LONG).show();
    }

    public static void doTest(final Context context)
    {
        DatabaseTest databaseTest = new DatabaseTest(context);
        try {
            databaseTest.getAllSora();
            Thread.sleep(1000);
            databaseTest.getAllQuarters();
            Thread.sleep(1000);
            databaseTest.getPageInfo();
            Thread.sleep(1000);
            databaseTest.getAyaTafseer();
            Thread.sleep(1000);
            databaseTest.PageTafseer();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
