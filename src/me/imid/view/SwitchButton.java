package me.imid.view;

import me.imid.movablecheckbox.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.Checkable;

public class SwitchButton extends View implements Checkable {
	private Paint mPaint;
	private ViewParent mParent;
	private Bitmap bottom;
	private Bitmap btn;
	private Bitmap btn_pressed;
	private Bitmap btn_unpressed;
	private Bitmap frame;
	private Bitmap mask;

	private float downY; // 首次按下的Y
	private float downX; // 首次按下的X
	private float realPos; // 图片的绘制位置
	private float btnPos; // 按钮的位置
	private float btnOnPos; // 开关打开的位置
	private float btnOffPos; // 开关关闭的位置
	private final float offsetY = 15; // Y轴方向扩大的区域
	private float maskWidth;
	private float maskHeight;
	private float btnWidth;
	private float btnInitPos;

	private int mClickTimeout;
	private int mTouchSlop;
	private int mAlpha;

	private boolean mChecked = false;
	private boolean mBroadcasting;
	private boolean doTurnOnAni;

	private PerformClick mPerformClick;

	private OnCheckedChangeListener mOnCheckedChangeListener;
	private OnCheckedChangeListener mOnCheckedChangeWidgetListener;
	
	private SetCheckedHandler setCheckedHandler = new SetCheckedHandler();

	public SwitchButton(Context context) {
		super(context);
		initView(context);
	}

