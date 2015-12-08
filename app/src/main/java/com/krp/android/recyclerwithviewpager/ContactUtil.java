package com.krp.android.recyclerwithviewpager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class ContactUtil {
	
	public static void sendEmail(Context ctx,String emailId,String subject,String body)
	{
		Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
				"mailto", emailId, null));
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(Intent.EXTRA_TEXT, body);
		ctx.startActivity(Intent.createChooser(emailIntent, "Send email..."));
	}
	
	public static void sendSms(Context ctx,String number,String body)
	{
		String url = "smsto:"+number;
		
	    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
	    
	    intent.putExtra("sms_body", body);
	    
	    ctx.startActivity(intent);
	}
	
	public static void callPhone(Context ctx,String number)
	{
		String url;
		if(number.contains("+91")){
			url ="tel:"+number;
		}else {
			url = "tel:+91"+number;
		}

		
//	    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(url));
//	    
//	    ctx.startActivity(intent);
	    
	    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(url));
	    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    ctx.startActivity(intent);
	}

}
