package com.goweb.browser.ui.dialog;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.goweb.browser.R;

public class CustomDialog {

    private Activity activity;
    private View overlayView;
    private OnDismissListener dismissListener;

    public interface OnDismissListener {
        void onDismiss();
    }

    public static class Builder {
        private Activity activity;
        private String title;
        private String message;
        private String[] items;
        private int selectedIndex = -1;
        private OnItemClickListener itemClickListener;
        private String positiveText;
        private Runnable positiveAction;
        private String negativeText;
        private Runnable negativeAction;
        private View customView;
        private boolean cancelable = true;

        public Builder(Activity activity) {
            this.activity = activity;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setItems(String[] items, OnItemClickListener listener) {
            this.items = items;
            this.itemClickListener = listener;
            return this;
        }

        public Builder setSelectedIndex(int index) {
            this.selectedIndex = index;
            return this;
        }

        public Builder setPositiveButton(String text, Runnable action) {
            this.positiveText = text;
            this.positiveAction = action;
            return this;
        }

        public Builder setNegativeButton(String text, Runnable action) {
            this.negativeText = text;
            this.negativeAction = action;
            return this;
        }

        public Builder setView(View view) {
            this.customView = view;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        public CustomDialog show() {
            return new CustomDialog(this);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private CustomDialog(final Builder builder) {
        this.activity = builder.activity;
        FrameLayout root = (FrameLayout) activity.getWindow().getDecorView().findViewById(android.R.id.content);

        overlayView = LayoutInflater.from(activity).inflate(R.layout.dialog_custom, root, false);

        TextView titleView = overlayView.findViewById(R.id.dialog_title);
        LinearLayout contentLayout = overlayView.findViewById(R.id.dialog_content);
        LinearLayout buttonsLayout = overlayView.findViewById(R.id.dialog_buttons);

        // Title
        if (builder.title != null && !builder.title.isEmpty()) {
            titleView.setText(builder.title);
            titleView.setVisibility(View.VISIBLE);
        }

        // Content
        if (builder.message != null && !builder.message.isEmpty()) {
            TextView msg = new TextView(activity);
            msg.setText(builder.message);
            msg.setTextSize(15);
            msg.setTextColor(0xFF555555);
            msg.setGravity(Gravity.CENTER);
            msg.setPadding(0, 8, 0, 8);
            contentLayout.addView(msg);
        }

        if (builder.items != null) {
            for (int i = 0; i < builder.items.length; i++) {
                String item = builder.items[i];
                TextView tv = new TextView(activity);
                tv.setText(i == builder.selectedIndex ? "✓ " + item : item);
                tv.setTextSize(15);
                tv.setTextColor(i == builder.selectedIndex ? 0xFF2d6a4f : 0xFF333333);
                tv.setPadding(0, 14, 0, 14);
                tv.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                tv.setClickable(true);
                if (i == builder.selectedIndex) {
                    tv.setTypeface(null, android.graphics.Typeface.BOLD);
                }
                final int pos = i;
                tv.setOnClickListener(v -> {
                    builder.itemClickListener.onItemClick(pos);
                    dismiss();
                });
                contentLayout.addView(tv);

                if (i < builder.items.length - 1) {
                    View div = new View(activity);
                    div.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, 1));
                    div.setBackgroundColor(0xFFEEEEEE);
                    contentLayout.addView(div);
                }
            }
        }

        if (builder.customView != null) {
            contentLayout.addView(builder.customView);
        }

        // Buttons
        if (builder.negativeText != null) {
            TextView negBtn = createButton(builder.negativeText, false);
            negBtn.setOnClickListener(v -> {
                if (builder.negativeAction != null) builder.negativeAction.run();
                dismiss();
            });
            buttonsLayout.addView(negBtn);
        }

        if (builder.positiveText != null) {
            TextView posBtn = createButton(builder.positiveText, true);
            posBtn.setOnClickListener(v -> {
                if (builder.positiveAction != null) builder.positiveAction.run();
                dismiss();
            });
            buttonsLayout.addView(posBtn);
        }

        // Cancel on outside touch (only on background, not content)
        if (builder.cancelable) {
            overlayView.setOnClickListener(v -> dismiss());
            View dialogCard = overlayView.findViewById(R.id.dialog_card);
            if (dialogCard != null) {
                dialogCard.setOnClickListener(v -> {});
            }
        }

        root.addView(overlayView);

        // Animate in with scale effect
        View dialogCard = overlayView.findViewById(R.id.dialog_card);
        overlayView.setAlpha(0f);
        if (dialogCard != null) {
            dialogCard.setScaleX(0.85f);
            dialogCard.setScaleY(0.85f);
        }
        overlayView.animate().alpha(1f).setDuration(200).start();
        if (dialogCard != null) {
            dialogCard.animate().scaleX(1f).scaleY(1f).setDuration(250)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator()).start();
        }
    }

    private TextView createButton(String text, boolean isPositive) {
        TextView btn = new TextView(activity);
        btn.setText(text);
        btn.setTextSize(15);
        btn.setTextColor(isPositive ? 0xFF2d6a4f : 0xFF999999);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(0, 0, 0, 0);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        btn.setLayoutParams(lp);
        btn.setClickable(true);

        if (isPositive) {
            btn.setTypeface(null, android.graphics.Typeface.BOLD);
        }

        return btn;
    }

    public void dismiss() {
        if (overlayView != null && overlayView.getParent() != null) {
            View dialogCard = overlayView.findViewById(R.id.dialog_card);
            if (dialogCard != null) {
                dialogCard.animate().scaleX(0.9f).scaleY(0.9f).setDuration(120).start();
            }
            overlayView.animate().alpha(0f).setDuration(150)
                    .withEndAction(() -> {
                        if (overlayView != null && overlayView.getParent() != null) {
                            ((ViewGroup) overlayView.getParent()).removeView(overlayView);
                        }
                        if (dismissListener != null) {
                            dismissListener.onDismiss();
                        }
                    }).start();
        } else {
            if (dismissListener != null) {
                dismissListener.onDismiss();
            }
        }
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this.dismissListener = listener;
    }

    public boolean isShowing() {
        return overlayView != null && overlayView.getParent() != null;
    }
}
