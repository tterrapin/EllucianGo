/*
 * Copyright 2016 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.MainActivity;
import com.ellucian.mobile.android.adapter.ModuleMenuAdapter;
import com.ellucian.mobile.android.util.FastBlur;
import com.ellucian.mobile.android.util.Utils;

import java.util.ArrayList;
import java.util.Collections;

public class ShortcutListFragment extends EllucianFragment {

    private View rootView;
    private LinearLayout shortcutParentLayout;
    private MainActivity mainActivity;
    private static final String TAG = ShortcutListFragment.class.getSimpleName();
    public static final String SHORTCUT_ITEMS = "shortcutItems";
    private static final String DARK_OVERLAY = "dark";
    private Animation animation;

    private static final float scaleFactor = 8;
    private static float radius;
    private static int color;

    String overlayConfig;

    public static ShortcutListFragment newInstance(ArrayList<ShortcutItem> shortcutItems) {
        ShortcutListFragment f = new ShortcutListFragment();

        Bundle args = new Bundle();
        args.putParcelableArrayList(SHORTCUT_ITEMS, shortcutItems);
        f.setArguments(args);
        return f;
    }

    public ArrayList<ShortcutItem> getShortcutItems() {
        return getArguments().getParcelableArrayList(SHORTCUT_ITEMS);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
        animation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
    }
    
    private static class RowViewHolder {
        public ImageView iconView;
        public TextView textView;
        public ImageView lockView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (container == null) {
            return null;
        }

        rootView = inflater.inflate(R.layout.fragment_shortcut_list, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        overlayConfig = Utils.getStringFromPreferences(mainActivity, Utils.CONFIGURATION, Utils.HOME_SCREEN_OVERLAY, "light");

        if (TextUtils.equals(overlayConfig, DARK_OVERLAY)) {
            radius = 4;
            color = R.color.home_dark_overlay_color;
        } else {
            radius = 3;
            color = R.color.home_light_overlay_color;
        }

        LinearLayout shortcutListLayout = (LinearLayout) rootView.findViewById(R.id.shortcut_ll);
        shortcutParentLayout = (LinearLayout) rootView.findViewById(R.id.shortcut_parent_layout);
        ArrayList<ShortcutItem> shortcutItems;
        if (getShortcutItems() != null) {
            shortcutItems = getShortcutItems();
            Collections.sort(shortcutItems);
        } else {
            Log.v(TAG, "No homescreen shortcuts to display. Return.");
            return;
        }

        DisplayMetrics metrics = mainActivity.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;

        if (rootView != null) {
            final LinearLayout shorcut_ll = (LinearLayout) rootView.findViewById(R.id.shortcut_ll);
            shorcut_ll.removeAllViews();
            final LayoutInflater inflater = getActivity().getLayoutInflater();

            for (final ShortcutItem shortcut : shortcutItems) {
                RowViewHolder holder = new RowViewHolder();
                View shortcutView = inflater.inflate(R.layout.shortcut_list_item, shortcutListLayout, false);
                View landscapeView = shortcutView.findViewById(R.id.shorcut_item_landscape);
                View tabletView = shortcutView.findViewById(R.id.shorcut_item_large);

                // For landscape layouts only, set the width of each item to the screen width
                // divided by the number of items to show.
                if (landscapeView != null) {
                    int widthPerIcon = width / shortcutItems.size();
                    landscapeView.getLayoutParams().width = widthPerIcon;
                }
                if (tabletView != null) {
                    int widthPerIcon = width / shortcutItems.size();
                    int maxWidth = getResources().getDimensionPixelSize(R.dimen.shortcut_item_width_tablet);
                    if (widthPerIcon > maxWidth) {
                        widthPerIcon = maxWidth; // ceiling value
                    }
                    tabletView.getLayoutParams().width = widthPerIcon;
                }
                holder.textView = (TextView) shortcutView.findViewById(R.id.shortcut_item_label);
                holder.iconView = (ImageView) shortcutView.findViewById(R.id.shortcut_item_image);
                holder.lockView = (ImageView) shortcutView.findViewById(R.id.shortcut_item_lock_image);
                shortcutView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DrawerLayoutHelper.menuItemClickListener(mainActivity, shortcut.moduleId,
                                shortcut.type, shortcut.secureString, shortcut.subType, shortcut.name);
                    }
                });

                Drawable icon = null;
                if (!TextUtils.isEmpty(shortcut.iconUrl)) {
                    icon = ModuleMenuAdapter.getIcon(mainActivity, shortcut.iconUrl);
                }

                shortcutView.setTag(holder);

                holder.textView.setText(shortcut.name);
                if (icon != null) {
                    holder.iconView.setImageDrawable(icon);
                    View iconBackground = shortcutView.findViewById(R.id.shortcut_image_layout);
                    if (iconBackground != null) {
                        iconBackground.setVisibility(View.VISIBLE);
                    }
                }
                if (shortcut.lock) {
                    holder.lockView.setVisibility(View.VISIBLE);
                } else {
                    holder.lockView.setVisibility(View.GONE);
                }

                if (TextUtils.equals(overlayConfig, DARK_OVERLAY)) {
                    Utils.setTextAppearanceHelper(mainActivity, holder.textView, R.style.homeScreenShortcutText_Dark);
                } else {
                    Utils.setTextAppearanceHelper(mainActivity, holder.textView, R.style.homeScreenShortcutText);
                }
                shortcutListLayout.addView(shortcutView);
            }

            blurImage();
        }
    }

    private void blurImage() {
        final ImageView image= (ImageView) mainActivity.findViewById(R.id.home_background);

        image.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                image.getViewTreeObserver().removeOnPreDrawListener(this);
                image.buildDrawingCache();

                Bitmap bmp = image.getDrawingCache();
                if (bmp != null && shortcutParentLayout != null) {
                    int viewHeight = shortcutParentLayout.getMeasuredHeight();
                    int viewWidth = shortcutParentLayout.getMeasuredWidth();

                    if (viewHeight > 0 && viewWidth > 0) {
                        blur(bmp, shortcutParentLayout);
                    }
                }
                return true;
            }
        });

        shortcutParentLayout.startAnimation(animation);

    }

    public static class ShortcutItem implements Comparable<ShortcutItem>, Parcelable {
        public String name;
        public String moduleId;
        public String type;
        public String subType;
        public String secureString;
        public String iconUrl;
        public boolean lock;
        public int order;

        public ShortcutItem(String name, String moduleId, String type, String subType,
                            String secureString, String iconUrl, boolean lock, int order) {
            this.name = name;
            this.moduleId = moduleId;
            this.type = type;
            this.subType = subType;
            this.secureString = secureString;
            this.iconUrl = iconUrl;
            this.lock = lock;
            this.order = order;
        }

        public ShortcutItem(Parcel in) {
            readFromParcel(in);
        }

        private void  readFromParcel(Parcel in) {
            name = in.readString();
            moduleId = in.readString();
            type = in.readString();
            subType = in.readString();
            secureString = in.readString();
            iconUrl = in.readString();
            lock = in.readByte() != 0;
            order = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeString(moduleId);
            dest.writeString(type);
            dest.writeString(subType);
            dest.writeString(secureString);
            dest.writeString(iconUrl);
            dest.writeByte((byte) (lock ? 1 : 0));
            dest.writeInt(order);
        }

        public static final Creator<ShortcutItem> CREATOR = new Creator<ShortcutItem>() {
            @Override
            public ShortcutItem createFromParcel(Parcel source) {
                return new ShortcutItem(source);
            }

            @Override
            public ShortcutItem[] newArray(int size) {
                return new ShortcutItem[size];
            }
        };

        @Override
        public int compareTo(ShortcutItem another) {
            return this.order > another.order ? +1 :
                   this.order < another.order ? -1 :
                   0;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private void blur(Bitmap bkg, View view) {
        long startMs = System.currentTimeMillis();

        int viewHeight = view.getMeasuredHeight();
        int viewWidth = view.getMeasuredWidth();

        Bitmap overlay = Bitmap.createBitmap((int) (viewWidth/scaleFactor),
                (int) (viewHeight/scaleFactor), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        int left = view.getLeft();
        int top = rootView.getHeight()-viewHeight;

        canvas.translate(-left / scaleFactor, -top / scaleFactor);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(bkg, 0, 0, paint);
        int colorHelper = Utils.getColorHelper(mainActivity, color);
        canvas.drawColor(colorHelper);

        overlay = FastBlur.doBlur(overlay, (int)radius, true);

        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(new BitmapDrawable(getResources(), overlay));
        } else {
            view.setBackground(new BitmapDrawable(getResources(), overlay));
        }

        long blurTime = System.currentTimeMillis() - startMs;
        Log.v(TAG, String.format("Time to blur: %d ms", blurTime));
    }

}