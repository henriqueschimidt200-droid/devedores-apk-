package br.com.devedores.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import java.util.Collections;

public class AddClientActivity extends Activity {
    EditText name, cpf, rg, phone, email, address, notes;
    DataStore ds;
    Uri pendingPhoto;
    ImageView photoPreview;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(Ui.BG); getWindow().setNavigationBarColor(Ui.BG);
        ds=new DataStore(this); build();
    }

    void build(){
        LinearLayout p=Ui.col(this);
        LinearLayout top=Ui.row(this);
        Button back=Ui.btnDark(this,"‹  Voltar"); back.setOnClickListener(v->finish());
        top.addView(back,new LinearLayout.LayoutParams(Ui.dp(this,88),Ui.dp(this,46)));
        LinearLayout tt=Ui.col(this); tt.setPadding(Ui.dp(this,10),0,0,0);
        tt.addView(Ui.title(this,"Novo cliente",24));
        tt.addView(Ui.label(this,"Cadastre o perfil completo e comece a organizar a pasta."));
        top.addView(tt,new LinearLayout.LayoutParams(0,Ui.dp(this,56),1));
        p.addView(top); Ui.gap(this,p,16);

        LinearLayout profile=Ui.heroCard(this,Ui.GOLD);
        LinearLayout pr=Ui.row(this);
        photoPreview=new ImageView(this); photoPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        photoPreview.setBackgroundColor(Ui.SURFACE_3); photoPreview.setImageResource(android.android.R.drawable.ic_menu_camera);
        pr.addView(photoPreview,new LinearLayout.LayoutParams(Ui.dp(this,88),Ui.dp(this,88)));
        LinearLayout pc=Ui.col(this); pc.setPadding(Ui.dp(this,14),0,0,0);
        pc.addView(Ui.eyebrow(this,"FOTO DO CLIENTE"));
        pc.addView(Ui.title(this,"Identificação visual",17));
        pc.addView(Ui.label(this,"Adicione uma foto para encontrar a pasta mais rapidamente."));
        Button choose=Ui.btn(this,"Adicionar foto"); choose.setOnClickListener(v->pickPhoto());
        pc.addView(choose,new LinearLayout.LayoutParams(-1,Ui.dp(this,42)));
        pr.addView(pc,new LinearLayout.LayoutParams(0,Ui.dp(this,102),1));
        profile.addView(pr); p.addView(profile); Ui.gap(this,p,14);

        name=Ui.field(this,"Nome completo *"); cpf=Ui.field(this,"CPF"); rg=Ui.field(this,"RG");
        phone=Ui.field(this,"Telefone / WhatsApp"); email=Ui.field(this,"E-mail"); address=Ui.field(this,"Endereço completo"); notes=Ui.field(this,"Observações iniciais");
        EditText[] es=new EditText[]{name,cpf,rg,phone,email,address,notes};
        String[] labs=new String[]{"IDENTIFICAÇÃO","DOCUMENTO","DOCUMENTO","CONTATO","CONTATO","ENDEREÇO","OBSERVAÇÕES"};
        for(int i=0;i<es.length;i++){p.addView(Ui.eyebrow(this,labs[i]));p.addView(es[i],new LinearLayout.LayoutParams(-1,i==6?Ui.dp(this,108):Ui.dp(this,56)));Ui.gap(this,p,9);}

        LinearLayout tips=Ui.softCard(this,Ui.BLUE);
        tips.addView(Ui.eyebrow(this,"ORGANIZAÇÃO AUTOMÁTICA"));
        tips.addView(Ui.label(this,"A pasta do cliente já nasce com RG, CPF, Comprovante, Contrato, Fotos e Outros."));
        p.addView(tips); Ui.gap(this,p,12);

        Button save=Ui.btn(this,"Criar cliente e abrir pasta");
        save.setOnClickListener(v->saveClient());
        p.addView(save,new LinearLayout.LayoutParams(-1,Ui.dp(this,56)));
        Ui.gap(this,p,8);
        Button cancel=Ui.btnGhost(this,"Cancelar"); cancel.setOnClickListener(v->finish()); p.addView(cancel);
        setContentView(sc); Ui.applySystemBars(this, p);
    }

    void pickPhoto(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT); i.setType("image/*"); i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i,901);
    }

    @Override protected void onActivityResult(int r,int res,Intent data){
        super.onActivityResult(r,res,data);
        if(r==901 && res==RESULT_OK && data!=null && data.getData()!=null){
            pendingPhoto=data.getData(); photoPreview.setImageURI(pendingPhoto);
        }
    }

    void saveClient(){
        if(name.getText().toString().trim().isEmpty()){name.setError("Informe o nome");return;}
        Models.Client c=new Models.Client();
        c.name=name.getText().toString().trim(); c.cpf=cpf.getText().toString().trim(); c.rg=rg.getText().toString().trim();
        c.phone=phone.getText().toString().trim(); c.email=email.getText().toString().trim(); c.address=address.getText().toString().trim(); c.notes=notes.getText().toString();
        Collections.addAll(c.folders,"RG","CPF","Comprovante","Contrato","Fotos","Outros");
        ds.clients.add(c);
        try {
            if(pendingPhoto!=null){
                String path=DocumentManager.copyToClient(this,c.id,"Fotos",pendingPhoto); c.profileImagePath=path;
                Models.Document d=new Models.Document(); d.folder="Fotos"; d.name=DocumentManager.displayName(getContentResolver(),pendingPhoto); if(d.name==null)d.name="Foto do cliente";
                d.uriOrPath=path; d.mime="image/*"; c.documents.add(d);
            }
        } catch(Exception ignored){}
        ds.save(); Toast.makeText(this,"Cliente salvo com sucesso",Toast.LENGTH_SHORT).show();
        Intent i=new Intent(this,ClientActivity.class); i.putExtra("id",c.id); startActivity(i); finish();
    }
}
