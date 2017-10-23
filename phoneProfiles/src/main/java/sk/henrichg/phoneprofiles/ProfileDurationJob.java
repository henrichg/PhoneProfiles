package sk.henrichg.phoneprofiles;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

class ProfileDurationJob extends Job {

    static final String JOB_TAG  = "ProfileDurationJob";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        PPApplication.logE("ProfileDurationJob.onRunJob", "xxx");

        final Context appContext = getContext().getApplicationContext();

        Bundle bundle = params.getTransientExtras();

        long profileId = bundle.getLong(PPApplication.EXTRA_PROFILE_ID, 0);
        if (profileId != 0)
        {
            DataWrapper dataWrapper = new DataWrapper(appContext, true, false, 0);

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
                    activateProfileId = Long.valueOf(ApplicationPreferences.applicationBackgroundProfile(appContext));
                    if (activateProfileId == Profile.PROFILE_NO_ACTIVATE)
                        activateProfileId = 0;
                }
                if (profile._afterDurationDo == Profile.AFTERDURATIONDO_UNDOPROFILE)
                {
                    activateProfileId = Profile.getActivatedProfileForDuration(appContext);
                }

                dataWrapper.getActivateProfileHelper().initialize(dataWrapper, appContext);
                dataWrapper.activateProfileAfterDuration(activateProfileId);
            }

            dataWrapper.invalidateDataWrapper();

        }

        return Result.SUCCESS;
    }

    static void start(Context context, long profile_id) {
        final JobRequest.Builder jobBuilder = new JobRequest.Builder(JOB_TAG);

        final Bundle bundle = new Bundle();
        bundle.putLong(PPApplication.EXTRA_PROFILE_ID, profile_id);

        final Handler handler = new Handler(context.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    jobBuilder
                            .setUpdateCurrent(false) // don't update current, it would cancel this currently running job
                            .setTransientExtras(bundle)
                            .startNow()
                            .build()
                            .schedule();
                } catch (Exception ignored) { }
            }
        });
    }

}
