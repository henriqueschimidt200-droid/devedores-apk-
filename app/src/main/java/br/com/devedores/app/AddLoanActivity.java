package br.com.devedores.app;

import android.app.*;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class AddLoanActivity extends Activity {
    DataStore ds;
    Models.Client client;
    EditText title, principal, interest, installments, firstDay, notes;
    Spinner freq, interestMode;
    Calendar due = Calendar.getInstance();
    LinearLayout preview, schedulePreview;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Ui.BG);
        getWindow().setNavigationBarColor(Ui.BG);
        ds = new DataStore(this);
        client = ds.client(getIntent().getStringExtra("clientId"));
        if (client == null) { finish(); return; }
        build();
    }

    void build() {
        LinearLayout p = Ui.col(this);
        ScrollView sc = new ScrollView(this);
        sc.setFillViewport(true); sc.setVerticalScrollBarEnabled(false); sc.addView(p); setContentView(sc); Ui.applySystemBars(this, p);

        LinearLayout top = Ui.row(this);
        Button back = Ui.btnDark(this, "‹  Voltar"); back.setOnClickListener(v -> finish());
        top.addView(back, new LinearLayout.LayoutParams(Ui.dp(this, 88), Ui.dp(this, 46)));
        LinearLayout tt = Ui.col(this); tt.setPadding(Ui.dp(this, 10), 0, 0, 0);
        tt.addView(Ui.title(this, "Novo contrato", 24));
        tt.addView(Ui.label(this, client.name + " • configure tudo antes de salvar"));
        top.addView(tt, new LinearLayout.LayoutParams(0, Ui.dp(this, 56), 1));
        p.addView(top); Ui.gap(this, p, 14);

        LinearLayout hero = Ui.heroCard(this, Ui.GOLD);
        hero.addView(Ui.eyebrow(this, "CRIAR EMPRÉSTIMO"));
        hero.addView(Ui.title(this, "Contrato sob medida", 22));
        hero.addView(Ui.label(this, "Defina principal, tipo de juros, periodicidade, primeiro vencimento e quantidade de parcelas."));
        p.addView(hero); Ui.gap(this, p, 14);

        p.addView(Ui.eyebrow(this, "IDENTIFICAÇÃO"));
        title = Ui.field(this, "Nome do contrato");
        p.addView(title, new LinearLayout.LayoutParams(-1, Ui.dp(this, 56))); Ui.gap(this, p, 9);

        p.addView(Ui.eyebrow(this, "VALORES"));
        principal = Ui.field(this, "Valor emprestado (R$)");
        interest = Ui.field(this, "Juros");
        p.addView(principal, new LinearLayout.LayoutParams(-1, Ui.dp(this, 56))); Ui.gap(this, p, 8);

        LinearLayout interestRow = Ui.row(this);
        interestMode = new Spinner(this);
        interestMode.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Percentual (%)", "Valor fixo (R$)"}));
        interestRow.addView(interestMode, new LinearLayout.LayoutParams(0, Ui.dp(this, 50), 0.45f)); Ui.gap(this, interestRow, 7);
        interestRow.addView(interest, new LinearLayout.LayoutParams(0, Ui.dp(this, 56), 0.55f));
        p.addView(interestRow); Ui.gap(this, p, 11);

        LinearLayout row = Ui.row(this);
        installments = Ui.field(this, "Parcelas");
        freq = new Spinner(this);
        freq.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Semanal", "Quinzenal", "Mensal"}));
        row.addView(installments, new LinearLayout.LayoutParams(0, Ui.dp(this, 56), 0.42f)); Ui.gap(this, row, 7);
        row.addView(freq, new LinearLayout.LayoutParams(0, Ui.dp(this, 50), 0.58f));
        p.addView(row); Ui.gap(this, p, 12);

        p.addView(Ui.eyebrow(this, "PRIMEIRO VENCIMENTO"));
        LinearLayout dateRow = Ui.row(this);
        firstDay = Ui.field(this, "Data");
        firstDay.setFocusable(false);
        firstDay.setText(dateTimeText(due.getTimeInMillis()));
        firstDay.setOnClickListener(v -> pickDateTime());
        dateRow.addView(firstDay, new LinearLayout.LayoutParams(0, Ui.dp(this, 56), 1));
        Button today = Ui.btnDark(this, "Hoje"); today.setOnClickListener(v -> { due = Calendar.getInstance(); due.set(Calendar.SECOND, 0); due.set(Calendar.MILLISECOND, 0); firstDay.setText(dateTimeText(due.getTimeInMillis())); updatePreview(); });
        dateRow.addView(today, new LinearLayout.LayoutParams(Ui.dp(this, 82), Ui.dp(this, 46)));
        p.addView(dateRow); Ui.gap(this, p, 12);

        notes = Ui.field(this, "Observações do contrato");
        p.addView(notes, new LinearLayout.LayoutParams(-1, Ui.dp(this, 92))); Ui.gap(this, p, 14);

        preview = Ui.heroCard(this, Ui.BLUE);
        p.addView(preview); Ui.gap(this, p, 10);

        schedulePreview = Ui.card(this);
        p.addView(schedulePreview); Ui.gap(this, p, 14);

        Button save = Ui.btn(this, "Criar contrato e agendar parcelas");
        save.setOnClickListener(v -> save());
        p.addView(save, new LinearLayout.LayoutParams(-1, Ui.dp(this, 56))); Ui.gap(this, p, 7);
        Button cancel = Ui.btnDark(this, "Cancelar"); cancel.setOnClickListener(v -> finish());
        p.addView(cancel, new LinearLayout.LayoutParams(-1, Ui.dp(this, 48)));

        android.text.TextWatcher tw = new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int before, int count) { updatePreview(); }
            public void afterTextChanged(android.text.Editable e) {}
        };
        principal.addTextChangedListener(tw); interest.addTextChangedListener(tw); installments.addTextChangedListener(tw);
        interestMode.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(android.widget.AdapterView<?> p) {}
            public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) { interest.setHint(pos == 0 ? "Juros (%)" : "Juros (R$)"); updatePreview(); }
        });
        freq.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(android.widget.AdapterView<?> p) {}
            public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) { updatePreview(); }
        });
        updatePreview();
    }

    String dateTimeText(long x) { return new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault()).format(new Date(x)); }
    String money(double x) { return String.format(Locale.getDefault(), "R$ %.2f", x); }

    double interestValue(double principalValue, double value) { return interestMode.getSelectedItemPosition() == 0 ? principalValue * value / 100d : value; }

    void updatePreview() {
        try {
            double p = parse(principal.getText().toString());
            double iv = interestValue(p, parse(interest.getText().toString()));
            int n = Integer.parseInt(installments.getText().toString());
            if (p <= 0 || n <= 0) throw new Exception();
            double total = p + iv, parcela = total / n;
            preview.removeAllViews();
            preview.addView(Ui.eyebrow(this, "PRÉVIA FINANCEIRA"));
            preview.addView(Ui.title(this, money(total), 28));
            preview.addView(Ui.label(this, String.format(Locale.getDefault(), "Principal %s  •  juros %s  •  %d parcelas de %s", money(p), money(iv), n, money(parcela))));
            schedulePreview.removeAllViews();
            schedulePreview.addView(Ui.eyebrow(this, "CRONOGRAMA"));
            int show = Math.min(n, 4);
            for (int i = 0; i < show; i++) {
                long when = dueAt(i);
                LinearLayout r = Ui.row(this);
                r.addView(Ui.pill(this, ""+(i + 1), Ui.BLUE, Ui.WHITE), new LinearLayout.LayoutParams(Ui.dp(this, 38), Ui.dp(this, 28)));
                LinearLayout tx = Ui.col(this); tx.setPadding(Ui.dp(this, 8), 0, 0, 0);
                tx.addView(Ui.title(this, new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(when)), 13));
                tx.addView(Ui.label(this, new SimpleDateFormat("EEEE 'às' HH:mm", Locale.getDefault()).format(new Date(when))));
                r.addView(tx, new LinearLayout.LayoutParams(0, Ui.dp(this, 46), 1));
                r.addView(Ui.title(this, money(parcela), 13));
                schedulePreview.addView(r);
                Ui.gap(this, schedulePreview, 4);
            }
            if (n > show) schedulePreview.addView(Ui.label(this, "+ " + (n - show) + " parcelas após estas"));
        } catch (Exception ignored) {
            preview.removeAllViews();
            preview.addView(Ui.eyebrow(this, "PRÉVIA FINANCEIRA"));
            preview.addView(Ui.label(this, "Preencha principal, juros e quantidade de parcelas para ver o cálculo."));
            schedulePreview.removeAllViews(); schedulePreview.addView(Ui.label(this, "O cronograma aparecerá automaticamente."));
        }
    }

    double parse(String s) {
        String x = s.replace("R$", "").trim();
        if (x.contains(",")) x = x.replace(".", "").replace(",", ".");
        return Double.parseDouble(x);
    }

    long dueAt(int i) {
        Calendar c = (Calendar) due.clone();
        if ("Semanal".equals(freq.getSelectedItem().toString())) c.add(Calendar.DAY_OF_YEAR, 7 * i);
        else if ("Quinzenal".equals(freq.getSelectedItem().toString())) c.add(Calendar.DAY_OF_YEAR, 14 * i);
        else c.add(Calendar.MONTH, i);
        return c.getTimeInMillis();
    }

    void pickDateTime() {
        Calendar base = (Calendar) due.clone();
        new DatePickerDialog(this, (v, y, m, d) -> {
            Calendar selected = (Calendar) base.clone(); selected.set(y, m, d);
            new TimePickerDialog(this, (tv, h, min) -> {
                selected.set(Calendar.HOUR_OF_DAY, h); selected.set(Calendar.MINUTE, min); selected.set(Calendar.SECOND, 0); selected.set(Calendar.MILLISECOND, 0);
                due = selected; firstDay.setText(dateTimeText(due.getTimeInMillis())); updatePreview();
            }, base.get(Calendar.HOUR_OF_DAY), base.get(Calendar.MINUTE), true).show();
        }, base.get(Calendar.YEAR), base.get(Calendar.MONTH), base.get(Calendar.DAY_OF_MONTH)).show();
    }

    void save() {
        try {
            double p = parse(principal.getText().toString());
            double rawInterest = parse(interest.getText().toString());
            int n = Integer.parseInt(installments.getText().toString());
            if (p <= 0 || n <= 0 || n > 360 || rawInterest < 0) throw new Exception();
            double iv = interestValue(p, rawInterest);
            Models.Loan l = new Models.Loan();
            l.title = title.getText().toString().trim().isEmpty() ? "Empréstimo" : title.getText().toString().trim();
            l.notes = notes.getText().toString(); l.principal = p; l.interestValue = iv;
            l.interestMode = interestMode.getSelectedItemPosition() == 0 ? "PERCENT" : "FIXED";
            l.interestPercent = l.interestMode.equals("PERCENT") ? rawInterest : (p == 0 ? 0 : rawInterest * 100d / p);
            l.total = p + iv; l.installments = n; l.frequency = freq.getSelectedItem().toString(); l.installmentAmount = l.total / n; l.firstDue = due.getTimeInMillis();
            client.loans.add(l); ds.save(); AlarmScheduler.scheduleAllInstallments(this, client, l);
            Toast.makeText(this, "Contrato criado • " + money(l.total), Toast.LENGTH_LONG).show(); finish();
        } catch (Exception e) { Toast.makeText(this, "Revise os valores informados.", Toast.LENGTH_LONG).show(); }
    }
}
