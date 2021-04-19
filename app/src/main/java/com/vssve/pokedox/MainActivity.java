package com.vssve.pokedox;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.FormatFlagsConversionMismatchException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    String Search = "";
    String SearchParam = "";
    DrawerLayout A;
    LinearLayout Scroll;

    List<JSONArray> Searches = new ArrayList<JSONArray>();

    JsonTask SearchTask = null;
    SearchAsync SearchJsonTask = null;
    String SearchUrl = "https://pokeapi.co/api/v2/";

    DBHelper Favdata;

    JSONArray SearchOrder = null;
    int Loadedindex = 0;
    int LoadOffset = 10;

    int Count = 0;

    float dps;
    ScrollView ScrollBar;
    ProgressBar Loading;

    TextView LoadingText;
    EditText SearchT;

    Handler SearchLoader,AsyncSearchDataH;

    int FavouriteMode = 0;

    JsonFilterFinder filters;
    HorizontalScrollView filtersChips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        filters = new JsonFilterFinder(this);

        Favdata = new DBHelper(this);

        Intent I = getIntent();
        SearchParam = I.getStringExtra("Params");

        filtersChips = (HorizontalScrollView)findViewById(R.id.filterchips);

        A = (DrawerLayout)findViewById(R.id.drawerLayout);


        ImageButton MenuButton = (ImageButton) findViewById(R.id.NavMenu);

        MenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                A.openDrawer(GravityCompat.START);
            }
        });

        NavigationView z = findViewById(R.id.NavView);
        z.setItemIconTintList(null);
        A.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        z.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent I;
                A.closeDrawers();
                SearchT.setText("");
                FavouriteMode =0;
                if (item.getItemId() == R.id.NavMenu_Pokemon) {
                    SearchParam ="pokemon";
                    StartSearch();
                }
                else if(item.getItemId() == R.id.NavMenu_Items)
                {
                    SearchParam ="item";
                    StartSearch();
                }
                else if(item.getItemId() == R.id.NavMenu_Location)
                {
                    SearchParam ="loc";
                    StartSearch();
                }
                else if(item.getItemId() == R.id.NavMenu_PokemonTypes)
                {
                    SearchParam ="type";
                    StartSearch();
                }
                else if(item.getItemId() == R.id.NavMenu_Region)
                {
                    SearchParam ="gen";
                    StartSearch();
                }
                else if(item.getItemId() == R.id.NavMenu_Favourites)
                {
                    SearchParam ="pokemon";
                    FavouriteMode = 2;
                    StartSearch();
                }
                return false;
            }
        });

        dps = getResources().getDisplayMetrics().density;


        LoadingText = (TextView)findViewById(R.id.pokeconnection2);
        Loading = (ProgressBar)findViewById(R.id.progressBar);
        ScrollBar = (ScrollView)findViewById(R.id.scrollView2);

        ScrollBar.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int ScrollY = ScrollBar.getScrollY();
                View view = (View) ScrollBar.getChildAt(ScrollBar.getChildCount() - 1);
                int diff = (view.getBottom() - (ScrollBar.getHeight() + ScrollBar.getScrollY()));

                if (diff == 0)
                {
                    UpdateSearch();
                }

            }
        });

        SearchT = findViewById(R.id.search);

        final TextView SearchFilterRecomendations = (TextView)findViewById(R.id.filterview);

        SearchT.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Search = s.toString();
                if (Search.startsWith("*"))
                {
                    boolean Space = false;
                    if (Search.substring(Search.length() - 1).matches(" "))
                    {
                        Search = Search.substring(0,Search.length() - 1);
                        Space = true;
                        Log.d("Heg",Search);
                    }

                    SearchFilterRecomendations.setBackgroundColor(Color.TRANSPARENT);
                    if (Search.length() == 1)
                    {
                        if (SearchParam.startsWith("pokemon"))
                            SearchFilterRecomendations.setText("Try *favourite, *type, *location, *region, *afavourite to add filters to this Search\nTry **item,**region,**location,**pokemon,**type to change Search Category");
                        else if (SearchParam.startsWith("loc"))
                            SearchFilterRecomendations.setText("Try *region to add filters to this Search\nTry **item,**region,**location,**pokemon,**type to change Search Category");
                        else
                            SearchFilterRecomendations.setText("Try **item,**region,**location,**pokemon,**type to change Search Category");
                    }
                    else if (Search.startsWith("**"))
                    {
                        SearchFilterRecomendations.setText("Try **item,**region,**location,**pokemon,**type to change Search Category");

                        if (Search.length() > 2) {
                            String Q[] = {"pokemon", "item", "location", "region","type"};
                            boolean contains = false;
                            for (int i = 0; i < Q.length; i++) {
                                if (Search.length() - 2 <= Q[i].length() && Q[i].startsWith(Search.substring(2))) {
                                    SearchFilterRecomendations.setText("Try **" + Q[i] + " to Search " + PokemonInfo.CapitalizeFirst(Q[i]) + "s");
                                    contains = true;

                                    if (Space)
                                    {
                                        SearchT.setText("**" + Q[i]);
                                        SearchT.setSelection(SearchT.getText().length());
                                        break;
                                    }

                                }
                            }
                            if (!contains) {
                                SearchFilterRecomendations.setText("No Filter Available!\n Try **item,**region,**location,**pokemon,**type to change Search Category");
                            }
                        }
                    }
                    else
                    {
                        if (SearchParam.startsWith("pokemon"))
                        {
                            if ("region".startsWith(Search.substring(1)))
                            {
                                SearchFilterRecomendations.setText("Try *region_ To See region Filter Recommendations");
                                if (Space)
                                {
                                    SearchT.setText("*region_");
                                    SearchT.setSelection(SearchT.getText().length());
                                }
                            }
                            else if (Search.startsWith("*region_"))
                            {
                                try {
                                    String Rec = "";
                                    JSONArray A = filters.region.JSONOutPut.getJSONArray("results");
                                    boolean C = false;
                                    for (int i = 0; i < A.length();i++)
                                    {
                                        if (A.getJSONObject(i).getString("name").startsWith(Search.substring(8)) && Rec.split("\n").length < 3)
                                        {
                                            Rec += "Try *region_"+A.getJSONObject(i).getString("name") + " To Add " + PokemonInfo.CapitalizeFirst(A.getJSONObject(i).getString("name")) + " region Filter\n";
                                            C = true;
                                            if (Space)
                                            {
                                                SearchT.setText("*region_"+A.getJSONObject(i).getString("name"));
                                                SearchT.setSelection(SearchT.getText().length());
                                                break;
                                            }
                                        }
                                    }

                                    if (C == false)
                                    {
                                        Rec = "Filter Not Found! Try *region_ to know available filters";
                                    }
                                    SearchFilterRecomendations.setText(Rec);
                                }
                                catch (Exception e)
                                {
                                    SearchFilterRecomendations.setText("You Seem to be Offline!!");
                                    SearchFilterRecomendations.setBackgroundColor(Color.RED);
                                }
                            }
                            else if ("location".startsWith(Search.substring(1)))
                            {
                                SearchFilterRecomendations.setText("Try *location_ To See region Filter Recommendations");
                                if (Space)
                                {
                                    SearchT.setText("*location_");
                                    SearchT.setSelection(SearchT.getText().length());
                                }
                            }
                            else if (Search.startsWith("*location_"))
                            {
                                try {
                                    String Rec = "";
                                    JSONArray A = filters.location.JSONOutPut.getJSONArray("results");
                                    boolean C = false;
                                    for (int i = 0; i < A.length();i++)
                                    {
                                        if (A.getJSONObject(i).getString("name").startsWith(Search.substring(10)) && Rec.split("\n").length < 3)
                                        {
                                            Rec += "Try *location_"+A.getJSONObject(i).getString("name") + " To Add " + PokemonInfo.CapitalizeFirst(A.getJSONObject(i).getString("name")) + " location Filter\n";
                                            C = true;
                                            if (Space)
                                            {
                                                SearchT.setText("*location_"+A.getJSONObject(i).getString("name"));
                                                SearchT.setSelection(SearchT.getText().length());
                                                break;
                                            }
                                        }
                                    }

                                    if (C == false)
                                    {
                                        Rec = "Filter Not Found! Try *location_ to know available filters";
                                    }
                                    SearchFilterRecomendations.setText(Rec);
                                }
                                catch (Exception e)
                                {
                                    SearchFilterRecomendations.setText("You Seem to be Offline!!");
                                    SearchFilterRecomendations.setBackgroundColor(Color.RED);
                                }
                            }
                            else if ("type".startsWith(Search.substring(1)))
                            {
                                SearchFilterRecomendations.setText("Try *type_ To See region Filter Recommendations");
                                if (Space)
                                {
                                    SearchT.setText("*type_");
                                    SearchT.setSelection(SearchT.getText().length());
                                }
                            }
                            else if (Search.startsWith("*type_"))
                            {
                                try {
                                    String Rec = "";
                                    JSONArray A = filters.type.JSONOutPut.getJSONArray("results");
                                    boolean C = false;
                                    for (int i = 0; i < A.length();i++)
                                    {
                                        if (A.getJSONObject(i).getString("name").startsWith(Search.substring(6)) && Rec.split("\n").length < 3)
                                        {
                                            Rec += "Try *type_"+A.getJSONObject(i).getString("name") + " To Add " + PokemonInfo.CapitalizeFirst(A.getJSONObject(i).getString("name")) + " Type pokemon Filter\n";
                                            C = true;
                                            if (Space)
                                            {
                                                SearchT.setText("*type_"+A.getJSONObject(i).getString("name"));
                                                SearchT.setSelection(SearchT.getText().length());
                                                break;
                                            }
                                        }
                                    }

                                    if (C == false)
                                    {
                                        Rec = "Filter Not Found! Try *type_ to know available filters";
                                    }
                                    SearchFilterRecomendations.setText(Rec);
                                }
                                catch (Exception e)
                                {
                                    SearchFilterRecomendations.setText("You Seem to be Offline!!");
                                    SearchFilterRecomendations.setBackgroundColor(Color.RED);
                                }
                            }
                            else if ("favourite".startsWith(Search.substring(1)))
                            {
                                SearchFilterRecomendations.setText("Try *favourite to Add Favourites Only Filter");
                                if (Space)
                                {
                                    SearchT.setText("*favourite");
                                    SearchT.setSelection(SearchT.getText().length());
                                }
                            }
                            else if ("afavourite".startsWith(Search.substring(1)))
                            {
                                SearchFilterRecomendations.setText("Try *afavourite to Add Non - Favourites Only Filter");
                                if (Space)
                                {
                                    SearchT.setText("*afavourite");
                                    SearchT.setSelection(SearchT.getText().length());
                                }
                            }
                            else
                            {
                                SearchFilterRecomendations.setText("Filter Not Found! Try *region,*type,*location,*favourite,*afavourite");
                            }
                        }
                        else if (SearchParam.startsWith("loc"))
                        {
                            if ("region".startsWith(Search.substring(1)))
                            {
                                SearchFilterRecomendations.setText("Try *region_ To See region Filter Recommendations");
                                if (Space)
                                {
                                    SearchT.setText("*region_");
                                    SearchT.setSelection(SearchT.getText().length());
                                }
                            }
                            else if (Search.startsWith("*region_"))
                            {
                                try {
                                    String Rec = "";
                                    JSONArray A = filters.region.JSONOutPut.getJSONArray("results");
                                    boolean C = false;
                                    for (int i = 0; i < A.length();i++)
                                    {
                                        if (A.getJSONObject(i).getString("name").startsWith(Search.substring(8)) && Rec.split("\n").length < 3)
                                        {
                                            Rec += "Try *region_"+A.getJSONObject(i).getString("name") + " To Add " + PokemonInfo.CapitalizeFirst(A.getJSONObject(i).getString("name")) + " region Filter\n";
                                            C = true;
                                            if (Space)
                                            {
                                                SearchT.setText("*region_"+A.getJSONObject(i).getString("name"));
                                                SearchT.setSelection(SearchT.getText().length());
                                                break;
                                            }
                                        }
                                    }

                                    if (C == false)
                                    {
                                        Rec = "Filter Not Found! Try *region_ to know available filters";
                                    }
                                    SearchFilterRecomendations.setText(Rec);
                                }
                                catch (Exception e)
                                {
                                    SearchFilterRecomendations.setText("You Seem to be Offline!!");
                                    SearchFilterRecomendations.setBackgroundColor(Color.RED);
                                }
                            }
                            else
                            {
                                SearchFilterRecomendations.setText("Filter Not Found! Try *region_ to know available filters");
                            }
                        }
                        else
                        {
                            SearchFilterRecomendations.setText("No Filter Available!\n Try **item,**region,**location,**pokemon,**type to change Search Category");
                        }
                    }
                }
                else
                {
                    StartSearch();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        SearchT.setImeActionLabel("Search", KeyEvent.KEYCODE_ENTER);
        SearchT.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId){
                    case EditorInfo.IME_ACTION_DONE:
                    case EditorInfo.IME_ACTION_NEXT:
                    case EditorInfo.IME_ACTION_PREVIOUS:

                        if (Search.startsWith("*"))
                        {
                            if (Search.startsWith("**"))
                            {
                                String Q[] = {"pokemon", "item", "location", "region","type"};
                                String J[] = {"pokemon","item","loc","gen","type"};
                                boolean C = false;
                                for (int i = 0; i < Q.length; i++) {
                                    if (Search.length() - 2 <= Q[i].length() && Q[i].matches(Search.substring(2))) {
                                        SearchParam = J[i];
                                        C = true;
                                    }
                                }
                                if (!C)
                                {
                                    SearchFilterRecomendations.setText("Sorry Pokdox Don't Allow your Search Criteria\nTry * Add Filters");
                                    SearchFilterRecomendations.setBackgroundColor(Color.RED);
                                }
                            }
                            else if (SearchParam.startsWith("loc"))
                            {
                                if (Search.startsWith("*region_"))
                                {
                                    try {
                                        String Rec = "";
                                        JSONArray A = filters.region.JSONOutPut.getJSONArray("results");
                                        boolean C = false;
                                        for (int i = 0; i < A.length();i++)
                                        {
                                            if (A.getJSONObject(i).getString("name").matches(Search.substring(8)))
                                            {
                                                String query = ",gen-"+A.getJSONObject(i).getString("url").split("/")[A.getJSONObject(i).getString("url").split("/").length - 1];
                                                if (SearchParam.contains(query))
                                                {
                                                    Rec = "Filter Already Present!";
                                                    SearchFilterRecomendations.setBackgroundColor(Color.RED);
                                                }
                                                else
                                                {
                                                    SearchParam += query;
                                                    Rec = "Added Filter Successfully!";
                                                    UpdateParams();
                                                }
                                                C = true;
                                                break;
                                            }
                                        }

                                        if (C == false)
                                        {
                                            Rec = "Filter Not Found! Try *region_ to know available filters";
                                        }
                                        SearchFilterRecomendations.setText(Rec);
                                    }
                                    catch (Exception e)
                                    {
                                        SearchFilterRecomendations.setText("You Seem to be Offline!!");
                                        SearchFilterRecomendations.setBackgroundColor(Color.RED);
                                    }
                                }
                                else
                                {
                                    SearchFilterRecomendations.setText("Filter Not Found! Try *region_ to know available filters");
                                }
                            }
                            else if (SearchParam.startsWith("pokemon"))
                            {
                                if (Search.startsWith("*region_"))
                                {
                                    try {
                                        String Rec = "";
                                        JSONArray A = filters.region.JSONOutPut.getJSONArray("results");
                                        boolean C = false;
                                        for (int i = 0; i < A.length();i++)
                                        {
                                            if (A.getJSONObject(i).getString("name").matches(Search.substring(8)))
                                            {
                                                String query = ",gen-"+A.getJSONObject(i).getString("url").split("/")[A.getJSONObject(i).getString("url").split("/").length - 1];
                                                if (SearchParam.contains(query))
                                                {
                                                    Rec = "Filter Already Present!";
                                                    SearchFilterRecomendations.setBackgroundColor(Color.RED);
                                                }
                                                else
                                                {
                                                    SearchParam += query;
                                                    Rec = "Added Filter Successfully!";
                                                    UpdateParams();
                                                }
                                                C = true;
                                                break;
                                            }
                                        }

                                        if (C == false)
                                        {
                                            Rec = "Filter Not Found! Try *region_ to know available filters";
                                        }
                                        SearchFilterRecomendations.setText(Rec);
                                    }
                                    catch (Exception e)
                                    {
                                        SearchFilterRecomendations.setText("You Seem to be Offline!!");
                                        SearchFilterRecomendations.setBackgroundColor(Color.RED);
                                    }
                                }
                                else if (Search.startsWith("*location_"))
                                {
                                    try {
                                        String Rec = "";
                                        JSONArray A = filters.location.JSONOutPut.getJSONArray("results");
                                        boolean C = false;
                                        for (int i = 0; i < A.length();i++)
                                        {
                                            if (A.getJSONObject(i).getString("name").matches(Search.substring(10)))
                                            {
                                                String query = ",loc-"+A.getJSONObject(i).getString("url").split("/")[A.getJSONObject(i).getString("url").split("/").length - 1];
                                                if (SearchParam.contains(query))
                                                {
                                                    Rec = "Filter Already Present!";
                                                    SearchFilterRecomendations.setBackgroundColor(Color.RED);
                                                }
                                                else
                                                {
                                                    SearchParam += query;
                                                    Rec = "Added Filter Successfully!";
                                                    UpdateParams();
                                                }
                                                C = true;
                                                break;
                                            }
                                        }

                                        if (C == false)
                                        {
                                            Rec = "Filter Not Found! Try *location_ to know available filters";
                                        }
                                        SearchFilterRecomendations.setText(Rec);
                                    }
                                    catch (Exception e)
                                    {
                                        SearchFilterRecomendations.setText("You Seem to be Offline!!");
                                        SearchFilterRecomendations.setBackgroundColor(Color.RED);
                                    }
                                }
                                else if (Search.startsWith("*type_"))
                                {
                                    try {
                                        String Rec = "";
                                        JSONArray A = filters.type.JSONOutPut.getJSONArray("results");
                                        boolean C = false;
                                        for (int i = 0; i < A.length();i++)
                                        {
                                            if (A.getJSONObject(i).getString("name").matches(Search.substring(6)))
                                            {
                                                String query = ",type-"+A.getJSONObject(i).getString("url").split("/")[A.getJSONObject(i).getString("url").split("/").length - 1];
                                                if (SearchParam.contains(query))
                                                {
                                                    Rec = "Filter Already Present!";
                                                    SearchFilterRecomendations.setBackgroundColor(Color.RED);
                                                }
                                                else
                                                {
                                                    SearchParam += query;
                                                    Rec = "Added Filter Successfully!";
                                                    UpdateParams();
                                                }
                                                C = true;
                                                break;
                                            }
                                        }

                                        if (C == false)
                                        {
                                            Rec = "Filter Not Found! Try *type_ to know available filters";
                                        }
                                        SearchFilterRecomendations.setText(Rec);
                                    }
                                    catch (Exception e)
                                    {
                                        SearchFilterRecomendations.setText("You Seem to be Offline!!");
                                        SearchFilterRecomendations.setBackgroundColor(Color.RED);
                                    }
                                }
                                else if (Search.equals("*favourite"))
                                {
                                    FavouriteMode = 2;
                                    UpdateParams();
                                    SearchFilterRecomendations.setText("Filter Changed Successfully");
                                }
                                else if (Search.equals("*afavourite"))
                                {
                                    FavouriteMode = 1;
                                    UpdateParams();
                                    SearchFilterRecomendations.setText("Filter Changed Successfully");
                                }
                                else
                                {
                                    SearchFilterRecomendations.setText("Filter Not Found! Try *region,*type,*location,*favourite,*afavourite");
                                }
                            }
                            else
                            {
                                SearchFilterRecomendations.setText("Filter Failed\nTry * Add Filters");
                                SearchFilterRecomendations.setBackgroundColor(Color.RED);
                            }
                            SearchT.setText("");
                            Search = "";
                        }
                        else
                        {
                            if (SearchOrder.length() > 0)
                            {
                                try {
                                    if (SearchOrder.getJSONObject(0).getString("name").matches(Search.toLowerCase()) || SearchOrder.length() == 1) {
                                        if (SearchParam.startsWith("pokemon"))
                                        {
                                            Intent J = new Intent(MainActivity.this,PokemonInfo.class);
                                            J.putExtra("URL",SearchOrder.getJSONObject(0).getString("url"));
                                            startActivity(J);
                                        }
                                        else if (SearchParam.startsWith("item"))
                                        {
                                            Intent J = new Intent(MainActivity.this,ItemInfo.class);
                                            J.putExtra("URL",SearchOrder.getJSONObject(0).getString("url"));
                                            startActivity(J);
                                        }
                                        else if (SearchParam.startsWith("loc"))
                                        {
                                            Intent J = new Intent(MainActivity.this,MainActivity.class);
                                            J.putExtra("Params","pokemon,loc-"+SearchOrder.getJSONObject(0).getString("url").split("/")[SearchOrder.getJSONObject(0).getString("url").split("/").length - 1]);
                                            startActivity(J);
                                        }
                                        else if (SearchParam.startsWith("gen"))
                                        {
                                            Intent J = new Intent(MainActivity.this,MainActivity.class);
                                            J.putExtra("Params","pokemon,gen-"+SearchOrder.getJSONObject(0).getString("url").split("/")[SearchOrder.getJSONObject(0).getString("url").split("/").length - 1]);
                                            startActivity(J);
                                        }
                                        else if (SearchParam.startsWith("type"))
                                        {
                                            Intent J = new Intent(MainActivity.this,MainActivity.class);
                                            J.putExtra("Params","pokemon,type-"+SearchOrder.getJSONObject(0).getString("url").split("/")[SearchOrder.getJSONObject(0).getString("url").split("/").length - 1]);
                                            startActivity(J);
                                        }
                                    }
                                }catch (JSONException e)
                                {

                                }
                            }
                        }

                        View view = getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }

                        return true;
                }
                return false;
            }
        });

        Scroll = (LinearLayout)findViewById(R.id.ScrollObj);

        SearchUrl = "https://pokeapi.co/api/v2/pokemon";

        if (SearchParam == null)
            SearchParam = "pokemon";

        UpdateParams();
        StartSearch();

    }

    void UpdateParams()
    {
        LinearLayout Az = (LinearLayout)findViewById(R.id.filterchipslayout);
        Az.removeAllViews();
        String Q[] = SearchParam.split(",");

        for (int i = 1; i < Q.length;i++)
        {
            if (!Q[i].contains("loc"))
            {
                Chip A = new Chip(this);
                A.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                A.setCloseIconVisible(true);
                View.OnClickListener zs = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Chip V = (Chip)v;
                        String q = V.getText().toString();
                        q = q.replace("region","gen").replace("Type","type");
                        if (SearchParam.contains(","+ q+","))
                        {
                            SearchParam = SearchParam.replace(","+ q+",",",");
                        }
                        else
                        {
                            SearchParam = SearchParam.replace("," + q,"");
                        }
                        UpdateParams();
                        StartSearch();
                    }
                };
                A.setOnClickListener(zs);
                A.setOnCloseIconClickListener(zs);
                A.setText(Q[i].replace("gen","region").replace("type","Type"));
                Az.addView(A);
            }
            else
            {
                String type = Q[i].split("area-")[0];

                boolean state = true;
                while (state)
                {
                    if (Q.length > i + 1)
                    {
                        state = Q[i+1].contains(type);
                        i++;
                    }
                    else
                    {
                        state = false;
                    }
                }

                Chip A = new Chip(this);
                A.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                A.setCloseIconVisible(true);
                View.OnClickListener zs = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Chip V = (Chip)v;
                        String q = V.getText().toString();
                        q = q.replace("location","loc");
                        String ChangeP[] = SearchParam.split(",");
                        SearchParam = "";
                        for (int i = 0; i < ChangeP.length;i++)
                        {
                            if (!ChangeP[i].contains(q))
                                SearchParam += ChangeP[i] + ",";
                        }
                        SearchParam.substring(0,SearchParam.length() - 1);
                        UpdateParams();
                        StartSearch();
                    }
                };
                A.setOnClickListener(zs);
                A.setOnCloseIconClickListener(zs);
                A.setText(type.replace("loc","location"));
                Az.addView(A);
            }
        }

        if (SearchParam.startsWith("pokemon"))
        {
            String a;
            if (FavouriteMode == 1)
                a = "Non-Favourites";
            else
                a = "Favourites";

            if (FavouriteMode > 0)
            {
                Chip A = new Chip(this);
                A.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                A.setCloseIconVisible(true);
                A.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FavouriteMode = 0;
                        UpdateParams();
                        StartSearch();
                    }
                });
                A.setText(a);
                Az.addView(A);
            }

        }

    }

    boolean useCountQuery = false;

    public void StartSearch()
    {
        Log.d("Heg",SearchUrl + " " + SearchParam);
        UpdateParams();
        if (SearchLoader != null)
        {
            SearchLoader.removeCallbacksAndMessages(null);
        }
        if (AsyncSearchDataH != null)
        {
            AsyncSearchDataH.removeCallbacksAndMessages(null);
        }

        SearchUrl = "https://pokeapi.co/api/v2/";
        Searches = new ArrayList<JSONArray>();
        Count = 0;
        String SearchQ[] = SearchParam.split(",");
        if (SearchQ[0].matches( "pokemon"))
        {
            SearchT.setHint("Search pokemon");
            if (SearchQ.length == 1)
            {
                SearchUrl += "pokemon";
                useCountQuery = true;
            }
            else
            {
                int i = Searches.size() + 1;

                if (SearchQ[i].contains("type"))
                {
                    SearchUrl += "type/" + SearchQ[i].split("-")[1];
                    useCountQuery = false;
                }
                else if (SearchQ[i].contains("gen"))
                {
                    SearchUrl += "generation/" + SearchQ[i].split("-")[1];
                    useCountQuery = false;
                }
                else if (SearchQ[i].contains("area"))
                {
                    SearchUrl += "location-area/" + SearchQ[i].split("-")[2];
                    useCountQuery = false;
                }
                else if (SearchQ[i].contains("loc"))
                {
                    SearchUrl += "location/" + SearchQ[i].split("-")[1];
                    useCountQuery = false;
                }

            }
        }
        else if (SearchQ[0].matches("item"))
        {
            SearchT.setHint("Search Items");
            SearchUrl += "item";
            useCountQuery = true;
        }
        else if (SearchQ[0].matches("type"))
        {
            SearchT.setHint("Search pokemon types");
            SearchUrl += "type";
            useCountQuery = true;
        }
        else if (SearchQ[0].matches("gen"))
        {
            SearchT.setHint("Search region");
            SearchUrl += "region";
            useCountQuery = true;
        }
        else if (SearchQ[0].matches("loc"))
        {
            SearchT.setHint("Search location");
            if (SearchQ.length == 1)
            {
                SearchUrl += "location";
                useCountQuery = true;
            }
            else
            {
                int i = Searches.size() + 1;

                if (SearchQ[i].contains("gen"))
                {
                    SearchUrl += "region/" + SearchQ[i].split("-")[1];
                    useCountQuery = false;
                }
            }
        }

        if (useCountQuery)
        {
            SearchUrl += "?limit=10000";
        }

        Loading.setVisibility(View.VISIBLE);
        LoadingText.setVisibility(View.VISIBLE);
        LoadingText.setText("Searching...");
        LoadingText.setBackgroundColor(Color.GRAY);
        ((TextView)findViewById(R.id.NRF)).setVisibility(View.GONE);
        while (Scroll.getChildCount() > 2)
        {
            Scroll.removeViewAt(0);
        }

        SearchTask = new JsonTask(this);
        SearchTask.execute(SearchUrl);
        RequestSearchData();
    }

    public void waitforNextSearch()
    {
        String SearchQ[] = SearchParam.split(",");
        try
        {
            if (SearchQ[0].matches("pokemon")) {
                int i = Searches.size();
                if (SearchQ.length > 1)
                    i = i + 1;

                if (SearchQ[i].contains("type")) {
                    JSONArray Final = new JSONArray();
                    for (int q = 0; q < SearchTask.JSONOutPut.getJSONArray("pokemon").length(); q++) {
                        Final.put(SearchTask.JSONOutPut.getJSONArray("pokemon").getJSONObject(q).getJSONObject("pokemon"));
                    }
                    Searches.add(Final);
                }
                else if (SearchQ[i].contains("area"))
                {
                    JSONArray Final = new JSONArray();
                    for (int q = 0; q < SearchTask.JSONOutPut.getJSONArray("pokemon_encounters").length(); q++) {
                        Final.put(SearchTask.JSONOutPut.getJSONArray("pokemon_encounters").getJSONObject(q).getJSONObject("pokemon"));
                    }
                    Searches.add(Final);
                }
                else if (SearchQ[i].contains("loc"))
                {
                    String Conditions = "";
                    String area1 = "";
                    for (int q =0; q < SearchTask.JSONOutPut.getJSONArray("areas").length(); q++)
                    {
                        if(q ==0)
                        {
                            area1 = SearchTask.JSONOutPut.getJSONArray("areas").getJSONObject(q).getString("url").split("/")[SearchTask.JSONOutPut.getJSONArray("areas").getJSONObject(q).getString("url").split("/").length - 1];
                        }
                        Conditions += SearchQ[i] + "area-" + SearchTask.JSONOutPut.getJSONArray("areas").getJSONObject(q).getString("url").split("/")[SearchTask.JSONOutPut.getJSONArray("areas").getJSONObject(q).getString("url").split("/").length - 1] + ",";
                    }
                    if (Conditions.length() > 0) {
                        SearchQ[i] = Conditions.substring(0, Conditions.length() - 1);
                        SearchParam = "";
                        for (int q = 0; q < SearchQ.length; q++) {
                            SearchParam += SearchQ[q] + ",";
                        }
                        SearchParam = SearchParam.substring(0, SearchParam.length() - 1);
                        SearchUrl = "https://pokeapi.co/api/v2/";
                        SearchUrl += "location-area/" + area1;
                        useCountQuery = false;
                        ReSearch();
                    }
                    else
                    {
                        SearchOrder = new JSONArray();
                        UpdateSearch();
                    }
                    return;
                }
                else if (SearchQ[i].contains("gen")) {
                    JSONArray Final = new JSONArray();
                    for (int q = 0; q < SearchTask.JSONOutPut.getJSONArray("pokemon_species").length(); q++) {
                        JSONObject f = new JSONObject();
                        f.put("name",SearchTask.JSONOutPut.getJSONArray("pokemon_species").getJSONObject(q).getString("name"));
                        f.put("url",SearchTask.JSONOutPut.getJSONArray("pokemon_species").getJSONObject(q).getString("url").replace("pokemon-species","pokemon"));
                        Final.put(f);
                    }
                    Searches.add(Final);
                }
                else {
                    Searches.add(SearchTask.JSONOutPut.getJSONArray("results"));
                }
            }
            else if (SearchQ[0].matches("item"))
            {
                Searches.add(SearchTask.JSONOutPut.getJSONArray("results"));
            }
            else if (SearchQ[0].matches("type"))
            {
                Searches.add(SearchTask.JSONOutPut.getJSONArray("results"));
            }
            else if (SearchQ[0].matches("gen"))
            {
                Searches.add(SearchTask.JSONOutPut.getJSONArray("results"));
            }
            else if (SearchQ[0].matches("loc"))
            {
                int i = Searches.size();
                if (SearchQ.length > 1)
                    i = i + 1;

                if (SearchQ[i].contains("gen")) {
                    JSONArray Final = new JSONArray();
                    for (int q = 0; q < SearchTask.JSONOutPut.getJSONArray("locations").length(); q++) {
                        Final.put(SearchTask.JSONOutPut.getJSONArray("locations").getJSONObject(q));
                    }
                    Searches.add(Final);
                }
                else {
                    Searches.add(SearchTask.JSONOutPut.getJSONArray("results"));
                }
            }
        }
        catch (JSONException e)
        {

        }

        if (SearchQ.length == 1) {
            SearchOrder = Searches.get(0);
            StartAsyncSearch();
        }
        else
        {
            int i = Searches.size() + 1;
            if (SearchQ.length == i)
            {
                if (Searches.size() == 1)
                {
                    SearchOrder = Searches.get(0);
                    StartAsyncSearch();
                }
                else
                {
                    try {
                        List<JSONObject> SearchO = new ArrayList<>();
                        List<Integer>Indexes = new ArrayList<>();

                        List<List<JSONObject>> SearchModifier = new ArrayList<>();
                        for (int j = 0; j < Searches.size(); j++) {
                            SearchModifier.add(new ArrayList<JSONObject>());
                            for (int k = 0; k < Searches.get(j).length(); k++) {
                                SearchModifier.get(j).add(Searches.get(j).getJSONObject(k));
                                boolean changed = false;
                                for (int m = 0; m < SearchO.size();m++)
                                {
                                    if(SearchO.get(m).getString("name").matches(Searches.get(j).getJSONObject(k).getString("name")))
                                    {
                                        Indexes.set(m,Indexes.get(m) + 1);
                                        changed = true;
                                    }
                                }
                                if (!changed)
                                {
                                    SearchO.add(Searches.get(j).getJSONObject(k));
                                    Indexes.add(1);
                                }
                            }
                        }

                        for (int j = 0; j < Indexes.size() - 1; j++)
                        {
                            for (int k = j + 1; k < Indexes.size(); k++)
                            {
                                if (Indexes.get(j) < Indexes.get(k))
                                {
                                    int a = Indexes.get(j);
                                    Indexes.set(j,Indexes.get(k));
                                    Indexes.set(k,a);

                                    JSONObject A = SearchO.get(j);
                                    SearchO.set(j,SearchO.get(k));
                                    SearchO.set(k,A);
                                }
                            }
                        }

                        SearchOrder = new JSONArray();
                        for (int j = 0; j < SearchO.size();j++)
                        {
                            SearchOrder.put(SearchO.get(j));
                        }
                        StartAsyncSearch();

                    }
                    catch (JSONException e)
                    {

                    }

                }
            }
            else
            {
                SearchUrl = "https://pokeapi.co/api/v2/";
                if (SearchQ[0].matches("pokemon"))
                {
                    if (SearchQ[i].contains("type"))
                    {
                        SearchUrl += "type/" + SearchQ[i].split("-")[1];
                        useCountQuery = false;
                    }
                    else if (SearchQ[i].contains("gen"))
                    {
                        SearchUrl += "generation/" + SearchQ[i].split("-")[1];
                        useCountQuery = false;
                    }
                    else if (SearchQ[i].contains("area"))
                    {
                        SearchUrl += "location-area/" + SearchQ[i].split("-")[2];
                        useCountQuery = false;
                    }
                    else if (SearchQ[i].contains("loc"))
                    {
                        SearchUrl += "location/" + SearchQ[i].split("-")[1];
                        useCountQuery = false;
                    }
                    ReSearch();
                }
                else if (SearchQ[0].matches("loc"))
                {
                    if (SearchQ[i].contains("gen"))
                    {
                        SearchUrl += "region/" + SearchQ[i].split("-")[1];
                        useCountQuery = false;
                    }
                    ReSearch();
                }
            }
        }
    }

    public void ReSearch()
    {
        SearchTask = new JsonTask(this);
        SearchTask.execute(SearchUrl);
        RequestSearchData();
    }

    public void RequestSearchData()
    {
        SearchLoader = new Handler();
        SearchLoader.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (SearchTask.ConnectionState == null)
                {
                    RequestSearchData();
                }
                else
                {
                    if (SearchTask.ConnectionState == "404")
                    {
                        LoadingText.setText("You Seem Offline. Reconnecting in 5sec...");
                        LoadingText.setBackgroundColor(Color.RED);
                        LoadingText.setVisibility(View.VISIBLE);
                        Handler a = new Handler();
                        a.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                LoadingText.setText("Reconnecting...");
                                LoadingText.setBackgroundColor(Color.RED);
                                LoadingText.setVisibility(View.VISIBLE);
                                SearchTask = new JsonTask(getApplicationContext());
                                SearchTask.execute(SearchUrl);
                                RequestSearchData();
                            }
                        },5000);
                    }
                    else
                    {
                        LoadingText.setText("Searching...");
                        LoadingText.setBackgroundColor(Color.GRAY);
                        LoadingText.setVisibility(View.VISIBLE);
                        waitforNextSearch();
                    }
                }
            }
        },10);
    }

    public void StartAsyncSearch()
    {
        SearchJsonTask = new SearchAsync(Search,FavouriteMode,this,SearchOrder,SearchParam.split(",")[0],Favdata);
        SearchJsonTask.execute();
        RequestAsyncSearchData();
    }

    public void RequestAsyncSearchData()
    {
        AsyncSearchDataH = new Handler();
        AsyncSearchDataH.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (SearchJsonTask.ConnectionState == null)
                {
                    RequestAsyncSearchData();
                }
                else
                {
                    if (SearchJsonTask.Searched == false)
                    {
                        SearchOrder = SearchJsonTask.SearchOrder;
                        Loadedindex = 0;
                        while (Scroll.getChildCount() > 2)
                        {
                            Scroll.removeViewAt(0);
                        }
                        UpdateSearch();
                        RequestAsyncSearchData();
                    }
                    else
                    {
                        Loadedindex = 0;
                        while (Scroll.getChildCount() > 2)
                        {
                            Scroll.removeViewAt(0);
                        }
                        SearchOrder = SearchJsonTask.SearchOrder;
                        UpdateSearch();
                    }
                }
            }
        },10);
    }

    public void UpdateSearch()
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

        typedValue = new TypedValue();
        theme.resolveAttribute(R.attr.Accent, typedValue, true);
        @ColorInt int FavouriteColor = typedValue.data;

        if (SearchParam.split(",")[0].matches("pokemon"))
        {
            try {
                int i;
                for (i = Loadedindex; i < Loadedindex + LoadOffset && i < SearchOrder.length(); i++) {

                    PokemonObj a = new PokemonObj(this,PokemonInfo.CapitalizeFirst(SearchOrder.getJSONObject(i).getString("name")), SearchOrder.getJSONObject(i).getString("url"), BackgroundColor,ForegroundColor,ShadowColor,FavouriteColor );
                    a.setLayoutParams(new LinearLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels - (int) (20 * dps), (int) (100 * dps)));
                    Scroll.addView(a, Scroll.getChildCount() - 2);
                }
                Loadedindex = i;

                if (SearchOrder.length() == 0)
                {
                    ((TextView)findViewById(R.id.NRF)).setVisibility(View.VISIBLE);
                }
                else
                {
                    ((TextView)findViewById(R.id.NRF)).setVisibility(View.GONE);
                }
                LoadingText.setText("Searching...");
                LoadingText.setBackgroundColor(Color.GRAY);
                LoadingText.setVisibility(View.GONE);
                if (Loadedindex >= SearchOrder.length())
                {
                    Loading.setVisibility(View.GONE);
                }
                else
                {
                    Loading.setVisibility(View.VISIBLE);
                }
            }
            catch (JSONException e)
            { }
        }
        else if (SearchParam.split(",")[0].matches("item"))
        {
            try {
                int i;
                for (i = Loadedindex; i < Loadedindex + LoadOffset && i < SearchOrder.length(); i++) {
                    Intent N = new Intent(this,ItemInfo.class);
                    N.putExtra("URL",SearchOrder.getJSONObject(i).getString("url"));

                    ListObj a = new ListObj(this,PokemonInfo.CapitalizeFirst(SearchOrder.getJSONObject(i).getString("name")), SearchOrder.getJSONObject(i).getString("url"), BackgroundColor,ForegroundColor,ShadowColor,FavouriteColor,"https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/" + SearchOrder.getJSONObject(i).getString("name") +".png",N);
                    a.setLayoutParams(new LinearLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels - (int) (20 * dps), (int) (100 * dps)));
                    Scroll.addView(a, Scroll.getChildCount() - 2);
                }
                Loadedindex = i;

                if (SearchOrder.length() == 0)
                {
                    ((TextView)findViewById(R.id.NRF)).setVisibility(View.VISIBLE);
                }
                else
                {
                    ((TextView)findViewById(R.id.NRF)).setVisibility(View.GONE);
                }
                LoadingText.setText("Searching...");
                LoadingText.setBackgroundColor(Color.GRAY);
                LoadingText.setVisibility(View.GONE);
                if (Loadedindex >= SearchOrder.length())
                {
                    Loading.setVisibility(View.GONE);
                }
                else
                {
                    Loading.setVisibility(View.VISIBLE);
                }
            }
            catch (JSONException e)
            { }
        }
        else if (SearchParam.split(",")[0].matches("loc"))
        {
            try {
                int i;
                for (i = Loadedindex; i < Loadedindex + LoadOffset && i < SearchOrder.length(); i++) {
                    Intent N = new Intent(this,MainActivity.class);
                    N.putExtra("Params","pokemon,"+"loc-"+SearchOrder.getJSONObject(i).getString("url").split("/")[SearchOrder.getJSONObject(i).getString("url").split("/").length - 1]);

                    ListObj a = new ListObj(this,PokemonInfo.CapitalizeFirst(SearchOrder.getJSONObject(i).getString("name")), SearchOrder.getJSONObject(i).getString("url"), BackgroundColor,ForegroundColor,ShadowColor,FavouriteColor,"",N);
                    a.setLayoutParams(new LinearLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels - (int) (20 * dps), (int) (100 * dps)));
                    Scroll.addView(a, Scroll.getChildCount() - 2);
                }
                Loadedindex = i;

                if (SearchOrder.length() == 0)
                {
                    ((TextView)findViewById(R.id.NRF)).setVisibility(View.VISIBLE);
                }
                else
                {
                    ((TextView)findViewById(R.id.NRF)).setVisibility(View.GONE);
                }
                LoadingText.setText("Searching...");
                LoadingText.setBackgroundColor(Color.GRAY);
                LoadingText.setVisibility(View.GONE);
                if (Loadedindex >= SearchOrder.length())
                {
                    Loading.setVisibility(View.GONE);
                }
                else
                {
                    Loading.setVisibility(View.VISIBLE);
                }
            }
            catch (JSONException e)
            { }
        }
        else if (SearchParam.split(",")[0].matches("gen"))
        {
            try {
                int i;
                for (i = Loadedindex; i < Loadedindex + LoadOffset && i < SearchOrder.length(); i++) {
                    Intent N = new Intent(this,MainActivity.class);
                    N.putExtra("Params","pokemon,"+"gen-"+SearchOrder.getJSONObject(i).getString("url").split("/")[SearchOrder.getJSONObject(i).getString("url").split("/").length - 1]);

                    ListObj a = new ListObj(this,PokemonInfo.CapitalizeFirst(SearchOrder.getJSONObject(i).getString("name")), SearchOrder.getJSONObject(i).getString("url"), BackgroundColor,ForegroundColor,ShadowColor,FavouriteColor,"",N);
                    a.setLayoutParams(new LinearLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels - (int) (20 * dps), (int) (100 * dps)));
                    Scroll.addView(a, Scroll.getChildCount() - 2);
                }
                Loadedindex = i;

                if (SearchOrder.length() == 0)
                {
                    ((TextView)findViewById(R.id.NRF)).setVisibility(View.VISIBLE);
                }
                else
                {
                    ((TextView)findViewById(R.id.NRF)).setVisibility(View.GONE);
                }
                LoadingText.setText("Searching...");
                LoadingText.setBackgroundColor(Color.GRAY);
                LoadingText.setVisibility(View.GONE);
                if (Loadedindex >= SearchOrder.length())
                {
                    Loading.setVisibility(View.GONE);
                }
                else
                {
                    Loading.setVisibility(View.VISIBLE);
                }
            }
            catch (JSONException e)
            { }
        }
        else if (SearchParam.split(",")[0].matches("type"))
        {
            try {
                int i;
                for (i = Loadedindex; i < Loadedindex + LoadOffset && i < SearchOrder.length(); i++) {
                    Intent N = new Intent(this,MainActivity.class);
                    N.putExtra("Params","pokemon,"+"type-"+SearchOrder.getJSONObject(i).getString("url").split("/")[SearchOrder.getJSONObject(i).getString("url").split("/").length - 1]);

                    int n = Integer.parseInt(SearchOrder.getJSONObject(i).getString("url").split("/")[SearchOrder.getJSONObject(i).getString("url").split("/").length - 1]);

                    Bitmap ns = n > 18 ? null: BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("type_"+(n-1),"drawable",getPackageName()));

                    ListTypeObj a = new ListTypeObj(this,PokemonInfo.CapitalizeFirst(SearchOrder.getJSONObject(i).getString("name")), SearchOrder.getJSONObject(i).getString("url"), BackgroundColor,ForegroundColor,ShadowColor,FavouriteColor,ns,N);
                    a.setLayoutParams(new LinearLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels - (int) (20 * dps), (int) (100 * dps)));
                    Scroll.addView(a, Scroll.getChildCount() - 2);
                }
                Loadedindex = i;

                if (SearchOrder.length() == 0)
                {
                    ((TextView)findViewById(R.id.NRF)).setVisibility(View.VISIBLE);
                }
                else
                {
                    ((TextView)findViewById(R.id.NRF)).setVisibility(View.GONE);
                }
                LoadingText.setText("Searching...");
                LoadingText.setBackgroundColor(Color.GRAY);
                LoadingText.setVisibility(View.GONE);
                if (Loadedindex >= SearchOrder.length())
                {
                    Loading.setVisibility(View.GONE);
                }
                else
                {
                    Loading.setVisibility(View.VISIBLE);
                }
            }
            catch (JSONException e)
            { }
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }
}

