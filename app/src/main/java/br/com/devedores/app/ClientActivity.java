package br.com.devedores.app;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ClientActivity extends Activity {
    DataStore ds; Models.Client c; LinearLayout root, docsBox, loansBox, galleryBox; String pendingFolder="Outros"; static final int REQ_PROFILE=902; static final int REQ_GALLERY=903; static final int REQ_DOC=44;

    @Override public void onCreate(Bundle b){super.onCreate(b); getWindow().setStatusBarColor(Ui.BG);getWindow().setNavigationBarColor(Ui.BG);ds=new DataStore(this);load();}
    void load(){c=ds.client(getIntent().getStringExtra("id"));if(c==null){finish();return;}build();}
    @Override protected void onResume(){super.onResume();if(ds!=null){ds.load();c=ds.client(getIntent().getStringExtra("id"));if(c!=null)build();}}
    String money(double x){return String.format(Locale.getDefault(),"R$ %.2f",x);}

    void build(){
        root=Ui.col(this); ScrollView sc=new ScrollView(this); sc.setFillViewport(true); sc.setVerticalScrollBarEnabled(false); sc.addView(root); setContentView(sc); Ui.applySystemBars(this, root);

        LinearLayout top=Ui.row(this);
        Button back=Ui.btnDark(this,"‹  Voltar"); back.setOnClickListener(v->finish()); top.addView(back,new LinearLayout.LayoutParams(Ui.dp(this,88),Ui.dp(this,44)));
        LinearLayout meta=Ui.col(this); meta.setPadding(Ui.dp(this,10),0,0,0); meta.addView(Ui.title(this,c.name,22)); meta.addView(Ui.label(this,"Carteira • perfil completo • documentos")); top.addView(meta,new LinearLayout.LayoutParams(0,Ui.dp(this,54),1));
        Button more=Ui.btnDark(this,"•••"); more.setOnClickListener(v->clientMenu()); top.addView(more,new LinearLayout.LayoutParams(Ui.dp(this,54),Ui.dp(this,44))); root.addView(top); Ui.gap(this,root,12);

        double paid=0,bal=0,total=0;int overdue=0; for(Models.Loan l:c.loans){paid+=l.paid();bal+=l.balance();total+=l.total;for(int i=0;i<l.installments;i++)if(!l.installmentPaid(i)&&l.dueAt(i)<System.currentTimeMillis())overdue++;}
        LinearLayout hero=Ui.heroCard(this,overdue>0?Ui.RED:(bal>0?Ui.GOLD:Ui.GREEN));
        LinearLayout hr=Ui.row(this);
        View photo=(c.profileImagePath!=null&&!c.profileImagePath.isEmpty())?Ui.profileImage(this,c.profileImagePath,c.name,66):Ui.avatar(this,c.name);
        hr.addView(photo,new LinearLayout.LayoutParams(Ui.dp(this,66),Ui.dp(this,66)));
        LinearLayout htxt=Ui.col(this); htxt.setPadding(Ui.dp(this,13),0,0,0);
        htxt.addView(Ui.eyebrow(this,overdue>0?"ATENÇÃO • HÁ ATRASOS":bal>0?"CONTRATO(S) EM ABERTO":"TUDO QUITADO"));
        htxt.addView(Ui.title(this,money(bal),28)); htxt.addView(Ui.label(this,"saldo atual  •  total contratado "+money(total)));
        hr.addView(htxt,new LinearLayout.LayoutParams(0,Ui.dp(this,78),1)); hero.addView(hr);
        LinearLayout hf=Ui.row(this); hf.addView(Ui.label(this,"Recebido: "+money(paid)),new LinearLayout.LayoutParams(0,Ui.dp(this,30),1)); hf.addView(Ui.pill(this,c.loans.size()+" contrato(s)",Ui.BLUE,Ui.WHITE)); hero.addView(hf);
        Button photoBtn=Ui.btnGhost(this,"📷 Alterar foto"); photoBtn.setOnClickListener(v->pickProfilePhoto()); hero.addView(photoBtn,new LinearLayout.LayoutParams(-1,Ui.dp(this,38)));
        root.addView(hero); Ui.gap(this,root,12);

        LinearLayout quick=Ui.row(this);
        Button loan=Ui.btn(this,"+ Empréstimo"); loan.setOnClickListener(v->{Intent i=new Intent(this,AddLoanActivity.class);i.putExtra("clientId",c.id);startActivity(i);});
        Button pay=Ui.btnDark(this,"Registrar pagamento"); pay.setOnClickListener(v->chooseLoanForPayment());
        quick.addView(loan,new LinearLayout.LayoutParams(0,Ui.dp(this,48),1)); Ui.gap(this,quick,6); quick.addView(pay,new LinearLayout.LayoutParams(0,Ui.dp(this,48),1)); root.addView(quick); Ui.gap(this,root,7);
        LinearLayout contact=Ui.row(this);
        Button wa=Ui.btnDark(this,"WhatsApp"); wa.setOnClickListener(v->openWhatsApp());
        Button call=Ui.btnDark(this,"Ligar"); call.setOnClickListener(v->callClient());
        Button note=Ui.btnDark(this,"Anotação"); note.setOnClickListener(v->editNote());
        contact.addView(wa,new LinearLayout.LayoutParams(0,Ui.dp(this,44),1));Ui.gap(this,contact,5);contact.addView(call,new LinearLayout.LayoutParams(0,Ui.dp(this,44),1));Ui.gap(this,contact,5);contact.addView(note,new LinearLayout.LayoutParams(0,Ui.dp(this,44),1));root.addView(contact);Ui.gap(this,root,16);

        root.addView(Ui.sectionTitle(this,"Resumo do perfil")); Ui.gap(this,root,5);
        LinearLayout info1=Ui.row(this); info1.addView(Ui.infoTile(this,"CPF",c.cpf.isEmpty()?"Não informado":c.cpf,Ui.BLUE),new LinearLayout.LayoutParams(0,Ui.dp(this,74),1)); Ui.gap(this,info1,6); info1.addView(Ui.infoTile(this,"RG",c.rg.isEmpty()?"Não informado":c.rg,Ui.PURPLE),new LinearLayout.LayoutParams(0,Ui.dp(this,74),1)); root.addView(info1); Ui.gap(this,root,6);
        LinearLayout info2=Ui.row(this); info2.addView(Ui.infoTile(this,"TELEFONE",c.phone.isEmpty()?"Não informado":c.phone,Ui.GREEN),new LinearLayout.LayoutParams(0,Ui.dp(this,74),1)); Ui.gap(this,info2,6); info2.addView(Ui.infoTile(this,"ARQUIVOS",c.documents.size()+" item(ns)",Ui.GOLD),new LinearLayout.LayoutParams(0,Ui.dp(this,74),1)); root.addView(info2); Ui.gap(this,root,6);
        LinearLayout info3=Ui.row(this); info3.addView(Ui.infoTile(this,"E-MAIL",c.email.isEmpty()?"Não informado":c.email,Ui.PURPLE),new LinearLayout.LayoutParams(0,Ui.dp(this,74),1));Ui.gap(this,info3,6);info3.addView(Ui.infoTile(this,"PASTAS",String.valueOf(c.folders.size()),Ui.BLUE),new LinearLayout.LayoutParams(0,Ui.dp(this,74),1));root.addView(info3);Ui.gap(this,root,6);
        LinearLayout addr=Ui.card(this); addr.addView(Ui.eyebrow(this,"ENDEREÇO")); addr.addView(Ui.text(this,c.address.isEmpty()?"Não informado":c.address,14)); root.addView(addr); Ui.gap(this,root,16);

        LinearLayout galleryHeader=Ui.sectionHeader(this,"Fotos do cliente",null,null);
        Button addImages=Ui.btn(this,"＋ Adicionar imagens");
        addImages.setOnClickListener(v->showImagePicker());
        galleryHeader.addView(addImages,new LinearLayout.LayoutParams(Ui.dp(this,168),Ui.dp(this,42)));
        root.addView(galleryHeader);
        root.addView(Ui.label(this,"Selecione uma ou várias imagens. Elas são copiadas para a pasta deste cliente e ficam disponíveis mesmo sem internet.")); Ui.gap(this,root,7);
        HorizontalScrollView hsv=new HorizontalScrollView(this); hsv.setHorizontalScrollBarEnabled(false); galleryBox=Ui.row(this); hsv.addView(galleryBox); root.addView(hsv); renderGallery(); Ui.gap(this,root,16);

        LinearLayout foldersHeader=Ui.sectionHeader(this,"Pastas e documentos","+ Pasta",v->createFolder()); root.addView(foldersHeader); root.addView(Ui.label(this,"Separe identidade, contratos, comprovantes e qualquer documento por pasta.")); Ui.gap(this,root,8);
        LinearLayout grid=new LinearLayout(this); grid.setOrientation(LinearLayout.VERTICAL); java.util.List<String> folders=new java.util.ArrayList<>(c.folders); if(folders.isEmpty())java.util.Collections.addAll(folders,"RG","CPF","Comprovante","Contrato","Fotos","Outros");
        for(int r=0;r<folders.size();r+=2){LinearLayout rr=Ui.row(this);for(int col=0;col<2;col++){int idx=r+col;if(idx>=folders.size()){rr.addView(new Space(this),new LinearLayout.LayoutParams(0,Ui.dp(this,78),1));break;}String folder=folders.get(idx);Button b=Ui.btnDark(this,folder+"\n"+countFolder(folder)+" arquivo(s)");b.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);b.setPadding(Ui.dp(this,14),0,Ui.dp(this,8),0);b.setOnClickListener(v->pickDoc(folder));rr.addView(b,new LinearLayout.LayoutParams(0,Ui.dp(this,78),1));if(col==0)Ui.gap(this,rr,6);}grid.addView(rr);Ui.gap(this,grid,6);}root.addView(grid);Ui.gap(this,root,14);

        root.addView(Ui.sectionHeader(this,"Arquivos recentes",null,null)); Ui.gap(this,root,4); docsBox=Ui.col(this); docsBox.setPadding(0,0,0,0); root.addView(docsBox); renderDocs(); Ui.gap(this,root,16);

        root.addView(Ui.sectionHeader(this,"Empréstimos","+ Novo",v->{Intent i=new Intent(this,AddLoanActivity.class);i.putExtra("clientId",c.id);startActivity(i);})); Ui.gap(this,root,4); loansBox=Ui.col(this); loansBox.setPadding(0,0,0,0); root.addView(loansBox); renderLoans(); Ui.gap(this,root,16);

        root.addView(Ui.sectionTitle(this,"Anotações internas")); EditText notes=Ui.field(this,"Informações importantes sobre este cliente"); notes.setText(c.notes); root.addView(notes,new LinearLayout.LayoutParams(-1,Ui.dp(this,115))); Ui.gap(this,root,7); Button sn=Ui.btn(this,"Salvar anotações"); sn.setOnClickListener(v->{c.notes=notes.getText().toString();ds.save();Toast.makeText(this,"Anotações salvas",Toast.LENGTH_SHORT).show();}); root.addView(sn); Ui.gap(this,root,16);
        LinearLayout footer=Ui.softCard(this,Ui.GREEN); footer.addView(Ui.eyebrow(this,"SALVAMENTO AUTOMÁTICO")); footer.addView(Ui.label(this,"Alterações, pagamentos e documentos são salvos no aparelho imediatamente.")); root.addView(footer); Ui.gap(this,root,12);
        Button del=Ui.btnDanger(this,"Excluir cliente e arquivos"); del.setOnClickListener(v->confirmDelete()); root.addView(del);
    }

    void renderGallery(){
        galleryBox.removeAllViews();
        int shown=0;
        for(Models.Document d:c.documents){
            if(d.mime==null||!d.mime.toLowerCase(Locale.getDefault()).startsWith("image/"))continue;
            FrameLayout tile=new FrameLayout(this);
            ImageView iv=new ImageView(this);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setBackgroundColor(Ui.SURFACE_3);
            try{
                Uri u=d.uriOrPath.startsWith("content://")?Uri.parse(d.uriOrPath):DocumentManager.uriForFile(this,d.uriOrPath);
                iv.setImageURI(u);
            }catch(Exception ignored){}
            tile.addView(iv,new FrameLayout.LayoutParams(Ui.dp(this,104),Ui.dp(this,104)));
            TextView tag=Ui.pill(this,"Imagem",Ui.DARK,Ui.WHITE);
            FrameLayout.LayoutParams tp=new FrameLayout.LayoutParams(Ui.dp(this,72),Ui.dp(this,28),Gravity.BOTTOM|Gravity.START);
            tp.setMargins(Ui.dp(this,6),0,0,Ui.dp(this,6));
            tile.addView(tag,tp);
            iv.setOnClickListener(v->openDoc(d));
            galleryBox.addView(tile,new LinearLayout.LayoutParams(Ui.dp(this,104),Ui.dp(this,104)));
            Ui.gap(this,galleryBox,8);
            shown++;
            if(shown>=12)break;
        }
        FrameLayout addTile=new FrameLayout(this);
        Button add=Ui.btnDark(this,"＋\nAdicionar");
        add.setGravity(Gravity.CENTER);
        add.setOnClickListener(v->showImagePicker());
        addTile.addView(add,new FrameLayout.LayoutParams(Ui.dp(this,104),Ui.dp(this,104)));
        galleryBox.addView(addTile,new LinearLayout.LayoutParams(Ui.dp(this,104),Ui.dp(this,104)));
        if(shown==0){
            LinearLayout empty=Ui.softCard(this,Ui.BLUE);empty.setMinimumWidth(Ui.dp(this,250));
            empty.addView(Ui.label(this,"Sua galeria está vazia. Toque em Adicionar para escolher uma ou várias imagens."));
            galleryBox.addView(empty,0);
        }
    }

    void pickProfilePhoto(){pickImage(REQ_PROFILE,"Fotos",false);}
    void pickImageIntoFolder(String folder){pickImage(REQ_GALLERY,folder,true);}
    void pickImage(int req,String folder){pickImage(req,folder,false);}
    void pickImage(int req,String folder,boolean multiple){
        pendingFolder=folder;
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("image/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,multiple);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(i,req);
    }
    void showImagePicker(){
        new AlertDialog.Builder(this)
            .setTitle("Adicionar imagens")
            .setMessage("Escolha uma ou várias fotos para guardar na pasta Fotos deste cliente.")
            .setPositiveButton("Abrir galeria",(d,w)->pickImage(REQ_GALLERY,"Fotos",true))
            .setNegativeButton("Cancelar",null)
            .show();
    }

    void createFolder(){EditText e=Ui.field(this,"Nome da pasta (ex.: Contratos 2026)");new AlertDialog.Builder(this).setTitle("Nova pasta").setView(e).setPositiveButton("Criar",(d,w)->{String n=e.getText().toString().trim();if(n.isEmpty())return;if(c.folders==null)c.folders=new java.util.ArrayList<>();for(String f:c.folders)if(f.equalsIgnoreCase(n)){Toast.makeText(this,"Já existe uma pasta com esse nome",Toast.LENGTH_SHORT).show();return;}c.folders.add(n);ds.save();build();}).setNegativeButton("Cancelar",null).show();}
    int countFolder(String folder){int n=0;for(Models.Document d:c.documents)if(folder.equals(d.folder))n++;return n;}

    void renderDocs(){docsBox.removeAllViews();if(c.documents.isEmpty()){LinearLayout empty=Ui.softCard(this,Ui.BLUE);empty.addView(Ui.label(this,"Nenhum documento adicionado ainda."));docsBox.addView(empty);return;}for(Models.Document d:c.documents){LinearLayout row=Ui.card(this);LinearLayout t=Ui.row(this);TextView av=Ui.iconBadge(this,(d.mime!=null&&d.mime.startsWith("image/"))?"◉":"▣");t.addView(av,new LinearLayout.LayoutParams(Ui.dp(this,36),Ui.dp(this,36)));LinearLayout tx=Ui.col(this);tx.setPadding(Ui.dp(this,10),0,0,0);tx.addView(Ui.title(this,d.name,14));tx.addView(Ui.label(this,d.folder+"  •  "+new SimpleDateFormat("dd/MM/yyyy HH:mm",Locale.getDefault()).format(new Date(d.addedAt))));t.addView(tx,new LinearLayout.LayoutParams(0,Ui.dp(this,56),1));Button x=Ui.btnDanger(this,"Excluir");x.setOnClickListener(v->deleteDoc(d));row.addView(t,new LinearLayout.LayoutParams(0,Ui.dp(this,60),1));row.addView(x,new LinearLayout.LayoutParams(Ui.dp(this,78),Ui.dp(this,44)));row.setOnClickListener(v->openDoc(d));docsBox.addView(row);Ui.gap(this,docsBox,6);}}

    void renderLoans(){loansBox.removeAllViews();if(c.loans.isEmpty()){LinearLayout empty=Ui.softCard(this,Ui.GOLD);empty.addView(Ui.title(this,"Nenhum empréstimo",15));empty.addView(Ui.label(this,"Crie um contrato para acompanhar parcelas e recebimentos."));loansBox.addView(empty);return;}for(Models.Loan l:c.loans){LinearLayout card=Ui.card(this);LinearLayout h=Ui.row(this);h.addView(Ui.title(this,l.title,16),new LinearLayout.LayoutParams(0,Ui.dp(this,32),1));h.addView(Ui.pill(this,l.balance()>0?"Em aberto":"Quitado",l.balance()>0?Ui.BLUE:Ui.GREEN,Ui.WHITE));card.addView(h);double pct=l.total<=0?0:(l.paid()/l.total*100.0);card.addView(Ui.label(this,"Recebido "+String.format(Locale.getDefault(),"%.0f",pct)+"%"));card.addView(Ui.progress(this,(int)pct,100),new LinearLayout.LayoutParams(-1,Ui.dp(this,7)));Ui.gap(this,card,5);card.addView(Ui.text(this,"Total "+money(l.total)+"  •  Pago "+money(l.paid())+"\nSaldo "+money(l.balance())+"  •  "+l.installments+" parcelas "+l.frequency,13));card.setOnClickListener(v->{Intent i=new Intent(this,LoanActivity.class);i.putExtra("clientId",c.id);i.putExtra("loanId",l.id);startActivity(i);});loansBox.addView(card);Ui.gap(this,loansBox,7);}}

    void pickDoc(String folder){pendingFolder=folder;Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"image/*","application/pdf","text/*","application/octet-stream"});i.addCategory(Intent.CATEGORY_OPENABLE);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivityForResult(i,REQ_DOC);}
    @Override protected void onActivityResult(int r,int res,Intent data){
        super.onActivityResult(r,res,data);
        if(res!=RESULT_OK||data==null)return;
        try{
            if(r==REQ_PROFILE){
                Uri u=data.getData(); if(u==null)return;
                String old=c.profileImagePath; String path=DocumentManager.copyToClient(this,c.id,"Fotos",u);
                if(old!=null&&!old.isEmpty()){
                    DocumentManager.delete(old);
                    for(int i=c.documents.size()-1;i>=0;i--){Models.Document od=c.documents.get(i);if(old.equals(od.uriOrPath))c.documents.remove(i);}
                }
                c.profileImagePath=path; addDocument(u,path,"Fotos");
                ds.save(); build(); Toast.makeText(this,"Foto do perfil atualizada",Toast.LENGTH_SHORT).show(); return;
            }
            if(r==REQ_GALLERY){
                int added=0;
                if(data.getClipData()!=null){
                    android.content.ClipData cd=data.getClipData();
                    for(int i=0;i<cd.getItemCount();i++){Uri u=cd.getItemAt(i).getUri();String path=DocumentManager.copyToClient(this,c.id,"Fotos",u);addDocument(u,path,"Fotos");added++;}
                } else if(data.getData()!=null){
                    Uri u=data.getData();String path=DocumentManager.copyToClient(this,c.id,"Fotos",u);addDocument(u,path,"Fotos");added=1;
                }
                ds.save(); build(); Toast.makeText(this,added+" imagem(ns) adicionada(s) à pasta Fotos",Toast.LENGTH_SHORT).show(); return;
            }
            if(r==REQ_DOC){
                Uri u=data.getData(); if(u==null)return;
                String path=DocumentManager.copyToClient(this,c.id,pendingFolder,u);addDocument(u,path,pendingFolder);ds.save();build();Toast.makeText(this,"Arquivo salvo em "+pendingFolder,Toast.LENGTH_SHORT).show();
            }
        }catch(Exception e){Toast.makeText(this,"Não foi possível salvar o arquivo",Toast.LENGTH_LONG).show();}
    }

    void addDocument(Uri u,String path,String folder){Models.Document d=new Models.Document();d.folder=folder;d.name=DocumentManager.displayName(getContentResolver(),u);if(d.name==null)d.name=folder.equals("Fotos")?"Imagem":"Documento";d.uriOrPath=path;d.mime=getContentResolver().getType(u);if(d.mime==null)d.mime=folder.equals("Fotos")?"image/*":"application/octet-stream";c.documents.add(d);}
    void openDoc(Models.Document d){try{Intent i=new Intent(Intent.ACTION_VIEW);Uri u=d.uriOrPath.startsWith("content://")?Uri.parse(d.uriOrPath):DocumentManager.uriForFile(this,d.uriOrPath);i.setDataAndType(u,d.mime);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(i);}catch(Exception e){Toast.makeText(this,"Nenhum aplicativo pode abrir esse arquivo",Toast.LENGTH_SHORT).show();}}
    void deleteDoc(Models.Document d){new AlertDialog.Builder(this).setTitle("Excluir arquivo?").setMessage(d.name).setPositiveButton("Excluir",(a,w)->{if(d.uriOrPath.equals(c.profileImagePath))c.profileImagePath="";DocumentManager.delete(d.uriOrPath);c.documents.remove(d);ds.save();build();}).setNegativeButton("Cancelar",null).show();}
    void editNote(){EditText e=Ui.field(this,"Anotações");e.setText(c.notes);new AlertDialog.Builder(this).setTitle("Anotações do cliente").setView(e).setPositiveButton("Salvar",(d,w)->{c.notes=e.getText().toString();ds.save();build();}).setNegativeButton("Cancelar",null).show();}

    void editClient(){LinearLayout p=Ui.col(this);p.setPadding(0,0,0,0);EditText n=Ui.field(this,"Nome");n.setText(c.name);EditText cp=Ui.field(this,"CPF");cp.setText(c.cpf);EditText rg=Ui.field(this,"RG");rg.setText(c.rg);EditText ph=Ui.field(this,"Telefone");ph.setText(c.phone);EditText em=Ui.field(this,"E-mail");em.setText(c.email);EditText ad=Ui.field(this,"Endereço");ad.setText(c.address);p.addView(n);Ui.gap(this,p,6);p.addView(cp);Ui.gap(this,p,6);p.addView(rg);Ui.gap(this,p,6);p.addView(ph);Ui.gap(this,p,6);p.addView(em);Ui.gap(this,p,6);p.addView(ad);new AlertDialog.Builder(this).setTitle("Editar perfil").setView(p).setPositiveButton("Salvar",(d,w)->{c.name=n.getText().toString();c.cpf=cp.getText().toString();c.rg=rg.getText().toString();c.phone=ph.getText().toString();c.email=em.getText().toString();c.address=ad.getText().toString();ds.save();build();}).setNegativeButton("Cancelar",null).show();}

    void clientMenu(){String[] items={"Editar perfil","Adicionar imagem","Criar pasta","Anotações","Excluir cliente"};new AlertDialog.Builder(this).setTitle("Ações do cliente").setItems(items,(d,w)->{if(w==0)editClient();else if(w==1)pickProfilePhoto();else if(w==2)createFolder();else if(w==3)editNote();else confirmDelete();}).show();}
    void chooseLoanForPayment(){if(c.loans.isEmpty()){Toast.makeText(this,"Este cliente ainda não possui empréstimos.",Toast.LENGTH_SHORT).show();return;}String[] n=new String[c.loans.size()];for(int i=0;i<c.loans.size();i++)n[i]=c.loans.get(i).title+" • "+money(c.loans.get(i).balance());new AlertDialog.Builder(this).setTitle("Escolha o contrato").setItems(n,(d,w)->{Intent i=new Intent(this,LoanActivity.class);i.putExtra("clientId",c.id);i.putExtra("loanId",c.loans.get(w).id);startActivity(i);}).show();}
    void openWhatsApp(){String raw=c.phone==null?"":c.phone.replaceAll("[^0-9]","");if(raw.isEmpty()){Toast.makeText(this,"Cadastre o telefone primeiro.",Toast.LENGTH_SHORT).show();return;}if(raw.length()<=11)raw="55"+raw;try{Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse("https://wa.me/"+raw));startActivity(i);}catch(Exception e){Toast.makeText(this,"WhatsApp não encontrado.",Toast.LENGTH_SHORT).show();}}
    void callClient(){String raw=c.phone==null?"":c.phone.replaceAll("[^0-9+]","");if(raw.isEmpty()){Toast.makeText(this,"Cadastre o telefone primeiro.",Toast.LENGTH_SHORT).show();return;}try{startActivity(new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+raw)));}catch(Exception ignored){}}
    void confirmDelete(){new AlertDialog.Builder(this).setTitle("Excluir cliente?").setMessage("O cliente, empréstimos e arquivos serão apagados deste aparelho.").setPositiveButton("Excluir",(d,w)->{for(Models.Loan l:c.loans)AlarmScheduler.cancelLoanAlarms(this,l.id,l.installments);for(Models.Document doc:c.documents)DocumentManager.delete(doc.uriOrPath);ds.removeClient(c.id);finish();}).setNegativeButton("Cancelar",null).show();}
}
