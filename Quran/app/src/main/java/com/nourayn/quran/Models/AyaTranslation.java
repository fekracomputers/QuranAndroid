package com.nourayn.quran.Models;

/**
 * Model class aya translate
 */
public class AyaTranslation {
    public int soraID , ayaID ;
    public String translate ;

    /**
     * Constructor to create aya translate
     * @param soraID translated sora id
     * @param ayaID translated aya id
     * @param translate translation text
     */
    public AyaTranslation(int soraID, int ayaID, String translate)
    {
        this.soraID = soraID ;
        this.ayaID = ayaID ;
        this.translate = translate ;
    }

}