class SearchAsync extends AsyncTask<Void,Void,Void>
{
    JSONArray SearchOrder;
    boolean Searched = false;

    String Search,SearchObject;
    int Favouritemode;

    Context mContext;

    String ConnectionState = null;
    DBHelper favdata;


    public SearchAsync(String Search,int Favouritemode,Context mContext,JSONArray SearchTask,String SearchObject,DBHelper Favdata)
    {
        this.Search = Search;
        this.Favouritemode= Favouritemode;
        this.mContext =mContext;
        this.SearchOrder =SearchTask;
        this.SearchObject = SearchObject;
        this.favdata = Favdata;
    }

    public void Search()
    {
        if (SearchObject.matches("pokemon"))
        {
            try
            {
                JSONArray N = new JSONArray();
                if (Favouritemode == 2)
                {
                    for (int i = 0; i < SearchOrder.length(); i++)
                    {
                        if (favdata.isFavourite(SearchOrder.getJSONObject(i).getString("name")))
                        {
                            N.put(SearchOrder.getJSONObject(i));
                        }
                    }
                }
                else if (Favouritemode == 1)
                {
                    for (int i = 0; i < SearchOrder.length(); i++)
                    {
                        if (!favdata.isFavourite(SearchOrder.getJSONObject(i).getString("name")))
                        {
                            N.put(SearchOrder.getJSONObject(i));
                        }
                    }
                }
                else
                {
                    N = SearchOrder;
                }
                SearchOrder = N;

            }
            catch (JSONException e)
            {

            }
        }

        ConnectionState = "Searching...";
        if (!Search.matches(""))
        {
            try {
                JSONArray NewSearch = SearchOrder;
                SearchOrder = new JSONArray();

                String[] Searches = Search.split(" ");
                List<Integer> Indexes = new ArrayList<Integer>();

                for (int i = 0; i < NewSearch.length(); i++)
                {
                    for (int j = 0; j < Searches.length; j++) {
                        if (NewSearch.getJSONObject(i).getString("name").toLowerCase().length() >= Searches[j].length()) {
                            if (NewSearch.getJSONObject(i).getString("name").toLowerCase().substring(0, Searches[j].length()).contains(Searches[j].toLowerCase()) && !Indexes.contains(i)) {
                                SearchOrder.put(NewSearch.getJSONObject(i));
                                Indexes.add(i);
                            }
                        }
                    }
                }

                for (int i = 0; i < NewSearch.length(); i++) {
                    for (int j = 0; j < Searches.length; j++) {
                        if (NewSearch.getJSONObject(i).getString("name").toLowerCase().contains(Searches[j].toLowerCase()) && !Indexes.contains(i)) {
                            SearchOrder.put(NewSearch.getJSONObject(i));
                            Indexes.add(i);
                        }
                    }
                }
                Searches = Search.split("");
                for (int i = 0; i < NewSearch.length(); i++) {
                    int Contains = 0;
                    String q = NewSearch.getJSONObject(i).getString("name").toLowerCase();
                    for (int j = 0; j < Searches.length; j++) {
                        if (q.contains(Searches[j].toLowerCase()) && !Indexes.contains(i)) {
                            Contains+=1;
                            q=q.replaceFirst(Searches[j],"");
                        }
                    }
                    if (Contains == Searches.length)
                    {
                        SearchOrder.put(NewSearch.getJSONObject(i));
                        Indexes.add(i);
                    }
                }
            }
            catch (JSONException e)
            {

            }
        }
        ConnectionState = "Searched";
        Searched = true;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Search();
        return null;
    }
}