package com.fekracomputers.quran.Models;

import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Model class for quran aya
 */
public class Aya implements Parcelable {
    public String text, name, nameEnglish;
    public int pageNumber, ayaID, suraID, partID;
    public List<RectF> ayaRects;

    /**
     * Constructor used when getting aya info
     *
     * @param pageNumber
     * @param ayaID
     * @param text
     * @param name
     * @param nameEnglish
     */
    public Aya(int pageNumber, int ayaID, int suraID, String text, String name, String nameEnglish) {

        this.pageNumber = pageNumber;
        this.ayaID = ayaID;
        this.suraID = suraID;
        this.text = text;
        this.name = name;
        this.nameEnglish = nameEnglish;
    }

    /**
     * Constructor for get aya small info
     * @param suraID Sura id
     * @param ayaID Aya id
     */
    public Aya(int suraID , int ayaID){
        this.suraID = suraID ;
        this.ayaID = ayaID ;
    }

    /**
     * Constructor used when getting aya selection
     *
     * @param pageNumber Pagenumber
     * @param suraID     Sura ID
     * @param ayaID      Aya ID
     * @param ayaRect    Aya positions
     */
    public Aya(int pageNumber, int suraID, int ayaID, List<RectF> ayaRect) {
        this.pageNumber = pageNumber;
        this.suraID = suraID;
        this.ayaID = ayaID;
        this.ayaRects = ayaRect;
    }

    /**
     * Constructor to get aya mini info
     *
     * @param pageNumber Page number
     * @param suraID     Sura Id
     * @param ayaID      Aya ID
     */
    public Aya(int pageNumber, int suraID, int ayaID) {
        this.pageNumber = pageNumber;
        this.suraID = suraID;
        this.ayaID = ayaID;
    }


    protected Aya(Parcel in) {
        text = in.readString();
        name = in.readString();
        nameEnglish = in.readString();
        pageNumber = in.readInt();
        ayaID = in.readInt();
        suraID = in.readInt();
        ayaRects = in.createTypedArrayList(RectF.CREATOR);
    }

    public static final Creator<Aya> CREATOR = new Creator<Aya>() {
        @Override
        public Aya createFromParcel(Parcel in) {
            return new Aya(in);
        }

        @Override
        public Aya[] newArray(int size) {
            return new Aya[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeString(name);
        dest.writeString(nameEnglish);
        dest.writeInt(pageNumber);
        dest.writeInt(ayaID);
        dest.writeInt(suraID);
        dest.writeTypedList(ayaRects);
    }
}
