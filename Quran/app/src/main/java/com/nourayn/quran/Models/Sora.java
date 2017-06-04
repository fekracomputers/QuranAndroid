package com.nourayn.quran.Models;

/**
 * Model class for sora info.
 */
public class Sora {
    public String name, name_english;
    public int ayahCount, startPageNumber, jozaNumber, places;
    private String tag;

    /**
     * Constructor to create sora
     *
     * @param name            sora name
     * @param name_english    sora name in english
     * @param ayahCount       all sora ayah count
     * @param startPageNumber sora start page number
     * @param jozaNumber      part number
     */
    public Sora(String name, String name_english, int ayahCount, int startPageNumber, int jozaNumber, int places) {
        this.name = name;
        this.name_english = name_english;
        this.ayahCount = ayahCount;
        this.startPageNumber = startPageNumber;
        this.jozaNumber = jozaNumber;
        this.places = places;
    }


    /**
     * Constructor to create sora
     *
     * @param name         Sora name
     * @param name_english Sora Name in english
     */
    public Sora(String name, String name_english) {
        this.name = name;
        this.name_english = name_english;
    }

    /**
     * Function to set tag for sora model
     *
     * @param tag Text tag
     */
    public void setSoraTag(String tag) {
        this.tag = tag;
    }

    /**
     * Function to get tag for sora model
     *
     * @return Text tag
     */
    public String getSoraTag() {
        return tag;
    }

}
