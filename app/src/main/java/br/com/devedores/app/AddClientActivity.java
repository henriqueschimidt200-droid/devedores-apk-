package br.com.devedores.app;

import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.widget.*;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.util.Collections;

public class AddClientActivity extends Activity {
    EditText name, cpf, rg, phone, email, address, notes;
    DataStore ds; Uri pendingPhoto; ImageView photoPreview; File cameraFile;
    static final int REQ_GALLERY=901, REQ_CAMERA=902, REQ_PERMISSION=903;

    @Override public void onCreate(Bundle b){super.onCreate(b);ds=new DataStore(this);build();}

    void build(){
        LinearLayout p=Ui.col(this);ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.setVerticalScrollBarEnabled(false);sc.addView(p);setContentView(sc);Ui.applySystemBars(this,p);
        LinearLayout top=Ui.row(this);Button back=Ui.btnDark(this,"‹  Voltar");back.setOnClickListener(v->finish());top.addView(back,new LinearLayout.LayoutParams(Ui.dp(this,88),Ui.dp(this,46)));LinearLayout tt=Ui.col(this);tt.setPadding(Ui.dp(this,10),0,0,0);tt.addView(Ui.title(this,"Novo cliente",24));tt.addView(Ui.label(this,"Monte o perfil completo e já organize os documentos."));top.addView(tt,new LinearLayout.LayoutParams(0,Ui.dp(this,56),1));p.addView(top);Ui.gap(this,p,16);
        LinearLayout profile=Ui.heroCard(this,Ui.GOLD);LinearLayout pr=Ui.row(this);photoPreview=new ImageView(this);photoPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);photoPreview.setBackgroundColor(Ui.SURFACE_3);photoPreview.setImageResource(android.R.drawable.ic_menu_camera);pr.addView(photoPreview,new LinearLayout.LayoutParams(Ui.dp(this,92),Ui.dp(this,92)));LinearLayout pc=Ui.col(this);pc.setPadding(Ui.dp(this,14),0,0,0);pc.addView(Ui.eyebrow(this,"FOTO DO CLIENTE"));pc.addView(Ui.title(this,"Identificação visual",17));pc.addView(Ui.label(this,"Escolha várias formas de adicionar a foto."));LinearLayout ar=Ui.row(this);Button choose=Ui.btn(this,"＋ Galeria");choose.setOnClickListener(v->pickGallery());Button cam=Ui.btnDark(this,"📷 Câmera");cam.setOnClickListener(v->openCamera());ar.addView(choose,new LinearLayout.LayoutParams(0,Ui.dp(this,42),1));Ui.gap(this,ar,5);ar.addView(cam,new LinearLayout.LayoutParams(0,Ui.dp(this,42),1));pc.addView(ar);pr.addView(pc,new LinearLayout.LayoutParams(0,Ui.dp(this,112),1));profile.addView(pr);p.addView(profile);Ui.gap(this,p,14);
        name=Ui.field(this,"Nome completo *");cpf=Ui.field(this,"CPF");rg=Ui.field(this,"RG");phone=Ui.field(this,"Telefone / WhatsApp");email=Ui.field(this,"E-mail");address=Ui.field(this,"Endereço completo");notes=Ui.field(this,"Observações iniciais");EditText[] es={name,cpf,rg,phone,email,address,notes};String[] labs={"IDENTIFICAÇÃO","DOCUMENTO","DOCUMENTO","CONTATO","CONTATO","ENDEREÇO","OBSERVAÇÕES"};for(int i=0;i<es.length;i++){p.addView(Ui.eyebrow(this,labs[i]));p.addView(es[i],new LinearLayout.LayoutParams(-1,i==6?Ui.dp(this,115):Ui.dp(this,56)));Ui.gap(this,p,9);}LinearLayout tips=Ui.softCard(this,Ui.BLUE);tips.addView(Ui.eyebrow(this,"PASTA AUTOMÁTICA"));tips.addView(Ui.label(this,"RG, CPF, Comprovante, Contrato, Fotos e Outros são criados automaticamente."));p.addView(tips);Ui.gap(this,p,12);Button save=Ui.btn(this,"Criar cliente e abrir pasta");save.setOnClickListener(v->saveClient());p.addView(save,new LinearLayout.LayoutParams(-1,Ui.dp(this,56)));Ui.gap(this,p,8);Button cancel=Ui.btnGhost(this,"Cancelar");cancel.setOnClickListener(v->finish());p.addView(cancel,new LinearLayout.LayoutParams(-1,Ui.dp(this,48)));
    }
    void pickGallery(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("image/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,REQ_GALLERY);}
    void openCamera(){if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},REQ_PERMISSION);return;}try{cameraFile=new File(getFilesDir(),"new_client_"+System.currentTimeMillis()+".jpg");Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);i.putExtra(MediaStore.EXTRA_OUTPUT,DocumentManager.uriForFile(this,cameraFile.getAbsolutePath()));i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivityForResult(i,REQ_CAMERA);}catch(Exception e){Toast.makeText(this,"Câmera indisponível",Toast.LENGTH_SHORT).show();}}
    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){super.onRequestPermissionsResult(requestCode,permissions,grantResults);if(requestCode==REQ_PERMISSION&&grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)openCamera();}
    @Override protected void onActivityResult(int r,int res,Intent data){super.onActivityResult(r,res,data);if(res!=RESULT_OK)return;try{if(r==REQ_GALLERY&&data!=null&&data.getData()!=null){pendingPhoto=data.getData();photoPreview.setImageURI(pendingPhoto);}else if(r==REQ_CAMERA&&cameraFile!=null&&cameraFile.exists()){pendingPhoto=DocumentManager.uriForFile(this,cameraFile.getAbsolutePath());photoPreview.setImageURI(pendingPhoto);}}catch(Exception e){Toast.makeText(this,"Não foi possível carregar a foto",Toast.LENGTH_SHORT).show();}}
    void saveClient(){if(name.getText().toString().trim().isEmpty()){name.setError("Informe o nome");return;}Models.Client c=new Models.Client();c.name=name.getText().toString().trim();c.cpf=cpf.getText().toString().trim();c.rg=rg.getText().toString().trim();c.phone=phone.getText().toString().trim();c.email=email.getText().toString().trim();c.address=address.getText().toString().trim();c.notes=notes.getText().toString();Collections.addAll(c.folders,"RG","CPF","Comprovante","Contrato","Fotos","Outros");ds.clients.add(c);try{if(cameraFile!=null&&cameraFile.exists()){String path=DocumentManager.copyFileToClient(this,c.id,"Fotos",cameraFile,"foto_perfil.jpg","image/jpeg");c.profileImagePath=path;Models.Document d=new Models.Document();d.folder="Fotos";d.name="Foto de perfil";d.uriOrPath=path;d.mime="image/jpeg";c.documents.add(d);cameraFile.delete();}else if(pendingPhoto!=null){String path=DocumentManager.copyToClient(this,c.id,"Fotos",pendingPhoto);c.profileImagePath=path;Models.Document d=new Models.Document();d.folder="Fotos";d.name=DocumentManager.displayName(getContentResolver(),pendingPhoto);if(d.name==null)d.name="Foto do cliente";d.uriOrPath=path;d.mime="image/*";c.documents.add(d);}}catch(Exception ignored){}ds.save();Toast.makeText(this,"Cliente criado",Toast.LENGTH_SHORT).show();Intent i=new Intent(this,ClientActivity.class);i.putExtra("id",c.id);startActivity(i);finish();}
}
