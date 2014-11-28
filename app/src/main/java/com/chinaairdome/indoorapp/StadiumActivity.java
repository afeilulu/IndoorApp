package com.chinaairdome.indoorapp;



import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;

import com.androidquery.AQuery;
import com.chinaairdome.indoorapp.fragment.DetailFragment;
import com.chinaairdome.indoorapp.model.Stadium;
import com.chinaairdome.indoorapp.util.LogUtil;
import com.chinaairdome.indoorapp.widget.NotifyingScrollView;
import com.google.gson.Gson;

import java.util.ArrayList;


public class StadiumActivity extends ActionBarActivity {

    private final static String TAG = LogUtil.makeLogTag(StadiumActivity.class);
    private ArrayList<Fragment> fragments;
    private FragmentManager fm;
    Stadium stadium;
    private Drawable mActionBarBackgroundDrawable;
    private AQuery aq;

    private NotifyingScrollView.OnScrollChangedListener mOnScrollChangedListener = new NotifyingScrollView.OnScrollChangedListener() {
        public void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt) {
            int actionBarHeight;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
                actionBarHeight = getSupportActionBar().getHeight();
            else
                actionBarHeight = getActionBar().getHeight();

            final int headerHeight = findViewById(R.id.image_header).getHeight() - actionBarHeight;
            final float ratio = (float) Math.min(Math.max(t, 0), headerHeight) / headerHeight;
            final int newAlpha = (int) (ratio * 255);
            mActionBarBackgroundDrawable.setAlpha(newAlpha);
        }
    };

    private Drawable.Callback mDrawableCallback = new Drawable.Callback() {
        @Override
        public void invalidateDrawable(Drawable who) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
                getSupportActionBar().setBackgroundDrawable(who);
            else
                getActionBar().setBackgroundDrawable(who);
        }

        @Override
        public void scheduleDrawable(Drawable who, Runnable what, long when) {
        }

        @Override
        public void unscheduleDrawable(Drawable who, Runnable what) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stadium);
        prepareFragment(savedInstanceState);
        mActionBarBackgroundDrawable = getResources().getDrawable(R.drawable.ab_background);
        mActionBarBackgroundDrawable.setAlpha(0);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mActionBarBackgroundDrawable.setCallback(mDrawableCallback);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
            getSupportActionBar().setBackgroundDrawable(mActionBarBackgroundDrawable);
        else
            getActionBar().setBackgroundDrawable(mActionBarBackgroundDrawable);

        ((NotifyingScrollView) findViewById(R.id.scroll_view)).setOnScrollChangedListener(mOnScrollChangedListener);

        aq = new AQuery(this);
        stadium = new Gson().fromJson(getIntent().getStringExtra("stadium_json"),Stadium.class);
        aq.id(R.id.image_header).image(stadium.getPicUrl(),true,true,0,0,null,AQuery.FADE_IN);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.stadium, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void prepareFragment(Bundle savedInstanceState) {
        fragments = new ArrayList<Fragment>();
        fragments.add(new DetailFragment());
        fm = getSupportFragmentManager();
        FragmentTransaction localFragmentTransaction = fm.beginTransaction();
        localFragmentTransaction.replace(R.id.content, fragments.get(0));
        localFragmentTransaction.addToBackStack(null);
        localFragmentTransaction.commit();
    }
}
