package sk.henrichg.phoneprofiles;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

class PPJobsCreator implements JobCreator {

    @Override
    public Job create(@NonNull String tag) {
        switch (tag) {
            case DonationNotificationJob.JOB_TAG:
                return new DonationNotificationJob();

            default:
                return null;
        }
    }

}
