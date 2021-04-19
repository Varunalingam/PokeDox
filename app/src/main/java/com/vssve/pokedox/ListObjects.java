package com.vssve.pokedox;

import android.animation.ValueAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;
import androidx.palette.graphics.Palette;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


class PokemonObj extends View {

    public int ForegroundC,ShadowC,BackgroundC,FavouriteC,AccentC;
    public boolean isFavourite;
    public boolean addedtofavourites;

    public DBHelper favdata;

    float favouriteoffset;

    Paint TextColor,RippleColor,ImageColor,FavouriteColor,FavouriteImageC;
    String DText,url;

    int Radius;

    float iposx,iposy;

    float xoffset,ripplex;

    float dps,sps;

    Bitmap Current_Image,AFavourite_Image,Favourite_Image;

    boolean isImageLoaded;

    ImageTask A = null;
    String ImageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/";

    public PokemonObj(Context context ,String DText,String url,int BackgroundColor,int ForegroundColor,int ShadowColor,int FavouriteColor) {
        super(context);
        this.DText = DText;
        this.url = url;
        this.BackgroundC = BackgroundColor;
        this.ForegroundC = ForegroundColor;
        this.ShadowC = ShadowColor;
        this.FavouriteC = FavouriteColor;
        this.AccentC = FavouriteColor;

        intialize();

        ColorFilter A = new LightingColorFilter(FavouriteC,1);
        this.ImageColor.setColorFilter(A);

        favdata = new DBHelper(context);
        if (favdata.isFavourite(DText.toLowerCase()))
        {
            addedtofavourites = true;
            isFavourite = true;
            xoffset = favouriteoffset;
            FavouriteImageC.setAlpha(255);
        }

        ImageUrl += url.split("/")[url.split("/").length - 1] + ".png";
        this.A = new ImageTask(getContext());
        this.A.execute(ImageUrl);
        RequestImage();
    }

    void intialize()
    {
        dps = getResources().getDisplayMetrics().density;
        sps = getResources().getDisplayMetrics().scaledDensity;

        Current_Image = ((BitmapDrawable)getResources().getDrawable(R.drawable.pokemon_imageloading)).getBitmap();

        AFavourite_Image = ((BitmapDrawable)getResources().getDrawable(R.drawable.afavourites)).getBitmap();
        Favourite_Image = ((BitmapDrawable)getResources().getDrawable(R.drawable.favourites)).getBitmap();

        favouriteoffset = dps * 75;

        Typeface PokemonFont = ResourcesCompat.getFont(getContext(),R.font.pokemon_solid);

        FavouriteColor = new Paint();
        FavouriteColor.setColor(FavouriteC);
        FavouriteColor.setStrokeWidth(2);

        FavouriteImageC = new Paint();
        FavouriteImageC.setAlpha(0);

        TextColor = new Paint();
        TextColor.setTextSize(sps * 32);
        TextColor.setLetterSpacing(0.2f);
        TextColor.setColor(ForegroundC);

        TextColor.setTypeface(PokemonFont);

        RippleColor = new Paint();
        RippleColor.setColor(ShadowC);

        ImageColor = new Paint();
        ImageColor.setAlpha(255);

        setBackgroundColor(BackgroundC);

        Favourite_Image = Bitmap.createScaledBitmap(Favourite_Image,(int)favouriteoffset/4 * 3,(int) favouriteoffset/4 * 3,false);
        AFavourite_Image = Bitmap.createScaledBitmap(AFavourite_Image,(int)favouriteoffset/4 * 3,(int) favouriteoffset/4 * 3,false);

        LoadingImageAnim();
    }

