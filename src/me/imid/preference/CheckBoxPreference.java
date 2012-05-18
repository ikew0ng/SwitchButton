package me.imid.preference;

import me.imid.movablecheckbox.R;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class CheckBoxPreference extends Preference {
	private Context mContext;
	private int mLayoutResId = R.layout.preference;
	private int mWidgetLayoutResId = R.layout.preference_widget_checkbox;

	private int mOrder = DEFAULT_ORDER;
	private CharSequence mTitle;
	private CharSequence mSummary;
	private String mKey;
	private Intent mIntent;
	private boolean mEnabled = true;
	private boolean mSelectable = true;
	private boolean mRequiresKey;
	private boolean mPersistent = true;
	private String mDependencyKey;
	private Object mDefaultValue;
	private boolean mDependencyMet = true;
	private boolean mShouldDisableView = true;

	private CharSequence mSummaryOn;
	private CharSequence mSummaryOff;

	private boolean mChecked;

	public CheckBoxPreference(Context context) {
		super(context);
	}

	public CheckBoxPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.Preference);
		for (int i = a.getIndexCount(); i >= 0; i--) {
			int attr = a.getIndex(i);
			switch (attr) {
			case R.styleable.Preference_key:
				mKey = a.getString(attr);
				break;

			case R.styleable.Preference_title:
				mTitle = a.getString(attr);
				break;

			case R.styleable.Preference_summary:
				mSummary = a.getString(attr);
				break;

			case R.styleable.Preference_order:
				mOrder = a.getInt(attr, mOrder);
				break;

			case R.styleable.Preference_widgetLayout:
				mWidgetLayoutResId = a.getResourceId(attr, mWidgetLayoutResId);
				break;

			case R.styleable.Preference_enabled:
				mEnabled = a.getBoolean(attr, true);
				break;

			case R.styleable.Preference_selectable:
				mSelectable = a.getBoolean(attr, true);
				break;

			case R.styleable.Preference_persistent:
				mPersistent = a.getBoolean(attr, mPersistent);
				break;

			case R.styleable.Preference_dependency:
				mDependencyKey = a.getString(attr);
				break;

			case R.styleable.Preference_defaultValue:
				mDefaultValue = onGetDefaultValue(a, attr);
				break;

			case R.styleable.Preference_shouldDisableView:
				mShouldDisableView = a.getBoolean(attr, mShouldDisableView);
				break;
			}
		}
		a.recycle();

	}

	/**
	 * Sets the checked state and saves it to the {@link SharedPreferences}.
	 * 
	 * @param checked
	 *            The checked state.
	 */
	public void setChecked(boolean checked) {
		if (mChecked != checked) {
			mChecked = checked;
			persistBoolean(checked);
			notifyDependencyChange(shouldDisableDependents());
			notifyChanged();
		}
	}

	/**
	 * Returns the checked state.
	 * 
	 * @return The checked state.
	 */
	public boolean isChecked() {
		return mChecked;
	}

	/**
	 * Gets the View that will be shown in the {@link PreferenceActivity}.
	 * 
	 * @param convertView
	 *            The old View to reuse, if possible. Note: You should check
	 *            that this View is non-null and of an appropriate type before
	 *            using. If it is not possible to convert this View to display
	 *            the correct data, this method can create a new View.
	 * @param parent
	 *            The parent that this View will eventually be attached to.
	 * @return Returns the same Preference object, for chaining multiple calls
	 *         into a single statement.
	 * @see #onCreateView(ViewGroup)
	 * @see #onBindView(View)
	 */
	public View getView(View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = onCreateView(parent);
		}
		onBindView(convertView);
		return convertView;
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

		return layout;
	}

	@Override
	protected void onBindView(View view) {
		// TODO Auto-generated method stub
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
		
		ImageView imageView = (ImageView)view.findViewById(R.id.icon);
		Drawable icon = getLeftIcon();
		if (icon!=null) {
			if(imageView.getVisibility()!=View.VISIBLE)
				imageView.setVisibility(View.VISIBLE);
			imageView.setImageDrawable(icon);
		}else {
			if(imageView.getVisibility()!=View.GONE)
				imageView.setVisibility(View.GONE);
		}
		
		if (mShouldDisableView) {
			setEnabledStateOnViews(view, isEnabled());
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
