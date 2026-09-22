package br.com.devedores.app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Space;
import android.widget.TextView;

public class Ui {
    public static final int BG = Color.rgb(8, 11, 16);
    public static final int SURFACE = Color.rgb(15, 19, 26);
    public static final int SURFACE_2 = Color.rgb(22, 27, 36);
    public static final int SURFACE_3 = Color.rgb(28, 34, 45);
    public static final int BORDER = Color.rgb(44, 52, 65);
    public static final int MUTED = Color.rgb(145, 156, 173);
    public static final int WHITE = Color.rgb(246, 248, 252);
    public static final int GOLD = Color.rgb(242, 191, 61);
    public static final int GOLD_DARK = Color.rgb(129, 92, 16);
    public static final int GREEN = Color.rgb(63, 205, 132);
    public static final int RED = Color.rgb(244, 96, 96);
    public static final int BLUE = Color.rgb(89, 157, 255);
    public static final int PURPLE = Color.rgb(161, 121, 255);

    public static int dp(Context c, int n) {
        return (int) (n * c.getResources().getDisplayMetrics().density + 0.5f);
    }

    private static GradientDrawable bg(Context c, int color, int radius, int strokeColor) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(c, radius));
        if (strokeColor != Color.TRANSPARENT) g.setStroke(1, strokeColor);
        return g;
    }

    private static int withAlpha(int color, int alpha) { return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color)); }

    public static TextView text(Context c, String s, int sp) {
        TextView v = new TextView(c);
        v.setText(s);
        v.setTextSize(sp);
        v.setTextColor(WHITE);
        v.setGravity(Gravity.CENTER_VERTICAL);
        v.setPadding(dp(c, 4), dp(c, 5), dp(c, 4), dp(c, 5));
        v.setIncludeFontPadding(false);
        v.setFontFeatureSettings("kern");
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
        TextView v = text(c, s.toUpperCase(), 10);
        v.setTextColor(GOLD);
        v.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        return v;
    }

    public static TextView sectionTitle(Context c, String s) {
        return title(c, s, 18);
    }

    public static LinearLayout sectionHeader(Context c, String title, String action, View.OnClickListener click) {
        LinearLayout row = row(c);
        TextView t = sectionTitle(c, title);
        row.addView(t, new LinearLayout.LayoutParams(0, dp(c, 38), 1));
        if (action != null) {
            Button b = btnGhost(c, action);
            b.setOnClickListener(click);
            row.addView(b, new LinearLayout.LayoutParams(dp(c, 92), dp(c, 38)));
        }
        return row;
    }

    public static TextView pill(Context c, String s, int color, int textColor) {
        TextView v = text(c, s, 11);
        v.setGravity(Gravity.CENTER);
        v.setTextColor(textColor);
        v.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        v.setPadding(dp(c, 11), 0, dp(c, 11), 0);
        v.setBackground(bg(c, withAlpha(color, 24), 30, withAlpha(color, 90)));
        return v;
    }

    public static Button btn(Context c, String s) {
        Button b = buttonBase(c, s);
        b.setTextColor(Color.rgb(13, 15, 19));
        b.setTextSize(14);
        b.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        b.setBackground(bg(c, GOLD, 16, Color.TRANSPARENT));
        return b;
    }

    public static Button btnDark(Context c, String s) {
        Button b = buttonBase(c, s);
        b.setTextColor(WHITE);
        b.setTextSize(13);
        b.setBackground(bg(c, SURFACE_2, 15, BORDER));
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
        b.setMinHeight(dp(c, 46));
        b.setMinWidth(0);
        b.setStateListAnimator(null);
        b.setPadding(dp(c, 13), 0, dp(c, 13), 0);
        return b;
    }

    public static EditText field(Context c, String hint) {
        EditText e = new EditText(c);
        e.setHint(hint);
        e.setTextColor(WHITE);
        e.setHintTextColor(Color.rgb(111, 121, 137));
        e.setTextSize(15);
        e.setSingleLine(false);
        e.setIncludeFontPadding(false);
        e.setPadding(dp(c, 14), dp(c, 11), dp(c, 14), dp(c, 11));
        e.setBackground(bg(c, SURFACE_2, 15, BORDER));
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
        l.setPadding(dp(c, 18), dp(c, 14), dp(c, 18), dp(c, 30));
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
        l.setPadding(dp(c, 16), dp(c, 15), dp(c, 16), dp(c, 15));
        l.setBackground(bg(c, SURFACE, 18, BORDER));
        return l;
    }

    public static LinearLayout softCard(Context c, int accent) {
        LinearLayout l = card(c);
        l.setBackground(bg(c, withAlpha(accent, 16), 18, withAlpha(accent, 72)));
        return l;
    }

    public static LinearLayout heroCard(Context c, int accent) {
        LinearLayout l = card(c);
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{withAlpha(accent, 54), SURFACE_2, SURFACE});
        g.setCornerRadius(dp(c, 22));
        g.setStroke(dp(c, 1), withAlpha(accent, 90));
        l.setBackground(g);
        l.setPadding(dp(c, 18), dp(c, 18), dp(c, 18), dp(c, 18));
        return l;
    }

    public static LinearLayout statCard(Context c, String caption, String value, int accent) {
        LinearLayout l = card(c);
        l.setPadding(dp(c, 14), dp(c, 13), dp(c, 14), dp(c, 13));
        TextView cap = text(c, caption, 10);
        cap.setTextColor(MUTED);
        cap.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        TextView val = title(c, value, 17);
        val.setTextColor(WHITE);
        l.addView(cap, new LinearLayout.LayoutParams(-1, dp(c, 22)));
        l.addView(val, new LinearLayout.LayoutParams(-1, dp(c, 34)));
        View line = new View(c);
        line.setBackgroundColor(accent);
        l.addView(line, new LinearLayout.LayoutParams(dp(c, 32), dp(c, 3)));
        return l;
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

    public static ImageView profileImage(Context c, String path, String name, int size) {
        ImageView iv = new ImageView(c);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        android.graphics.Bitmap bm = null;
        try { if (path != null && !path.trim().isEmpty()) bm = BitmapFactory.decodeFile(path); } catch (Exception ignored) {}
        if (bm != null) {
            RoundedBitmapDrawable d = RoundedBitmapDrawableFactory.create(c.getResources(), bm);
            d.setCircular(true);
            iv.setImageDrawable(d);
            iv.setBackground(bg(c, SURFACE_3, size / 2, BORDER));
        } else {
            TextView fallback = avatar(c, name);
            iv.setImageDrawable(null);
            iv.setBackground(bg(c, withAlpha(GOLD, 24), size / 2, withAlpha(GOLD, 80)));
            iv.setTag(name == null ? "C" : name.substring(0, Math.min(1, name.length())).toUpperCase());
            iv.setContentDescription(name);
        }
        return iv;
    }

    public static TextView avatar(Context c, String name) {
        String initial = "C";
        if (name != null) {
            String clean = name.trim();
            if (!clean.isEmpty()) initial = clean.substring(0, 1).toUpperCase();
        }
        TextView v = iconBadge(c, initial);
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
        ProgressBar p = new ProgressBar(c, null, android.R.attr.progressBarStyleHorizontal);
        p.setMax(Math.max(1, max));
        p.setProgress(Math.max(0, Math.min(progress, max)));
        p.setIndeterminate(false);
        p.setProgressTintList(android.content.res.ColorStateList.valueOf(GOLD));
        p.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(BORDER));
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
}
