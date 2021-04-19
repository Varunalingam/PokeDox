package com.vssve.pokedox;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PokemonInfo extends AppCompatActivity {

    String URL = "url",ImageURL,SURL,EURL;
    JsonTask A = null;
    JsonTask S = null;
    JsonTask E = null;
    ImageTask Ai = null;
    ProgressBar Loading;

    TextView LoadingText;

    boolean isImageLoaded;
    boolean offlineupdated;

    ImageView PokeImage;
    TextView PokeName,PokeSpecies,Pokedim,PokeAbi,Poketype1,Poketype2,Stats;
    ImageView Generation;

    ImageButton Type1b,Type2b,Shareb,Backb,AddFavb;

    TextView EvoChainTitle;
    HorizontalScrollView EvoView;
    LinearLayout EvoObject;

    DBHelper Favdata;

    boolean ViewAction = false;

    @Override
    public void onBackPressed()
    {
        if (ViewAction)
        {
            startActivity(new Intent(this,MainActivity.class));
        }
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon_info);
        Favdata = new DBHelper(this);

        Intent I = getIntent();

        if (I.getAction()==Intent.ACTION_VIEW)
        {
            Uri uri = I.getData();
            URL = uri.getQueryParameter(URL);
            ViewAction = true;
        }
        else
        {
            URL = I.getStringExtra("URL");
        }
        Loading = (ProgressBar)findViewById(R.id.pokeload);
        Loading.setVisibility(View.VISIBLE);

        PokeImage = (ImageView)findViewById(R.id.pokepic);
        PokeImage.setVisibility(View.VISIBLE);

        PokeName = (TextView)findViewById(R.id.pokename);
        PokeName.setVisibility(View.GONE);
        PokeSpecies = (TextView)findViewById(R.id.speciest);
        PokeSpecies.setVisibility(View.GONE);
        Pokedim = (TextView)findViewById(R.id.dimt);
        Pokedim.setVisibility(View.GONE);
        PokeAbi = (TextView)findViewById(R.id.posabt);
        PokeAbi.setVisibility(View.GONE);
        Poketype1 = (TextView)findViewById(R.id.type1t);
        Poketype1.setVisibility(View.GONE);
        Poketype2 = (TextView)findViewById(R.id.type2t);
        Poketype2.setVisibility(View.GONE);
        Stats = (TextView)findViewById(R.id.statst);
        Stats.setVisibility(View.GONE);

        Generation = (ImageButton)findViewById(R.id.genbi);
        Generation.setVisibility(View.GONE);

        Type1b = (ImageButton)findViewById(R.id.type1b);
        Type1b.setVisibility(View.GONE);
        Type2b = (ImageButton)findViewById(R.id.type2b);
        Type2b.setVisibility(View.GONE);
        Shareb = (ImageButton)findViewById(R.id.shareb);
        Shareb.setVisibility(View.GONE);
        AddFavb = (ImageButton)findViewById(R.id.favb);
        AddFavb.setVisibility(View.GONE);

        Backb = (ImageButton)findViewById(R.id.backb);

        Backb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        EvoChainTitle = (TextView)findViewById(R.id.evotitle);
        EvoView = (HorizontalScrollView)findViewById(R.id.evoview);
        EvoObject = (LinearLayout)findViewById(R.id.evoobject);

        EvoChainTitle.setVisibility(View.GONE);
        EvoView.setVisibility(View.GONE);

        LoadingText = (findViewById(R.id.pokeconnection));
        LoadingAnim(false,"Connecting....");

        ImageLoadingAnim();

        A = new JsonTask(this);
        A.execute(URL);
        RequestConnection();
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

    public void ImageLoadingAnim()
    {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();
        theme.resolveAttribute(R.attr.Foreground, typedValue, true);
        @ColorInt int Foreground = typedValue.data;
        if (isImageLoaded == false)
        {
            ValueAnimator ImageAnim = ValueAnimator.ofFloat(0.25f,0.75f);
            ImageAnim.setDuration(500);
            PokeImage.setColorFilter(Foreground);
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
            PokeImage.setColorFilter(null);
            PokeImage.setAlpha(1f);
        }
    }

    public void RequestConnection()
    {
        Handler As = new Handler();
        As.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("End",A.ConnectionState != null?A.ConnectionState:"null");
                if (A.ConnectionState == null)
                {
                    RequestConnection();
                }
                else
                {
                    if (A.ConnectionState == "404")
                    {
                        LoadingAnim(false,"You Have Gone Offline.Rechecking in 5sec...");
                        Handler g = new Handler();
                        g.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                A = new JsonTask(getApplicationContext());
                                A.execute(URL);
                                RequestConnection();
                            }
                        },5000);
                    }
                    else
                    {
                        if (A.ConnectionState == "Offline")
                        {
                            A = new JsonTask(getApplicationContext());
                            A.execute(URL);
                            RequestConnection();
                            if (offlineupdated == false)
                            {
                                parseJsontoview(A.JSONOutPut);
                                offlineupdated = true;
                            }
                        }
                        else
                        {
                            parseJsontoview(A.JSONOutPut);
                        }
                    }
                }
            }
        },10);
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
                        isImageLoaded = true;
                        PokeImage.setImageBitmap(Ai.Image);
                    }
                }
            }
        },10);
    }

    public void RequestSpeciesDataConnection()
    {
        Handler As = new Handler();
        As.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (S.ConnectionState == null)
                {
                    RequestSpeciesDataConnection();
                }
                else
                {
                    if (S.ConnectionState == "404")
                    {
                        LoadingAnim(false,"You Have Gone Offline.Rechecking in 5sec...");
                        Handler g = new Handler();
                        g.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                S = new JsonTask(getApplicationContext());
                                S.execute(SURL);
                                RequestSpeciesDataConnection();
                            }
                        },5000);
                    }
                    else
                    {
                        try {
                            String type[] = S.JSONOutPut.getJSONObject("generation").getString("url").split("/");
                            final String types = type[type.length-1];
                            Log.d("End",type[type.length - 1]);
                            Generation.setImageBitmap(BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("gen" + type[type.length-1],"drawable",getPackageName())));
                            Generation.setVisibility(View.VISIBLE);
                            Generation.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent I = new Intent(PokemonInfo.this,MainActivity.class);
                                    I.putExtra("Params","pokemon,gen-"+types);
                                    startActivity(I);
                                }
                            });

                            EURL = S.JSONOutPut.getJSONObject("evolution_chain").getString("url");

                            E = new JsonTask(getApplicationContext());
                            E.execute(EURL);
                            RequestEvoChainDataConnection();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        },10);
    }

    public void RequestEvoChainDataConnection()
    {
        Handler As = new Handler();
        As.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (E.ConnectionState == null)
                {
                    RequestEvoChainDataConnection();
                }
                else
                {
                    if (S.ConnectionState == "404")
                    {
                        LoadingAnim(false,"You Have Gone Offline.Rechecking in 5sec...");
                        Handler g = new Handler();
                        g.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                E = new JsonTask(getApplicationContext());
                                E.execute(EURL);
                                RequestEvoChainDataConnection();
                            }
                        },5000);
                    }
                    else
                    {
                        LoadingAnim(true,A.ConnectionState);
                        try {
                            if (E.JSONOutPut.getJSONObject("chain").getJSONArray("evolves_to").length() > 0)
                            {
                                EvoChainTitle.setVisibility(View.VISIBLE);
                                EvoView.setVisibility(View.VISIBLE);

                                JSONArray Evolves_To = new JSONArray();
                                Evolves_To.put(E.JSONOutPut.getJSONObject("chain"));
                                GenerateEvoObjects(Evolves_To,EvoObject);


                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        },10);
    }

    public void GenerateEvoObjects(JSONArray Evolves_To,LinearLayout EvoOb)
    {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();
        theme.resolveAttribute(R.attr.Background, typedValue, true);
        @ColorInt int BackgroundColor = typedValue.data;

        typedValue = new TypedValue();
        theme.resolveAttribute(R.attr.Foreground, typedValue, true);
        @ColorInt int ForegroundColor = typedValue.data;

        typedValue = new TypedValue();
        theme.resolveAttribute(R.attr.Shadow, typedValue, true);
        @ColorInt int ShadowColor = typedValue.data;

        if (Evolves_To.length() > 1)
        {
            LinearLayout G = new LinearLayout(getApplicationContext());
            G.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
            G.setOrientation(LinearLayout.VERTICAL);
            EvoOb.addView(G);

            for (int i = 0; i < Evolves_To.length();i++)
            {
                Log.d("End","" + i);
                try {
                    LinearLayout GH = new LinearLayout(getApplicationContext());
                    GH.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
                    GH.setOrientation(LinearLayout.HORIZONTAL);
                    GH.setGravity(Gravity.CENTER);
                    G.addView(GH);
                    PokemonEvoObj abscs = new PokemonEvoObj(this,CapitalizeFirst(Evolves_To.getJSONObject(i).getJSONObject("species").getString("name")),Evolves_To.getJSONObject(i).getJSONObject("species").getString("url").replace("pokemon-species","pokemon"), BackgroundColor,ForegroundColor,ShadowColor);
                    abscs.setLayoutParams(new LinearLayout.LayoutParams((int)(100 * getResources().getDisplayMetrics().density),(int)(100 * getResources().getDisplayMetrics().density)));
                    GH.addView(abscs);
                    if (Evolves_To.getJSONObject(i).getJSONArray("evolves_to").length()>0)
                    {
                        GenerateEvoObjects(Evolves_To.getJSONObject(i).getJSONArray("evolves_to"),GH);
                    }
                }
                catch (JSONException e)
                {

                }
            }
        }
        else
        {
            try {
                LinearLayout GH = new LinearLayout(getApplicationContext());
                GH.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
                GH.setOrientation(LinearLayout.HORIZONTAL);
                GH.setGravity(Gravity.CENTER);
                EvoOb.addView(GH);
            PokemonEvoObj abscs = new PokemonEvoObj(this,CapitalizeFirst(Evolves_To.getJSONObject(0).getJSONObject("species").getString("name")),Evolves_To.getJSONObject(0).getJSONObject("species").getString("url").replace("pokemon-species","pokemon"), BackgroundColor,ForegroundColor,ShadowColor);
            abscs.setLayoutParams(new LinearLayout.LayoutParams((int)(100 * getResources().getDisplayMetrics().density),(int)(100 * getResources().getDisplayMetrics().density)));
            GH.addView(abscs);
            if (Evolves_To.getJSONObject(0).getJSONArray("evolves_to").length()>0)
            {
                Log.d("End","Success");
                GenerateEvoObjects(Evolves_To.getJSONObject(0).getJSONArray("evolves_to"),GH);
            }
            }
            catch (JSONException e)
            {

            }
        }
    }

    public void parseJsontoview(JSONObject A){

        try
        {
            S = new JsonTask(getApplicationContext());
            SURL = A.getJSONObject("species").getString("url");
            S.execute(SURL);
            RequestSpeciesDataConnection();
        }
        catch (JSONException e) {

        }

        try {
            ImageURL = A.getJSONObject("sprites").getString("front_default");
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

        try {

            String PokeNames = A.getString("name");
            PokeNames = CapitalizeFirst(PokeNames);
            PokeName.setText(PokeNames);
            PokeName.setVisibility(View.VISIBLE);

            PokeSpecies.setText("( " + A.getJSONObject("species").getString("name")  + " )");
            PokeSpecies.setVisibility(View.VISIBLE);

            String PokeAbis = "Possible Abilities\n";

            for (int i = 0; i < A.getJSONArray("abilities").length();i++)
            {
                PokeAbis += CapitalizeFirst(A.getJSONArray("abilities").getJSONObject(i).getJSONObject("ability").getString("name")) + "\n";
            }
            PokeAbi.setText(PokeAbis);
            PokeAbi.setVisibility(View.VISIBLE);

            String dims = "Weight : " + A.getInt("weight") + " hg\nHeight : " + A.getInt("height") + " dm";
            Pokedim.setText(dims);
            Pokedim.setVisibility(View.VISIBLE);

            String Statss = "Stats\n";
            for (int i = 0; i < A.getJSONArray("stats").length();i++)
            {
                Statss += CapitalizeFirst(A.getJSONArray("stats").getJSONObject(i).getJSONObject("stat").getString("name")) + " : " + A.getJSONArray("stats").getJSONObject(i).getInt("base_stat") + "\n";
            }
            Stats.setText(Statss);
            Stats.setVisibility(View.VISIBLE);

            Poketype2.setText("" +CapitalizeFirst(A.getJSONArray("types").getJSONObject(0).getJSONObject("type").getString("name")));
            Poketype2.setVisibility(View.VISIBLE);

            String type[] = A.getJSONArray("types").getJSONObject(0).getJSONObject("type").getString("url").split("/");
            final int type2b= Integer.parseInt(type[type.length - 1]);
            Type2b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent I = new Intent(PokemonInfo.this,MainActivity.class);
                    I.putExtra("Params","pokemon,type-" + type2b);
                    PokemonInfo.this.startActivity(I);
                }
            });
            Type2b.setImageDrawable(getDrawable(getResources().getIdentifier("type_" +  (Integer.parseInt(type[type.length - 1]) - 1), "drawable",getPackageName())));
            Type2b.setVisibility(View.VISIBLE);
            Poketype1.setVisibility(View.INVISIBLE);
            Type1b.setVisibility(View.INVISIBLE);

            if (Favdata.isFavourite(PokeNames.toLowerCase()))
            {
                AddFavb.setImageDrawable(getDrawable(R.drawable.favourites));
            }
            else
            {
                AddFavb.setImageDrawable(getDrawable(R.drawable.afavourites));
            }

            final String finalPokeNames = PokeNames;
            AddFavb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Favdata.isFavourite(finalPokeNames.toLowerCase()))
                    {
                        AddFavb.setImageDrawable(getDrawable(R.drawable.afavourites));
                        Favdata.deleteFavourite(finalPokeNames.toLowerCase());
                        Toast.makeText(getApplicationContext(),"Removed " + finalPokeNames + " From Favourites",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        AddFavb.setImageDrawable(getDrawable(R.drawable.favourites));
                        Favdata.insertFavourite(finalPokeNames.toLowerCase());
                        Toast.makeText(getApplicationContext(),"Added " + finalPokeNames + " to Favourites",Toast.LENGTH_SHORT).show();
                    }
                }
            });

            if (A.getJSONArray("types").length() > 1)
            {
                type = A.getJSONArray("types").getJSONObject(1).getJSONObject("type").getString("url").split("/");
                Poketype1.setText("" +CapitalizeFirst(A.getJSONArray("types").getJSONObject(1).getJSONObject("type").getString("name")));
                Poketype1.setVisibility(View.VISIBLE);
                Type1b.setImageDrawable(getDrawable(getResources().getIdentifier("type_" +  (Integer.parseInt(type[type.length - 1]) - 1), "drawable",getPackageName())));
                Type1b.setVisibility(View.VISIBLE);
                final int type1b= Integer.parseInt(type[type.length - 1]);
                Type1b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent I = new Intent(PokemonInfo.this,MainActivity.class);
                        I.putExtra("Params","pokemon,type-" + type1b);
                        PokemonInfo.this.startActivity(I);
                    }
                });
            }

            Shareb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/html");
                    String shareBody = "pokemon " + finalPokeNames + "\nDownload PokeDox To Know more about "+finalPokeNames+"\nhttp://com.vssve.pokedox?url="+URL;
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                    sharingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(Intent.createChooser(sharingIntent, "Share via"));
                }
            });

            Shareb.setVisibility(View.VISIBLE);
            AddFavb.setVisibility(View.VISIBLE);

        }
        catch (JSONException e)
        {

        }


    }

    public static String CapitalizeFirst(String s)
    {
        return s.replaceFirst("" + s.charAt(0),("" + s.charAt(0)).toUpperCase());
    }
}