    public void LoadingImageAnim()
    {
        if (!isImageLoaded)
        {
            ValueAnimator ImageAnim = ValueAnimator.ofInt(64,192);
            ImageAnim.setDuration(500);
            ImageAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    ImageColor.setAlpha((int)animation.getAnimatedValue());
                    if ((int)animation.getAnimatedValue() == 192)
                    {
                        LoadingImageAnim();
                    }
                    invalidate();
                }
            });
            ImageAnim.start();
        }
        else
        {
            ImageColor.setAlpha(255);
            ImageColor.setColorFilter(null);
        }
    }

    public void RequestImage()
    {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (A.ConnectionState == null)
                {
                    RequestImage();
                }
                else
                {
                    isImageLoaded = true;
                    if (A.Image == null)
                    {

                    }
                    else
                    {
                        Current_Image = A.Image;
                        Palette.generateAsync(Current_Image, new Palette.PaletteAsyncListener() {
                            public void onGenerated(Palette palette) {
                                AccentC = palette.getDominantColor(AccentC);
                                invalidate();
                            }
                        });
                    }
                }
            }
        },10);
    }

    @Override
    public void onDraw(Canvas c)
    {
        boolean changedfav = favdata.isFavourite(DText.toLowerCase());
        if (changedfav != addedtofavourites)
        {
            addedtofavourites = isFavourite = changedfav;
            if (addedtofavourites == false)
            {
                xoffset = 0;
                FavouriteImageC.setAlpha(0);
            }
            else
            {
                xoffset = favouriteoffset;
                FavouriteImageC.setAlpha(255);
            }
        }

        if ( (Current_Image.getScaledHeight(c)>Current_Image.getScaledWidth(c)?Current_Image.getScaledHeight(c):Current_Image.getScaledWidth(c)) != 3*getHeight()/4 && getWidth() > 0 && getHeight() > 0)
        {
            int Width,Height;
            if (Current_Image.getScaledHeight(c)>Current_Image.getScaledWidth(c))
            {
                Width = (int) ((float)(3*getHeight()/4/ (float)Current_Image.getScaledHeight(c) )* Current_Image.getScaledWidth(c));
                Height = 3*getHeight()/4;
            }
            else
            {
                Height = (int) ((float)((3*getHeight()/4) / (float)Current_Image.getScaledWidth(c)) * Current_Image.getScaledHeight(c));
                Width = 3*getHeight()/4;
            }
            Current_Image = Bitmap.createScaledBitmap(Current_Image,Width,Height,false);
        }

        if (Radius > 0)
        {
            c.drawCircle(xoffset + ripplex,getHeight()/2,Radius,RippleColor);
        }

        while (TextColor.measureText(DText) > getWidth() - getHeight() - (10*dps) && getWidth() > 0 && getHeight() > 0)
        {
            TextColor.setTextSize(TextColor.getTextSize() - 1);
        }

        Shader textShader=new LinearGradient(0, 0, TextColor.measureText(DText), TextColor.getTextSize(),
                new int[]{AccentC,ForegroundC},
                new float[]{0.7f,1f}, Shader.TileMode.CLAMP);

        TextColor.setShader(textShader);
        c.drawText(DText,getHeight() + xoffset, getHeight()/2 - ((TextColor.ascent() + TextColor.descent())/2),TextColor);

        c.drawBitmap(Current_Image,xoffset + getHeight()/8,getHeight()/8,ImageColor);

        c.drawRect(new Rect(0,0,(int) xoffset,getHeight()),FavouriteColor);
        c.drawLine(0,getHeight(),getWidth(),getHeight(),FavouriteColor);



        if (addedtofavourites)
            c.drawBitmap(Favourite_Image,favouriteoffset/8,(getHeight()-(favouriteoffset/4 * 3))/2,FavouriteImageC);
        else
            c.drawBitmap(AFavourite_Image,favouriteoffset/8,(getHeight()-(favouriteoffset/4 * 3))/2,FavouriteImageC);
    }

    public void BgRipple()
    {
        ripplex = iposx;
        ValueAnimator A = ValueAnimator.ofInt(getHeight()/2,(int) Math.sqrt(Math.pow(getWidth() + getHeight(),2))/2 );
        A.setDuration(250);
        A.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Radius = (int)animation.getAnimatedValue();
                invalidate();
                if (Radius == (int) Math.sqrt(Math.pow(getWidth() + getHeight(),2))/2)
                {
                    Radius = 0;
                }
            }
        });
        A.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                iposx = event.getX();
                iposy = event.getY();
                BgRipple();
                return true;

            case MotionEvent.ACTION_MOVE:
                if (iposx + favouriteoffset < event.getX() && !isFavourite)
                {
                    xoffset = favouriteoffset;
                    iposx = event.getX();
                    isFavourite = true;
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                else if (iposx < event.getX() && !isFavourite)
                {
                    if (xoffset == 0 && iposx + (favouriteoffset/10) < event.getX())
                    {
                        xoffset = (event.getX() - iposx);
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    else
                    {
                        xoffset = (event.getX() - iposx);
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
                else if (isFavourite && iposx - favouriteoffset > event.getX())
                {
                    xoffset = 0;
                    iposx = event.getX();
                    isFavourite = false;
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                else if (isFavourite && iposx >= event.getX())
                {
                    if (xoffset == favouriteoffset && iposx - (favouriteoffset/10) > event.getX())
                    {
                        xoffset = favouriteoffset - (iposx - event.getX());
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    else
                    {
                        xoffset = favouriteoffset - (iposx - event.getX());
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
                else
                {
                    if(isFavourite) {
                        xoffset = favouriteoffset;
                    }
                    else
                    {
                        xoffset = 0;
                    }
                }

                if (xoffset == favouriteoffset)
                    FavouriteImageC.setAlpha(255);
                else
                    FavouriteImageC.setAlpha(0);

                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
                if (iposx + favouriteoffset < event.getX() && !isFavourite)
                {
                    xoffset = favouriteoffset;
                    isFavourite = true;
                }
                else if (!isFavourite)
                {
                    xoffset = 0;
                    isFavourite = false;
                }
                else if (xoffset < favouriteoffset)
                {
                    xoffset = 0;
                    isFavourite = false;
                }

                if (!addedtofavourites && isFavourite)
                {
                    Toast.makeText(getContext().getApplicationContext(),"Added " + DText +" to Favourites",Toast.LENGTH_SHORT).show();
                    addedtofavourites = true;
                    favdata.insertFavourite(DText.toLowerCase());
                }
                else if (addedtofavourites && !isFavourite)
                {
                    Toast.makeText(getContext().getApplicationContext(),"Removed "+ DText+" From Favourites",Toast.LENGTH_SHORT).show();
                    addedtofavourites = false;
                    favdata.deleteFavourite(DText.toLowerCase());
                }
                else if (Math.abs(iposx-event.getX()) < favouriteoffset/10 && Math.abs(event.getY() - (getHeight()/2)) < getHeight()/2)
                {
                    Intent I = new Intent(getContext(),PokemonInfo.class);
                    I.putExtra("URL",url);
                    getContext().startActivity(I);
                }

                getParent().requestDisallowInterceptTouchEvent(false);
                invalidate();

                return true;

            case MotionEvent.ACTION_CANCEL:
                if (!addedtofavourites && isFavourite)
                {
                    Toast.makeText(getContext().getApplicationContext(),"Added " + DText +" to Favourites",Toast.LENGTH_SHORT).show();
                    addedtofavourites = true;
                    favdata.insertFavourite(DText.toLowerCase());
                }
                else if (addedtofavourites && !isFavourite)
                {
                    Toast.makeText(getContext().getApplicationContext(),"Removed "+ DText+" From Favourites",Toast.LENGTH_SHORT).show();
                    addedtofavourites = false;
                    favdata.deleteFavourite(DText.toLowerCase());
                }
                iposx = -1;
                iposy = -1;
                invalidate();
        }
        return super.onTouchEvent(event);
    }
}

class ListObj extends View {

    public int ForegroundC,ShadowC,BackgroundC,FavouriteC;

    Paint TextColor,RippleColor,ImageColor,FavouriteColor,FavouriteImageC;

    String DText,url;

    int Radius;

    float iposx,iposy;

    float ripplex;

    float dps,sps;

    Bitmap Current_Image;

    boolean isImageLoaded;

    ImageTask A = null;
    String ImageUrl ;

    Intent I;

    public ListObj(Context context ,String DText,String url,int BackgroundColor,int ForegroundColor,int ShadowColor,int FavouriteColor,String ImageURL,Intent i) {
        super(context);
        this.DText = DText;
        this.url = url;
        this.BackgroundC = BackgroundColor;
        this.ForegroundC = ForegroundColor;
        this.ShadowC = ShadowColor;
        this.FavouriteC = FavouriteColor;
        this.ImageUrl = ImageURL;
        this.I = i;
        intialize();

        this.DText = this.DText.replace("-"," ");

        ColorFilter A = new LightingColorFilter(FavouriteC,1);
        this.ImageColor.setColorFilter(A);

        if (ImageURL != "") {
            this.A = new ImageTask(getContext());
            this.A.execute(ImageUrl);
            RequestImage();
        }
    }

    void intialize()
    {
        dps = getResources().getDisplayMetrics().density;
        sps = getResources().getDisplayMetrics().scaledDensity;

        if (ImageUrl != "")
        Current_Image = ((BitmapDrawable)getResources().getDrawable(R.drawable.pokemon_imageloading)).getBitmap();

        Typeface PokemonFont = ResourcesCompat.getFont(getContext(),R.font.pokemon_solid);

        FavouriteColor = new Paint();
        FavouriteColor.setColor(FavouriteC);
        FavouriteColor.setStrokeWidth(2);

        FavouriteImageC = new Paint();
        FavouriteImageC.setAlpha(0);

        TextColor = new Paint();
        TextColor.setTextSize(sps * 32);
        TextColor.setLetterSpacing(0.2f);
        TextColor.setColor(ForegroundC);

        TextColor.setTypeface(PokemonFont);

        RippleColor = new Paint();
        RippleColor.setColor(ShadowC);

        ImageColor = new Paint();
        ImageColor.setAlpha(255);

        setBackgroundColor(BackgroundC);

        if (ImageUrl != "")
        LoadingImageAnim();
    }

    public void LoadingImageAnim()
    {
        if (!isImageLoaded)
        {
            ValueAnimator ImageAnim = ValueAnimator.ofInt(64,192);
            ImageAnim.setDuration(500);
            ImageAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    ImageColor.setAlpha((int)animation.getAnimatedValue());
                    if ((int)animation.getAnimatedValue() == 192)
                    {
                        LoadingImageAnim();
                    }
                    invalidate();
                }
            });
            ImageAnim.start();
        }
        else
        {
            ImageColor.setAlpha(255);
            ImageColor.setColorFilter(null);
        }
    }

    public void RequestImage()
    {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (A.ConnectionState == null)
                {
                    RequestImage();
                }
                else
                {
                    isImageLoaded = true;
                    if (A.Image == null)
                    {

                    }
                    else
                    {
                        Current_Image = A.Image;
                        invalidate();
                    }
                }
            }
        },10);
    }

    @Override
    public void onDraw(Canvas c)
    {
        if (ImageUrl!= "") {
            if ((Current_Image.getScaledHeight(c) > Current_Image.getScaledWidth(c) ? Current_Image.getScaledHeight(c) : Current_Image.getScaledWidth(c)) != 3 * getHeight() / 4 && getWidth() > 0 && getHeight() > 0) {
                int Width, Height;
                if (Current_Image.getScaledHeight(c) > Current_Image.getScaledWidth(c)) {
                    Width = (int) ((float) (3 * getHeight() / 4 / (float) Current_Image.getScaledHeight(c)) * Current_Image.getScaledWidth(c));
                    Height = 3 * getHeight() / 4;
                } else {
                    Height = (int) ((float) ((3 * getHeight() / 4) / (float) Current_Image.getScaledWidth(c)) * Current_Image.getScaledHeight(c));
                    Width = 3 * getHeight() / 4;
                }
                Current_Image = Bitmap.createScaledBitmap(Current_Image, Width, Height, false);
            }
        }

        if (Radius > 0)
        {
            c.drawCircle(ripplex,getHeight()/2,Radius,RippleColor);
        }

        if (ImageUrl == "")
        {
            while (TextColor.measureText(DText) > getWidth() - (20*dps) && getWidth() > 0 && getHeight() > 0)
            {
                TextColor.setTextSize(TextColor.getTextSize() - 1);
            }
        }
        else
        {
            while (TextColor.measureText(DText) > getWidth() - getHeight() - (10*dps) && getWidth() > 0 && getHeight() > 0)
            {
                TextColor.setTextSize(TextColor.getTextSize() - 1);
            }
        }

        c.drawText(DText,ImageUrl!=""?getHeight():5*dps, getHeight()/2 - ((TextColor.ascent() + TextColor.descent())/2),TextColor);

        if (ImageUrl!="")
        c.drawBitmap(Current_Image,getHeight()/8,getHeight()/8,ImageColor);

        c.drawLine(0,getHeight(),getWidth(),getHeight(),FavouriteColor);
    }

    public void BgRipple()
    {
        ripplex = iposx;
        ValueAnimator A = ValueAnimator.ofInt(getHeight()/2,(int) Math.sqrt(Math.pow(getWidth() + getHeight(),2))/2 );
        A.setDuration(250);
        A.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Radius = (int)animation.getAnimatedValue();
                invalidate();
                if (Radius == (int) Math.sqrt(Math.pow(getWidth() + getHeight(),2))/2)
                {
                    Radius = 0;
                }
            }
        });
        A.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                iposx = event.getX();
                iposy = event.getY();
                BgRipple();
                return true;

            case MotionEvent.ACTION_UP:
                if (Math.abs(event.getX() - getWidth()/2) < getWidth()/2 && Math.abs(event.getY() - getHeight()/2) < getHeight()/2)
                    getContext().startActivity(I);
                invalidate();

                return true;
        }
        return super.onTouchEvent(event);
    }
}

