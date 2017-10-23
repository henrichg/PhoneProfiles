package sk.henrichg.phoneprofiles;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

class ExecuteRadioProfilePrefsJob extends Job {

    static final String JOB_TAG  = "ExecuteRadioProfilePrefsJob";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        PPApplication.logE("ExecuteRadioProfilePrefsJob.onRunJob", "xxx");

        final Context appContext = getContext().getApplicationContext();

        DataWrapper dataWrapper = new DataWrapper(appContext, false, false, 0);

        Bundle bundle = params.getTransientExtras();
        long profile_id = bundle.getLong(PPApplication.EXTRA_PROFILE_ID, 0);
        PPApplication.logE("ExecuteRadioProfilePrefsJob.onRunJob", "profile_id="+profile_id);

        Profile profile = dataWrapper.getProfileById(profile_id);

        profile = Profile.getMappedProfile(profile, appContext);
        if (profile != null) {
            if (Permissions.checkProfileRadioPreferences(appContext, profile)) {
                // run execute radios from ActivateProfileHelper
                ActivateProfileHelper aph = dataWrapper.getActivateProfileHelper();
                aph.initialize(dataWrapper, appContext);
                aph.executeForRadios(profile);
            }
        }

        dataWrapper.invalidateDataWrapper();

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
