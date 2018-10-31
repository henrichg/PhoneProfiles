package sk.henrichg.phoneprofiles;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.view.View;
import android.widget.RemoteViews;

public class OneRowWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds)
    {
        PPApplication.startHandlerThreadWidget();
        final Handler handler = new Handler(PPApplication.handlerThreadWidget.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                int monochromeValue = 0xFF;
                String applicationWidgetOneRowIconLightness = ApplicationPreferences.applicationWidgetOneRowIconLightness(context);
                if (applicationWidgetOneRowIconLightness.equals("0")) monochromeValue = 0x00;
                if (applicationWidgetOneRowIconLightness.equals("25")) monochromeValue = 0x40;
                if (applicationWidgetOneRowIconLightness.equals("50")) monochromeValue = 0x80;
                if (applicationWidgetOneRowIconLightness.equals("75")) monochromeValue = 0xC0;
                //if (applicationWidgetOneRowIconLightness.equals("100")) monochromeValue = 0xFF;

                DataWrapper dataWrapper = new DataWrapper(context,
                        ApplicationPreferences.applicationWidgetOneRowIconColor(context).equals("1"),
                        monochromeValue);

                Profile profile = dataWrapper.getActivatedProfile(true,
                        ApplicationPreferences.applicationWidgetOneRowPrefIndicator(context));

                try {
                    ComponentName thisWidget = new ComponentName(context, OneRowWidgetProvider.class);
                    int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

                    for (int widgetId : allWidgetIds) {
                        boolean isIconResourceID;
                        String iconIdentifier;
                        String profileName;
                        if (profile != null) {
                            isIconResourceID = profile.getIsIconResourceID();
                            iconIdentifier = profile.getIconIdentifier();
                            profileName = profile.getProfileNameWithDuration(false, context);
                        } else {
                            // create empty profile and set icon resource
                            profile = new Profile();
                            profile._name = context.getResources().getString(R.string.profiles_header_profile_name_no_activated);
                            profile._icon = Profile.PROFILE_ICON_DEFAULT + "|1|0|0";

                            profile.generateIconBitmap(context,
                                    ApplicationPreferences.applicationWidgetOneRowIconColor(context).equals("1"),
                                    monochromeValue);
                            isIconResourceID = profile.getIsIconResourceID();
                            iconIdentifier = profile.getIconIdentifier();
                            profileName = profile._name;
                        }

                        RemoteViews remoteViews;
                        if (ApplicationPreferences.applicationWidgetOneRowPrefIndicator(context))
                            remoteViews = new RemoteViews(context.getPackageName(), R.layout.one_row_widget);
                        else
                            remoteViews = new RemoteViews(context.getPackageName(), R.layout.one_row_widget_no_indicator);


                        // set background
                        int red = 0x00;
                        int green;
                        int blue;
                        if (ApplicationPreferences.applicationWidgetOneRowBackgroundType(context)) {
                            int bgColor = Integer.valueOf(ApplicationPreferences.applicationWidgetOneRowBackgroundColor(context));
                            red = Color.red(bgColor);
                            green = Color.green(bgColor);
                            blue = Color.blue(bgColor);
                        } else {
                            String applicationWidgetOneRowLightnessB = ApplicationPreferences.applicationWidgetOneRowLightnessB(context);
                            //if (applicationWidgetOneRowLightnessB.equals("0")) red = 0x00;
                            if (applicationWidgetOneRowLightnessB.equals("25")) red = 0x40;
                            if (applicationWidgetOneRowLightnessB.equals("50")) red = 0x80;
                            if (applicationWidgetOneRowLightnessB.equals("75")) red = 0xC0;
                            if (applicationWidgetOneRowLightnessB.equals("100")) red = 0xFF;
                            green = red;
                            blue = red;
                        }
                        int alpha = 0x40;
                        String applicationWidgetOneRowBackground = ApplicationPreferences.applicationWidgetOneRowBackground(context);
                        if (applicationWidgetOneRowBackground.equals("0")) alpha = 0x00;
                        //if (applicationWidgetOneRowBackground.equals("25")) alpha = 0x40;
                        if (applicationWidgetOneRowBackground.equals("50")) alpha = 0x80;
                        if (applicationWidgetOneRowBackground.equals("75")) alpha = 0xC0;
                        if (applicationWidgetOneRowBackground.equals("100")) alpha = 0xFF;
                        boolean roundedCorners = ApplicationPreferences.applicationWidgetOneRowRoundedCorners(context);
                        if (roundedCorners) {
                            remoteViews.setViewVisibility(R.id.widget_one_row_background, View.VISIBLE);
                            remoteViews.setViewVisibility(R.id.widget_one_row_not_rounded_border, View.GONE);
                            remoteViews.setViewVisibility(R.id.widget_one_row_rounded_border, View.VISIBLE);
                            remoteViews.setInt(R.id.widget_one_row_root, "setBackgroundColor", 0x00000000);
                            remoteViews.setInt(R.id.widget_one_row_background, "setColorFilter", Color.argb(0xFF, red, green, blue));
                            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                            remoteViews.setInt(R.id.widget_one_row_background, "setImageAlpha", alpha);
                            //else
                            //    remoteViews.setInt(R.id.widget_one_row_background, "setAlpha", alpha);
                        } else {
                            remoteViews.setViewVisibility(R.id.widget_one_row_background, View.GONE);
                            remoteViews.setViewVisibility(R.id.widget_one_row_rounded_border, View.GONE);
                            remoteViews.setViewVisibility(R.id.widget_one_row_not_rounded_border, View.VISIBLE);
                            remoteViews.setInt(R.id.widget_one_row_root, "setBackgroundColor", Color.argb(alpha, red, green, blue));
                            /*remoteViews.setInt(R.id.widget_one_row_background, "setColorFilter", 0x00000000);
                            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                            remoteViews.setInt(R.id.widget_one_row_background, "setImageAlpha", 0);
                            //else
                            //    remoteViews.setInt(R.id.widget_one_row_background, "setAlpha", 0);*/
                        }

                        if (isIconResourceID) {
                            if (profile._iconBitmap != null)
                                remoteViews.setImageViewBitmap(R.id.widget_one_row_header_profile_icon, profile._iconBitmap);
                            else {
                                //remoteViews.setImageViewResource(R.id.activate_profile_widget_icon, 0);
                                //int iconResource = context.getResources().getIdentifier(iconIdentifier, "drawable", context.getPackageName());
                                int iconResource = Profile.getIconResource(iconIdentifier);
                                remoteViews.setImageViewResource(R.id.widget_one_row_header_profile_icon, iconResource);
                            }
                        } else {
                            remoteViews.setImageViewBitmap(R.id.widget_one_row_header_profile_icon, profile._iconBitmap);
                        }
                        //if (PPApplication.applicationWidgetOneRowIconColor.equals("1"))
                        //{
                        red = 0xFF;
                        String applicationWidgetOneRowLightnessT = ApplicationPreferences.applicationWidgetOneRowLightnessT(context);
                        if (applicationWidgetOneRowLightnessT.equals("0")) red = 0x00;
                        if (applicationWidgetOneRowLightnessT.equals("25")) red = 0x40;
                        if (applicationWidgetOneRowLightnessT.equals("50")) red = 0x80;
                        if (applicationWidgetOneRowLightnessT.equals("75")) red = 0xC0;
                        //if (applicationWidgetOneRowLightnessT.equals("100")) red = 0xFF;
                        green = red;
                        blue = red;
                        remoteViews.setTextColor(R.id.widget_one_row_header_profile_name, Color.argb(0xFF, red, green, blue));
                        //}
                        //else
                        //{
                        //	remoteViews.setTextColor(R.id.widget_one_row_header_profile_name, Color.parseColor("#33b5e5"));
                        //}
                        remoteViews.setTextViewText(R.id.widget_one_row_header_profile_name, profileName);
                        if (ApplicationPreferences.applicationWidgetOneRowPrefIndicator(context)) {
                            if (profile._preferencesIndicator == null)
                                remoteViews.setImageViewResource(R.id.widget_one_row_header_profile_pref_indicator, R.drawable.ic_empty);
                            else
                                remoteViews.setImageViewBitmap(R.id.widget_one_row_header_profile_pref_indicator, profile._preferencesIndicator);
                        }
                        /*
                        if (PPApplication.applicationWidgetOneRowIconColor.equals("1"))
                        {
                            monochromeValue = 0xFF;
                            if (PPApplication.applicationWidgetOneRowIconLightness.equals("0")) monochromeValue = 0x00;
                            if (PPApplication.applicationWidgetOneRowIconLightness.equals("25")) monochromeValue = 0x40;
                            if (PPApplication.applicationWidgetOneRowIconLightness.equals("50")) monochromeValue = 0x80;
                            if (PPApplication.applicationWidgetOneRowIconLightness.equals("75")) monochromeValue = 0xC0;
                            if (PPApplication.applicationWidgetOneRowIconLightness.equals("100")) monochromeValue = 0xFF;

                            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_profile_activated);
                            bitmap = BitmapManipulator.monochromeBitmap(bitmap, monochromeValue, context);
                            remoteViews.setImageViewBitmap(R.id.widget_one_row_header_profile_activated, bitmap);
                        }
                        else
                        {
                            remoteViews.setImageViewResource(R.id.widget_one_row_header_profile_activated, R.drawable.ic_profile_activated);
                        }
                        */

                        Intent intent = new Intent(context, ActivateProfileActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_WIDGET);
                        PendingIntent pendingIntent = PendingIntent.getActivity(context, 200, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        remoteViews.setOnClickPendingIntent(R.id.widget_one_row_header, pendingIntent);

                        try {
                            appWidgetManager.updateAppWidget(widgetId, remoteViews);
                        } catch (Exception ignored) {}
                    }
                } catch (Exception ignored) {}

                dataWrapper.invalidateDataWrapper();
            }
        });
    }

}