class ListTypeObj extends View {

    public int ForegroundC,ShadowC,BackgroundC,FavouriteC;

    Paint TextColor,RippleColor,ImageColor,FavouriteColor,FavouriteImageC;

    String DText,url;

    int Radius;

    float iposx,iposy;

    float ripplex;

    float dps,sps;

    Bitmap Current_Image;

    Intent I;

    public ListTypeObj(Context context ,String DText,String url,int BackgroundColor,int ForegroundColor,int ShadowColor,int FavouriteColor,Bitmap Image,Intent i) {
        super(context);
        this.DText = DText;
        this.url = url;
        this.BackgroundC = BackgroundColor;
        this.ForegroundC = ForegroundColor;
        this.ShadowC = ShadowColor;
        this.FavouriteC = FavouriteColor;
        this.I = i;
        if (Image != null)
        {
            Current_Image = Image;
        }
        intialize();

        this.DText = this.DText.replace("-"," ");
    }

    void intialize()
    {
        dps = getResources().getDisplayMetrics().density;
        sps = getResources().getDisplayMetrics().scaledDensity;

        Typeface PokemonFont = ResourcesCompat.getFont(getContext(),R.font.pokemon_solid);

        FavouriteColor = new Paint();
        FavouriteColor.setColor(FavouriteC);
        FavouriteColor.setStrokeWidth(2);

        FavouriteImageC = new Paint();
        FavouriteImageC.setAlpha(0);

        TextColor = new Paint();
        TextColor.setTextSize(sps * 32);
        TextColor.setLetterSpacing(0.2f);
        TextColor.setColor(ForegroundC);

        TextColor.setTypeface(PokemonFont);

        RippleColor = new Paint();
        RippleColor.setColor(ShadowC);

        ImageColor = new Paint();
        ImageColor.setAlpha(255);

        setBackgroundColor(BackgroundC);
    }


