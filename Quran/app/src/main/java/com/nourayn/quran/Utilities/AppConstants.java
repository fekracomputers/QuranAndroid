package com.nourayn.quran.Utilities;

/**
 * Interface for the app constants
 */
public interface AppConstants {


    interface General {
        String SEARCH_TEXT = "SearchText",
                PAGE_NUMBER = "PageNumber",
                PAGE = "Page",
                BOOK_MARK = "bookmark",
                AYA_ID = "aya_id",
                SURA_ID = "sura_id";
    }

    /**
     * File and folder paths constants
     */
    interface Paths {
        String MAIN_DATABASE_PATH = "/Quran_nourayn/quran.sqlite",
                TAFSEER_DATABASE_PATH = "/Quran_nourayn/tafaseer",
                TAFSEER_LINK = "http://www.mindtrack.net/data/quran/tafaseer/tafseer";
        ;
    }

    /**
     * File extensions constants
     */
    interface Extensions {
        String MP3 = ".mp3",
                ZIP = ".zip",
                SQLITE = ".sqlite";
    }

    /**
     * Media player constants
     */
    interface MediaPlayer {
        String INTENT = "quranPageReadPlayer",
                PLAY = "play",
                PAUSE = "pause",
                STOP = "stop",
                RESUME = "resume",
                FORWARD = "forward",
                BACK = "back",
                REPEAT_ON = "repeatOn",
                REPEAT_OFF = "repeatOff",
                STREAM_LINK = "streamLink",
                AYAT = "ayat",
                LOCATIONS_LIST = "aya_list_locations",
                VERSE = "aya",
                PLAYING = "playing",
                OTHER_PAGE = "other_page",
                PAGE = "page",
                READER = "reader",
                ONE_VERSE = "one_verse",
                SURA = "sura";
    }

    /**
     * Download constants
     */
    interface Download {
        String INTENT = "DownloadStatusReciver",
                DOWNLOAD_URL = "download_url",
                DOWNLOAD_LOCATION = "download_location",
                DOWNLOAD = "download",
                SUCCESS = "success",
                FAILED = "failed",
                NUMBER = "Number",
                MAX = "max",
                IN_DOWNLOAD = "in download",
                IN_EXTRACT = "in extract",
                FILES = "Files",
                UNZIP = "unzipped",
                DOWNLOAD_LINKS = "download_links";
    }

    /**
     * Image highlight constants
     */
    interface Highlight {
        String INTENT = "HighlightAya",
                VERSE_NUMBER = "ayaNumber",
                SORA_NUMBER = "soraNumber",
                PAGE_NUMBER = "pageNumber",
                ARG_SECTION_NUMBER = "section_number",
                RESET_IMAGE = "RESETIMAGE",
                RESET = "reset",
                INTENT_FILTER = "Quran.mindtrack.image";
    }

    /**
     * applications preferences constants
     */
    interface Preferences {

        //download
        int DOWNLOAD_FAILED = 400,
                DOWNLOAD_SUCCESS = 200,
        //download types
        TAFSEER = 1,
                IMAGES = 2;

        //shared preference keys
        String CONFIG = "configurations",
                DOWNLOAD_STATUS = "download_status",
                DOWNLOAD_STATUS_TEXT = "download_status_text",
                DOWNLOAD_TYPE = "download_type",
                DOWNLOAD_ID = "download_id",
                LAST_PAGE_NUMBER = "last_page_number",
                SCREEN_RESOLUTION = "screen_resolution",
                VOLUME_NAVIGATION = "volume",
                LANGUAGE = "app_language",
                DEFAULT_EXPLANATION = "default_tafseer",
                ORIENTATION = "orientation",
                ARABIC_MOOD = "language",
                TRANSLATIONS = "translations",
                AYA_APPEAR = "aya",
                TRANSLATION_SIZE = "size",
                SELECT_VERSE = "select",
                STREAM = "stream";
    }


    /**
     * Tafseer constants
     */
    interface Tafseer {
        String INTENT = "tafseerMood",
                MOOD = "tafseer_mode",
                AYA = "aya",
                SORA = "sora";
    }


}
