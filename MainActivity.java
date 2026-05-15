package com.jv.paqueteria;

import android.app.*;
import android.os.*;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import android.view.*;
import android.widget.*;
import android.text.InputType;
import java.io.*;
import java.text.*;
import java.util.*;

public class MainActivity extends Activity {
    LinearLayout root;
    String conductor = "";
    ArrayList<Entrega> entregas = new ArrayList<>();
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    static class Entrega { String ref, cliente, direccion, estado="Pendiente"; Entrega(String r,String c,String d){ref=r;cliente=c;direccion=d;} }

    public void onCreate(Bundle b){ super.onCreate(b); seed(); login(); }
    void seed(){ entregas.add(new Entrega("JV-1001","Cliente Demo 1","Calle Mayor 12")); entregas.add(new Entrega("JV-1002","Cliente Demo 2","Avenida Andalucía 8")); entregas.add(new Entrega("JV-1003","Cliente Demo 3","Polígono Industrial Nave 4")); }
    TextView tv(String t,int sp,int color){ TextView v=new TextView(this); v.setText(t); v.setTextSize(sp); v.setTextColor(color); v.setPadding(20,14,20,14); return v; }
    Button btn(String t){ Button b=new Button(this); b.setText(t); b.setAllCaps(false); b.setPadding(12,12,12,12); return b; }
    void base(){ ScrollView sv=new ScrollView(this); root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(24,24,24,24); sv.addView(root); setContentView(sv); }
    void login(){ base(); root.addView(tv("JV Driver - Paquetería",26,Color.rgb(20,40,70))); root.addView(tv("Acceso conductor",18,Color.DKGRAY)); EditText user=new EditText(this); user.setHint("Usuario conductor"); root.addView(user); EditText pass=new EditText(this); pass.setHint("Contraseña"); pass.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD); root.addView(pass); Button entrar=btn("Entrar"); root.addView(entrar); root.addView(tv("Demo: escribe cualquier usuario y contraseña.",14,Color.GRAY)); entrar.setOnClickListener(v->{ conductor=user.getText().toString().trim(); if(conductor.isEmpty()) conductor="Conductor Demo"; ruta(); }); }
    void ruta(){ base(); root.addView(tv("Ruta de hoy",26,Color.rgb(20,40,70))); root.addView(tv("Conductor: "+conductor+"\nFecha: "+sdf.format(new Date()),16,Color.DKGRAY)); for(Entrega e: entregas){ Button b=btn(e.ref+" - "+e.cliente+"\n"+e.direccion+"\nEstado: "+e.estado); root.addView(b); b.setOnClickListener(v->detalle(e)); } }
    void detalle(Entrega e){ base(); root.addView(tv("Albarán digital",24,Color.rgb(20,40,70))); root.addView(tv("Ref: "+e.ref+"\nCliente: "+e.cliente+"\nDirección: "+e.direccion+"\nEstado: "+e.estado,17,Color.DKGRAY)); EditText receptor=new EditText(this); receptor.setHint("Nombre receptor"); root.addView(receptor); SignatureView firma=new SignatureView(this); firma.setMinimumHeight(320); root.addView(tv("Firma en pantalla:",16,Color.BLACK)); root.addView(firma,new LinearLayout.LayoutParams(-1,320)); Button entregado=btn("Marcar entregado y generar PDF"); Button ausente=btn("Marcar ausente"); Button limpiar=btn("Limpiar firma"); root.addView(entregado); root.addView(ausente); root.addView(limpiar); limpiar.setOnClickListener(v->firma.clear()); ausente.setOnClickListener(v->{ e.estado="Ausente"; toast("Marcado ausente"); ruta(); }); entregado.setOnClickListener(v->{ e.estado="Entregado"; File pdf=crearPdf(e,receptor.getText().toString(),firma.getBitmap()); toast("PDF creado: "+pdf.getName()); ruta(); }); }
    File crearPdf(Entrega e,String receptor,Bitmap firma){ try{ PdfDocument doc=new PdfDocument(); PdfDocument.PageInfo info=new PdfDocument.PageInfo.Builder(595,842,1).create(); PdfDocument.Page p=doc.startPage(info); Canvas c=p.getCanvas(); Paint paint=new Paint(); paint.setTextSize(22); c.drawText("ALBARÁN DE ENTREGA",40,60,paint); paint.setTextSize(15); int y=110; String[] lines={"Referencia: "+e.ref,"Cliente: "+e.cliente,"Dirección: "+e.direccion,"Conductor: "+conductor,"Receptor: "+receptor,"Estado: "+e.estado,"Fecha/hora: "+sdf.format(new Date()),"GPS: pendiente de activar en versión producción"}; for(String line:lines){ c.drawText(line,40,y,paint); y+=30; } c.drawText("Firma:",40,y+20,paint); if(firma!=null)c.drawBitmap(Bitmap.createScaledBitmap(firma,300,140,false),40,y+40,paint); doc.finishPage(p); File dir=new File(getExternalFilesDir(null),"albaranes"); dir.mkdirs(); File f=new File(dir,e.ref+".pdf"); FileOutputStream out=new FileOutputStream(f); doc.writeTo(out); doc.close(); out.close(); return f; }catch(Exception ex){ toast("Error PDF: "+ex.getMessage()); return new File(""); } }
    void toast(String s){ Toast.makeText(this,s,Toast.LENGTH_LONG).show(); }
    public static class SignatureView extends View { Path path=new Path(); Paint paint=new Paint(); Bitmap bm; Canvas canvas; public SignatureView(android.content.Context c){ super(c); paint.setColor(Color.BLACK); paint.setStrokeWidth(5); paint.setStyle(Paint.Style.STROKE); setBackgroundColor(Color.rgb(245,245,245)); } protected void onSizeChanged(int w,int h,int oldw,int oldh){ bm=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888); canvas=new Canvas(bm); canvas.drawColor(Color.WHITE); } protected void onDraw(Canvas c){ c.drawPath(path,paint); } public boolean onTouchEvent(android.view.MotionEvent e){ float x=e.getX(), y=e.getY(); if(e.getAction()==0) path.moveTo(x,y); else if(e.getAction()==2) path.lineTo(x,y); else if(e.getAction()==1 && canvas!=null) canvas.drawPath(path,paint); invalidate(); return true; } Bitmap getBitmap(){ if(canvas!=null) canvas.drawPath(path,paint); return bm; } void clear(){ path.reset(); if(canvas!=null) canvas.drawColor(Color.WHITE); invalidate(); } }
}