    @Override
    public void onDraw(Canvas c)
    {
        if (Current_Image != null) {
            if ((Current_Image.getScaledHeight(c) > Current_Image.getScaledWidth(c) ? Current_Image.getScaledHeight(c) : Current_Image.getScaledWidth(c)) != 3 * getHeight() / 4 && getWidth() > 0 && getHeight() > 0) {
                int Width, Height;
                if (Current_Image.getScaledHeight(c) > Current_Image.getScaledWidth(c)) {
                    Width = (int) ((float) (3 * getHeight() / 4 / (float) Current_Image.getScaledHeight(c)) * Current_Image.getScaledWidth(c));
                    Height = 3 * getHeight() / 4;
                } else {
                    Height = (int) ((float) ((3 * getHeight() / 4) / (float) Current_Image.getScaledWidth(c)) * Current_Image.getScaledHeight(c));
                    Width = 3 * getHeight() / 4;
                }
                Current_Image = Bitmap.createScaledBitmap(Current_Image, Width, Height, false);
            }
        }

        if (Radius > 0)
        {
            c.drawCircle(ripplex,getHeight()/2,Radius,RippleColor);
        }

        if (Current_Image == null)
        {
            while (TextColor.measureText(DText) > getWidth() - (20*dps) && getWidth() > 0 && getHeight() > 0)
            {
                TextColor.setTextSize(TextColor.getTextSize() - 1);
            }
        }
        else
        {
            while (TextColor.measureText(DText) > getWidth() - getHeight() - (10*dps) && getWidth() > 0 && getHeight() > 0)
            {
                TextColor.setTextSize(TextColor.getTextSize() - 1);
            }
        }

        c.drawText(DText,Current_Image!=null?getHeight():5*dps, getHeight()/2 - ((TextColor.ascent() + TextColor.descent())/2),TextColor);

        if (Current_Image!=null)
            c.drawBitmap(Current_Image,getHeight()/8,getHeight()/8,ImageColor);

        c.drawLine(0,getHeight(),getWidth(),getHeight(),FavouriteColor);
    }

