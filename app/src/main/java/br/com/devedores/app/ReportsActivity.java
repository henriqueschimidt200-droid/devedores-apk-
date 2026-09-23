package br.com.devedores.app;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.*;

public class ReportsActivity extends Activity {
    DataStore ds;
    LinearLayout root;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Ui.BG); getWindow().setNavigationBarColor(Ui.BG);
        ds = new DataStore(this); build();
    }

    String money(double x) { return String.format(Locale.getDefault(), "R$ %.2f", x); }

    void build() {
        root = Ui.col(this);
        ScrollView sc = new ScrollView(this); sc.setFillViewport(true); sc.setVerticalScrollBarEnabled(false); sc.addView(root); setContentView(sc); Ui.applySystemBars(this, root);

        LinearLayout top = Ui.row(this);
        Button back = Ui.btnDark(this, "‹  Voltar"); back.setOnClickListener(v -> finish());
        top.addView(back, new LinearLayout.LayoutParams(Ui.dp(this, 88), Ui.dp(this, 46)));
        LinearLayout tt = Ui.col(this); tt.setPadding(Ui.dp(this, 10), 0, 0, 0);
        tt.addView(Ui.title(this, "Relatórios", 23)); tt.addView(Ui.label(this, "Visão financeira da sua carteira"));
        top.addView(tt, new LinearLayout.LayoutParams(0, Ui.dp(this, 54), 1)); root.addView(top); Ui.gap(this, root, 14);

        double total=0, paid=0, balance=0, overdueValue=0; int overdueCount=0, active=0;
        long now = System.currentTimeMillis();
        for (Models.Client c : ds.clients) {
            boolean activeClient=false;
            for (Models.Loan l : c.loans) {
                total += l.total; paid += l.paid(); balance += l.balance(); if (l.balance()>0.005) activeClient=true;
                for(int i=0;i<l.installments;i++) if(!l.installmentPaid(i) && l.dueAt(i)<now) { overdueCount++; overdueValue += Math.max(0, l.installmentAmount-l.paidForInstallment(i)); }
            }
            if(activeClient) active++;
        }

        double pct = total <= 0 ? 0 : paid/total*100d;
        LinearLayout hero = Ui.heroCard(this, overdueCount>0?Ui.RED:Ui.GOLD);
        hero.addView(Ui.eyebrow(this, "RESUMO DA CARTEIRA")); hero.addView(Ui.title(this, money(balance), 30));
        hero.addView(Ui.label(this, "Saldo ainda em aberto")); Ui.gap(this, hero, 8);
        hero.addView(Ui.progress(this, (int)Math.min(100,pct), 100), new LinearLayout.LayoutParams(-1, Ui.dp(this, 8)));
        Ui.gap(this, hero, 5); hero.addView(Ui.label(this, String.format(Locale.getDefault(), "%.0f%% recebido de %s contratados", pct, money(total))));
        root.addView(hero); Ui.gap(this, root, 10);

        LinearLayout r1=Ui.row(this), r2=Ui.row(this);
        r1.addView(Ui.statCard(this,"TOTAL CONTRATADO",money(total),Ui.GOLD), lp()); Ui.gap(this,r1,7); r1.addView(Ui.statCard(this,"RECEBIDO",money(paid),Ui.GREEN),lp());
        r2.addView(Ui.statCard(this,"EM ABERTO",money(balance),Ui.BLUE),lp()); Ui.gap(this,r2,7); r2.addView(Ui.statCard(this,"EM ATRASO",money(overdueValue),Ui.RED),lp());
        root.addView(r1); Ui.gap(this,root,7); root.addView(r2); Ui.gap(this,root,16);

        root.addView(Ui.sectionTitle(this,"Recebimentos dos últimos 6 meses")); Ui.gap(this,root,4);
        addSixMonthChart(); Ui.gap(this,root,16);

        root.addView(Ui.sectionTitle(this,"Clientes com maior saldo")); Ui.gap(this,root,4);
        List<Models.Client> sorted = new ArrayList<>(ds.clients);
        Collections.sort(sorted, (a,b)->Double.compare(balanceOf(b), balanceOf(a)));
        int show=0;
        for(Models.Client c:sorted){ if(balanceOf(c)<=0.005) continue; show++; LinearLayout card=Ui.card(this); LinearLayout rr=Ui.row(this); rr.addView(c.profileImagePath==null||c.profileImagePath.isEmpty()?Ui.avatar(this,c.name):Ui.profileImage(this,c.profileImagePath,c.name,42),new LinearLayout.LayoutParams(Ui.dp(this,42),Ui.dp(this,42))); LinearLayout tx=Ui.col(this); tx.setPadding(Ui.dp(this,10),0,0,0); tx.addView(Ui.title(this,c.name,15)); tx.addView(Ui.label(this,c.phone.isEmpty()?"":c.phone)); rr.addView(tx,new LinearLayout.LayoutParams(0,Ui.dp(this,50),1)); rr.addView(Ui.title(this,money(balanceOf(c)),14)); card.addView(rr); root.addView(card); Ui.gap(this,root,7); if(show>=8) break; }
        if(show==0) root.addView(Ui.label(this,"Nenhum cliente possui saldo em aberto."));
        Ui.gap(this,root,10); root.addView(Ui.softCard(this,Ui.BLUE)); LinearLayout info=Ui.card(this); // kept as a visual footer
        root.removeViewAt(root.getChildCount()-1); LinearLayout footer=Ui.softCard(this,Ui.BLUE); footer.addView(Ui.eyebrow(this,"INDICADORES")); footer.addView(Ui.text(this,"Clientes ativos: "+active+"  •  Parcelas atrasadas: "+overdueCount,14)); footer.addView(Ui.label(this,"Use o calendário e os filtros de clientes para agir nas pendências.")); root.addView(footer);
    }

    LinearLayout.LayoutParams lp(){return new LinearLayout.LayoutParams(0,Ui.dp(this,86),1);}
    double balanceOf(Models.Client c){double x=0;for(Models.Loan l:c.loans)x+=l.balance();return x;}

    void addSixMonthChart(){
        Calendar start=Calendar.getInstance(); start.set(Calendar.DAY_OF_MONTH,1); start.set(Calendar.HOUR_OF_DAY,0); start.set(Calendar.MINUTE,0); start.set(Calendar.SECOND,0); start.set(Calendar.MILLISECOND,0); start.add(Calendar.MONTH,-5);
        double[] vals=new double[6]; double max=0; Calendar probe=(Calendar)start.clone();
        for(int m=0;m<6;m++){ Calendar end=(Calendar)probe.clone(); end.add(Calendar.MONTH,1); double sum=0; for(Models.Client c:ds.clients)for(Models.Loan l:c.loans)for(Models.Payment p:l.payments)if(p.date>=probe.getTimeInMillis()&&p.date<end.getTimeInMillis())sum+=p.amount;vals[m]=sum;if(sum>max)max=sum;probe=end; }
        for(int i=0;i<6;i++){ Calendar labelCal=(Calendar)start.clone();labelCal.add(Calendar.MONTH,i); LinearLayout card=Ui.card(this); LinearLayout r=Ui.row(this); TextView lab=Ui.text(this,new SimpleDateFormat("MMM",new Locale("pt","BR")).format(labelCal.getTime()),12); lab.setTextColor(Ui.MUTED); r.addView(lab,new LinearLayout.LayoutParams(Ui.dp(this,38),Ui.dp(this,30))); LinearLayout barBox=Ui.row(this); View bar=new View(this); bar.setBackgroundColor(i==5?Ui.GOLD:Ui.BLUE); int width=max<=0?2:(int)Math.max(4,Ui.dp(this,190)*(vals[i]/max)); barBox.addView(bar,new LinearLayout.LayoutParams(width,Ui.dp(this,16))); r.addView(barBox,new LinearLayout.LayoutParams(0,Ui.dp(this,30),1)); r.addView(Ui.title(this,money(vals[i]),12)); card.addView(r); root.addView(card); Ui.gap(this,root,5); }
    }
}
