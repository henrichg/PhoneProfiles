package sk.henrichg.phoneprofiles;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

class PPJobsCreator implements JobCreator {

    @Override
    public Job create(@NonNull String tag) {
        switch (tag) {
            case AboutApplicationJob.JOB_TAG:
                return new AboutApplicationJob();

            default:
                return null;
        }
    }

}
