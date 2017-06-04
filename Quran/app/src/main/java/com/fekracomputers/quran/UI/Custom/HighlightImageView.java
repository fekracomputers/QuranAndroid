package com.fekracomputers.quran.UI.Custom;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.fekracomputers.quran.Audio.service.AudioService;
import com.fekracomputers.quran.Database.AppPreference;
import com.fekracomputers.quran.Database.DatabaseAccess;
import com.fekracomputers.quran.Downloader.DownloadService;
import com.fekracomputers.quran.Models.Aya;
import com.fekracomputers.quran.Models.Sora;
import com.fekracomputers.quran.R;
import com.fekracomputers.quran.UI.Activities.QuranPageReadActivity;
import com.fekracomputers.quran.UI.Activities.TranslationReadActivity;
import com.fekracomputers.quran.UI.Fragments.QuranPageFragment;
import com.fekracomputers.quran.Utilities.AppConstants;
import com.fekracomputers.quran.Utilities.FileManager;
import com.fekracomputers.quran.Utilities.QuranValidateSources;
import com.fekracomputers.quran.Utilities.Settingsss;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom class for image view with Highlight
 */
public class HighlightImageView extends AppCompatImageView implements View.OnLongClickListener, View.OnTouchListener {
    public static boolean selectionFromTouch;
    public List<RectF> mRects;
    public static boolean inAnimation;
    private Context context;
    private Paint mPaint;
    private double bitmapH, bitmapW, canvasFirstX, canvasLastX, screenX, screenY, scaleRatio = 1.0;
    private PopupWindow popup = null;
    private View popupView = null;
    private int suraID, pageID, ayaID, xImageOffset = 0, yImageOffset = 0;
    private List<Aya> ayat;
    boolean mAdjustViewBounds;
    private  boolean isTabletDevice;
    /**
     * Highlight Image Constructor
     *
     * @param context Application context
     */
    public HighlightImageView(Context context) {
        super(context);
        isTabletDevice = isTablet(context);
        init();
    }

    public HighlightImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HighlightImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Function to adjust view bound to make image not stretch
     *
     * @param adjustViewBounds
     */
    @Override
    public void setAdjustViewBounds(boolean adjustViewBounds) {
        mAdjustViewBounds = adjustViewBounds;
        super.setAdjustViewBounds(adjustViewBounds);
    }

    /**
     * @param widthMeasureSpec  Measured width
     * @param heightMeasureSpec Measured height
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable mDrawable = getDrawable();
        if (mDrawable == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        if (mAdjustViewBounds) {
            int mDrawableWidth = mDrawable.getIntrinsicWidth();
            int mDrawableHeight = mDrawable.getIntrinsicHeight();
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);

            if (heightMode == MeasureSpec.EXACTLY && widthMode != MeasureSpec.EXACTLY) {
                // Fixed Height & Adjustable Width
                int height = heightSize;
                int width = height * mDrawableWidth / mDrawableHeight;
                if (isInScrollingContainer())
                    setMeasuredDimension(width, height);
                else
                    setMeasuredDimension(Math.min(width, widthSize), Math.min(height, heightSize));
            } else if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
                // Fixed Width & Adjustable Height
                int width = widthSize;
                int height = width * mDrawableHeight / mDrawableWidth;

                //calculate new ratios and scale
                calculateBitmapScale(width, height, mDrawableWidth, mDrawableHeight);

                if (isInScrollingContainer())
                    setMeasuredDimension(width, height);
                else
                    setMeasuredDimension(Math.min(width, widthSize), Math.min(height, heightSize));
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * Function to adjust in scroll
     *
     * @return Flag of scroll
     */
    private boolean isInScrollingContainer() {
        ViewParent p = getParent();
        while (p != null && p instanceof ViewGroup) {
            if (((ViewGroup) p).shouldDelayChildPressedState()) {
                return true;
            }
            p = p.getParent();
        }
        return false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setImageDrawable(null);
    }

