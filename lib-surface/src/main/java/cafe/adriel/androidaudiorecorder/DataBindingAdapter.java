package cafe.adriel.androidaudiorecorder;

import android.databinding.BindingAdapter;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;

/**
 * Target: 为databinding补充绑定方法
 */
public class DataBindingAdapter {
	@BindingAdapter("icon")
	public static void icon(ImageView view, int drawable) {
		view.setImageResource(drawable);
	}

	@BindingAdapter("visible")
	public static void setVisible(ImageView view, boolean visible) {
		if (visible)
			view.setVisibility(View.VISIBLE);
		else
			view.setVisibility(View.INVISIBLE);
	}

	@BindingAdapter("colorFilter")
	public static void colorFilter(ImageView view, boolean isFilter) {
		if (isFilter)
			view.setColorFilter(Color.BLACK);
	}
}