    public void BgRipple()
    {
        ripplex = iposx;
        ValueAnimator A = ValueAnimator.ofInt(getHeight()/2,(int) Math.sqrt(Math.pow(getWidth() + getHeight(),2))/2 );
        A.setDuration(250);
        A.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Radius = (int)animation.getAnimatedValue();
                invalidate();
                if (Radius == (int) Math.sqrt(Math.pow(getWidth() + getHeight(),2))/2)
                {
                    Radius = 0;
                }
            }
        });
        A.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                iposx = event.getX();
                iposy = event.getY();
                BgRipple();
                return true;

            case MotionEvent.ACTION_UP:
                if (Math.abs(event.getX() - getWidth()/2) < getWidth()/2 && Math.abs(event.getY() - getHeight()/2) < getHeight()/2)
                    getContext().startActivity(I);
                invalidate();

                return true;
        }
        return super.onTouchEvent(event);
    }
}

class PokemonEvoObj extends View {

    public int ForegroundC,ShadowC,BackgroundC,AccentC;

    Paint TextColor,RippleColor,ImageColor;
    String DText,url;

    int Radius;

    float iposx,iposy;

    float ripplex;

    float dps,sps;

    Bitmap Current_Image;

    boolean isImageLoaded;

    ImageTask A = null;
    String ImageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/";

