package me.imid.preference;

import me.imid.movablecheckbox.R;
import me.imid.view.SwitchButton;
import me.imid.view.SwitchButton.OnCheckedChangeListener;
import android.app.Service;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.PreferenceActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Checkable;
import android.widget.TextView;

public class CheckBoxPreference extends android.preference.CheckBoxPreference {
	private Context mContext;
	private int mLayoutResId = R.layout.preference;
	private int mWidgetLayoutResId = R.layout.preference_widget_checkbox;
	private int minHeight;
	private int dimenHeightAttr = android.R.attr.listPreferredItemHeight;

	private boolean mShouldDisableView = true;

	private CharSequence mSummaryOn;
	private CharSequence mSummaryOff;

	private boolean mSendAccessibilityEventViewClickedType;

	private AccessibilityManager mAccessibilityManager;

	public CheckBoxPreference(Context context) {
		super(context);
	}

	public CheckBoxPreference(Context context, AttributeSet attrset) {
		super(context, attrset);
		mContext = context;
		mSummaryOn = getSummaryOn();
		mSummaryOff = getSummaryOff();
		mAccessibilityManager = (AccessibilityManager) mContext
				.getSystemService(Service.ACCESSIBILITY_SERVICE);
		dimenHeightAttr = android.os.Build.MANUFACTURER.toLowerCase().contains(
				"meizu") ? android.R.attr.listPreferredItemHeightMz
				: android.R.attr.listPreferredItemHeight;
		
		TypedArray typedArray =context.obtainStyledAttributes(new int[]{dimenHeightAttr});
		minHeight = (int) typedArray.getDimension(0, 100);
		typedArray.recycle();
	}

	/**
	 * Creates the View to be shown for this Preference in the
	 * {@link PreferenceActivity}. The default behavior is to inflate the main
	 * layout of this Preference (see {@link #setLayoutResource(int)}. If
	 * changing this behavior, please specify a {@link ViewGroup} with ID
	 * {@link android.R.id#widget_frame}.
	 * <p>
	 * Make sure to call through to the superclass's implementation.
	 * 
	 * @param parent
	 *            The parent that this View will eventually be attached to.
	 * @return The View that displays this Preference.
	 * @see #onBindView(View)
	 */
	protected View onCreateView(ViewGroup parent) {
		final LayoutInflater layoutInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		final View layout = layoutInflater.inflate(mLayoutResId, parent, false);

		if (mWidgetLayoutResId != 0) {
			final ViewGroup widgetFrame = (ViewGroup) layout
					.findViewById(R.id.widget_frame);
			layoutInflater.inflate(mWidgetLayoutResId, widgetFrame);
		}
		layout.setMinimumHeight(minHeight);
		return layout;
	}

	@Override
	protected void onBindView(View view) {
		// 屏蔽item点击事件
		view.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return true;
			}
		});

		TextView textView = (TextView) view.findViewById(R.id.title);
		if (textView != null) {
			textView.setText(getTitle());
		}

		textView = (TextView) view.findViewById(R.id.summary);
		if (textView != null) {
			final CharSequence summary = getSummary();
			if (!TextUtils.isEmpty(summary)) {
				if (textView.getVisibility() != View.VISIBLE) {
					textView.setVisibility(View.VISIBLE);
				}

				textView.setText(getSummary());
			} else {
				if (textView.getVisibility() != View.GONE) {
					textView.setVisibility(View.GONE);
				}
			}
		}

		if (mShouldDisableView) {
			setEnabledStateOnViews(view, isEnabled());
		}

		View checkboxView = view.findViewById(R.id.checkbox);
		if (checkboxView != null && checkboxView instanceof Checkable) {
			((Checkable) checkboxView).setChecked(isChecked());
			SwitchButton switchButton = (SwitchButton) checkboxView;
			switchButton
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						public void onCheckedChanged(SwitchButton buttonView,
								boolean isChecked) {
							// TODO Auto-generated method stub
							mSendAccessibilityEventViewClickedType = true;
							if (!callChangeListener(isChecked)) {
								return;
							}
							setChecked(isChecked);
						}
					});
			// send an event to announce the value change of the CheckBox and is
			// done here
			// because clicking a preference does not immediately change the
			// checked state
			// for example when enabling the WiFi
			if (mSendAccessibilityEventViewClickedType
					&& mAccessibilityManager.isEnabled()
					&& checkboxView.isEnabled()) {
				mSendAccessibilityEventViewClickedType = false;

				int eventType = AccessibilityEvent.TYPE_VIEW_CLICKED;
				checkboxView.sendAccessibilityEventUnchecked(AccessibilityEvent
						.obtain(eventType));
			}
		}

		// Sync the summary view
		TextView summaryView = (TextView) view.findViewById(R.id.summary);
		if (summaryView != null) {
			boolean useDefaultSummary = true;
			if (isChecked() && mSummaryOn != null) {
				summaryView.setText(mSummaryOn);
				useDefaultSummary = false;
			} else if (!isChecked() && mSummaryOff != null) {
				summaryView.setText(mSummaryOff);
				useDefaultSummary = false;
			}

			if (useDefaultSummary) {
				final CharSequence summary = getSummary();
				if (summary != null) {
					summaryView.setText(summary);
					useDefaultSummary = false;
				}
			}

			int newVisibility = View.GONE;
			if (!useDefaultSummary) {
				// Someone has written to it
				newVisibility = View.VISIBLE;
			}
			if (newVisibility != summaryView.getVisibility()) {
				summaryView.setVisibility(newVisibility);
			}
		}
	}

	/**
	 * Makes sure the view (and any children) get the enabled state changed.
	 */
	private void setEnabledStateOnViews(View v, boolean enabled) {
		v.setEnabled(enabled);

		if (v instanceof ViewGroup) {
			final ViewGroup vg = (ViewGroup) v;
			for (int i = vg.getChildCount() - 1; i >= 0; i--) {
				setEnabledStateOnViews(vg.getChildAt(i), enabled);
			}
		}
	}

}