    /**
     * Function to init image view settings
     */
    private void init() {
        this.context = super.getContext();
        mRects = new ArrayList<RectF>();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.argb(150, 146, 144, 248));
        setOnLongClickListener(this);
        setOnTouchListener(this);
    }

    /**
     * Function to set internal bitmap size
     *
     * @param bitmapH Bitmap height
     * @param bitmapW Bitmap width
     */
    public void InternalBitmapSize(double bitmapH, double bitmapW) {
        this.bitmapH = bitmapH;
        this.bitmapW = bitmapW;
    }


    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);

        Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        //calculate new ratios and scale
        calculateBitmapScale(size.x, size.y, bm.getWidth(), bm.getHeight());
    }


    /**
     * Function to calculate the bitmap scale and ratios
     *
     * @param displayWidthSize  display width
     * @param displayHeightSize Display height
     * @param bitmapWidthSize   Original bitmap width
     * @param bitmapHeightSize  Original bitmap height
     */
    private void calculateBitmapScale(int displayWidthSize, int displayHeightSize, int bitmapWidthSize, int bitmapHeightSize) {
        int sWidth = displayWidthSize;
        int sHeight = displayHeightSize;

        double sRatio = sWidth / ((double) sHeight);

        int iWidth = bitmapWidthSize;
        int iHeight = bitmapHeightSize;
        double iRatio = iWidth / ((double) iHeight);

        int newImageWidth = iWidth;
        int newImageHeight = iHeight;

        if (sRatio < iRatio) {
            scaleRatio = sWidth / ((double) iWidth);
            newImageWidth = (int) (iWidth * scaleRatio);
            newImageHeight = (int) (iHeight * scaleRatio);
        } else {
            scaleRatio = sHeight / ((double) iHeight);
            newImageWidth = (int) (iWidth * scaleRatio);
            newImageHeight = (int) (iHeight * scaleRatio);
        }
        xImageOffset = (sWidth - newImageWidth) / 2;
        yImageOffset = (sHeight - newImageHeight) / 2;
    }


    /**
     * Function fire in repaint
     *
     * @param canvas View canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //show popup in touch selection mood if there is no running activities
        if (mRects.size() != 0
                && selectionFromTouch
                && !Settingsss.isMyServiceRunning(context, AudioService.class)) {
            //check the display configuration
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                displayPopupWindow((int) (xImageOffset + (float) (mRects.get(0).left * scaleRatio))
//                        , (int) (yImageOffset + (float) (mRects.get(0).top * scaleRatio)));
                displayPopupWindow((int) (xImageOffset + (float) (mRects.get(mRects.size() - 1).left * scaleRatio))
                        , (int) (yImageOffset + (float) (mRects.get(mRects.size() - 1).bottom * scaleRatio)));
            } else {
                displayPopupWindow((int) (xImageOffset + (float) (mRects.get(mRects.size() - 1).left * scaleRatio))
                        , (int) (yImageOffset + (float) (mRects.get(mRects.size() - 1).bottom * scaleRatio)));
            }
        }

        //Draw rectangles on image
        for (RectF rect : mRects) {
            float rTop = yImageOffset + (float) (rect.top * scaleRatio);
            float rBottom = yImageOffset + (float) (rect.bottom * scaleRatio);
            float rLeft = xImageOffset + (float) (rect.left * scaleRatio);
            float rRight = xImageOffset + (float) (rect.right * scaleRatio);
            canvas.drawRect(new RectF(rLeft, rTop, rRight, rBottom), mPaint);
        }
    }

    /**
     * Function to show image without selection
     */
    public void resetImage() {
        if (popup != null) popup.dismiss();
        mRects.clear();
        invalidate();
    }

    /**
     * Function to scale screen ratios
     *
     * @return Scaled screen points
     */
    public Point screenToImagePoint() {
        int imageX = (int) ((screenX - xImageOffset) / scaleRatio);
        int imageY = (int) ((screenY - yImageOffset) / scaleRatio);
        return new Point(imageX, imageY);
    }

    /**
     * Function fire in long click in image view
     *
     * @param v View
     * @return Flag
     */
    @Override
    public boolean onLongClick(View v) {

        if (!Settingsss.isMyServiceRunning(context, AudioService.class)) {
            mRects.clear();
            selectionFromTouch = true;
            Point imagePoint = screenToImagePoint();
            Aya aya = new DatabaseAccess().getTouchedAya(QuranPageReadActivity.selectPage,
                    (int) imagePoint.x, (int) imagePoint.y);

            suraID = aya.suraID;
            pageID = aya.pageNumber;
            ayaID = aya.ayaID;

            if (aya.ayaRects != null) {
                mRects.addAll(aya.ayaRects);
                invalidate();
                //make selection mood true to disable header and footer hiding
                QuranPageFragment.SELECTION = true;
            }
        }

        return false;
    }

    /**
     * Function fire when touch image view
     *
     * @param v     View touched
     * @param event Touch event type
     * @return Flag
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        //stop image view touch while animate
        if (inAnimation == true) return true;

        //get the touch event
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                selectionFromTouch = true;
                canvasFirstX = event.getRawX();
                screenX = event.getX();
                screenY = event.getY();

                //get screen points
                Point imagePoint = screenToImagePoint();

                //start selection from touch mood where audio not work
                if (QuranPageFragment.SELECTION && !Settingsss.isMyServiceRunning(context, AudioService.class)) {

                    //get nearest aya touched
                    Aya aya = new DatabaseAccess().getTouchedAya(
                            QuranPageReadActivity.selectPage,
                            (int) imagePoint.x,
                            (int) imagePoint.y);

                    //extract aya information to use it public
                    suraID = aya.suraID;
                    pageID = aya.pageNumber;
                    ayaID = aya.ayaID;

                    //clear previous rectangle in new touch selection
                    if (aya.ayaRects != null) {
                        mRects.clear();
                        mRects.addAll(aya.ayaRects);
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                //hide or show bars
                if (!QuranPageFragment.SELECTION) {
                    canvasLastX = event.getRawX();
                    double deltaX = canvasLastX - canvasFirstX;
                    if (Math.abs(deltaX) < 50) {
                        inAnimation = true;
                        getRequiredActivity(this).showHideToolBar();
                    }
                }
                break;
        }

        return false;
    }

    private QuranPageReadActivity getRequiredActivity(View req_view) {
        Context context = req_view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof QuranPageReadActivity) {
                return (QuranPageReadActivity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    /**
     * Function to display popup in the to of selection
     *
     * @param left Left position of the rectangle
     * @param top  Top position of the rectangle
     */
    private void displayPopupWindow(int left, int top) {
        if (popup != null) popup.dismiss();
        popup = new PopupWindow(context);
        popupView = getRequiredActivity(this).getLayoutInflater().inflate(R.layout.popup_imageview, null);
        popup.setContentView(popupView);
        initPopup(popupView);
        if(isTabletDevice) {
            popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT + 50);
            popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT + 250);
        }else{
            popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT );
            popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT );
        }
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //popup.showAsDropDown(this, left, -top);
//            Toast.makeText(context, "totat Now", Toast.LENGTH_SHORT).show();
            popup.showAtLocation(this, Gravity.LEFT | Gravity.TOP, left, (top));
        } else {
            popup.showAtLocation(this, Gravity.LEFT | Gravity.TOP, left, (top));
        }

    }

    /**
     * @param popupView init popup buttons
     */
    private void initPopup(View popupView) {

        final ImageView play = (ImageView) popupView.findViewById(R.id.play);
        ImageView tafseer = (ImageView) popupView.findViewById(R.id.tafseer);
        ImageView share = (ImageView) popupView.findViewById(R.id.share);
        ImageView playFrom = (ImageView) popupView.findViewById(R.id.playfrom);
        final ImageView copy = (ImageView) popupView.findViewById(R.id.copy);

        //Play aya button
        play.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                //check if the internet is connected
                int internetStatus = Settingsss.checkInternetStatus(context);

                //check if there audio still running
                if (!Settingsss.isMyServiceRunning(context, AudioService.class)) {

                    if (AppPreference.isStreamMood()) {
                        //check if the internet is enable
                        if (internetStatus <= 0) {
                            android.support.v7.app.AlertDialog.Builder builder =
                                    new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
                            builder.setTitle(getResources().getString(R.string.Alert));
                            builder.setMessage(getResources().getString(R.string.no_internet_alert));
                            builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                            builder.show();
                        } else {
                            //streaming mood
                            ((QuranPageReadActivity) context).showFooter();
                            ((QuranPageReadActivity) context).showToolbar();
                            //audio intent
                            Intent player = new Intent(context, AudioService.class);
                            player.putExtra(AppConstants.MediaPlayer.PAGE, pageID);
                            player.putExtra(AppConstants.MediaPlayer.READER, QuranPageReadActivity.readerID);
                            player.putExtra(AppConstants.MediaPlayer.VERSE, ayaID);
                            player.putExtra(AppConstants.MediaPlayer.SURA, suraID);
                            player.putExtra(AppConstants.MediaPlayer.ONE_VERSE, ayaID);
                            player.putExtra(AppConstants.MediaPlayer.STREAM_LINK, QuranPageReadActivity.downloadLink);
                            context.startService(player);
                            popup.dismiss();

                            //make selection mood false to enable header and footer hiding
                            QuranPageFragment.SELECTION = false;
                        }
                    } else {
                        //check if there is other download in progress
                        if (!Settingsss.isMyServiceRunning(context, DownloadService.class)) {
                            //internal media play
                            String downloadLink = createDownloadLink(ayaID, suraID);
                            if (downloadLink != null) {
                                //check if the internet is connected
                                if (internetStatus <= 0) {
                                    android.support.v7.app.AlertDialog.Builder builder =
                                            new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
                                    builder.setTitle(getResources().getString(R.string.Alert));
                                    builder.setMessage(getResources().getString(R.string.no_internet_alert));
                                    builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                                    builder.show();
                                } else {
                                    //check audio folders
                                    String filePath = Environment
                                            .getExternalStorageDirectory()
                                            .getAbsolutePath()
                                            + context.getString(R.string.app_folder_path)
                                            + "/Audio/" + QuranPageReadActivity.readerID;

                                    //make dirs if not found
                                    File file = new File(filePath);
                                    if (!file.exists()) file.mkdirs();

                                    //start download service
                                    context.startService(new Intent(context, DownloadService.class)
                                            .putExtra(AppConstants.Download.DOWNLOAD_URL, downloadLink)
                                            .putExtra(AppConstants.Download.DOWNLOAD_LOCATION, filePath));

                                    //show toolbar and footer for the download
                                    ((QuranPageReadActivity) context).showFooter();
                                    ((QuranPageReadActivity) context).showToolbar();
                                }

                            } else {
                                List<String> filesLocations = new ArrayList<>();
                                //Create files locations for the all page ayas
                               /* for (Aya ayaItem : ayat) {
                                    filesLocations.add(FileManager.createAyaAudioLinkLocation(context, QuranPageReadActivity.readerID, ayaItem.ayaID, ayaItem.suraID));
                                }*/

                                //Start media player service
                                context.startService(new Intent(context, AudioService.class)
                                        .putExtra(AppConstants.MediaPlayer.PAGE, pageID)
                                        .putExtra(AppConstants.MediaPlayer.READER, QuranPageReadActivity.readerID)
                                        .putExtra(AppConstants.MediaPlayer.VERSE, ayaID)
                                        .putExtra(AppConstants.MediaPlayer.SURA, suraID)
                                        .putExtra(AppConstants.MediaPlayer.ONE_VERSE, ayaID));
                                ;
                                popup.dismiss();

                                //make selection mood false to enable header and footer hiding
                                QuranPageFragment.SELECTION = false;
                            }
                        } else {
                            //Other thing in download
                            Toast.makeText(context, context.getString(R.string.download_busy), Toast.LENGTH_SHORT).show();
                        }

                    }

                }


            }
        });

        //show tafseer of aya
        tafseer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //send intent to translation with aya information
                Intent tafseer = new Intent(context, TranslationReadActivity.class);
                tafseer.putExtra(AppConstants.Tafseer.AYA, ayaID);
                tafseer.putExtra(AppConstants.Tafseer.SORA, suraID);
                  int imagesResource=QuranPageReadActivity.hiii();
                tafseer.putExtra("imagesResource",""+imagesResource);
                Log.e("tag",""+imagesResource);

                context.startActivity(tafseer);
                ((Activity) context).finish();
                popup.dismiss();
            }
        });


        //share aya
        share.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                //get aya information
                DatabaseAccess db = new DatabaseAccess();
                Aya aya = db.getAyaFromPosition(db.getAyaPosition(suraID, ayaID));

                //share aya by sharing intent
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                Sora sora=db.getSuraNameByID(suraID);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, Settingsss.ChangeNumbers(context, "\""+aya.text +"\""+ ", " +ayaID+", "+sora.name + System.getProperty ("line.separator")+ "https://play.google.com/store/apps/details?id=com.fekracomputers.quran"));
                context.startActivity(Intent.createChooser(sharingIntent, "Share using"));

            }
        });


        //listener for play from her
        playFrom.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                //check if the internet is connected
                int internetStatus = Settingsss.checkInternetStatus(context);

                //get page aya list
                List<Aya> ayaList = new DatabaseAccess().getPageAyat(pageID);

                //get position of the aya you will start from
                int ayaPositionInPage = getAyaLocationInPage(ayaList, ayaID);

                if (AppPreference.isStreamMood()) {
                    //check internet is opened or not to start stream
                    if (internetStatus <= 0) {
                        android.support.v7.app.AlertDialog.Builder builder =
                                new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
                        builder.setTitle(getResources().getString(R.string.Alert));
                        builder.setMessage(getResources().getString(R.string.no_internet_alert));
                        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    } else {
                        //start stream mood
                        popup.dismiss();
                        Intent player = new Intent(context, AudioService.class);
                        player.putExtra(AppConstants.MediaPlayer.PAGE, pageID);
                        player.putExtra("streamLink", QuranPageReadActivity.downloadLink);
                        player.putExtra(AppConstants.MediaPlayer.READER, QuranPageReadActivity.readerID);
                        player.putExtra(AppConstants.MediaPlayer.VERSE, ayaPositionInPage);
                        context.startService(player);

                        //make selection mood false to enable header and footer hiding
                        QuranPageFragment.SELECTION = false;
                    }

                } else {
                    //check if there is other download in progress
                    if (!Settingsss.isMyServiceRunning(context, DownloadService.class)) {

                        //internal media play
                        List<String> Links = createDownloadLinks(ayaList);
                        if (Links.size() != 0) {

                            if (internetStatus <= 0) {
                                android.support.v7.app.AlertDialog.Builder builder =
                                        new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
                                builder.setTitle(getResources().getString(R.string.Alert));
                                builder.setMessage(getResources().getString(R.string.no_internet_alert));
                                builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                                builder.show();
                            } else {

                            }

                            //check audio folders
                            String filePath = Environment
                                    .getExternalStorageDirectory()
                                    .getAbsolutePath()
                                    + context.getString(R.string.app_folder_path)
                                    + "/Audio/" + QuranPageReadActivity.readerID;

                            //make dirs if not found
                            File file = new File(filePath);
                            if (!file.exists()) file.mkdirs();

                            //start download service
                            context.startService(new Intent(context, DownloadService.class)
                                    .putStringArrayListExtra(AppConstants.Download.DOWNLOAD_LINKS, (ArrayList<String>) Links)
                                    .putExtra(AppConstants.Download.DOWNLOAD_LOCATION, filePath));
                        } else {
                            List<String> filesLocations = new ArrayList<>();
                            //Create files locations for the all page ayas
                            for (Aya ayaItem : ayaList) {
                                filesLocations.add(FileManager.createAyaAudioLinkLocation(context, QuranPageReadActivity.readerID, ayaItem.ayaID, ayaItem.suraID));
                            }

                            //Start media player service
                            context.startService(new Intent(context, AudioService.class)
                                    .putExtra(AppConstants.MediaPlayer.PAGE, pageID)
                                    .putExtra(AppConstants.MediaPlayer.READER, QuranPageReadActivity.readerID)
                                    .putExtra(AppConstants.MediaPlayer.VERSE, ayaPositionInPage));

                            //make selection mood false to enable header and footer hiding
                            QuranPageFragment.SELECTION = false;

                            popup.dismiss();
                        }
                    } else {
                        //Other thing in download
                        Toast.makeText(context, context.getString(R.string.download_busy), Toast.LENGTH_SHORT).show();
                    }
                }
            }


        });


        //listener to copy aya
        copy.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //get aya information
                DatabaseAccess db = new DatabaseAccess();
                Aya aya = db.getAyaFromPosition(db.getAyaPosition(suraID, ayaID));

                //create new clip board
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                Sora sora=db.getSuraNameByID(suraID);
                android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", "\""+aya.text +"\""+ ", "+ayaID+", "+ sora.name+ ", "+getResources().getString(R.string.app_name)+","+" "+context.getString(R.string.nameodcompany));
                clipboard.setPrimaryClip(clip);

                //toast and dismiss popup
                Toast.makeText(context, context.getString(R.string.aya_copy), Toast.LENGTH_SHORT).show();
                popup.dismiss();
            }
        });


    }

    /**
     * Function to create download link
     */
    public String createDownloadLink(int aya, int sura) {

        //loop for all page ayat
        //validate if aya download or not
        if (!QuranValidateSources.validateAyaAudio(context, QuranPageReadActivity.readerID, aya, sura)) {

            //create aya link
            int suraLength = String.valueOf(sura).trim().length();
            String suraID = sura + "";
            int ayaLength = String.valueOf(aya).trim().length();
            String ayaID = aya + "";
            if (suraLength == 1)
                suraID = "00" + sura;
            else if (suraLength == 2)
                suraID = "0" + sura;

            if (ayaLength == 1)
                ayaID = "00" + aya;
            else if (ayaLength == 2)
                ayaID = "0" + aya;

            //add aya link to list
            Log.d("DownloadLinks", QuranPageReadActivity.downloadLink + suraID + ayaID + AppConstants.Extensions.MP3);
            return QuranPageReadActivity.downloadLink + suraID + ayaID + AppConstants.Extensions.MP3;

        }
        return null;
    }

    /**
     * Function to create download link
     */
    public List<String> createDownloadLinks(List<Aya> ayaList) {

        List<String> downloadLinks = new ArrayList<>();
        //loop for all page ayat
        for (Aya ayaItem : ayaList) {
            //validate if aya download or not
            if (!QuranValidateSources.validateAyaAudio(context, QuranPageReadActivity.readerID, ayaItem.ayaID, ayaItem.suraID)) {

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
                downloadLinks.add(QuranPageReadActivity.downloadLink + suraID + ayaID + AppConstants.Extensions.MP3);
            }

        }
        return downloadLinks;
    }

    /**
     * Function to get aya location in Page
     *
     * @return Aya id number
     */
    public int getAyaLocationInPage(List<Aya> ayaList, int ayaID) {
        int count = 0;
        for (Aya aya : ayaList) {
            count++;
            if (aya.ayaID == ayaID) {
                return count - 2;
            }
        }
        return -1;
    }
    private static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
}