	public SwitchButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initView(context);
	}

	private void initView(Context context) {
		mPaint = new Paint();
		mPaint.setColor(Color.WHITE);
		Resources resources = context.getResources();

		ViewConfiguration.get(context);
		ViewConfiguration.get(context);
		// get viewConfiguration
		mClickTimeout = ViewConfiguration.getPressedStateDuration()
				+ ViewConfiguration.getTapTimeout();
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

		// get Bitmap
		bottom = BitmapFactory.decodeResource(resources, R.drawable.bottom);
		btn_pressed = BitmapFactory.decodeResource(resources,
				R.drawable.btn_pressed);
		btn_unpressed = BitmapFactory.decodeResource(resources,
				R.drawable.btn_unpressed);
		frame = BitmapFactory.decodeResource(resources, R.drawable.frame);
		mask = BitmapFactory.decodeResource(resources, R.drawable.mask);
		btn = btn_unpressed;

		btnWidth = btn_pressed.getWidth();
		maskWidth = mask.getWidth();
		maskHeight = mask.getHeight();

		btnOffPos = btnWidth / 2;
		btnOnPos = maskWidth - btnWidth / 2;

		btnPos = mChecked ? btnOnPos : btnOffPos;
		realPos = getRealPos(btnPos);
	}

	@Override
	public void setEnabled(boolean enabled) {
		// TODO Auto-generated method sstub
		mAlpha = enabled ? 255 : 128;
		super.setEnabled(enabled);
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return super.isEnabled();
	}

	/**
	 * 返回选中状态
	 * 
	 * @return 选中状态
	 */
	public boolean isChecked() {
		return mChecked;
	}

	public void toggle() {
		setChecked(!mChecked);
	}

	/**
	 * 内部调用此方法设置checked状态，此方法会延迟执行各种回调函数，保证动画的流畅度
	 * @param checked
	 */
	private void _setChecked(boolean checked) {
		int msg = checked ? 1 : 0;
		setCheckedHandler.sendEmptyMessageDelayed(msg, 10);
	}

	/**
	 * <p>
	 * Changes the checked state of this button.
	 * </p>
	 * 
	 * @param checked
	 *            true to check the button, false to uncheck it
	 */
	public void setChecked(boolean checked) {

		if (mChecked != checked) {
			mChecked = checked;

			btnPos = checked ? btnOnPos : btnOffPos;
			realPos = getRealPos(btnPos);
			invalidate();

			// Avoid infinite recursions if setChecked() is called from a
			// listener
			if (mBroadcasting) {
				return;
			}

			mBroadcasting = true;
			if (mOnCheckedChangeListener != null) {
				mOnCheckedChangeListener.onCheckedChanged(SwitchButton.this,
						mChecked);
			}
			if (mOnCheckedChangeWidgetListener != null) {
				mOnCheckedChangeWidgetListener.onCheckedChanged(
						SwitchButton.this, mChecked);
			}

			mBroadcasting = false;
		}
	}

	

	private class SetCheckedHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			boolean checked = msg.what == 1;
			if (mChecked != checked) {
				mChecked = checked;

				// btnPos = checked ? btnOnPos : btnOffPos;
				// realPos = getRealPos(btnPos);
				// invalidate();

				// Avoid infinite recursions if setChecked() is called from a
				// listener
				if (mBroadcasting) {
					return;
				}

				mBroadcasting = true;
				if (mOnCheckedChangeListener != null) {
					mOnCheckedChangeListener.onCheckedChanged(
							SwitchButton.this, mChecked);
				}
				if (mOnCheckedChangeWidgetListener != null) {
					mOnCheckedChangeWidgetListener.onCheckedChanged(
							SwitchButton.this, mChecked);
				}

				mBroadcasting = false;
			}
			super.handleMessage(msg);
		}

	}

	/**
	 * Register a callback to be invoked when the checked state of this button
	 * changes.
	 * 
	 * @param listener
	 *            the callback to call on checked state change
	 */
	public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
		mOnCheckedChangeListener = listener;
	}

	/**
	 * Register a callback to be invoked when the checked state of this button
	 * changes. This callback is used for internal purpose only.
	 * 
	 * @param listener
	 *            the callback to call on checked state change
	 * @hide
	 */
	void setOnCheckedChangeWidgetListener(OnCheckedChangeListener listener) {
		mOnCheckedChangeWidgetListener = listener;
	}

	/**
	 * Interface definition for a callback to be invoked when the checked state
	 * of a compound button changed.
	 */
	public static interface OnCheckedChangeListener {
		/**
		 * Called when the checked state of a compound button has changed.
		 * 
		 * @param buttonView
		 *            The compound button view whose state has changed.
		 * @param isChecked
		 *            The new checked state of buttonView.
		 */
		void onCheckedChanged(SwitchButton buttonView, boolean isChecked);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		int action = event.getAction();
		float x = event.getX();
		float y = event.getY();
		float deltaX = Math.abs(x - downX);
		float deltaY = Math.abs(y - downY);
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			attemptClaimDrag();
			downX = x;
			downY = y;
			btn = btn_pressed;
			btnInitPos = mChecked ? btnOnPos : btnOffPos;
			break;
		case MotionEvent.ACTION_MOVE:
			float time = event.getEventTime() - event.getDownTime();
			btnPos = btnInitPos + event.getX() - downX;
			if (btnPos >= btnOffPos) {
				btnPos = btnOffPos;
			}
			if (btnPos <= btnOnPos) {
				btnPos = btnOnPos;
			}
			doTurnOnAni = btnPos > (btnOffPos - btnOnPos) / 2 + btnOnPos;

			realPos = getRealPos(btnPos);
			break;
		case MotionEvent.ACTION_UP:
			btn = btn_unpressed;
			time = event.getEventTime() - event.getDownTime();
			if (deltaY < mTouchSlop && deltaX < mTouchSlop
					&& time < mClickTimeout) {
				if (mPerformClick == null) {
					mPerformClick = new PerformClick();
				}
				if (!post(mPerformClick)) {
					performClick();
				}
			} else {
				btnAnimation.start(!doTurnOnAni);
			}
			break;
		}

		invalidate();
		return isEnabled();
	}
	
	private final class PerformClick implements Runnable {
		public void run() {
			performClick();
		}
	}

	@Override
	public boolean performClick() {
		// TODO Auto-generated method stub
		btnAnimation.start(!mChecked);
		return super.performClick();
	}

	/**
	 * Tries to claim the user's drag motion, and requests disallowing any
	 * ancestors from stealing events in the drag.
	 */
	private void attemptClaimDrag() {
		mParent = getParent();
		if (mParent != null) {
			mParent.requestDisallowInterceptTouchEvent(true);
		}
	}

	/**
	 * 将btnPos转换成RealPos
	 * 
	 * @param btnPos
	 * @return
	 */
	private float getRealPos(float btnPos) {
		return btnPos - btnWidth / 2;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		canvas.saveLayerAlpha(
				new RectF(0, offsetY, mask.getWidth(), mask.getHeight()
						+ offsetY), mAlpha, Canvas.MATRIX_SAVE_FLAG
						| Canvas.CLIP_SAVE_FLAG
						| Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
						| Canvas.FULL_COLOR_LAYER_SAVE_FLAG
						| Canvas.CLIP_TO_LAYER_SAVE_FLAG);
		// 绘制蒙板
		canvas.drawBitmap(mask, 0, offsetY, mPaint);
		mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

		// 绘制底部图片
		canvas.drawBitmap(bottom, realPos, offsetY, mPaint);
		mPaint.setXfermode(null);
		// 绘制边框
		canvas.drawBitmap(frame, 0, offsetY, mPaint);

		// 绘制按钮
		canvas.drawBitmap(btn, realPos, offsetY, mPaint);
		canvas.restore();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		setMeasuredDimension((int) maskWidth, (int) (maskHeight + 2 * offsetY));
	}

	// animation
	private BtnAnimation btnAnimation = new BtnAnimation();

	private class BtnAnimation extends IncrementAnimation {
		private final float INIT_VELOCITY = 400;

		public void start(boolean doTurnOn) {
			long now = SystemClock.uptimeMillis();
			mAnimationLastTime = now;
			mAnimatedVelocity = doTurnOn ? -INIT_VELOCITY : INIT_VELOCITY;
			mAnimationPosition = btnPos;
			mCurrentAnimationTime = now + ANIMATION_FRAME_DURATION;
			mAnimating = true;

			mHandler.removeMessages(MSG_ANIMATE);
			mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE),
					mCurrentAnimationTime);
		}

		@Override
		protected void doAnimation() {
			// TODO Auto-generated method stub
			if (mAnimating) {
				incrementAnimation();
				if (mAnimationPosition <= btnOnPos) {
					mAnimating = false;
					mAnimationPosition = btnOnPos;
					_setChecked(true);
				} else if (mAnimationPosition >= btnOffPos) {
					mAnimating = false;
					mAnimationPosition = btnOffPos;
					_setChecked(false);
				} else {
					mCurrentAnimationTime += ANIMATION_FRAME_DURATION;
					mHandler.sendMessageAtTime(
							mHandler.obtainMessage(MSG_ANIMATE),
							mCurrentAnimationTime);
				}
				moveView(mAnimationPosition);
			}
		}

		@Override
		protected void moveView(float position) {
			// TODO Auto-generated method stub
			btnPos = position;
			realPos = getRealPos(btnPos);
			invalidate();
		}

	}

	// static class SavedState extends BaseSavedState {
	// boolean checked;
	//
	// /**
	// * Constructor called from {@link CompoundButton#onSaveInstanceState()}
	// */
	// SavedState(Parcelable superState) {
	// super(superState);
	// }
	//
	// /**
	// * Constructor called from {@link #CREATOR}
	// */
	// private SavedState(Parcel in) {
	// super(in);
	// checked = (Boolean) in.readValue(null);
	// }
	//
	// @Override
	// public void writeToParcel(Parcel out, int flags) {
	// super.writeToParcel(out, flags);
	// out.writeValue(checked);
	// }
	//
	// @Override
	// public String toString() {
	// return "CompoundButton.SavedState{"
	// + Integer.toHexString(System.identityHashCode(this))
	// + " checked=" + checked + "}";
	// }
	//
	// public static final Parcelable.Creator<SavedState> CREATOR = new
	// Parcelable.Creator<SavedState>() {
	// public SavedState createFromParcel(Parcel in) {
	// return new SavedState(in);
	// }
	//
	// public SavedState[] newArray(int size) {
	// return new SavedState[size];
	// }
	// };
	// }
	//
	// @Override
	// public Parcelable onSaveInstanceState() {
	// // Force our ancestor class to save its state
	// Parcelable superState = super.onSaveInstanceState();
	//
	// SavedState ss = new SavedState(superState);
	//
	// ss.checked = isChecked();
	// return ss;
	// }
	//
	// @Override
	// public void onRestoreInstanceState(Parcelable state) {
	// SavedState ss = (SavedState) state;
	//
	// super.onRestoreInstanceState(ss.getSuperState());
	// setChecked(ss.checked);
	// requestLayout();
	// }
}
