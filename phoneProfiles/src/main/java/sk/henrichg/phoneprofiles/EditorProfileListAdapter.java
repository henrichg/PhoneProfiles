package sk.henrichg.phoneprofiles;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.util.Collections;

class EditorProfileListAdapter extends RecyclerView.Adapter<EditorProfileListViewHolder>
                                implements ItemTouchHelperAdapter
{

    private EditorProfileListFragment fragment;
    private final DataWrapper activityDataWrapper;

    private final OnStartDragItemListener mDragStartListener;

    //private boolean targetHelpsSequenceStarted;
    static final String PREF_START_TARGET_HELPS = "editor_profile_list_adapter_start_target_helps";

    EditorProfileListAdapter(EditorProfileListFragment f, DataWrapper pdw,
                             OnStartDragItemListener dragStartListener)
    {
        fragment = f;
        activityDataWrapper = pdw;

        this.mDragStartListener = dragStartListener;
    }

    @NonNull
    @Override
    public EditorProfileListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (ApplicationPreferences.applicationEditorPrefIndicator(fragment.getActivity()))
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.editor_profile_list_item, parent, false);
        else
            view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.editor_profile_list_item_no_indicator, parent, false);

        return new EditorProfileListViewHolder(view, fragment, fragment.getActivity());
    }

    @Override
    public void onBindViewHolder(@NonNull final EditorProfileListViewHolder holder, int position) {
        Profile profile = getItem(position);
        holder.bindProfile(profile);

        holder.dragHandle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mDragStartListener.onStartDrag(holder);
                        break;
                    case MotionEvent.ACTION_UP:
                        v.performClick();
                        break;
                    default:
                        break;
                }
                /*if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }*/
                return false;
            }
        });
    }

    public void release()
    {
        fragment = null;
    }

    @Override
    public int getItemCount() {
        fragment.textViewNoData.setVisibility(
                (((activityDataWrapper.profileList != null) &&
                  (activityDataWrapper.profileList.size() > 0))
                ) ? View.GONE : View.VISIBLE);

        if (activityDataWrapper.profileList == null)
            return 0;

        return activityDataWrapper.profileList.size();
    }

    private Profile getItem(int position)
    {
        if (activityDataWrapper.profileList == null)
            return null;

        if (activityDataWrapper.profileList.size() == 0)
            return null;

        return activityDataWrapper.profileList.get(position);
    }

    int getItemPosition(Profile profile)
    {
        if (profile == null)
            return -1;

        if (activityDataWrapper.profileList == null)
            return -1;

        int pos = -1;

        for (int i = 0; i < activityDataWrapper.profileList.size(); i++)
        {
            ++pos;
            if (activityDataWrapper.profileList.get(i)._id == profile._id)
                return pos;
        }
        return -1;
    }

    /*
    public void setList(List<Profile> pl)
    {
        profileList = pl;
        fragment.listView.getRecycledViewPool().clear();
        notifyDataSetChanged();
    }
    */

    void addItem(Profile profile/*, boolean refresh*/)
    {
        if (activityDataWrapper.profileList == null)
            return;

        activityDataWrapper.profileList.add(profile);
        /*if (refresh) {
            fragment.listView.getRecycledViewPool().clear();
            notifyDataSetChanged();
        }*/
    }

    void deleteItemNoNotify(Profile profile)
    {
        activityDataWrapper.deleteProfile(profile);
    }

    void clearNoNotify()
    {
        activityDataWrapper.deleteAllProfiles();
    }

    /*
    public void clear()
    {
        clearNoNotify();
        fragment.listView.getRecycledViewPool().clear();
        notifyDataSetChanged();
    }
    */

    public Profile getActivatedProfile()
    {
        if (activityDataWrapper.profileList == null)
            return null;

        for (Profile p : activityDataWrapper.profileList)
        {
            if (p._checked)
            {
                return p;
            }
        }

        return null;
    }

    public void activateProfile(Profile profile)
    {
        if (activityDataWrapper.profileList == null)
            return;

        for (Profile p : activityDataWrapper.profileList)
        {
            p._checked = false;
        }

        int position = getItemPosition(profile);
        if (position != -1)
        {
            Profile _profile = activityDataWrapper.profileList.get(position);
            if (_profile != null)
                _profile._checked = true;
        }
        fragment.listView.getRecycledViewPool().clear();
        notifyDataSetChanged();
    }

    void notifyDataSetChanged(boolean refreshIcons) {
        if (refreshIcons) {
            for (Profile profile : activityDataWrapper.profileList) {
                activityDataWrapper.refreshProfileIcon(profile, true,
                        ApplicationPreferences.applicationEditorPrefIndicator(activityDataWrapper.context));
            }
        }
        fragment.listView.getRecycledViewPool().clear();
        notifyDataSetChanged();
    }

    @Override
    public void onItemDismiss(int position) {
        activityDataWrapper.profileList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (activityDataWrapper.profileList == null)
            return false;

        //Log.d("----- EditorProfileListAdapter.onItemMove", "fromPosition="+fromPosition);
        //Log.d("----- EditorProfileListAdapter.onItemMove", "toPosition="+toPosition);

        if ((fromPosition < 0) || (toPosition < 0))
            return false;

        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(activityDataWrapper.profileList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(activityDataWrapper.profileList, i, i - 1);
            }
        }
        for (int i = 0; i < activityDataWrapper.profileList.size(); i++) {
            activityDataWrapper.profileList.get(i)._porder = i + 1;
        }

        DatabaseHandler.getInstance(activityDataWrapper.context).setPOrder(activityDataWrapper.profileList);  // set profiles _porder and write it into db
        ActivateProfileHelper.updateGUI(activityDataWrapper.context, false);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    void showTargetHelps(final Activity activity, EditorProfileListFragment fragment, final View listItemView) {
        /*if (Build.VERSION.SDK_INT <= 19)
            // TapTarget.forToolbarMenuItem FC :-(
            // Toolbar.findViewById() returns null
            return;*/

        if (fragment.targetHelpsSequenceStarted)
            return;

        ApplicationPreferences.getSharedPreferences(activity);

        if (ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS, true)) {

            //Log.d("EditorProfileListAdapter.showTargetHelps", "PREF_START_TARGET_HELPS_ORDER=true");

            int circleColor = 0xFFFFFF;
            if (ApplicationPreferences.applicationTheme(fragment.getActivity()).equals("dark"))
                circleColor = 0x7F7F7F;

            if (ApplicationPreferences.preferences.getBoolean(PREF_START_TARGET_HELPS, true)) {
                //Log.d("EditorProfileListAdapter.showTargetHelps", "PREF_START_TARGET_HELPS=true");

                SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                editor.putBoolean(PREF_START_TARGET_HELPS, false);
                editor.apply();

                Rect profileItemTarget = new Rect(0, 0, listItemView.getHeight(), listItemView.getHeight());
                int[] screenLocation = new int[2];
                listItemView.getLocationOnScreen(screenLocation);
                profileItemTarget.offset(screenLocation[0] + listItemView.getWidth() / 2 - listItemView.getHeight() / 2, screenLocation[1]);

                final TapTargetSequence sequence = new TapTargetSequence(activity);

                sequence.targets(
                        TapTarget.forBounds(profileItemTarget, activity.getString(R.string.editor_activity_targetHelps_profilePreferences_title), activity.getString(R.string.editor_activity_targetHelps_profilePreferences_description))
                                .transparentTarget(true)
                                .textColorInt(0xFFFFFF)
                                .drawShadow(true)
                                .id(1),
                        TapTarget.forView(listItemView.findViewById(R.id.main_list_item_edit_menu), activity.getString(R.string.editor_activity_targetHelps_profileMenu_title), activity.getString(R.string.editor_activity_targetHelps_profileMenu_description))
                                .targetCircleColorInt(circleColor)
                                .textColorInt(0xFFFFFF)
                                .drawShadow(true)
                                .id(2),
                        TapTarget.forView(listItemView.findViewById(R.id.main_list_drag_handle), activity.getString(R.string.editor_activity_targetHelps_profileOrderHandler_title), activity.getString(R.string.editor_activity_targetHelps_profileOrderHandler_description))
                                .targetCircleColorInt(circleColor)
                                .textColorInt(0xFFFFFF)
                                .drawShadow(true)
                                .id(3)
                );
                sequence.listener(new TapTargetSequence.Listener() {
                    // This listener will tell us when interesting(tm) events happen in regards
                    // to the sequence
                    @Override
                    public void onSequenceFinish() {
                        //targetHelpsSequenceStarted = false;
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        //Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        //targetHelpsSequenceStarted = false;
                    }
                });
                sequence.continueOnCancel(true)
                        .considerOuterCircleCanceled(true);
                //targetHelpsSequenceStarted = true;
                sequence.start();
            }
        }

    }

}
