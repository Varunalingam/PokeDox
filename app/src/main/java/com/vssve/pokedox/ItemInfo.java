package com.vssve.pokedox;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class ItemInfo extends AppCompatActivity {
    String URL, ImageURL;
    ImageTask Ai = null;
    JsonTask A = null;

    boolean isImageLoaded = false;
    boolean offlineupdated = false;

    ProgressBar Loading;
    TextView LoadingText;

    ImageView PokeImage;

    TextView PokeName;
    TextView Stats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_info);

        Intent I = getIntent();

        URL = I.getStringExtra("URL");

        Loading = (ProgressBar)findViewById(R.id.loading);
        LoadingText = (TextView) findViewById(R.id.connectionstate);
        PokeImage = (ImageView)findViewById(R.id.pokepic);
        PokeName = (TextView)findViewById(R.id.pokename);
        PokeName.setVisibility(View.GONE);
        Stats = (TextView)findViewById(R.id.statst);
        Stats.setVisibility(View.GONE);

        ImageButton Backb = (ImageButton) (findViewById(R.id.backb));
        Backb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        LoadingText.setBackgroundColor(Color.GRAY);
        LoadingAnim(false,"Connecting...");
        ImageLoadingAnim();

        A = new JsonTask(getApplicationContext());
        A.execute(URL);
        RequestConnection();

    }

    public void ImageLoadingAnim()
    {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();
        theme.resolveAttribute(R.attr.Foreground, typedValue, true);
        @ColorInt int Foreground = typedValue.data;

        if (isImageLoaded == false)
        {
            PokeImage.setColorFilter(Foreground);
            ValueAnimator ImageAnim = ValueAnimator.ofFloat(0.25f,0.75f);
            ImageAnim.setDuration(500);
            ImageAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    PokeImage.setAlpha((float)animation.getAnimatedValue());
                    if ((float)animation.getAnimatedValue() == 0.75f)
                    {
                        ImageLoadingAnim();
                    }
                }
            });
            ImageAnim.start();
        }
        else
        {
            PokeImage.setAlpha(1f);
            PokeImage.setColorFilter(null);
        }
    }

    public void LoadingAnim(boolean connected,String Condition)
    {
        if (connected)
        {
            Loading.setVisibility(View.GONE);
            LoadingText.setText(Condition);
            if (Condition == "Offline")
            {
                LoadingText.setBackgroundColor(Color.GRAY);
            }
            else
            {
                LoadingText.setBackgroundColor(Color.GREEN);
                Handler As = new Handler();
                As.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LoadingText.setVisibility(View.GONE);
                    }
                },1000);
            }
        }
        else
        {
            LoadingText.setVisibility(View.VISIBLE);
            LoadingText.setText(Condition);
            Loading.setVisibility(View.VISIBLE);
        }
    }

    public void RequestConnection() {
        Handler As = new Handler();
        As.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("End", A.ConnectionState != null ? A.ConnectionState : "null");
                if (A.ConnectionState == null) {
                    RequestConnection();
                } else {
                    if (A.ConnectionState == "404") {
                        LoadingAnim(false, "You Have Gone Offline.Rechecking in 5sec...");
                        Handler g = new Handler();
                        g.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                A = new JsonTask(getApplicationContext());
                                A.execute(URL);
                                RequestConnection();
                            }
                        }, 5000);
                    } else {
                        if (A.ConnectionState == "Offline") {
                            A = new JsonTask(getApplicationContext());
                            A.execute(URL);
                            RequestConnection();
                            if (offlineupdated == false) {
                                parseJsontoview(A.JSONOutPut);
                                offlineupdated = true;
                            }
                        } else {
                            parseJsontoview(A.JSONOutPut);
                        }
                    }
                }
            }
        }, 10);
    }

    public void RequestImageConnection()
    {
        Handler As = new Handler();
        As.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Ai.ConnectionState == null)
                {
                    RequestImageConnection();
                }
                else
                {
                    if (Ai.ConnectionState == "404")
                    {
                        LoadingAnim(false,"You Have Gone Offline.Rechecking in 5sec...");
                        Handler g = new Handler();
                        g.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Ai = new ImageTask(getApplicationContext());
                                Ai.execute(ImageURL);
                                RequestImageConnection();
                            }
                        },5000);
                    }
                    else
                    {
                        LoadingAnim(true,A.ConnectionState);
                        isImageLoaded = true;
                        PokeImage.setImageBitmap(Ai.Image);
                    }
                }
            }
        },10);
    }

    public void  parseJsontoview(JSONObject A)
    {
        try {
            ImageURL = A.getJSONObject("sprites").getString("default");
        } catch (JSONException e) {
            ImageURL = null;
        }

        if (ImageURL != null)
        {
            Ai = new ImageTask(getApplicationContext());
            Ai.execute(ImageURL);
            RequestImageConnection();
        }
        else
        {
            isImageLoaded = true;
        }

        try
        {
            String Name = "";
            for (int i = 0; i <A.getJSONArray("names").length();i++ )
            {
                if (A.getJSONArray("names").getJSONObject(i).getJSONObject("language").getString("name").matches("en"))
                {
                    Name = PokemonInfo.CapitalizeFirst(A.getJSONArray("names").getJSONObject(i).getString("name"));
                    break;
                }
            }
            PokeName.setText(Name.length() > 0?Name:"");
            PokeName.setVisibility(View.VISIBLE);

            String Stat = A.getJSONArray("effect_entries").getJSONObject(0).getString("effect").replaceAll("\n","");

            while(Stat.contains("  "))
            {
                Stat = Stat.replaceAll("  "," ");
            }

            Stat = "    " + Stat + "\n\n";

            Stat += "Cost : " + A.getInt("cost") + " PokeDollars\n";
            Stat += "Category : " + PokemonInfo.CapitalizeFirst(A.getJSONObject("category").getString("name"));

            Stats.setText(Stat.length() > 0?Stat:"");
            Stats.setVisibility(View.VISIBLE);

        }
        catch (JSONException e)
        {

        }
    }
}
