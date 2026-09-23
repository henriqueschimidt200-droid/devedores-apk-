package br.com.devedores.app;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Space;
import android.widget.TextView;

import android.app.Activity;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;

public class Ui {
    public static final int BG = Color.rgb(7, 10, 15);
    public static final int SURFACE = Color.rgb(14, 18, 25);
    public static final int SURFACE_2 = Color.rgb(20, 26, 35);
    public static final int SURFACE_3 = Color.rgb(28, 35, 46);
    public static final int BORDER = Color.rgb(43, 51, 64);
    public static final int MUTED = Color.rgb(148, 160, 177);
    public static final int WHITE = Color.rgb(245, 247, 251);
    public static final int GOLD = Color.rgb(245, 190, 59);
    public static final int GOLD_DARK = Color.rgb(133, 93, 14);
    public static final int DARK = Color.rgb(42, 48, 58);
    public static final int GREEN = Color.rgb(63, 207, 133);
    public static final int RED = Color.rgb(245, 94, 102);
    public static final int BLUE = Color.rgb(92, 159, 255);
    public static final int PURPLE = Color.rgb(168, 128, 255);
    public static final int CYAN = Color.rgb(63, 202, 216);
    public static final int ORANGE = Color.rgb(255, 149, 77);

    public static int dp(Context c, int n) {
        return (int) (n * c.getResources().getDisplayMetrics().density + 0.5f);
    }

