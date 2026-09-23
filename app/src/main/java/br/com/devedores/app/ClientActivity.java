package br.com.devedores.app;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class ClientActivity extends Activity {
    DataStore ds; Models.Client c; LinearLayout root, docsBox, loansBox, galleryBox;
    String pendingFolder="Outros"; File cameraFile; boolean cameraForProfile=false;
    static final int REQ_PROFILE=902, REQ_GALLERY=903, REQ_DOC=904, REQ_CAMERA_PROFILE=905, REQ_CAMERA_GALLERY=906, REQ_CAMERA_FOLDER=907;
    String cameraFolder="Fotos";

    @Override public void onCreate(Bundle b){
        super.onCreate(b); ds=new DataStore(this); load();
    }
    void load(){c=ds.client(getIntent().getStringExtra("id"));if(c==null){finish();return;}build();}
    @Override protected void onResume(){super.onResume();if(ds!=null){ds.load();c=ds.client(getIntent().getStringExtra("id"));if(c!=null)build();}}
    String money(double x){return String.format(Locale.getDefault(),"R$ %.2f",x);}

    void build(){
        root=Ui.col(this);
        ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.setVerticalScrollBarEnabled(false);sc.addView(root);setContentView(sc);Ui.applySystemBars(this,root);

        LinearLayout top=Ui.row(this);
        Button back=Ui.btnDark(this,"‹  Voltar");back.setOnClickListener(v->finish());top.addView(back,new LinearLayout.LayoutParams(Ui.dp(this,88),Ui.dp(this,46)));
        LinearLayout meta=Ui.col(this);meta.setPadding(Ui.dp(this,10),0,0,0);meta.addView(Ui.title(this,c.name,22));meta.addView(Ui.label(this,"Perfil completo • documentos • contratos"));top.addView(meta,new LinearLayout.LayoutParams(0,Ui.dp(this,54),1));
        Button more=Ui.btnDark(this,"•••");more.setOnClickListener(v->clientMenu());top.addView(more,new LinearLayout.LayoutParams(Ui.dp(this,54),Ui.dp(this,46)));root.addView(top);Ui.gap(this,root,12);

        double paid=0,bal=0,total=0;int overdue=0;
        for(Models.Loan l:c.loans){paid+=l.paid();bal+=l.balance();total+=l.total;for(int i=0;i<l.installments;i++)if(!l.installmentPaid(i)&&l.dueAt(i)<System.currentTimeMillis())overdue++;}
        LinearLayout hero=Ui.heroCard(this,overdue>0?Ui.RED:(bal>0?Ui.GOLD:Ui.GREEN));
        LinearLayout hr=Ui.row(this);
        View photo=(c.profileImagePath!=null&&!c.profileImagePath.isEmpty())?Ui.profileImage(this,c.profileImagePath,c.name,70):Ui.avatar(this,c.name);
        hr.addView(photo,new LinearLayout.LayoutParams(Ui.dp(this,70),Ui.dp(this,70)));
        LinearLayout htxt=Ui.col(this);htxt.setPadding(Ui.dp(this,13),0,0,0);htxt.addView(Ui.eyebrow(this,overdue>0?"ATENÇÃO • HÁ ATRASOS":bal>0?"CONTRATO(S) EM ABERTO":"TUDO QUITADO"));htxt.addView(Ui.title(this,money(bal),28));htxt.addView(Ui.label(this,"saldo atual • total contratado "+money(total)));hr.addView(htxt,new LinearLayout.LayoutParams(0,Ui.dp(this,78),1));hero.addView(hr);
        LinearLayout hf=Ui.row(this);hf.addView(Ui.label(this,"Recebido: "+money(paid)),new LinearLayout.LayoutParams(0,Ui.dp(this,30),1));hf.addView(Ui.pill(this,c.loans.size()+" contrato(s)",Ui.BLUE,Ui.WHITE));hero.addView(hf);
        LinearLayout photoActions=Ui.row(this);Button pf=Ui.btnGhost(this,"📷 Foto");pf.setOnClickListener(v->pickProfilePhoto());Button ga=Ui.btnGhost(this,"🖼 Galeria");ga.setOnClickListener(v->showImagePicker());photoActions.addView(pf,new LinearLayout.LayoutParams(0,Ui.dp(this,40),1));Ui.gap(this,photoActions,5);photoActions.addView(ga,new LinearLayout.LayoutParams(0,Ui.dp(this,40),1));hero.addView(photoActions);
        root.addView(hero);Ui.gap(this,root,12);

        LinearLayout quick=Ui.row(this);Button loan=Ui.btn(this,"＋ Empréstimo");loan.setOnClickListener(v->{Intent i=new Intent(this,AddLoanActivity.class);i.putExtra("clientId",c.id);startActivity(i);});Button pay=Ui.btnDark(this,"Registrar pagamento");pay.setOnClickListener(v->chooseLoanForPayment());quick.addView(loan,new LinearLayout.LayoutParams(0,Ui.dp(this,48),1));Ui.gap(this,quick,6);quick.addView(pay,new LinearLayout.LayoutParams(0,Ui.dp(this,48),1));root.addView(quick);Ui.gap(this,root,7);
        LinearLayout contact=Ui.row(this);Button wa=Ui.btnDark(this,"WhatsApp");wa.setOnClickListener(v->openWhatsApp());Button call=Ui.btnDark(this,"Ligar");call.setOnClickListener(v->callClient());Button note=Ui.btnDark(this,"Anotação");note.setOnClickListener(v->editNote());contact.addView(wa,new LinearLayout.LayoutParams(0,Ui.dp(this,44),1));Ui.gap(this,contact,5);contact.addView(call,new LinearLayout.LayoutParams(0,Ui.dp(this,44),1));Ui.gap(this,contact,5);contact.addView(note,new LinearLayout.LayoutParams(0,Ui.dp(this,44),1));root.addView(contact);Ui.gap(this,root,16);

        root.addView(Ui.sectionTitle(this,"Resumo do perfil"));Ui.gap(this,root,5);
        addInfoRow("CPF",c.cpf,"RG",c.rg,Ui.BLUE,Ui.PURPLE);addInfoRow("Telefone",c.phone,"Arquivos",String.valueOf(c.documents.size()),Ui.GREEN,Ui.GOLD);addInfoRow("E-mail",c.email,"Pastas",String.valueOf(c.folders.size()),Ui.PURPLE,Ui.BLUE);
        LinearLayout addr=Ui.card(this);addr.addView(Ui.eyebrow(this,"ENDEREÇO"));addr.addView(Ui.text(this,c.address.isEmpty()?"Não informado":c.address,14));root.addView(addr);Ui.gap(this,root,16);

        LinearLayout gh=Ui.sectionHeader(this,"Galeria do cliente",null,null);Button add=Ui.btn(this,"＋ Imagem");add.setOnClickListener(v->showImagePicker());gh.addView(add,new LinearLayout.LayoutParams(Ui.dp(this,120),Ui.dp(this,42)));root.addView(gh);root.addView(Ui.label(this,"Galeria local: você pode usar a câmera, escolher várias fotos, definir uma foto de perfil, mover imagens para pastas e compartilhar."));Ui.gap(this,root,7);
        galleryBox=new LinearLayout(this);galleryBox.setOrientation(LinearLayout.VERTICAL);root.addView(galleryBox);renderGallery();Ui.gap(this,root,16);

        root.addView(Ui.sectionHeader(this,"Pastas e documentos","＋ Pasta",v->createFolder()));root.addView(Ui.label(this,"Cada pasta aceita fotos, PDFs e outros arquivos."));Ui.gap(this,root,8);renderFolderGrid();Ui.gap(this,root,14);
        root.addView(Ui.sectionHeader(this,"Empréstimos","＋ Novo",v->{Intent i=new Intent(this,AddLoanActivity.class);i.putExtra("clientId",c.id);startActivity(i);}));Ui.gap(this,root,4);loansBox=Ui.col(this);loansBox.setPadding(0,0,0,0);root.addView(loansBox);renderLoans();Ui.gap(this,root,16);
        root.addView(Ui.sectionTitle(this,"Anotações internas"));EditText notes=Ui.field(this,"Informações importantes sobre este cliente");notes.setText(c.notes);root.addView(notes,new LinearLayout.LayoutParams(-1,Ui.dp(this,120)));Ui.gap(this,root,7);Button sn=Ui.btn(this,"Salvar anotações");sn.setOnClickListener(v->{c.notes=notes.getText().toString();ds.save();Toast.makeText(this,"Anotações salvas",Toast.LENGTH_SHORT).show();});root.addView(sn);Ui.gap(this,root,14);
        LinearLayout footer=Ui.softCard(this,Ui.GREEN);footer.addView(Ui.eyebrow(this,"SALVAMENTO AUTOMÁTICO"));footer.addView(Ui.label(this,"Dados e caminhos de documentos são salvos imediatamente neste aparelho."));root.addView(footer);Ui.gap(this,root,12);Button del=Ui.btnDanger(this,"Excluir cliente e arquivos");del.setOnClickListener(v->confirmDelete());root.addView(del);
    }

    void addInfoRow(String a,String av,String b,String bv,int ca,int cb){LinearLayout r=Ui.row(this);r.addView(Ui.infoTile(this,a,(av==null||av.isEmpty())?"Não informado":av,ca),new LinearLayout.LayoutParams(0,Ui.dp(this,78),1));Ui.gap(this,r,6);r.addView(Ui.infoTile(this,b,(bv==null||bv.isEmpty())?"Não informado":bv,cb),new LinearLayout.LayoutParams(0,Ui.dp(this,78),1));root.addView(r);Ui.gap(this,root,6);}

    void renderGallery(){
        galleryBox.removeAllViews();List<Models.Document> images=new ArrayList<>();for(Models.Document d:c.documents)if(d.mime!=null&&d.mime.toLowerCase(Locale.getDefault()).startsWith("image/"))images.add(d);
        if(images.isEmpty()){LinearLayout empty=Ui.softCard(this,Ui.BLUE);empty.addView(Ui.title(this,"Nenhuma imagem ainda",15));empty.addView(Ui.label(this,"Use + Imagem para escolher fotos ou abrir a câmera."));Button add=Ui.btn(this,"＋ Adicionar imagem");add.setOnClickListener(v->showImagePicker());empty.addView(add);galleryBox.addView(empty);return;}
        for(int r=0;r<images.size();r+=3){LinearLayout row=Ui.row(this);for(int col=0;col<3;col++){int i=r+col;if(i>=images.size()){row.addView(new Space(this),new LinearLayout.LayoutParams(0,Ui.dp(this,132),1));continue;}Models.Document d=images.get(i);row.addView(imageTile(d),new LinearLayout.LayoutParams(0,Ui.dp(this,132),1));if(col<2)Ui.gap(this,row,6);}galleryBox.addView(row);Ui.gap(this,galleryBox,6);}
        LinearLayout foot=Ui.softCard(this,Ui.BLUE);foot.addView(Ui.label(this,images.size()+" imagem(ns) armazenada(s) neste cliente."));Button add=Ui.btnDark(this,"＋ Adicionar mais imagens");add.setOnClickListener(v->showImagePicker());foot.addView(add);galleryBox.addView(foot);
    }

    View imageTile(Models.Document d){
        LinearLayout card=Ui.card(this);card.setPadding(Ui.dp(this,5),Ui.dp(this,5),Ui.dp(this,5),Ui.dp(this,5));ImageView iv=new ImageView(this);iv.setScaleType(ImageView.ScaleType.CENTER_CROP);iv.setBackgroundColor(Ui.SURFACE_3);try{Uri u=uriFor(d);iv.setImageURI(u);}catch(Exception ignored){}card.addView(iv,new LinearLayout.LayoutParams(-1,Ui.dp(this,92)));TextView name=Ui.label(this,d.name);name.setSingleLine(true);name.setEllipsize(android.text.TextUtils.TruncateAt.END);card.addView(name,new LinearLayout.LayoutParams(-1,Ui.dp(this,25)));card.setOnClickListener(v->showMediaActions(d));card.setOnLongClickListener(v->{showMediaActions(d);return true;});return card;
    }

    Uri uriFor(Models.Document d){return d.uriOrPath.startsWith("content://")?Uri.parse(d.uriOrPath):DocumentManager.uriForFile(this,d.uriOrPath);}

    void renderFolderGrid(){
        LinearLayout grid=new LinearLayout(this);grid.setOrientation(LinearLayout.VERTICAL);List<String> folders=new ArrayList<>(c.folders);if(folders.isEmpty())Collections.addAll(folders,"RG","CPF","Comprovante","Contrato","Fotos","Outros");
        for(int r=0;r<folders.size();r+=2){LinearLayout rr=Ui.row(this);for(int col=0;col<2;col++){int idx=r+col;if(idx>=folders.size()){rr.addView(new Space(this),new LinearLayout.LayoutParams(0,Ui.dp(this,104),1));continue;}String folder=folders.get(idx);LinearLayout tile=Ui.card(this);tile.setPadding(Ui.dp(this,13),Ui.dp(this,11),Ui.dp(this,10),Ui.dp(this,10));tile.addView(Ui.eyebrow(this,folder));tile.addView(Ui.title(this,countFolder(folder)+" arquivo(s)",18));LinearLayout ar=Ui.row(this);Button open=Ui.btnDark(this,"Abrir");open.setOnClickListener(v->showFolderDialog(folder));Button img=Ui.btnGhost(this,"＋ imagem");img.setOnClickListener(v->addImageToFolder(folder));ar.addView(open,new LinearLayout.LayoutParams(0,Ui.dp(this,40),1));Ui.gap(this,ar,5);ar.addView(img,new LinearLayout.LayoutParams(0,Ui.dp(this,40),1));tile.addView(ar);rr.addView(tile,new LinearLayout.LayoutParams(0,Ui.dp(this,110),1));if(col==0)Ui.gap(this,rr,6);}grid.addView(rr);Ui.gap(this,grid,6);}
        root.addView(grid);
    }

    int countFolder(String folder){int n=0;for(Models.Document d:c.documents)if(folder.equals(d.folder))n++;return n;}

    void showFolderDialog(String folder){
        LinearLayout box=Ui.col(this);box.setPadding(0,0,0,0);List<Models.Document> list=new ArrayList<>();for(Models.Document d:c.documents)if(folder.equals(d.folder))list.add(d);
        if(list.isEmpty())box.addView(Ui.label(this,"Esta pasta está vazia."));
        for(Models.Document d:list){LinearLayout r=Ui.card(this);TextView icon=Ui.iconBadge(this,d.mime.startsWith("image/")?"▣":"▤");r.addView(icon,new LinearLayout.LayoutParams(Ui.dp(this,40),Ui.dp(this,40)));LinearLayout tx=Ui.col(this);tx.setPadding(Ui.dp(this,9),0,0,0);tx.addView(Ui.title(this,d.name,14));tx.addView(Ui.label(this,new SimpleDateFormat("dd/MM/yyyy HH:mm",Locale.getDefault()).format(new Date(d.addedAt))));r.addView(tx,new LinearLayout.LayoutParams(0,Ui.dp(this,56),1));r.setOnClickListener(v->showMediaActions(d));box.addView(r);Ui.gap(this,box,5);}
        Button addImg=Ui.btn(this,"＋ Adicionar imagem");addImg.setOnClickListener(v->{((Dialog)v.getTag()).dismiss();addImageToFolder(folder);});
        Button addFile=Ui.btnDark(this,"＋ Adicionar arquivo");addFile.setOnClickListener(v->{((Dialog)v.getTag()).dismiss();pickDoc(folder);});
        box.addView(addImg);Ui.gap(this,box,5);box.addView(addFile);
        AlertDialog dlg=new AlertDialog.Builder(this).setTitle("Pasta • "+folder).setView(box).setNegativeButton("Fechar",null).create();addImg.setTag(dlg);addFile.setTag(dlg);dlg.show();
    }

    void showMediaActions(Models.Document d){
        String[] actions={"Abrir imagem","Definir como foto do cliente","Mover para outra pasta","Compartilhar","Renomear","Excluir"};
        new AlertDialog.Builder(this).setTitle(d.name).setItems(actions,(dlg,which)->{
            if(which==0)previewImage(d);
            else if(which==1)setProfileFromDocument(d);
            else if(which==2)moveDocument(d);
            else if(which==3)shareDocument(d);
            else if(which==4)renameDocument(d);
            else deleteDoc(d);
        }).show();
    }

    void previewImage(Models.Document d){
        if(d.mime==null||!d.mime.startsWith("image/")){openDoc(d);return;}
        ImageView iv=new ImageView(this);iv.setBackgroundColor(Color.BLACK);iv.setScaleType(ImageView.ScaleType.FIT_CENTER);try{iv.setImageURI(uriFor(d));}catch(Exception ignored){}
        LinearLayout box=new LinearLayout(this);box.setPadding(0,0,0,0);box.setBackgroundColor(Color.BLACK);box.addView(iv,new LinearLayout.LayoutParams(-1,Ui.dp(this,420)));
        new AlertDialog.Builder(this).setTitle(d.name).setView(box).setPositiveButton("Fechar",null).show();
    }

    void setProfileFromDocument(Models.Document d){c.profileImagePath=d.uriOrPath;ds.save();build();Toast.makeText(this,"Foto de perfil atualizada",Toast.LENGTH_SHORT).show();}

    void moveDocument(Models.Document d){final String[] folders=c.folders.toArray(new String[0]);new AlertDialog.Builder(this).setTitle("Mover para pasta").setItems(folders,(x,which)->{String folder=folders[which];try{String np=DocumentManager.moveInsideClient(this,c.id,d.uriOrPath,folder,d.name);d.uriOrPath=np;d.folder=folder;ds.save();build();Toast.makeText(this,"Arquivo movido",Toast.LENGTH_SHORT).show();}catch(Exception e){Toast.makeText(this,"Não foi possível mover",Toast.LENGTH_SHORT).show();}}).show();}

    void renameDocument(Models.Document d){EditText e=Ui.field(this,"Novo nome");e.setText(d.name);new AlertDialog.Builder(this).setTitle("Renomear arquivo").setView(e).setPositiveButton("Salvar",(x,w)->{String n=e.getText().toString().trim();if(!n.isEmpty()){d.name=n;ds.save();build();}}).setNegativeButton("Cancelar",null).show();}

    void shareDocument(Models.Document d){try{Intent i=new Intent(Intent.ACTION_SEND);i.setType(d.mime==null?"application/octet-stream":d.mime);i.putExtra(Intent.EXTRA_STREAM,uriFor(d));i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(i,"Compartilhar arquivo"));}catch(Exception e){Toast.makeText(this,"Não foi possível compartilhar",Toast.LENGTH_SHORT).show();}}

    void addImageToFolder(String folder){new AlertDialog.Builder(this).setTitle("Adicionar imagem").setItems(new String[]{"Escolher da galeria","Tirar foto"},(d,w)->{if(w==0)pickImage(REQ_DOC,folder,true);else openCamera(folder,false);}).show();}
    void showImagePicker(){new AlertDialog.Builder(this).setTitle("Adicionar imagens").setItems(new String[]{"Escolher uma ou várias da galeria","Tirar foto com a câmera"},(d,w)->{if(w==0)pickImage(REQ_GALLERY,"Fotos",true);else openCamera("Fotos",false);}).show();}
    void pickProfilePhoto(){new AlertDialog.Builder(this).setTitle("Foto do cliente").setItems(new String[]{"Escolher da galeria","Tirar foto"},(d,w)->{if(w==0)pickImage(REQ_PROFILE,"Fotos",false);else openCamera("Fotos",true);}).show();}

    void pickImage(int req,String folder,boolean multiple){
        pendingFolder=folder;Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("image/*");i.addCategory(Intent.CATEGORY_OPENABLE);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);if(multiple)i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);startActivityForResult(i,req);
    }
    void pickDoc(String folder){pendingFolder=folder;Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"image/*","application/pdf","text/*","application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document","application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"});i.addCategory(Intent.CATEGORY_OPENABLE);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);startActivityForResult(i,REQ_DOC);}

    void openCamera(String folder,boolean profile){
        cameraForProfile=profile;cameraFolder=folder;
        if(Build.VERSION.SDK_INT>=23&&ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},991);return;}
        try{cameraFile=new File(getFilesDir(),"camera_"+System.currentTimeMillis()+".jpg");Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);i.putExtra(MediaStore.EXTRA_OUTPUT,DocumentManager.uriForFile(this,cameraFile.getAbsolutePath()));i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivityForResult(i,profile?REQ_CAMERA_PROFILE:REQ_CAMERA_GALLERY);}catch(Exception e){Toast.makeText(this,"Não foi possível abrir a câmera",Toast.LENGTH_SHORT).show();}
    }
    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){super.onRequestPermissionsResult(requestCode,permissions,grantResults);if(requestCode==991&&grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)openCamera(cameraFolder,cameraForProfile);}

    @Override protected void onActivityResult(int r,int res,Intent data){
        super.onActivityResult(r,res,data);if(res!=RESULT_OK)return;
        try{
            if(r==REQ_PROFILE){Uri u=data==null?null:data.getData();if(u==null)return;saveProfileUri(u);return;}
            if(r==REQ_GALLERY){int added=addSelectedImages(data,"Fotos");ds.save();build();Toast.makeText(this,added+" imagem(ns) adicionada(s)",Toast.LENGTH_SHORT).show();return;}
            if(r==REQ_DOC){if(data==null)return;int added=addSelectedDocuments(data,pendingFolder);ds.save();build();Toast.makeText(this,added+" arquivo(s) salvo(s) em "+pendingFolder,Toast.LENGTH_SHORT).show();return;}
            if(r==REQ_CAMERA_PROFILE||r==REQ_CAMERA_GALLERY){if(cameraFile==null||!cameraFile.exists())return;String path=DocumentManager.copyFileToClient(this,c.id,"Fotos",cameraFile,"foto_"+System.currentTimeMillis()+".jpg","image/jpeg");Models.Document d=new Models.Document();d.folder="Fotos";d.name="Foto "+new SimpleDateFormat("dd-MM-yyyy HH-mm",Locale.getDefault()).format(new Date());d.uriOrPath=path;d.mime="image/jpeg";c.documents.add(d);if(r==REQ_CAMERA_PROFILE)c.profileImagePath=path;cameraFile.delete();cameraFile=null;ds.save();build();Toast.makeText(this,r==REQ_CAMERA_PROFILE?"Foto de perfil salva":"Foto adicionada",Toast.LENGTH_SHORT).show();return;}
        }catch(Exception e){Toast.makeText(this,"Não foi possível salvar o arquivo",Toast.LENGTH_LONG).show();}
    }

    void saveProfileUri(Uri u)throws Exception{String old=c.profileImagePath;String path=DocumentManager.copyToClient(this,c.id,"Fotos",u);if(old!=null&&!old.isEmpty()){DocumentManager.delete(old);for(int i=c.documents.size()-1;i>=0;i--)if(old.equals(c.documents.get(i).uriOrPath))c.documents.remove(i);}c.profileImagePath=path;Models.Document d=new Models.Document();d.folder="Fotos";d.name=DocumentManager.displayName(getContentResolver(),u);if(d.name==null)d.name="Foto do cliente";d.uriOrPath=path;d.mime="image/*";c.documents.add(d);ds.save();build();Toast.makeText(this,"Foto do perfil atualizada",Toast.LENGTH_SHORT).show();}

    int addSelectedImages(Intent data,String folder)throws Exception{int n=0;if(data==null)return n;if(data.getClipData()!=null){android.content.ClipData cd=data.getClipData();for(int i=0;i<cd.getItemCount();i++){Uri u=cd.getItemAt(i).getUri();persistRead(u);String path=DocumentManager.copyToClient(this,c.id,folder,u);addDocument(u,path,folder);n++;}}else if(data.getData()!=null){Uri u=data.getData();persistRead(u);String path=DocumentManager.copyToClient(this,c.id,folder,u);addDocument(u,path,folder);n++;}return n;}
    int addSelectedDocuments(Intent data,String folder)throws Exception{int n=0;if(data.getClipData()!=null){android.content.ClipData cd=data.getClipData();for(int i=0;i<cd.getItemCount();i++){Uri u=cd.getItemAt(i).getUri();persistRead(u);String path=DocumentManager.copyToClient(this,c.id,folder,u);addDocument(u,path,folder);n++;}}else if(data.getData()!=null){Uri u=data.getData();persistRead(u);String path=DocumentManager.copyToClient(this,c.id,folder,u);addDocument(u,path,folder);n++;}return n;}
    void persistRead(Uri u){try{getContentResolver().takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}}
    void addDocument(Uri u,String path,String folder){Models.Document d=new Models.Document();d.folder=folder;d.name=DocumentManager.displayName(getContentResolver(),u);if(d.name==null)d.name=folder.equals("Fotos")?"Imagem":"Documento";d.uriOrPath=path;d.mime=getContentResolver().getType(u);if(d.mime==null)d.mime=folder.equals("Fotos")?"image/*":"application/octet-stream";c.documents.add(d);}
    void openDoc(Models.Document d){try{Intent i=new Intent(Intent.ACTION_VIEW);Uri u=uriFor(d);i.setDataAndType(u,d.mime);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(i);}catch(Exception e){Toast.makeText(this,"Nenhum aplicativo pode abrir esse arquivo",Toast.LENGTH_SHORT).show();}}
    void deleteDoc(Models.Document d){new AlertDialog.Builder(this).setTitle("Excluir arquivo?").setMessage(d.name).setPositiveButton("Excluir",(a,w)->{if(d.uriOrPath.equals(c.profileImagePath))c.profileImagePath="";DocumentManager.delete(d.uriOrPath);c.documents.remove(d);ds.save();build();}).setNegativeButton("Cancelar",null).show();}
    void createFolder(){EditText e=Ui.field(this,"Nome da pasta (ex.: Contratos 2026)");new AlertDialog.Builder(this).setTitle("Nova pasta").setView(e).setPositiveButton("Criar",(d,w)->{String n=e.getText().toString().trim();if(n.isEmpty())return;if(c.folders==null)c.folders=new ArrayList<>();for(String f:c.folders)if(f.equalsIgnoreCase(n)){Toast.makeText(this,"Essa pasta já existe",Toast.LENGTH_SHORT).show();return;}c.folders.add(n);ds.save();build();}).setNegativeButton("Cancelar",null).show();}
    void renderLoans(){loansBox.removeAllViews();if(c.loans.isEmpty()){LinearLayout empty=Ui.softCard(this,Ui.GOLD);empty.addView(Ui.title(this,"Nenhum empréstimo",15));empty.addView(Ui.label(this,"Crie um contrato para acompanhar parcelas e recebimentos."));loansBox.addView(empty);return;}for(Models.Loan l:c.loans){LinearLayout card=Ui.card(this);LinearLayout h=Ui.row(this);h.addView(Ui.title(this,l.title,16),new LinearLayout.LayoutParams(0,Ui.dp(this,32),1));h.addView(Ui.pill(this,l.balance()>0?"Em aberto":"Quitado",l.balance()>0?Ui.BLUE:Ui.GREEN,Ui.WHITE));card.addView(h);double pct=l.total<=0?0:(l.paid()/l.total*100.0);card.addView(Ui.label(this,"Recebido "+String.format(Locale.getDefault(),"%.0f",pct)+"%"));card.addView(Ui.progress(this,(int)pct,100),new LinearLayout.LayoutParams(-1,Ui.dp(this,7)));Ui.gap(this,card,5);card.addView(Ui.text(this,"Total "+money(l.total)+"  •  Pago "+money(l.paid())+"\nSaldo "+money(l.balance())+"  •  "+l.installments+" parcelas "+l.frequency,13));card.setOnClickListener(v->{Intent i=new Intent(this,LoanActivity.class);i.putExtra("clientId",c.id);i.putExtra("loanId",l.id);startActivity(i);});loansBox.addView(card);Ui.gap(this,loansBox,7);}}
    void editNote(){EditText e=Ui.field(this,"Anotações");e.setText(c.notes);new AlertDialog.Builder(this).setTitle("Anotações do cliente").setView(e).setPositiveButton("Salvar",(d,w)->{c.notes=e.getText().toString();ds.save();build();}).setNegativeButton("Cancelar",null).show();}
    void editClient(){LinearLayout p=Ui.col(this);p.setPadding(0,0,0,0);EditText n=Ui.field(this,"Nome");n.setText(c.name);EditText cp=Ui.field(this,"CPF");cp.setText(c.cpf);EditText rg=Ui.field(this,"RG");rg.setText(c.rg);EditText ph=Ui.field(this,"Telefone");ph.setText(c.phone);EditText em=Ui.field(this,"E-mail");em.setText(c.email);EditText ad=Ui.field(this,"Endereço");ad.setText(c.address);p.addView(n);Ui.gap(this,p,6);p.addView(cp);Ui.gap(this,p,6);p.addView(rg);Ui.gap(this,p,6);p.addView(ph);Ui.gap(this,p,6);p.addView(em);Ui.gap(this,p,6);p.addView(ad);new AlertDialog.Builder(this).setTitle("Editar perfil").setView(p).setPositiveButton("Salvar",(d,w)->{c.name=n.getText().toString();c.cpf=cp.getText().toString();c.rg=rg.getText().toString();c.phone=ph.getText().toString();c.email=em.getText().toString();c.address=ad.getText().toString();ds.save();build();}).setNegativeButton("Cancelar",null).show();}
    void clientMenu(){String[] items={"Editar perfil","Adicionar imagem","Abrir galeria","Criar pasta","Anotações","Excluir cliente"};new AlertDialog.Builder(this).setTitle("Ações do cliente").setItems(items,(d,w)->{if(w==0)editClient();else if(w==1)showImagePicker();else if(w==2){showImagePicker();}else if(w==3)createFolder();else if(w==4)editNote();else confirmDelete();}).show();}
    void chooseLoanForPayment(){if(c.loans.isEmpty()){Toast.makeText(this,"Este cliente ainda não possui empréstimos.",Toast.LENGTH_SHORT).show();return;}String[] n=new String[c.loans.size()];for(int i=0;i<c.loans.size();i++)n[i]=c.loans.get(i).title+" • "+money(c.loans.get(i).balance());new AlertDialog.Builder(this).setTitle("Escolha o contrato").setItems(n,(d,w)->{Intent i=new Intent(this,LoanActivity.class);i.putExtra("clientId",c.id);i.putExtra("loanId",c.loans.get(w).id);startActivity(i);}).show();}
    void openWhatsApp(){String raw=c.phone==null?"":c.phone.replaceAll("[^0-9]","");if(raw.isEmpty()){Toast.makeText(this,"Cadastre o telefone primeiro.",Toast.LENGTH_SHORT).show();return;}if(raw.length()<=11)raw="55"+raw;try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://wa.me/"+raw)));}catch(Exception e){Toast.makeText(this,"WhatsApp não encontrado.",Toast.LENGTH_SHORT).show();}}
    void callClient(){String raw=c.phone==null?"":c.phone.replaceAll("[^0-9+]","");if(raw.isEmpty()){Toast.makeText(this,"Cadastre o telefone primeiro.",Toast.LENGTH_SHORT).show();return;}try{startActivity(new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+raw)));}catch(Exception ignored){}}
    void confirmDelete(){new AlertDialog.Builder(this).setTitle("Excluir cliente?").setMessage("O cliente, empréstimos e arquivos serão apagados deste aparelho.").setPositiveButton("Excluir",(d,w)->{for(Models.Loan l:c.loans)AlarmScheduler.cancelLoanAlarms(this,l.id,l.installments);for(Models.Document doc:c.documents)DocumentManager.delete(doc.uriOrPath);ds.removeClient(c.id);finish();}).setNegativeButton("Cancelar",null).show();}
}
