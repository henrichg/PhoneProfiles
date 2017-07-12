package sk.henrichg.phoneprofiles;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class ProfileDurationService extends WakefulIntentService {

    public ProfileDurationService() {
        super("ProfileDurationService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        if (intent != null) {
            Context context = getApplicationContext();

            //PPApplication.loadPreferences(context);

            if (PPApplication.getApplicationStarted(context, false))
            {
                long profileId = intent.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
                if (profileId != 0)
                {
                    DataWrapper dataWrapper = new DataWrapper(context, true, false, 0);

                    Profile profile = dataWrapper.getProfileById(profileId);
                    Profile activatedProfile = dataWrapper.getActivatedProfile();

                    if ((profile != null) && (activatedProfile != null) &&
                            (activatedProfile._id == profile._id) &&
                            (profile._afterDurationDo != Profile.AFTERDURATIONDO_NOTHING))
                    {
                        // alarm is from activated profile

                        long activateProfileId = 0;
                        if (profile._afterDurationDo == Profile.AFTERDURATIONDO_BACKGROUNPROFILE)
                        {
                            activateProfileId = Long.valueOf(ApplicationPreferences.applicationBackgroundProfile(context));
                            if (activateProfileId == Profile.PROFILE_NO_ACTIVATE)
                                activateProfileId = 0;
                        }
                        if (profile._afterDurationDo == Profile.AFTERDURATIONDO_UNDOPROFILE)
                        {
                            activateProfileId = Profile.getActivatedProfileForDuration(context);
                        }

                        dataWrapper.getActivateProfileHelper().initialize(dataWrapper, context);
                        dataWrapper.activateProfileAfterDuration(activateProfileId);
                    }

                    dataWrapper.invalidateDataWrapper();

                }
            }

        }
    }

}
