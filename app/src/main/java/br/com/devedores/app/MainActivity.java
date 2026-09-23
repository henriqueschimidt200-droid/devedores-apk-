package br.com.devedores.app;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    DataStore ds;
    LinearLayout root, clientList;
    EditText search;
    int filterIndex = 0;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Ui.BG); getWindow().setNavigationBarColor(Ui.BG);
        ds = new DataStore(this); requestNotif(); build();
        if (!AlarmScheduler.canExact(this)) new AlertDialog.Builder(this)
                .setTitle("Ative os alarmes do Devedores")
                .setMessage("Permita 'Alarmes e lembretes' para receber notificações no horário mesmo quando o app estiver fechado.")
                .setPositiveButton("Ativar", (d,w)->AlarmScheduler.openExactSettings(this))
                .setNegativeButton("Depois", null).show();
    }

    void requestNotif() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 77);
    }

    @Override protected void onResume() { super.onResume(); if(ds!=null){ ds.load(); if(root!=null) render(); AlarmScheduler.rescheduleAll(this); } }

    void build() { root = Ui.col(this); ScrollView sc = new ScrollView(this); sc.setFillViewport(true); sc.setVerticalScrollBarEnabled(false); sc.addView(root); setContentView(sc); Ui.applySystemBars(this, root); }

    String money(double x) { return String.format(Locale.getDefault(), "R$ %.2f", x); }

    void render() {
        root.removeAllViews();
        header(); hero(); metrics(); criticalAlerts(); quickActions(); clientSection(); upcomingSection(); securitySection(); footer();
    }

    void header() {
        LinearLayout row = Ui.row(this);
        TextView brand = Ui.iconBadge(this, "D"); row.addView(brand,new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48)));
        Ui.gap(this,row,10);
        LinearLayout title = Ui.col(this); title.setPadding(0,0,0,0);
        title.addView(Ui.title(this,"Devedores",25));
        title.addView(Ui.label(this,new SimpleDateFormat("EEEE, dd/MM • HH:mm",new Locale("pt","BR")).format(new Date())));
        row.addView(title,new LinearLayout.LayoutParams(0,Ui.dp(this,54),1));
        Button report=Ui.btnDark(this,"Relatório"); report.setOnClickListener(v->startActivity(new Intent(this,ReportsActivity.class))); row.addView(report,new LinearLayout.LayoutParams(Ui.dp(this,90),Ui.dp(this,44)));
        root.addView(row); Ui.gap(this,root,12);
    }

    void hero() {
        double total=0, paid=0, balance=0; int overdue=0;
        for(Models.Client c:ds.clients)for(Models.Loan l:c.loans){total+=l.total;paid+=l.paid();balance+=l.balance();for(int i=0;i<l.installments;i++)if(!l.installmentPaid(i)&&l.dueAt(i)<System.currentTimeMillis())overdue++;}
        double pct=total<=0?0:paid/total*100d;
        LinearLayout h=Ui.heroCard(this,overdue>0?Ui.RED:Ui.GOLD);
        LinearLayout top=Ui.row(this);
        LinearLayout copy=Ui.col(this);copy.setPadding(0,0,0,0);copy.addView(Ui.eyebrow(this,overdue>0?"AÇÃO NECESSÁRIA":"CARTEIRA ATIVA"));copy.addView(Ui.title(this,money(balance),31));copy.addView(Ui.label(this,"saldo total a receber"));top.addView(copy,new LinearLayout.LayoutParams(0,Ui.dp(this,88),1));
        TextView ring=Ui.iconBadge(this,(int)pct+"%"); ring.setTextSize(13); top.addView(ring,new LinearLayout.LayoutParams(Ui.dp(this,54),Ui.dp(this,54))); h.addView(top);
        Ui.gap(this,h,7); h.addView(Ui.progress(this,(int)Math.min(100,pct),100),new LinearLayout.LayoutParams(-1,Ui.dp(this,8))); Ui.gap(this,h,4);
        h.addView(Ui.label(this,String.format(Locale.getDefault(),"%s recebido de %s contratados",money(paid),money(total))));
        LinearLayout foot=Ui.row(this); TextView state=Ui.pill(this,overdue==0?"Carteira saudável":overdue+" parcela(s) atrasada(s)",overdue==0?Ui.GREEN:Ui.RED,Ui.WHITE);foot.addView(state,new LinearLayout.LayoutParams(0,Ui.dp(this,34),1));Button cal=Ui.btnGhost(this,"Abrir agenda");cal.setOnClickListener(v->startActivity(new Intent(this,CalendarActivity.class)));foot.addView(cal,new LinearLayout.LayoutParams(Ui.dp(this,110),Ui.dp(this,34)));h.addView(foot);
        root.addView(h); Ui.gap(this,root,14);
    }

    void metrics() {
        double receivedMonth=0, overdueValue=0, next7=0; int active=0, overdueCount=0; Calendar month=Calendar.getInstance();month.set(Calendar.DAY_OF_MONTH,1);month.set(Calendar.HOUR_OF_DAY,0);month.set(Calendar.MINUTE,0);month.set(Calendar.SECOND,0);month.set(Calendar.MILLISECOND,0);long now=System.currentTimeMillis(),seven=now+7L*86400000L;
        for(Models.Client c:ds.clients){boolean ac=false;for(Models.Loan l:c.loans){if(l.balance()>0.005)ac=true;for(Models.Payment p:l.payments)if(p.date>=month.getTimeInMillis()&&p.date<=now)receivedMonth+=p.amount;for(int i=0;i<l.installments;i++){if(l.installmentPaid(i))continue;long d=l.dueAt(i);if(d<now){overdueCount++;overdueValue+=Math.max(0,l.installmentAmount-l.paidForInstallment(i));}else if(d<=seven)next7+=Math.max(0,l.installmentAmount-l.paidForInstallment(i));}}if(ac)active++;}
        root.addView(Ui.sectionTitle(this,"Visão executiva")); Ui.gap(this,root,4);
        LinearLayout r1=Ui.row(this),r2=Ui.row(this);
        r1.addView(Ui.miniStat(this,"R","+"+money(receivedMonth),"Recebido no mês",Ui.GREEN),new LinearLayout.LayoutParams(0,Ui.dp(this,72),1));Ui.gap(this,r1,7);r1.addView(Ui.miniStat(this,"!",String.valueOf(overdueCount),"Parcelas atrasadas",Ui.RED),new LinearLayout.LayoutParams(0,Ui.dp(this,72),1));
        r2.addView(Ui.miniStat(this,"7",money(next7),"Vencimentos em 7 dias",Ui.GOLD),new LinearLayout.LayoutParams(0,Ui.dp(this,72),1));Ui.gap(this,r2,7);r2.addView(Ui.miniStat(this,"C",String.valueOf(active),"Clientes ativos",Ui.BLUE),new LinearLayout.LayoutParams(0,Ui.dp(this,72),1));
        root.addView(r1);Ui.gap(this,root,7);root.addView(r2);Ui.gap(this,root,15);
    }

    void criticalAlerts() {
        root.addView(Ui.sectionHeader(this,"Pendências críticas","Ver todas",v->{filterIndex=2;render();})); Ui.gap(this,root,4);
        int count=0; long now=System.currentTimeMillis();
        for(Models.Client c:ds.clients)for(Models.Loan l:c.loans)for(int i=0;i<l.installments;i++){
            if(l.installmentPaid(i)||l.dueAt(i)>=now)continue;
            count++; LinearLayout card=Ui.softCard(this,Ui.RED);LinearLayout r=Ui.row(this);
            TextView icon=Ui.iconBadge(this,"!");r.addView(icon,new LinearLayout.LayoutParams(Ui.dp(this,40),Ui.dp(this,40)));
            LinearLayout tx=Ui.col(this);tx.setPadding(Ui.dp(this,9),0,0,0);tx.addView(Ui.title(this,c.name,15));int days=Math.max(1,(int)((now-l.dueAt(i))/86400000L));tx.addView(Ui.label(this,"Parcela "+(i+1)+" • "+days+" dia(s) atrasada • "+money(Math.max(0,l.installmentAmount-l.paidForInstallment(i)))));r.addView(tx,new LinearLayout.LayoutParams(0,Ui.dp(this,50),1));
            Button open=Ui.btnDark(this,"Abrir");final String cid=c.id,lid=l.id;open.setOnClickListener(v->{Intent x=new Intent(this,LoanActivity.class);x.putExtra("clientId",cid);x.putExtra("loanId",lid);startActivity(x);});r.addView(open,new LinearLayout.LayoutParams(Ui.dp(this,62),Ui.dp(this,42)));card.addView(r);root.addView(card);Ui.gap(this,root,6);if(count>=3)break;
            }
        if(count==0){LinearLayout ok=Ui.softCard(this,Ui.GREEN);ok.addView(Ui.title(this,"Nenhuma pendência crítica",14));ok.addView(Ui.label(this,"Até agora, a agenda não tem parcela vencida."));root.addView(ok);}Ui.gap(this,root,15);
    }

    void quickActions() {
        root.addView(Ui.sectionTitle(this,"Ações rápidas"));Ui.gap(this,root,4);
        LinearLayout r1=Ui.row(this),r2=Ui.row(this);
        Button c=Ui.btn(this,"＋ Cliente");c.setOnClickListener(v->startActivity(new Intent(this,AddClientActivity.class)));
        Button l=Ui.btnDark(this,"＋ Empréstimo");l.setOnClickListener(v->chooseClientForLoan());
        Button r=Ui.btnDark(this,"🔔 Lembrete");r.setOnClickListener(v->newReminder());
        Button cal=Ui.btnDark(this,"▦ Calendário");cal.setOnClickListener(v->startActivity(new Intent(this,CalendarActivity.class)));
        r1.addView(c,new LinearLayout.LayoutParams(0,Ui.dp(this,48),1));Ui.gap(this,r1,6);r1.addView(l,new LinearLayout.LayoutParams(0,Ui.dp(this,48),1));
        r2.addView(r,new LinearLayout.LayoutParams(0,Ui.dp(this,46),1));Ui.gap(this,r2,6);r2.addView(cal,new LinearLayout.LayoutParams(0,Ui.dp(this,46),1));
        root.addView(r1);Ui.gap(this,root,6);root.addView(r2);Ui.gap(this,root,15);
    }

    void clientSection() {
        root.addView(Ui.sectionHeader(this,"Clientes","+ Novo",v->startActivity(new Intent(this,AddClientActivity.class)))); Ui.gap(this,root,4);
        search=Ui.searchField(this,"⌕  Buscar por nome, CPF ou telefone");search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){renderClientList();}public void afterTextChanged(Editable e){}});root.addView(search,new LinearLayout.LayoutParams(-1,Ui.dp(this,48)));Ui.gap(this,root,7);
        LinearLayout chips=Ui.row(this);String[] names={"Todos","Abertos","Atrasados","Quitados"};for(int i=0;i<names.length;i++){final int idx=i;Button b=Ui.btnGhost(this,names[i]);b.setTextColor(idx==filterIndex?Ui.BG:Ui.MUTED);b.setBackground(UiChip(idx==filterIndex?Ui.GOLD:Ui.SURFACE_2));b.setOnClickListener(v->{String q=search==null?"":search.getText().toString();filterIndex=idx;render();if(search!=null){search.setText(q);search.setSelection(search.length());}});chips.addView(b,new LinearLayout.LayoutParams(0,Ui.dp(this,36),1));if(i<3)Ui.gap(this,chips,4);}root.addView(chips);Ui.gap(this,root,8);
        clientList=Ui.col(this);clientList.setPadding(0,0,0,0);root.addView(clientList);renderClientList();
    }

    android.graphics.drawable.GradientDrawable UiChip(int bg){android.graphics.drawable.GradientDrawable g=new android.graphics.drawable.GradientDrawable();g.setColor(bg);g.setCornerRadius(Ui.dp(this,16));g.setStroke(Ui.dp(this,1),Ui.BORDER);return g;}

    void renderClientList(){if(clientList==null)return;clientList.removeAllViews();int shown=0;for(Models.Client c:ds.clients){if(!matches(c))continue;shown++;double bal=0,received=0;int overdue=0;long next=Long.MAX_VALUE;for(Models.Loan l:c.loans){bal+=l.balance();received+=l.paid();for(int i=0;i<l.installments;i++){if(!l.installmentPaid(i)&&l.dueAt(i)<System.currentTimeMillis())overdue++;if(!l.installmentPaid(i)&&l.dueAt(i)>=System.currentTimeMillis())next=Math.min(next,l.dueAt(i));}}LinearLayout card=Ui.card(this);LinearLayout top=Ui.row(this);View avatar=(c.profileImagePath!=null&&!c.profileImagePath.isEmpty())?Ui.profileImage(this,c.profileImagePath,c.name,50):Ui.avatar(this,c.name);top.addView(avatar,new LinearLayout.LayoutParams(Ui.dp(this,50),Ui.dp(this,50)));LinearLayout tx=Ui.col(this);tx.setPadding(Ui.dp(this,11),0,0,0);tx.addView(Ui.title(this,c.name,16));tx.addView(Ui.label(this,(c.phone.isEmpty()?"Sem telefone":c.phone)+" • "+c.loans.size()+" contrato(s)"));top.addView(tx,new LinearLayout.LayoutParams(0,Ui.dp(this,58),1));top.addView(Ui.pill(this,overdue>0?"Atrasado":bal>0?"Em aberto":"Quitado",overdue>0?Ui.RED:bal>0?Ui.BLUE:Ui.GREEN,Ui.WHITE));card.addView(top);Ui.gap(this,card,7);card.addView(Ui.progress(this,(int)(bal<=0?100:(received/(Math.max(0.01,received+bal))*100)),100),new LinearLayout.LayoutParams(-1,Ui.dp(this,6)));Ui.gap(this,card,5);LinearLayout foot=Ui.row(this);LinearLayout a=Ui.col(this);a.setPadding(0,0,0,0);a.addView(Ui.eyebrow(this,"SALDO"));a.addView(Ui.title(this,money(bal),15));foot.addView(a,new LinearLayout.LayoutParams(0,Ui.dp(this,48),1));LinearLayout b=Ui.col(this);b.setPadding(0,0,0,0);b.addView(Ui.eyebrow(this,"PRÓXIMO"));b.addView(Ui.label(this,next==Long.MAX_VALUE?"—":new SimpleDateFormat("dd/MM HH:mm",Locale.getDefault()).format(new Date(next))));foot.addView(b,new LinearLayout.LayoutParams(0,Ui.dp(this,48),1));card.addView(foot);LinearLayout actions=Ui.row(this);Button open=Ui.btnDark(this,"Abrir pasta");final String id=c.id;open.setOnClickListener(v->{Intent i=new Intent(this,ClientActivity.class);i.putExtra("id",id);startActivity(i);});Button loan=Ui.btn(this,"＋ Empréstimo");loan.setOnClickListener(v->{Intent i=new Intent(this,AddLoanActivity.class);i.putExtra("clientId",id);startActivity(i);});actions.addView(open,new LinearLayout.LayoutParams(0,Ui.dp(this,43),1));Ui.gap(this,actions,6);actions.addView(loan,new LinearLayout.LayoutParams(0,Ui.dp(this,43),1));card.addView(actions);clientList.addView(card);Ui.gap(this,clientList,8);}if(ds.clients.isEmpty()){LinearLayout e=Ui.heroCard(this,Ui.GOLD);e.addView(Ui.eyebrow(this,"COMEÇE AGORA"));e.addView(Ui.title(this,"Nenhum cliente cadastrado",20));e.addView(Ui.label(this,"Crie o primeiro perfil para liberar pastas, contratos, imagens e cobranças."));Ui.gap(this,e,8);Button b=Ui.btn(this,"＋ Cadastrar primeiro cliente");b.setOnClickListener(v->startActivity(new Intent(this,AddClientActivity.class)));e.addView(b,new LinearLayout.LayoutParams(-1,Ui.dp(this,50)));clientList.addView(e);}else if(shown==0){clientList.addView(Ui.softCard(this,Ui.BLUE));TextView t=Ui.label(this,"Nenhum cliente corresponde à busca e ao filtro atuais.");((LinearLayout)clientList.getChildAt(clientList.getChildCount()-1)).addView(t);}}

    boolean matches(Models.Client c){String q=search==null?"":search.getText().toString().trim().toLowerCase(Locale.getDefault());boolean txt=q.isEmpty()||c.name.toLowerCase(Locale.getDefault()).contains(q)||c.cpf.toLowerCase(Locale.getDefault()).contains(q)||c.phone.toLowerCase(Locale.getDefault()).contains(q);double bal=0;boolean overdue=false;for(Models.Loan l:c.loans){bal+=l.balance();for(int i=0;i<l.installments;i++)if(!l.installmentPaid(i)&&l.dueAt(i)<System.currentTimeMillis())overdue=true;}return txt&&(filterIndex==0||(filterIndex==1&&bal>0.005)||(filterIndex==2&&overdue)||(filterIndex==3&&bal<=0.005));}

    void upcomingSection(){Ui.gap(this,root,14);root.addView(Ui.sectionHeader(this,"Próximos recebimentos","Abrir agenda",v->startActivity(new Intent(this,CalendarActivity.class))));Ui.gap(this,root,4);int shown=0;long now=System.currentTimeMillis();for(Models.Client c:ds.clients)for(Models.Loan l:c.loans)for(int i=0;i<l.installments;i++){if(l.installmentPaid(i)||l.dueAt(i)<now)continue;long due=l.dueAt(i);LinearLayout card=Ui.card(this);LinearLayout r=Ui.row(this);r.addView(Ui.pill(this,new SimpleDateFormat("dd/MM",Locale.getDefault()).format(new Date(due)),Ui.GOLD,Ui.WHITE),new LinearLayout.LayoutParams(Ui.dp(this,60),Ui.dp(this,32)));LinearLayout tx=Ui.col(this);tx.setPadding(Ui.dp(this,10),0,0,0);tx.addView(Ui.title(this,c.name,14));tx.addView(Ui.label(this,"Parcela "+(i+1)+" • "+new SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date(due))));r.addView(tx,new LinearLayout.LayoutParams(0,Ui.dp(this,46),1));r.addView(Ui.title(this,money(Math.max(0,l.installmentAmount-l.paidForInstallment(i))),13));card.addView(r);root.addView(card);Ui.gap(this,root,6);shown++;if(shown>=5)break;}if(shown==0)root.addView(Ui.label(this,"Nenhum recebimento futuro cadastrado."));}

    void securitySection(){Ui.gap(this,root,14);root.addView(Ui.sectionHeader(this,"Segurança e dados",null,null));Ui.gap(this,root,4);LinearLayout r=Ui.row(this);Button ex=Ui.btnDark(this,"Exportar backup");Button im=Ui.btnDark(this,"Restaurar backup");Button rel=Ui.btnDark(this,"Relatórios");ex.setOnClickListener(v->exportBackup());im.setOnClickListener(v->importBackup());rel.setOnClickListener(v->startActivity(new Intent(this,ReportsActivity.class)));r.addView(ex,new LinearLayout.LayoutParams(0,Ui.dp(this,44),1));Ui.gap(this,r,5);r.addView(im,new LinearLayout.LayoutParams(0,Ui.dp(this,44),1));root.addView(r);Ui.gap(this,root,5);root.addView(rel,new LinearLayout.LayoutParams(-1,Ui.dp(this,44)));}
    void footer(){Ui.gap(this,root,18);LinearLayout f=Ui.softCard(this,Ui.GREEN);f.addView(Ui.eyebrow(this,"DE VERSÃO 5.0"));f.addView(Ui.title(this,"Tudo salvo no aparelho",15));f.addView(Ui.label(this,"Clientes, imagens, contratos, pagamentos e lembretes permanecem disponíveis sem internet."));root.addView(f);}

    void chooseClientForLoan(){if(ds.clients.isEmpty()){startActivity(new Intent(this,AddClientActivity.class));return;}String[] names=new String[ds.clients.size()];for(int i=0;i<ds.clients.size();i++)names[i]=ds.clients.get(i).name;new AlertDialog.Builder(this).setTitle("Novo empréstimo para").setItems(names,(d,w)->{Intent i=new Intent(this,AddLoanActivity.class);i.putExtra("clientId",ds.clients.get(w).id);startActivity(i);}).show();}

    void newReminder(){LinearLayout p=Ui.col(this);p.setPadding(0,0,0,0);EditText t=Ui.field(this,"Título do lembrete");EditText d=Ui.field(this,"Detalhes");p.addView(t);Ui.gap(this,p,7);p.addView(d);new AlertDialog.Builder(this).setTitle("Novo lembrete").setView(p).setPositiveButton("Escolher data",(x,w)->pickDateTime(t.getText().toString(),d.getText().toString())).setNegativeButton("Cancelar",null).show();}
    void pickDateTime(String title,String details){Calendar c=Calendar.getInstance();new DatePickerDialog(this,(v,y,m,day)->{Calendar x=Calendar.getInstance();x.set(y,m,day);new TimePickerDialog(this,(tv,h,min)->{x.set(Calendar.HOUR_OF_DAY,h);x.set(Calendar.MINUTE,min);x.set(Calendar.SECOND,0);x.set(Calendar.MILLISECOND,0);Models.Reminder r=new Models.Reminder();r.title=title.trim().isEmpty()?"Lembrete":title.trim();r.details=details;r.when=x.getTimeInMillis();ds.reminders.add(r);ds.save();AlarmScheduler.schedule(this,"reminder",r.id,r.title,r.when);Toast.makeText(this,"Lembrete agendado",Toast.LENGTH_SHORT).show();render();},c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),true).show();},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();}
    void exportBackup(){Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("application/json");i.putExtra(Intent.EXTRA_TITLE,"devedores-backup-v5.json");startActivityForResult(i,301);}
    void importBackup(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("application/json");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,302);}
    @Override protected void onActivityResult(int r,int res,Intent data){super.onActivityResult(r,res,data);if(res!=RESULT_OK||data==null)return;try{if(r==301){java.io.OutputStream out=getContentResolver().openOutputStream(data.getData());out.write(ds.exportJson().getBytes("UTF-8"));out.close();Toast.makeText(this,"Backup exportado",Toast.LENGTH_SHORT).show();}else if(r==302){java.io.InputStream in=getContentResolver().openInputStream(data.getData());java.io.ByteArrayOutputStream b=new java.io.ByteArrayOutputStream();byte[] x=new byte[8192];int n;while((n=in.read(x))>0)b.write(x,0,n);in.close();if(ds.importJson(new String(b.toByteArray(),"UTF-8"))){AlarmScheduler.rescheduleAll(this);render();Toast.makeText(this,"Backup restaurado",Toast.LENGTH_SHORT).show();}else Toast.makeText(this,"Backup inválido",Toast.LENGTH_LONG).show();}}catch(Exception e){Toast.makeText(this,"Não foi possível concluir",Toast.LENGTH_LONG).show();}}
}
