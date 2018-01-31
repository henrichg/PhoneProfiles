package sk.henrichg.phoneprofiles;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.labo.kaji.relativepopupwindow.RelativePopupWindow;

class DialogHelpPopupWindow extends GuiInfoPopupWindow {

    private DialogHelpPopupWindow(Context context, int helpTextResource) {
        super(R.layout.dialog_help_popup_window, context);

        // Disable default animation for circular reveal
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setAnimationStyle(0);
        }

        TextView textView = popupView.findViewById(R.id.dialog_help_popup_window_text);
        textView.setText(helpTextResource);
    }

    static void showPopup(MaterialDialog dialog, ImageView helpIcon, Context context, int helpTextResource) {
        DialogHelpPopupWindow popup = new DialogHelpPopupWindow(context, helpTextResource);

        View contentView = popup.getContentView();
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int measuredWPopup = contentView.getMeasuredWidth();
        int measuredHPopup = contentView.getMeasuredHeight();

        int[] locationHelpIcon = new int[2];
        helpIcon.getLocationOnScreen(locationHelpIcon);

        int x = 0;
        int y = 0;

        if (locationHelpIcon[0] + helpIcon.getWidth() - measuredWPopup < 0)
            x = -(locationHelpIcon[0] + helpIcon.getWidth() - measuredWPopup);

        Point screenSize = GlobalGUIRoutines.getRealScreenSize(context);
        Point navigationBarSize = GlobalGUIRoutines.getNavigationBarSize(context);
        if ((screenSize != null) && (navigationBarSize != null)) {
            int screenBottom = screenSize.y - navigationBarSize.y;

            if ((locationHelpIcon[1] + measuredHPopup) > screenBottom)
                y = -((locationHelpIcon[1] + measuredHPopup) - screenBottom);
        }

        popup.setClippingEnabled(false);
        popup.showOnAnchor(helpIcon, RelativePopupWindow.VerticalPosition.ALIGN_TOP,
                RelativePopupWindow.HorizontalPosition.ALIGN_RIGHT, x, y, false);
    }
}
