package com.fekracomputers.quran.Audio.helper;

import android.content.Context;
import android.util.Log;

import com.fekracomputers.quran.Audio.service.AudioManager;
import com.fekracomputers.quran.Models.Aya;
import com.fekracomputers.quran.Utilities.AppConstants;
import com.fekracomputers.quran.Utilities.QuranValidateSources;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper functions for the audio class
 */
public class AudioHelper {

    /**
     * Function to create stream link
     *
     * @param aya Aya info object
     * @return streaming link
     */
    public static synchronized String createStreamLink(Aya aya , String streamURL) {
        int suraNumber = String.valueOf(aya.suraID).length();
        String suraID = aya.suraID + "";

        if (suraNumber == 1)
            suraID = "00" + aya.suraID;
        else if (suraNumber == 2)
            suraID = "0" + aya.suraID;

        int ayaNumber = String.valueOf(aya.ayaID).length();
        String ayaID = aya.ayaID + "";

        if (ayaNumber == 1)
            ayaID = "00" + aya.ayaID;
        else if (ayaNumber == 2)
            ayaID = "0" + aya.ayaID;

        String link = streamURL + suraID + ayaID + AppConstants.Extensions.MP3;

        Log.e(AudioHelper.class.getSimpleName(), "streamURL : " + link);
        return link;
    }


    /**
     * Function to create download link
     */
    public List<String> createDownloadLinks(Context context ,  List<Aya> ayaList , String downloadLink , int readerID) {

        List<String> downloadLinks = new ArrayList<>();
        ayaList.add(0 , new Aya(1,1,1));
        //loop for all page ayat
        for (Aya ayaItem : ayaList) {
            //validate if aya download or not
            if (!QuranValidateSources.validateAyaAudio(context, readerID, ayaItem.ayaID, ayaItem.suraID)) {

                //create aya link
                int suraLength = String.valueOf(ayaItem.suraID).trim().length();
                String suraID = ayaItem.suraID + "";
                int ayaLength = String.valueOf(ayaItem.ayaID).trim().length();
                String ayaID = ayaItem.ayaID + "";
                if (suraLength == 1)
                    suraID = "00" + ayaItem.suraID;
                else if (suraLength == 2)
                    suraID = "0" + ayaItem.suraID;

                if (ayaLength == 1)
                    ayaID = "00" + ayaItem.ayaID;
                else if (ayaLength == 2)
                    ayaID = "0" + ayaItem.ayaID;

                //add aya link to list
                downloadLinks.add(downloadLink + suraID + ayaID + AppConstants.Extensions.MP3);
                Log.d("DownloadLinks", downloadLink + suraID + ayaID + AppConstants.Extensions.MP3);
            }
        }
        ayaList.remove(0);
        return downloadLinks;
    }

}