    public PokemonEvoObj(Context context,String DText,String url,int BackgroundColor,int ForegroundColor,int ShadowColor) {
        super(context);

        this.DText = DText;
        this.url = url;
        this.BackgroundC = BackgroundColor;
        this.ForegroundC = ForegroundColor;
        this.ShadowC = ShadowColor;


        intialize();

        ColorFilter A = new LightingColorFilter(ForegroundColor,1);
        this.ImageColor.setColorFilter(A);

        Log.d("Ends",url);
        ImageUrl += url.split("/")[url.split("/").length - 1] + ".png";
        this.A = new ImageTask(getContext());
        this.A.execute(ImageUrl);
        RequestImage();

    }
    public void RequestImage()
    {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (A.ConnectionState == null)
                {
                    RequestImage();
                }
                else
                {
                    isImageLoaded = true;
                    if (A.Image == null)
                    {

                    }
                    else
                    {
                        Current_Image = A.Image;
                        Palette.generateAsync(Current_Image, new Palette.PaletteAsyncListener() {
                            public void onGenerated(Palette palette) {
                                AccentC = palette.getDominantColor(AccentC);
                                invalidate();
                            }
                        });
                    }
                }
            }
        },10);
    }

    void intialize()
    {
        dps = getResources().getDisplayMetrics().density;
        sps = getResources().getDisplayMetrics().scaledDensity;

        Current_Image = ((BitmapDrawable)getResources().getDrawable(R.drawable.pokemon_imageloading)).getBitmap();

        Typeface PokemonFont = ResourcesCompat.getFont(getContext(),R.font.pokemon_solid);

        TextColor = new Paint();
        TextColor.setTextSize(sps * 18);
        TextColor.setColor(ForegroundC);
        TextColor.setLetterSpacing(0.2f);
        TextColor.setTypeface(PokemonFont);

        RippleColor = new Paint();
        RippleColor.setColor(ShadowC);

        ImageColor = new Paint();
        ImageColor.setAlpha(255);

        setBackgroundColor(BackgroundC);

        LoadingImageAnim();
    }

    public void LoadingImageAnim()
    {
        if (!isImageLoaded)
        {
            ValueAnimator ImageAnim = ValueAnimator.ofInt(64,192);
            ImageAnim.setDuration(500);
            ImageAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    ImageColor.setAlpha((int)animation.getAnimatedValue());
                    if ((int)animation.getAnimatedValue() == 192)
                    {
                        LoadingImageAnim();
                    }
                    invalidate();
                }
            });
            ImageAnim.start();
        }
        else
        {
            ImageColor.setAlpha(255);
            ImageColor.setColorFilter(null);
        }
    }

    @Override
    public void onDraw(Canvas c)
    {
        if ( (Current_Image.getScaledHeight(c)>Current_Image.getScaledWidth(c)?Current_Image.getScaledHeight(c):Current_Image.getScaledWidth(c)) != 3*getWidth()/4 && getWidth() > 0 && getHeight() > 0)
        {
            int Width,Height;
            if (Current_Image.getScaledHeight(c)>Current_Image.getScaledWidth(c))
            {
                Width = (int) ((float)(3*getWidth()/4/ (float)Current_Image.getScaledHeight(c) )* Current_Image.getScaledWidth(c));
                Height = 3*getWidth()/4;
            }
            else
            {
                Height = (int) ((float)((3*getWidth()/4) / (float)Current_Image.getScaledWidth(c)) * Current_Image.getScaledHeight(c));
                Width = 3*getWidth()/4;
            }
            Current_Image = Bitmap.createScaledBitmap(Current_Image,Width,Height,false);
        }

        if (Radius > 0)
        {
            c.drawCircle(ripplex,getHeight()/2,Radius,RippleColor);
        }

        while (TextColor.measureText(DText) > getWidth() - 10)
        {
            TextColor.setTextSize(TextColor.getTextSize() - 1);
        }

        Shader textShader=new LinearGradient(0, 0, TextColor.measureText(DText), TextColor.getTextSize(),
                new int[]{AccentC,ForegroundC},
                new float[]{0.7f,1f}, Shader.TileMode.CLAMP);

        TextColor.setShader(textShader);

        c.drawText(DText,(getWidth() - TextColor.measureText(DText))/2, 7* getHeight()/8 - ((TextColor.ascent() + TextColor.descent())/2),TextColor);
        c.drawBitmap(Current_Image,getWidth()/8,0,ImageColor);
    }

    public void BgRipple()
    {
        ripplex = iposx;
        ValueAnimator A = ValueAnimator.ofInt(getWidth()/4,(int) Math.sqrt(Math.pow(getWidth() + getHeight(),2))/2 );
        A.setDuration(250);
        A.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Radius = (int)animation.getAnimatedValue();
                invalidate();
                if (Radius == (int) Math.sqrt(Math.pow(getWidth() + getHeight(),2))/2)
                {
                    Radius = 0;
                }
            }
        });
        A.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                iposx = event.getX();
                iposy = event.getY();
                BgRipple();
                return true;

            case MotionEvent.ACTION_UP:
                invalidate();
                if (Math.abs(event.getX() - (getWidth()/2)) < getWidth()/2 && Math.abs(iposy - (getHeight()/2)) < getHeight()/2)
                {
                    Intent I = new Intent(getContext(),PokemonInfo.class);
                    I.putExtra("URL",url);
                    getContext().startActivity(I);
                }

                return true;
        }
        return super.onTouchEvent(event);
    }
}

