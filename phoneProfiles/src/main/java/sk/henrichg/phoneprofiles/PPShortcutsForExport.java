package sk.henrichg.phoneprofiles;

import android.os.Parcel;
import android.os.Parcelable;

public class PPShortcutsForExport implements Parcelable {

    long KEY_S_ID;
    String KEY_S_INTENT;
    String KEY_S_NAME;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.KEY_S_ID);
        dest.writeString(this.KEY_S_INTENT);
        dest.writeString(this.KEY_S_NAME);
    }

    public PPShortcutsForExport() {
    }

    protected PPShortcutsForExport(Parcel in) {
        this.KEY_S_ID = in.readLong();
        this.KEY_S_INTENT = in.readString();
        this.KEY_S_NAME = in.readString();
    }

    public static final Parcelable.Creator<PPShortcutsForExport> CREATOR = new Parcelable.Creator<PPShortcutsForExport>() {
        @Override
        public PPShortcutsForExport createFromParcel(Parcel source) {
            return new PPShortcutsForExport(source);
        }

        @Override
        public PPShortcutsForExport[] newArray(int size) {
            return new PPShortcutsForExport[size];
        }
    };
}