    private static GradientDrawable bg(Context c, int color, int radius, int strokeColor) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(c, radius));
        if (strokeColor != Color.TRANSPARENT) g.setStroke(dp(c, 1), strokeColor);
        return g;
    }

    private static GradientDrawable gradient(Context c, int[] colors, int radius, int strokeColor) {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR, colors);
        g.setCornerRadius(dp(c, radius));
        if (strokeColor != Color.TRANSPARENT) g.setStroke(dp(c, 1), strokeColor);
        return g;
    }

    private static int withAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    public static TextView text(Context c, String s, int sp) {
        TextView v = new TextView(c);
        v.setText(s);
        v.setTextSize(sp);
        v.setTextColor(WHITE);
        v.setGravity(Gravity.CENTER_VERTICAL);
        v.setPadding(dp(c, 2), dp(c, 4), dp(c, 2), dp(c, 4));
        v.setIncludeFontPadding(false);
        return v;
    }

    public static TextView title(Context c, String s, int sp) {
        TextView v = text(c, s, sp);
        v.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        return v;
    }

    public static TextView label(Context c, String s) {
        TextView v = text(c, s, 12);
        v.setTextColor(MUTED);
        return v;
    }

    public static TextView eyebrow(Context c, String s) {
        TextView v = text(c, s == null ? "" : s.toUpperCase(), 10);
        v.setTextColor(GOLD);
        v.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        return v;
    }

    public static TextView sectionTitle(Context c, String s) {
        return title(c, s, 20);
    }

    /**
     * Keeps the UI away from status/navigation bars on Android 15+ while still
     * allowing the app to use the full display. This prevents headers/buttons
     * from being clipped on phones with gesture navigation or display cutouts.
     */
    public static void applySystemBars(Activity activity, View content) {
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
        activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        activity.getWindow().setNavigationBarColor(Color.TRANSPARENT);
        try {
            WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView())
                    .setAppearanceLightStatusBars(false);
            WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView())
                    .setAppearanceLightNavigationBars(false);
        } catch (Exception ignored) {}

        final int baseLeft = content.getPaddingLeft();
        final int baseTop = content.getPaddingTop();
        final int baseRight = content.getPaddingRight();
        final int baseBottom = content.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(baseLeft + bars.left, baseTop + bars.top,
                    baseRight + bars.right, baseBottom + bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(content);
    }

    public static LinearLayout sectionHeader(Context c, String title, String action, View.OnClickListener click) {
        LinearLayout row = row(c);
        TextView t = sectionTitle(c, title);
        row.addView(t, new LinearLayout.LayoutParams(0, dp(c, 38), 1));
        if (action != null) {
            Button b = btnGhost(c, action);
            b.setOnClickListener(click);
            row.addView(b, new LinearLayout.LayoutParams(dp(c, 98), dp(c, 38)));
        }
        return row;
    }

    public static TextView pill(Context c, String s, int color, int textColor) {
        TextView v = text(c, s, 11);
        v.setGravity(Gravity.CENTER);
        v.setTextColor(textColor);
        v.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        v.setPadding(dp(c, 11), 0, dp(c, 11), 0);
        v.setBackground(bg(c, withAlpha(color, 22), 30, withAlpha(color, 90)));
        return v;
    }

    public static Button btn(Context c, String s) {
        Button b = buttonBase(c, s);
        b.setTextColor(Color.rgb(14, 15, 19));
        b.setTextSize(14);
        b.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        b.setBackground(gradient(c, new int[]{GOLD, Color.rgb(255, 209, 89)}, 15, Color.TRANSPARENT));
        b.setElevation(dp(c, 2));
        return b;
    }

    public static Button btnDark(Context c, String s) {
        Button b = buttonBase(c, s);
        b.setTextColor(WHITE);
        b.setTextSize(13);
        b.setBackground(bg(c, SURFACE_2, 14, BORDER));
        b.setElevation(dp(c, 1));
        return b;
    }

    public static Button btnGhost(Context c, String s) {
        Button b = buttonBase(c, s);
        b.setTextColor(GOLD);
        b.setTextSize(12);
        b.setBackground(bg(c, Color.TRANSPARENT, 13, Color.TRANSPARENT));
        b.setPadding(dp(c, 6), 0, dp(c, 6), 0);
        return b;
    }

    public static Button btnDanger(Context c, String s) {
        Button b = buttonBase(c, s);
        b.setTextColor(RED);
        b.setTextSize(13);
        b.setBackground(bg(c, withAlpha(RED, 16), 14, withAlpha(RED, 70)));
        return b;
    }

    private static Button buttonBase(Context c, String s) {
        Button b = new Button(c);
        b.setText(s);
        b.setAllCaps(false);
        b.setGravity(Gravity.CENTER);
        b.setMinHeight(dp(c, 48));
        b.setMinWidth(0);
        b.setStateListAnimator(null);
        b.setPadding(dp(c, 15), 0, dp(c, 15), 0);
        b.setMaxLines(2);
        b.setEllipsize(null);
        return b;
    }

    public static EditText field(Context c, String hint) {
        EditText e = new EditText(c);
        e.setHint(hint);
        e.setTextColor(WHITE);
        e.setHintTextColor(Color.rgb(104, 115, 132));
        e.setTextSize(16);
        e.setSingleLine(false);
        e.setIncludeFontPadding(false);
        e.setPadding(dp(c, 15), dp(c, 12), dp(c, 15), dp(c, 12));
        e.setBackground(bg(c, SURFACE_2, 14, BORDER));
        return e;
    }

    public static EditText searchField(Context c, String hint) {
        EditText e = field(c, hint);
        e.setSingleLine(true);
        e.setTextSize(14);
        e.setPadding(dp(c, 16), 0, dp(c, 16), 0);
        return e;
    }

    public static LinearLayout col(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(c, 20), dp(c, 18), dp(c, 20), dp(c, 42));
        l.setBackgroundColor(BG);
        return l;
    }

    public static LinearLayout row(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    public static LinearLayout card(Context c) {
        LinearLayout l = col(c);
        l.setPadding(dp(c, 18), dp(c, 18), dp(c, 18), dp(c, 18));
        l.setBackground(bg(c, SURFACE, 18, BORDER));
        l.setElevation(dp(c, 2));
        return l;
    }

    public static LinearLayout softCard(Context c, int accent) {
        LinearLayout l = card(c);
        l.setBackground(gradient(c, new int[]{withAlpha(accent, 30), withAlpha(SURFACE_2, 245)}, 18, withAlpha(accent, 75)));
        return l;
    }

    public static LinearLayout heroCard(Context c, int accent) {
        LinearLayout l = card(c);
        l.setBackground(gradient(c,
                new int[]{withAlpha(accent, 68), withAlpha(SURFACE_2, 250), SURFACE}, 22, withAlpha(accent, 95)));
        l.setPadding(dp(c, 18), dp(c, 18), dp(c, 18), dp(c, 18));
        l.setElevation(dp(c, 4));
        return l;
    }

    public static LinearLayout statCard(Context c, String caption, String value, int accent) {
        LinearLayout l = card(c);
        l.setPadding(dp(c, 14), dp(c, 12), dp(c, 14), dp(c, 12));
        TextView cap = text(c, caption, 10);
        cap.setTextColor(MUTED);
        cap.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        TextView val = title(c, value, 17);
        val.setTextColor(WHITE);
        l.addView(cap, new LinearLayout.LayoutParams(-1, dp(c, 22)));
        l.addView(val, new LinearLayout.LayoutParams(-1, dp(c, 34)));
        View line = new View(c);
        line.setBackground(bg(c, accent, 3, Color.TRANSPARENT));
        l.addView(line, new LinearLayout.LayoutParams(dp(c, 38), dp(c, 3)));
        return l;
    }

    public static LinearLayout miniStat(Context c, String icon, String value, String caption, int accent) {
        LinearLayout card = softCard(c, accent);
        LinearLayout r = row(c);
        TextView ic = iconBadge(c, icon);
        r.addView(ic, new LinearLayout.LayoutParams(dp(c, 38), dp(c, 38)));
        LinearLayout copy = col(c);
        copy.setPadding(dp(c, 10), 0, 0, 0);
        copy.addView(title(c, value, 16));
        copy.addView(label(c, caption));
        r.addView(copy, new LinearLayout.LayoutParams(0, dp(c, 52), 1));
        card.addView(r);
        return card;
    }

    public static TextView iconBadge(Context c, String s) {
        TextView v = text(c, s, 17);
        v.setGravity(Gravity.CENTER);
        v.setTextColor(GOLD);
        v.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        v.setBackground(bg(c, withAlpha(GOLD, 22), 15, withAlpha(GOLD, 75)));
        v.setPadding(0, 0, 0, 0);
        return v;
    }

    public static LinearLayout infoTile(Context c, String label, String value, int accent) {
        LinearLayout l = softCard(c, accent);
        TextView a = eyebrow(c, label);
        TextView b = title(c, value, 14);
        l.addView(a, new LinearLayout.LayoutParams(-1, dp(c, 22)));
        l.addView(b, new LinearLayout.LayoutParams(-1, dp(c, 34)));
        return l;
    }

    public static ProgressBar progress(Context c, int progress, int max) {
        ProgressBar p = new ProgressBar(c, null, android.android.R.attr.progressBarStyleHorizontal);
        p.setMax(Math.max(1, max));
        p.setProgress(Math.max(0, Math.min(progress, max)));
        p.setIndeterminate(false);
        p.setProgressTintList(ColorStateList.valueOf(GOLD));
        p.setProgressBackgroundTintList(ColorStateList.valueOf(BORDER));
        p.setPadding(0, 0, 0, 0);
        return p;
    }

    public static View divider(Context c) {
        View v = new View(c);
        v.setBackgroundColor(BORDER);
        return v;
    }

    public static void gap(Context c, LinearLayout p, int h) {
        Space s = new Space(c);
        p.addView(s, new LinearLayout.LayoutParams(1, dp(c, h)));
    }

    public static ImageView profileImage(Context c, String path, String name, int size) {
        ImageView iv = new ImageView(c);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        android.graphics.Bitmap bm = null;
        try { if (path != null && !path.trim().isEmpty()) bm = android.graphics.BitmapFactory.decodeFile(new File(path).getAbsolutePath()); } catch (Exception ignored) {}
        if (bm != null) {
            RoundedBitmapDrawable d = RoundedBitmapDrawableFactory.create(c.getResources(), bm);
            d.setCircular(true);
            iv.setImageDrawable(d);
        } else {
            iv.setImageResource(android.android.R.drawable.ic_menu_camera);
        }
        iv.setBackground(bg(c, SURFACE_3, size / 2, BORDER));
        iv.setPadding(dp(c, 6), dp(c, 6), dp(c, 6), dp(c, 6));
        return iv;
    }

    public static TextView avatar(Context c, String name) {
        String initial = "C";
        if (name != null) {
            String clean = name.trim();
            if (!clean.isEmpty()) initial = clean.substring(0, 1).toUpperCase();
        }
        TextView v = iconBadge(c, initial);
        v.setTextSize(16);
        return v;
    }
}