class JsonTask extends AsyncTask<String,String,String>
{
    String ConnectionState = null;
    JSONObject JSONOutPut = null;
    Context mContext;
    String Url = null;

    public JsonTask (Context c)
    {
        this.mContext = c;
    }

    protected String doInBackground(String... params) {

        Url = params[0];
        Url = Url.replace('/','_');
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            InputStream inputStream = mContext.openFileInput(Url);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                String Cache = stringBuilder.toString();
                String date = Cache.split("\n")[1];
                Date date1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
                if (Calendar.getInstance().getTime().getTime() - date1.getTime() < 3600000)
                {
                    return Cache.split("\n")[2];
                }
            }
        }
        catch (FileNotFoundException e)
        { }
        catch (IOException e) { }
        catch (ParseException e) { }


        try {
            URL url = new URL(params[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line = "";

            while ((line = reader.readLine()) != null) {
                buffer.append(line+"\n");
                Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

            }

            try
            {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter( mContext.openFileOutput(Url, Context.MODE_PRIVATE));
                Date date = Calendar.getInstance().getTime();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String Cache = dateFormat.format(date) + "\n" + buffer.toString();
                outputStreamWriter.write(Cache);
                outputStreamWriter.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return buffer.toString();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "404";
    }
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (result == "404")
        {
            try {
                InputStream inputStream = mContext.openFileInput(Url);

                if ( inputStream != null ) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString = "";
                    StringBuilder stringBuilder = new StringBuilder();

                    while ( (receiveString = bufferedReader.readLine()) != null ) {
                        stringBuilder.append("\n").append(receiveString);
                    }

                    inputStream.close();
                    String Cache = stringBuilder.toString();
                    result = Cache.split("\n")[2];
                    ConnectionState = "Offline";
                }
                else
                {
                    result = "404";
                }
            }
            catch (FileNotFoundException e) {
                result = "404";
            } catch (IOException e) {
                result = "404";
            }
        }

        ConnectionState = ConnectionState != null? ConnectionState : result == "404"?result:"Connected";

        try
        {
            JSONOutPut = new JSONObject(result);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

class ImageTask extends AsyncTask<String,String,String>
{
    String ConnectionState = null;
    Bitmap Image = null;
    Context mContext;
    String Url = null;
    Uri FileUri = null;

    public ImageTask (Context c)
    {
        this.mContext = c;
    }

    protected String doInBackground(String... params) {

        Url = params[0];
        Url = Url.replace('/','_');
        HttpURLConnection connection = null;

        try {
            File A = new File(mContext.getApplicationInfo().dataDir,Url);
            FileInputStream Imagefile = new FileInputStream(A);
            Image = BitmapFactory.decodeStream(Imagefile);
            Imagefile.close();
            FileUri = Uri.fromFile(A);
            if (Calendar.getInstance().getTimeInMillis() - A.lastModified() < 3600000)
                return "Connected";
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            URL url = new URL(params[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream stream = connection.getInputStream();

            BufferedInputStream bufferedInputStream = new BufferedInputStream(stream);
            Image = CropBitmapTransparency(BitmapFactory.decodeStream(bufferedInputStream));

            try
            {
                File A = new File(mContext.getApplicationInfo().dataDir,Url);
                FileOutputStream Imagefile = new FileOutputStream(A);
                Image.compress(Bitmap.CompressFormat.PNG,100,Imagefile);
                A.setLastModified(Calendar.getInstance().getTimeInMillis());
                FileUri = Uri.fromFile(A);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return "Connected";
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return "404";
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (result == "404")
        {
            try {
                File A = new File(mContext.getApplicationInfo().dataDir,Url);
                FileInputStream Imagefile = new FileInputStream(A);
                Image = BitmapFactory.decodeStream(Imagefile);
                Imagefile.close();
                FileUri = Uri.fromFile(A);
                Log.d("End","" + A.lastModified());
                ConnectionState = "Offline";
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ConnectionState = ConnectionState != null? ConnectionState : result == "404"?result:"Connected";
    }

    Bitmap CropBitmapTransparency(Bitmap sourceBitmap)
    {
        int minX = sourceBitmap.getWidth();
        int minY = sourceBitmap.getHeight();
        int maxX = -1;
        int maxY = -1;
        for(int y = 0; y < sourceBitmap.getHeight(); y++)
        {
            for(int x = 0; x < sourceBitmap.getWidth(); x++)
            {
                int alpha = (sourceBitmap.getPixel(x, y) >> 24) & 255;
                if(alpha > 0)   // pixel is not 100% transparent
                {
                    if(x < minX)
                        minX = x;
                    if(x > maxX)
                        maxX = x;
                    if(y < minY)
                        minY = y;
                    if(y > maxY)
                        maxY = y;
                }
            }
        }
        if((maxX < minX) || (maxY < minY))
            return null; // Bitmap is entirely transparent

        // crop bitmap to non-transparent area and return:
        return Bitmap.createBitmap(sourceBitmap, minX, minY, (maxX - minX) + 1, (maxY - minY) + 1);
    }

}

class JsonFilterFinder
{
    JsonTask type,region,location;
    public JsonFilterFinder(Context mC)
    {
        String URL = "https://pokeapi.co/api/v2/";
        type = new JsonTask(mC);
        type.execute(URL + "type");

        region = new JsonTask(mC);
        region.execute(URL + "region");

        location = new JsonTask(mC);
        location.execute(URL+ "location?limit=10000");

    }
}

class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "favdata.db";
    public static final String TABLE_NAME = "favourites";
    public static final String COLUMN_NAME = "name";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table favourites " +
                        "(name text primary key)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS favourites");
        onCreate(db);
    }

    public boolean insertFavourite (String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        db.insert("favourites", null, contentValues);
        db.close();
        return true;
    }

    public Integer deleteFavourite (String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        int a = db.delete("favourites",
                "name = ? ",
                new String[] { name });
        db.close();
        return a;
    }

    public boolean isFavourite(String name)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = null;
        boolean a = false;
        try {
            c = db.rawQuery("select * from favourites where name = '" + name + "'", null);
            a = c.getCount() > 0;
            c.close();
        }finally {
            if (c != null)
                c.close();
            db.close();
        }
        return a;
    }
}