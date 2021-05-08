package com.google.android.apps.nexuslauncher.smartspace;

import android.view.View;
import com.android.launcher3.AbstractFloatingView;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.popup.SystemShortcut;
import com.moez.QKSMS.R;

class SmartspacePreferencesShortcut extends SystemShortcut {
  SmartspacePreferencesShortcut() {
    super(R.drawable.ic_smartspace_preferences, R.string.smartspace_preferences);
  }

  public View.OnClickListener getOnClickListener(final Launcher launcher, ItemInfo itemInfo) {
    return new View.OnClickListener() {
      public void onClick(final View view) {
        SmartspaceController.get(view.getContext()).cZ();
        AbstractFloatingView.closeAllOpenViews(launcher);
      }
    };
  }
}
