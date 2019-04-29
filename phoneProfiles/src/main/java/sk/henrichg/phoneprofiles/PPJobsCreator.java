package sk.henrichg.phoneprofiles;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

import androidx.annotation.NonNull;

class PPJobsCreator implements JobCreator {

    @Override
    public Job create(@NonNull String tag) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (tag) {
            case DonationNotificationJob.JOB_TAG:
                return new DonationNotificationJob();

            default:
                return null;
        }
    }

}
