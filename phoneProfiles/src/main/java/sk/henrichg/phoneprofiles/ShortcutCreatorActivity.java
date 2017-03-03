package sk.henrichg.phoneprofiles;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

public class ShortcutCreatorActivity extends AppCompatActivity {

    private DataWrapper dataWrapper;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        super.onCreate(savedInstanceState);

        GlobalGUIRoutines.setTheme(this, true, false);
        GlobalGUIRoutines.setLanguage(getBaseContext());

        dataWrapper = new DataWrapper(getApplicationContext(), true, false, 0);

    // set window dimensions ----------------------------------------------------------

        Display display = getWindowManager().getDefaultDisplay();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        LayoutParams params = getWindow().getAttributes();
        params.alpha = 1.0f;
        params.dimAmount = 0.5f;
        getWindow().setAttributes(params);

        // display dimensions
        float popupWidth = display.getWidth();
        float popupMaxHeight = display.getHeight();
        float popupHeight = 0;
        float actionBarHeight = 0;

        // action bar height
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, tv, true))
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());

        // set max. dimensions for display orientation
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            //popupWidth = Math.round(popupWidth / 100f * 50f);
            //popupMaxHeight = Math.round(popupMaxHeight / 100f * 90f);
            popupWidth = popupWidth / 100f * 50f;
            popupMaxHeight = popupMaxHeight / 100f * 90f;
        }
        else
        {
            //popupWidth = Math.round(popupWidth / 100f * 70f);
            //popupMaxHeight = Math.round(popupMaxHeight / 100f * 90f);
            popupWidth = popupWidth / 100f * 80f;
            popupMaxHeight = popupMaxHeight / 100f * 90f;
        }

        // add action bar height
        popupHeight = popupHeight + actionBarHeight;

        final float scale = getResources().getDisplayMetrics().density;

        // add list items height
        int profileCount = dataWrapper.getDatabaseHandler().getProfilesCount();
        popupHeight = popupHeight + (60f * scale * profileCount); // item
        popupHeight = popupHeight + (1f * scale * (profileCount-1)); // divider

        popupHeight = popupHeight + (20f * scale); // listview padding

        if (popupHeight > popupMaxHeight)
            popupHeight = popupMaxHeight;

        // set popup window dimensions
        getWindow().setLayout((int) (popupWidth + 0.5f), (int) (popupHeight + 0.5f));

    //-----------------------------------------------------------------------------------

        setContentView(R.layout.activity_shortcut_creator);

        getSupportActionBar().setTitle(R.string.title_activity_shortcut_creator);

        //databaseHandler = new DatabaseHandler(this);

    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onDestroy()
    {
    //	Debug.stopMethodTracing();
        super.onDestroy();

        dataWrapper.invalidateDataWrapper();
        dataWrapper = null;
    }

    /*
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
        GlobalGUIRoutines.reloadActivity(this, false);
    }
    */

}
