package sk.henrichg.phoneprofiles;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

class PPJobsCreator implements JobCreator {

    @Override
    public Job create(@NonNull String tag) {
        switch (tag) {
            case PackageReplacedJob.JOB_TAG:
                return new PackageReplacedJob();
            case FirstStartJob.JOB_TAG:
                return new FirstStartJob();
            case ExecuteRadioProfilePrefsJob.JOB_TAG:
                return new ExecuteRadioProfilePrefsJob();
            case ExecuteVolumeProfilePrefsJob.JOB_TAG:
                return new ExecuteVolumeProfilePrefsJob();
            case ExecuteWallpaperProfilePrefsJob.JOB_TAG:
                return new ExecuteWallpaperProfilePrefsJob();
            case ExecuteRunApplicationsProfilePrefsJob.JOB_TAG:
                return new ExecuteRunApplicationsProfilePrefsJob();
            case ExecuteRootProfilePrefsJob.JOB_TAG:
                return new ExecuteRootProfilePrefsJob();
            case ProfileDurationJob.JOB_TAG:
                return new ProfileDurationJob();
            case ScreenOnOffJob.JOB_TAG:
                return new ScreenOnOffJob();
            case PhoneCallJob.JOB_TAG:
                return new PhoneCallJob();
            case DashClockJob.JOB_TAG:
                return new DashClockJob();
            default:
                return null;
        }
    }

}
