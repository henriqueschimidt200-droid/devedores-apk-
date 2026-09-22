package br.com.devedores.app;

import android.app.*;
import android.graphics.Color;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class LoanActivity extends Activity {
    DataStore ds; Models.Client client; Models.Loan loan; LinearLayout root;
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(Ui.BG);getWindow().setNavigationBarColor(Ui.BG);ds=new DataStore(this);load();}
    void load(){client=ds.client(getIntent().getStringExtra("clientId"));loan=ds.loan(getIntent().getStringExtra("loanId"));if(loan==null){finish();return;}build();}
    @Override protected void onResume(){super.onResume();if(ds!=null){ds.load();client=ds.client(getIntent().getStringExtra("clientId"));loan=ds.loan(getIntent().getStringExtra("loanId"));if(loan!=null)build();}}
    String money(double x){return String.format(Locale.getDefault(),"R$ %.2f",x);}

    void build(){
        root=Ui.col(this);ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.setVerticalScrollBarEnabled(false);sc.addView(root);setContentView(sc);
        LinearLayout top=Ui.row(this);Button back=Ui.btnDark(this,"‹  Voltar");back.setOnClickListener(v->finish());top.addView(back,new LinearLayout.LayoutParams(Ui.dp(this,88),Ui.dp(this,44)));LinearLayout tt=Ui.col(this);tt.setPadding(Ui.dp(this,10),0,0,0);tt.addView(Ui.title(this,loan.title,21));tt.addView(Ui.label(this,"Contrato de "+client.name));top.addView(tt,new LinearLayout.LayoutParams(0,Ui.dp(this,52),1));root.addView(top);Ui.gap(this,root,14);

        double pct=loan.total<=0?0:(loan.paid()/loan.total*100.0);boolean quitado=loan.balance()<=0.005;LinearLayout hero=Ui.heroCard(this,quitado?Ui.GREEN:Ui.GOLD);hero.addView(Ui.eyebrow(this,quitado?"CONTRATO QUITADO":"SALDO DEVIDO"));hero.addView(Ui.title(this,money(loan.balance()),30));hero.addView(Ui.label(this,"de um total de "+money(loan.total)));Ui.gap(this,hero,8);hero.addView(Ui.progress(this,(int)pct,100),new LinearLayout.LayoutParams(-1,Ui.dp(this,8)));Ui.gap(this,hero,4);hero.addView(Ui.label(this,String.format(Locale.getDefault(),"%.0f%% recebido",pct)));root.addView(hero);Ui.gap(this,root,10);

        LinearLayout r1=Ui.row(this);r1.addView(Ui.statCard(this,"EMPRESTADO",money(loan.principal),Ui.GOLD),new LinearLayout.LayoutParams(0,Ui.dp(this,84),1));Ui.gap(this,r1,6);r1.addView(Ui.statCard(this,"JUROS",money(loan.interestValue),Ui.RED),new LinearLayout.LayoutParams(0,Ui.dp(this,84),1));root.addView(r1);Ui.gap(this,root,7);
        LinearLayout r2=Ui.row(this);r2.addView(Ui.statCard(this,"RECEBIDO",money(loan.paid()),Ui.GREEN),new LinearLayout.LayoutParams(0,Ui.dp(this,84),1));Ui.gap(this,r2,6);r2.addView(Ui.statCard(this,"PARCELAS",String.valueOf(loan.installments),Ui.BLUE),new LinearLayout.LayoutParams(0,Ui.dp(this,84),1));root.addView(r2);Ui.gap(this,root,14);

        LinearLayout act=Ui.row(this);Button pay=Ui.btn(this,"Registrar pagamento");pay.setOnClickListener(v->pay());Button note=Ui.btnDark(this,"Observações");note.setOnClickListener(v->editNotes());act.addView(pay,new LinearLayout.LayoutParams(0,Ui.dp(this,48),1));Ui.gap(this,act,6);act.addView(note,new LinearLayout.LayoutParams(0,Ui.dp(this,48),1));root.addView(act);Ui.gap(this,root,16);

        root.addView(Ui.sectionTitle(this,"Parcelas e vencimentos"));Ui.gap(this,root,4);
        for(int i=0;i<loan.installments;i++){
            double pg=loan.paidForInstallment(i);long due=loan.dueAt(i);boolean paid=loan.installmentPaid(i);boolean late=!paid&&due<System.currentTimeMillis();int accent=paid?Ui.GREEN:late?Ui.RED:Ui.GOLD;String st=paid?"Paga":late?"Atrasada":"Pendente";double pctPar=loan.installmentAmount<=0?0:Math.min(100,pg/loan.installmentAmount*100.0);
            LinearLayout card=Ui.card(this);LinearLayout h=Ui.row(this);h.addView(Ui.title(this,"Parcela "+(i+1),16),new LinearLayout.LayoutParams(0,Ui.dp(this,30),1));h.addView(Ui.pill(this,st,accent,Ui.WHITE));card.addView(h);card.addView(Ui.label(this,new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm",Locale.getDefault()).format(new Date(due))));Ui.gap(this,card,3);card.addView(Ui.progress(this,(int)pctPar,100),new LinearLayout.LayoutParams(-1,Ui.dp(this,7)));Ui.gap(this,card,3);card.addView(Ui.text(this,"Pago "+money(pg)+" de "+money(loan.installmentAmount),13));Button rs=Ui.btnDark(this,"Alterar data e horário");final int idx=i;rs.setOnClickListener(v->reschedule(idx));card.addView(rs,new LinearLayout.LayoutParams(-1,Ui.dp(this,42)));root.addView(card);Ui.gap(this,root,7);
        }

        if(!loan.payments.isEmpty()){
            Ui.gap(this,root,8);root.addView(Ui.sectionTitle(this,"Histórico de pagamentos"));Ui.gap(this,root,4);
            for(Models.Payment p:loan.payments){LinearLayout c=Ui.card(this);LinearLayout r=Ui.row(this);TextView b=Ui.iconBadge(this,"✓");r.addView(b,new LinearLayout.LayoutParams(Ui.dp(this,38),Ui.dp(this,38)));LinearLayout tx=Ui.col(this);tx.setPadding(Ui.dp(this,10),0,0,0);tx.addView(Ui.title(this,money(p.amount),14));tx.addView(Ui.label(this,new SimpleDateFormat("dd/MM/yyyy HH:mm",Locale.getDefault()).format(new Date(p.date))));r.addView(tx,new LinearLayout.LayoutParams(0,Ui.dp(this,48),1));c.addView(r);root.addView(c);Ui.gap(this,root,6);}
        }
        if(!loan.notes.isEmpty()){Ui.gap(this,root,8);LinearLayout n=Ui.softCard(this,Ui.PURPLE);n.addView(Ui.eyebrow(this,"OBSERVAÇÕES"));n.addView(Ui.text(this,loan.notes,14));root.addView(n);}
    }

    void pay(){if(loan.balance()<=0){Toast.makeText(this,"Empréstimo já quitado",Toast.LENGTH_SHORT).show();return;}EditText e=Ui.field(this,"Valor pago (R$)");new AlertDialog.Builder(this).setTitle("Registrar pagamento").setView(e).setPositiveButton("Confirmar",(d,w)->{try{double a=Double.parseDouble(e.getText().toString().replace(',','.'));if(a<=0)throw new Exception();Models.Payment p=new Models.Payment();p.amount=Math.min(a,loan.balance());p.date=System.currentTimeMillis();loan.payments.add(p);ds.save();AlarmScheduler.cancelLoanAlarms(this,loan.id,loan.installments);AlarmScheduler.scheduleAllInstallments(this,client,loan);build();Toast.makeText(this,"Pagamento registrado",Toast.LENGTH_SHORT).show();}catch(Exception x){Toast.makeText(this,"Informe um valor válido.",Toast.LENGTH_SHORT).show();}}).setNegativeButton("Cancelar",null).show();}
    void reschedule(int index){Calendar c=Calendar.getInstance();c.setTimeInMillis(loan.dueAt(index));new DatePickerDialog(this,(v,y,m,d)->{Calendar x=Calendar.getInstance();x.set(y,m,d);new TimePickerDialog(this,(tv,h,min)->{x.set(Calendar.HOUR_OF_DAY,h);x.set(Calendar.MINUTE,min);x.set(Calendar.SECOND,0);x.set(Calendar.MILLISECOND,0);long delta=x.getTimeInMillis()-loan.dueAt(index);loan.firstDue+=delta;ds.save();AlarmScheduler.cancelLoanAlarms(this,loan.id,loan.installments);AlarmScheduler.scheduleAllInstallments(this,client,loan);build();},c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),true).show();},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();}
    void editNotes(){EditText e=Ui.field(this,"Observações do contrato");e.setText(loan.notes);new AlertDialog.Builder(this).setTitle("Observações").setView(e).setPositiveButton("Salvar",(d,w)->{loan.notes=e.getText().toString();ds.save();build();}).setNegativeButton("Cancelar",null).show();}
}
