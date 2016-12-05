package sk.henrichg.phoneprofiles;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class ProfileListWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return(new ProfileListWidgetFactory(this.getBaseContext(),
                intent));
    }

}
