package com.sabaothtech.zxingdemo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.google.zxing.WriterException;
import com.google.zxing.client.android.Intents;
import com.google.zxing.client.android.encode.QRCodeEncoder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity {

	private Button btn_Decode, btn_Encode;
	private EditText txt_Encode;
	private TextView lab_Info;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		btn_Decode = (Button)findViewById(R.id.btn_Decode);
		btn_Decode.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				IntentIntegrator integrator = new IntentIntegrator(Main.this);
                integrator.initiateScan(IntentIntegrator.ALL_CODE_TYPES);
			}
			
		});
		
		txt_Encode = (EditText)findViewById(R.id.txt_Encode);
		lab_Info = (TextView)findViewById(R.id.lab_Info);
		btn_Encode = (Button)findViewById(R.id.btn_Encode);
		btn_Encode.setOnClickListener(EncodeText);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
	    IntentResult result = 
	    		IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
	    if (result != null) {
	      String contents = result.getContents();
	      if (contents != null) {
	        showDialog("Decode Info!", contents);
	      } else {
	        Toast.makeText(this, "Decode Fail!", Toast.LENGTH_LONG).show();
	      }
	    }
	}

	private void showDialog(String Title, String Content){
		AlertDialog.Builder builder = 
			    new AlertDialog.Builder(this);
        builder.setTitle(Title);
		builder.setMessage(Content);
		builder.setPositiveButton("確定", null);
		builder.show();
	}
	
	private OnClickListener EncodeText = new OnClickListener(){

		@Override
		public void onClick(View v) {
			WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
		    Display display = manager.getDefaultDisplay();
		    int width = display.getWidth();
		    int height = display.getHeight();
		    int smallerDimension = width < height ? width : height;
		    smallerDimension = smallerDimension * 7 / 8;

		    Intent intent = new Intent(Intents.Encode.ACTION);//Encode Action
		    intent.addCategory(Intent.CATEGORY_DEFAULT);
		    intent.putExtra("ENCODE_TYPE", "TEXT_TYPE");//編碼類型為純文字
		    intent.putExtra("ENCODE_DATA", txt_Encode.getText().toString());//編碼內容
		    intent.putExtra(Intents.Encode.FORMAT, "QR_CODE");//編碼格式
		  
		    try {
		    	boolean useVCard = false;
		        QRCodeEncoder qrCodeEncoder = 
		        		new QRCodeEncoder(Main.this, intent, smallerDimension, useVCard);
		        Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
		        if (bitmap == null) {
		        	System.out.println("Could not encode barcode");
			        return;
		        }

		        //設定檔案到ImageView
		        ImageView view = (ImageView) findViewById(R.id.imageView1);
		        view.setImageBitmap(bitmap);
		        
		        //寫出檔案到SDCard
		        File file = new File(
		        		android.os.Environment.getExternalStorageDirectory().getAbsolutePath(), 
		        		"QRCode.jpg");
			      if (file.exists()) file.delete();
			     
			      FileOutputStream out = null;
			      try {
			           out = new FileOutputStream(file);
			           bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
			      } catch (Exception e) {
			           e.printStackTrace();
			      } finally {
			        if (out != null) {
			            try {
							out.flush();
							out.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
			        }
			    }
			   
			    lab_Info.setText(file.getAbsolutePath());
			    
		      } catch (WriterException e) {
		    	System.out.println("Could not encode barcode");
		      }
		}
		
	};
}
