package sk.henrichg.phoneprofiles;

class Application {
    boolean shortcut = false;
    String appLabel = "";
    String packageName = "";
    String activityName = "";
    long shortcutId = 0;
    boolean checked = false;
    int startApplicationDelay;

    public Application() {
    }

    public String toString() {
        return appLabel;
    }

    void toggleChecked() {
        checked = !checked;
    }
}