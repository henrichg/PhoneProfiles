package sk.henrichg.phoneprofiles;

import android.graphics.drawable.Drawable;

class Application {
    boolean shortcut = false;
    String appLabel = "";
    String packageName = "";
    String activityName = "";
    long shortcutId = 0;
    boolean checked = false;

    public Application() {
    }

    public String toString() {
        return appLabel;
    }

    void toggleChecked() {
        checked = !checked;
    }
}