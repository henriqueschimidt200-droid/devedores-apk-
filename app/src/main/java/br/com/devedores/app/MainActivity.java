package br.com.devedores.app;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    DataStore ds;
    LinearLayout root, list;
    EditText search;
    int filterIndex = 0;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Ui.BG);
        getWindow().setNavigationBarColor(Ui.BG);
        ds = new DataStore(this);
        requestNotif();
        build();
        if (!AlarmScheduler.canExact(this)) {
            new AlertDialog.Builder(this)
                    .setTitle("Ative os alarmes do Devedores")
                    .setMessage("Para receber os lembretes no horário mesmo com o app fechado, permita 'Alarmes e lembretes' no Android.")
                    .setPositiveButton("Ativar", (d,w)->AlarmScheduler.openExactSettings(this))
                    .setNegativeButton("Depois", null).show();
        }
    }

    void requestNotif() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 77);
    }

    @Override protected void onResume() {
        super.onResume();
        if (ds != null) ds.load();
        if (root != null) render();
        AlarmScheduler.rescheduleAll(this);
    }

    void build() {
        root = Ui.col(this);
        ScrollView sc = new ScrollView(this);
        sc.setFillViewport(true);
        sc.setClipToPadding(false);
        sc.setVerticalScrollBarEnabled(false);
        sc.addView(root);
        setContentView(sc);
    }

    void render() {
        root.removeAllViews();
        addHeader();
        addHero();
        addStats();
        addInsights();
        addQuickActions();
        addTodaySummary();
        addSearchAndFilter();
        addClients();
        addUpcoming();
        addBackup();
    }

    void addHeader() {
        LinearLayout top = Ui.row(this);
        TextView badge = Ui.iconBadge(this, "D");
        top.addView(badge, new LinearLayout.LayoutParams(Ui.dp(this, 46), Ui.dp(this, 46)));
        Ui.gap(this, top, 12);

        LinearLayout titles = Ui.col(this);
        titles.setPadding(0,0,0,0);
        titles.addView(Ui.title(this, "Devedores", 25), new LinearLayout.LayoutParams(-1, Ui.dp(this, 31)));
        titles.addView(Ui.label(this, "Gestão profissional de recebimentos"), new LinearLayout.LayoutParams(-1, Ui.dp(this, 25)));
        top.addView(titles, new LinearLayout.LayoutParams(0, Ui.dp(this, 52), 1));

        Button add = Ui.btn(this, "+ Cliente");
        add.setOnClickListener(v -> startActivity(new Intent(this, AddClientActivity.class)));
        top.addView(add, new LinearLayout.LayoutParams(Ui.dp(this, 102), Ui.dp(this, 46)));
        root.addView(top);
        Ui.gap(this, root, 7);

        TextView date = Ui.label(this, new SimpleDateFormat("EEEE, dd 'de' MMMM", new Locale("pt","BR")).format(new Date()));
        date.setTextColor(Color.rgb(128, 140, 157));
        root.addView(date);
        Ui.gap(this, root, 14);
    }

    void addHero() {
        double receivable = 0, received = 0;
        for (Models.Client c : ds.clients) for (Models.Loan l : c.loans) {
            receivable += l.balance();
            received += l.paid();
        }
        LinearLayout hero = Ui.heroCard(this, Ui.GOLD);
        LinearLayout top = Ui.row(this);
        LinearLayout copy = Ui.col(this);
        copy.setPadding(0,0,0,0);
        copy.addView(Ui.eyebrow(this, "CARTEIRA A RECEBER"));
        copy.addView(Ui.title(this, money(receivable), 29), new LinearLayout.LayoutParams(-1, Ui.dp(this, 42)));
        copy.addView(Ui.label(this, "Valor em aberto nos seus contratos"));
        top.addView(copy, new LinearLayout.LayoutParams(0, Ui.dp(this, 92), 1));
        TextView icon = Ui.iconBadge(this, "↗");
        top.addView(icon, new LinearLayout.LayoutParams(Ui.dp(this, 50), Ui.dp(this, 50)));
        hero.addView(top);
        hero.addView(Ui.divider(this), new LinearLayout.LayoutParams(-1, 1));
        LinearLayout foot = Ui.row(this);
        foot.addView(Ui.label(this, "Recebido no histórico: "+money(received)), new LinearLayout.LayoutParams(0, Ui.dp(this, 32), 1));
        Button cal = Ui.btnGhost(this, "Ver calendário");
        cal.setOnClickListener(v -> startActivity(new Intent(this, CalendarActivity.class)));
        foot.addView(cal, new LinearLayout.LayoutParams(Ui.dp(this, 112), Ui.dp(this, 34)));
        hero.addView(foot);
        root.addView(hero);
        Ui.gap(this, root, 12);
    }

    void addStats() {
        double total=0, received=0, balance=0; int overdue=0; long now=System.currentTimeMillis();
        for (Models.Client c: ds.clients) for (Models.Loan l: c.loans) {
            total += l.total; received += l.paid(); balance += l.balance();
            for(int i=0;i<l.installments;i++) if(!l.installmentPaid(i) && l.dueAt(i)<now) overdue++;
        }
        LinearLayout row1=Ui.row(this), row2=Ui.row(this);
        row1.addView(Ui.statCard(this,"CONTRATOS",money(total),Ui.GOLD),weightParams());
        row1.addView(Ui.statCard(this,"RECEBIDO",money(received),Ui.GREEN),weightParams());
        root.addView(row1); Ui.gap(this,root,7);
        row2.addView(Ui.statCard(this,"A RECEBER",money(balance),Ui.BLUE),weightParams());
        row2.addView(Ui.statCard(this,"ATRASADAS",String.valueOf(overdue),Ui.RED),weightParams());
        root.addView(row2); Ui.gap(this,root,15);
    }

    LinearLayout.LayoutParams weightParams(){
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,Ui.dp(this,86),1);
        p.setMargins(0,0,Ui.dp(this,7),0);
        return p;
    }

    String money(double x){return String.format(Locale.getDefault(),"R$ %.2f",x);}

    void addInsights() {
        double overdueValue=0, monthReceived=0; int active=0, due7=0;
        Calendar now=Calendar.getInstance(); Calendar month=Calendar.getInstance(); month.set(Calendar.DAY_OF_MONTH,1); month.set(Calendar.HOUR_OF_DAY,0); month.set(Calendar.MINUTE,0); month.set(Calendar.SECOND,0); month.set(Calendar.MILLISECOND,0);
        long nowMs=System.currentTimeMillis(); long seven=nowMs+7L*24*60*60*1000;
        for(Models.Client c:ds.clients){ boolean has=false; for(Models.Loan l:c.loans){ if(l.balance()>0.005) has=true; for(Models.Payment p:l.payments){if(p.date>=month.getTimeInMillis()&&p.date<=nowMs)monthReceived+=p.amount;} for(int i=0;i<l.installments;i++){ if(l.installmentPaid(i)) continue; long due=l.dueAt(i); if(due<nowMs) overdueValue+=Math.max(0,l.installmentAmount-l.paidForInstallment(i)); else if(due<=seven) due7++; } } if(has)active++; }
        root.addView(Ui.sectionTitle(this,"Visão executiva")); Ui.gap(this,root,4);
        LinearLayout r1=Ui.row(this), r2=Ui.row(this);
        r1.addView(Ui.statCard(this,"A VENCER EM 7 DIAS",String.valueOf(due7),Ui.GOLD),weightParams());
        r1.addView(Ui.statCard(this,"CLIENTES ATIVOS",String.valueOf(active),Ui.BLUE),weightParams());
        root.addView(r1); Ui.gap(this,root,7);
        r2.addView(Ui.statCard(this,"RECEBIDO NO MÊS",money(monthReceived),Ui.GREEN),weightParams());
        r2.addView(Ui.statCard(this,"VALOR EM ATRASO",money(overdueValue),Ui.RED),weightParams());
        root.addView(r2); Ui.gap(this,root,15);
    }

    void addQuickActions() {
        root.addView(Ui.sectionTitle(this,"Ações rápidas"));
        Ui.gap(this, root, 2);
        LinearLayout row=Ui.row(this);
        Button cal=Ui.btnDark(this,"Calendário"); cal.setOnClickListener(v->startActivity(new Intent(this,CalendarActivity.class)));
        Button rem=Ui.btnDark(this,"Lembrete"); rem.setOnClickListener(v->newReminder());
        Button newLoan=Ui.btnDark(this,"Novo empréstimo"); newLoan.setOnClickListener(v->chooseClientForLoan());
        row.addView(cal,new LinearLayout.LayoutParams(0,Ui.dp(this,46),1));Ui.gap(this,row,6);
        row.addView(rem,new LinearLayout.LayoutParams(0,Ui.dp(this,46),1));Ui.gap(this,row,6);
        row.addView(newLoan,new LinearLayout.LayoutParams(0,Ui.dp(this,46),1));
        root.addView(row); Ui.gap(this,root,15);
    }

    void addTodaySummary() {
        int today=0, overdue=0, clients=ds.clients.size();
        long start=dayStart(System.currentTimeMillis()), end=start+24L*60*60*1000-1;
        for(Models.Client c:ds.clients) for(Models.Loan l:c.loans) for(int i=0;i<l.installments;i++) {
            if(l.installmentPaid(i)) continue;
            long due=l.dueAt(i);
            if(due>=start && due<=end) today++;
            if(due<start) overdue++;
        }
        LinearLayout card=Ui.softCard(this, today>0?Ui.GOLD:Ui.GREEN);
        LinearLayout r=Ui.row(this);
        TextView icon=Ui.iconBadge(this, today>0?"!":"✓");
        r.addView(icon,new LinearLayout.LayoutParams(Ui.dp(this,42),Ui.dp(this,42)));
        LinearLayout text=Ui.col(this);text.setPadding(Ui.dp(this,11),0,0,0);
        text.addView(Ui.title(this,today>0?today+" vencimento(s) hoje":"Tudo em dia hoje",16));
        text.addView(Ui.label(this, overdue>0?overdue+" parcela(s) já estão atrasadas":"Nenhum atraso além do previsto"));
        r.addView(text,new LinearLayout.LayoutParams(0,Ui.dp(this,52),1));
        TextView c=Ui.pill(this,clients+" clientes",Ui.BLUE,Ui.WHITE);r.addView(c,new LinearLayout.LayoutParams(Ui.dp(this,104),Ui.dp(this,32)));
        card.addView(r);root.addView(card);Ui.gap(this,root,15);
    }

    long dayStart(long t){Calendar c=Calendar.getInstance();c.setTimeInMillis(t);c.set(Calendar.HOUR_OF_DAY,0);c.set(Calendar.MINUTE,0);c.set(Calendar.SECOND,0);c.set(Calendar.MILLISECOND,0);return c.getTimeInMillis();}

    void chooseClientForLoan(){
        if(ds.clients.isEmpty()){Toast.makeText(this,"Cadastre um cliente primeiro.",Toast.LENGTH_SHORT).show();return;}
        String[] names=new String[ds.clients.size()];for(int i=0;i<ds.clients.size();i++)names[i]=ds.clients.get(i).name;
        new AlertDialog.Builder(this).setTitle("Escolha o cliente").setItems(names,(d,w)->{Intent i=new Intent(this,AddLoanActivity.class);i.putExtra("clientId",ds.clients.get(w).id);startActivity(i);}).setNegativeButton("Cancelar",null).show();
    }

    void addSearchAndFilter(){
        LinearLayout h=Ui.row(this);
        h.addView(Ui.sectionTitle(this,"Clientes"),new LinearLayout.LayoutParams(0,Ui.dp(this,38),1));
        h.addView(Ui.label(this,ds.clients.size()+" cadastrados"),new LinearLayout.LayoutParams(Ui.dp(this,100),Ui.dp(this,38)));
        root.addView(h);
        search=Ui.searchField(this,"Buscar por nome, CPF ou telefone");
        search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){renderClients();}public void afterTextChanged(android.text.Editable e){}});
        root.addView(search,new LinearLayout.LayoutParams(-1,Ui.dp(this,50)));
        Ui.gap(this,root,8);
        LinearLayout chips=Ui.row(this);
        String[] names={"Todos","Em aberto","Atrasados","Quitados"};
        for(int i=0;i<names.length;i++){
            final int idx=i;
            Button b=Ui.btnGhost(this,names[i]);
            b.setBackground(UiChip(this,idx==filterIndex?Ui.GOLD:Ui.SURFACE_2,idx==filterIndex?Ui.BG:Ui.MUTED));
            b.setOnClickListener(v->{filterIndex=idx;addSearchAndFilterRefresh();});
            chips.addView(b,new LinearLayout.LayoutParams(0,Ui.dp(this,36),1));
            if(i<names.length-1)Ui.gap(this,chips,4);
        }
        root.addView(chips);Ui.gap(this,root,8);
    }

    android.graphics.drawable.GradientDrawable UiChip(int bg,int text){
        android.graphics.drawable.GradientDrawable g=new android.graphics.drawable.GradientDrawable();g.setColor(bg);g.setCornerRadius(Ui.dp(this,14));g.setStroke(1,Ui.BORDER);return g;
    }

    void addSearchAndFilterRefresh(){
        // Rebuild only the client header/filter area cleanly.
        render();
        if(search!=null){search.requestFocus();search.setSelection(search.length());}
    }

    boolean matches(Models.Client c){
        String q=search==null?"":search.getText().toString().trim().toLowerCase(Locale.getDefault());
        boolean txt=q.isEmpty()||c.name.toLowerCase(Locale.getDefault()).contains(q)||c.cpf.toLowerCase(Locale.getDefault()).contains(q)||c.phone.toLowerCase(Locale.getDefault()).contains(q);
        double bal=0;boolean overdue=false;for(Models.Loan l:c.loans){bal+=l.balance();for(int i=0;i<l.installments;i++)if(!l.installmentPaid(i)&&l.dueAt(i)<System.currentTimeMillis())overdue=true;}
        return txt&&(filterIndex==0||(filterIndex==1&&bal>0.005)||(filterIndex==2&&overdue)||(filterIndex==3&&bal<=0.005));
    }

    void addClients(){
        list=Ui.col(this);list.setPadding(0,0,0,0);root.addView(list);renderClients();
        if(ds.clients.isEmpty()){
            LinearLayout empty=Ui.heroCard(this,Ui.GOLD);
            empty.addView(Ui.eyebrow(this,"PRIMEIRO PASSO"));
            empty.addView(Ui.title(this,"Crie seu primeiro cliente",20));
            empty.addView(Ui.label(this,"Depois você poderá guardar documentos, contratos, parcelas, pagamentos e lembretes em uma única pasta."));
            Ui.gap(this,empty,8);
            Button b=Ui.btn(this,"+ Cadastrar cliente");b.setOnClickListener(v->startActivity(new Intent(this,AddClientActivity.class)));empty.addView(b,new LinearLayout.LayoutParams(-1,Ui.dp(this,48)));list.addView(empty);
        }
    }

    void renderClients(){
        if(list==null)return;list.removeAllViews();int shown=0;
        for(Models.Client c:ds.clients){if(!matches(c))continue;shown++;
            LinearLayout card=Ui.card(this);
            LinearLayout top=Ui.row(this);
            View icon=(c.profileImagePath!=null&&!c.profileImagePath.isEmpty())?Ui.profileImage(this,c.profileImagePath,c.name,44):Ui.avatar(this,c.name);top.addView(icon,new LinearLayout.LayoutParams(Ui.dp(this,44),Ui.dp(this,44)));
            LinearLayout meta=Ui.col(this);meta.setPadding(Ui.dp(this,11),0,0,0);
            meta.addView(Ui.title(this,c.name,16));
            meta.addView(Ui.label(this,(c.phone.isEmpty()?"Sem telefone":c.phone)+"  •  "+c.documents.size()+" arquivo(s)  •  "+c.folders.size()+" pastas"));
            top.addView(meta,new LinearLayout.LayoutParams(0,Ui.dp(this,54),1));
            double bal=0;int overdue=0;for(Models.Loan l:c.loans){bal+=l.balance();for(int i=0;i<l.installments;i++)if(!l.installmentPaid(i)&&l.dueAt(i)<System.currentTimeMillis())overdue++;}
            top.addView(Ui.pill(this,overdue>0?"Atrasado":bal>0?"Em aberto":"Quitado",overdue>0?Ui.RED:bal>0?Ui.BLUE:Ui.GREEN,Ui.WHITE));card.addView(top);
            card.addView(Ui.divider(this),new LinearLayout.LayoutParams(-1,1));
            LinearLayout info=Ui.row(this);
            LinearLayout left=Ui.col(this);left.setPadding(0,0,0,0);left.addView(Ui.eyebrow(this,"SALDO"));left.addView(Ui.title(this,money(bal),17));info.addView(left,new LinearLayout.LayoutParams(0,Ui.dp(this,55),1));
            LinearLayout right=Ui.col(this);right.setPadding(0,0,0,0);right.addView(Ui.eyebrow(this,"CONTRATOS"));right.addView(Ui.label(this,c.loans.size()+" contrato(s)  •  "+(overdue>0?overdue+" atrasada(s)":"sem atrasos")));
            long next=Long.MAX_VALUE; for(Models.Loan l:c.loans) for(int i=0;i<l.installments;i++) if(!l.installmentPaid(i)&&l.dueAt(i)>=System.currentTimeMillis()&&l.dueAt(i)<next) next=l.dueAt(i);
            right.addView(Ui.label(this,next==Long.MAX_VALUE?"Sem próximo vencimento":"Próximo: "+new SimpleDateFormat("dd/MM",Locale.getDefault()).format(new Date(next))));info.addView(right,new LinearLayout.LayoutParams(0,Ui.dp(this,55),1));card.addView(info);
            LinearLayout acts=Ui.row(this);Button open=Ui.btnDark(this,"Abrir pasta");open.setOnClickListener(v->{Intent i=new Intent(this,ClientActivity.class);i.putExtra("id",c.id);startActivity(i);});Button loan=Ui.btn(this,"+ Empréstimo");loan.setOnClickListener(v->{Intent i=new Intent(this,AddLoanActivity.class);i.putExtra("clientId",c.id);startActivity(i);});acts.addView(open,new LinearLayout.LayoutParams(0,Ui.dp(this,44),1));Ui.gap(this,acts,6);acts.addView(loan,new LinearLayout.LayoutParams(0,Ui.dp(this,44),1));card.addView(acts);
            list.addView(card);Ui.gap(this,list,8);
        }
        if(shown==0 && !ds.clients.isEmpty()){LinearLayout no=Ui.softCard(this,Ui.BLUE);no.addView(Ui.title(this,"Nenhum resultado",16));no.addView(Ui.label(this,"Tente mudar a busca ou o filtro selecionado."));list.addView(no);}
    }

    void addUpcoming(){
        Ui.gap(this,root,7);root.addView(Ui.sectionTitle(this,"Próximos vencimentos"));
        LinearLayout up=Ui.col(this);up.setPadding(0,0,0,0);long now=System.currentTimeMillis();int count=0;
        for(Models.Client c:ds.clients)for(Models.Loan l:c.loans)for(int i=0;i<l.installments;i++){
            if(l.installmentPaid(i)||l.dueAt(i)<now)continue;
            long due=l.dueAt(i);LinearLayout card=Ui.card(this);LinearLayout row=Ui.row(this);
            TextView badge=Ui.pill(this,new SimpleDateFormat("dd/MM",Locale.getDefault()).format(new Date(due)),Ui.GOLD,Ui.WHITE);row.addView(badge,new LinearLayout.LayoutParams(Ui.dp(this,58),Ui.dp(this,32)));
            LinearLayout t=Ui.col(this);t.setPadding(Ui.dp(this,10),0,0,0);t.addView(Ui.title(this,c.name,14));t.addView(Ui.label(this,"Parcela "+(i+1)+" • "+new SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date(due))));row.addView(t,new LinearLayout.LayoutParams(0,Ui.dp(this,48),1));row.addView(Ui.title(this,money(l.installmentAmount),14));card.addView(row);up.addView(card);Ui.gap(this,up,6);count++;if(count>=4)break;
        }
        if(count==0)up.addView(Ui.label(this,"Nenhum vencimento próximo."));root.addView(up);
    }

    void addBackup(){Ui.gap(this,root,15);root.addView(Ui.sectionTitle(this,"Dados e segurança"));Ui.gap(this,root,2);LinearLayout r=Ui.row(this);Button e=Ui.btnDark(this,"Exportar backup");Button i=Ui.btnDark(this,"Restaurar backup");e.setOnClickListener(v->exportBackup());i.setOnClickListener(v->importBackup());r.addView(e,new LinearLayout.LayoutParams(0,Ui.dp(this,46),1));Ui.gap(this,r,7);r.addView(i,new LinearLayout.LayoutParams(0,Ui.dp(this,46),1));root.addView(r);}

    void newReminder(){LinearLayout p=Ui.col(this);p.setPadding(0,0,0,0);EditText t=Ui.field(this,"Título");EditText d=Ui.field(this,"Detalhes");p.addView(t);Ui.gap(this,p,7);p.addView(d);new AlertDialog.Builder(this).setTitle("Novo lembrete").setView(p).setPositiveButton("Escolher data",(x,w)->pickDateTime(t.getText().toString(),d.getText().toString())).setNegativeButton("Cancelar",null).show();}
    void pickDateTime(String title,String details){Calendar c=Calendar.getInstance();new DatePickerDialog(this,(v,y,m,day)->{Calendar x=Calendar.getInstance();x.set(y,m,day);new TimePickerDialog(this,(tv,h,min)->{x.set(Calendar.HOUR_OF_DAY,h);x.set(Calendar.MINUTE,min);x.set(Calendar.SECOND,0);x.set(Calendar.MILLISECOND,0);Models.Reminder r=new Models.Reminder();r.title=title.trim().isEmpty()?"Lembrete":title.trim();r.details=details;r.when=x.getTimeInMillis();ds.reminders.add(r);ds.save();AlarmScheduler.schedule(this,"reminder",r.id,r.title,r.when);Toast.makeText(this,"Lembrete salvo",Toast.LENGTH_SHORT).show();render();},c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),true).show();},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();}
    void exportBackup(){Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("application/json");i.putExtra(Intent.EXTRA_TITLE,"devedores-backup.json");startActivityForResult(i,301);}
    void importBackup(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("application/json");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,302);}
    @Override protected void onActivityResult(int r,int res,Intent data){super.onActivityResult(r,res,data);if(res!=RESULT_OK||data==null)return;try{if(r==301){OutputStream out=getContentResolver().openOutputStream(data.getData());out.write(ds.exportJson().getBytes("UTF-8"));out.close();Toast.makeText(this,"Backup exportado",Toast.LENGTH_SHORT).show();}else if(r==302){InputStream in=getContentResolver().openInputStream(data.getData());java.io.ByteArrayOutputStream b=new java.io.ByteArrayOutputStream();byte[] x=new byte[8192];int n;while((n=in.read(x))>0)b.write(x,0,n);in.close();if(ds.importJson(new String(b.toByteArray(),"UTF-8"))){AlarmScheduler.rescheduleAll(this);render();Toast.makeText(this,"Backup restaurado",Toast.LENGTH_SHORT).show();}else Toast.makeText(this,"Backup inválido",Toast.LENGTH_LONG).show();}}catch(Exception e){Toast.makeText(this,"Não foi possível concluir",Toast.LENGTH_LONG).show();}}
}
