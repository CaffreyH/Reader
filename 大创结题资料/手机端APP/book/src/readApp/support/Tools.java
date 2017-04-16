package readApp.support;

import java.io.ByteArrayOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class Tools {  
    /** 
     * ��pxֵת��Ϊdip��dpֵ����֤�ߴ��С���� 
     *  
     * @param pxValue 
     * @param scale 
     *            ��DisplayMetrics��������density�� 
     * @return 
     */  
    public static int px2dip(Context context, float pxValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (pxValue / scale + 0.5f);  
    }  
  
    /** 
     * ��dip��dpֵת��Ϊpxֵ����֤�ߴ��С���� 
     *  
     * @param dipValue 
     * @param scale 
     *            ��DisplayMetrics��������density�� 
     * @return 
     */  
    public static int dip2px(Context context, float dipValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (dipValue * scale + 0.5f);  
    }  
  
    /** 
     * ��pxֵת��Ϊspֵ����֤���ִ�С���� 
     *  
     * @param pxValue 
     * @param fontScale 
     *            ��DisplayMetrics��������scaledDensity�� 
     * @return 
     */  
    public static int px2sp(Context context, float pxValue) {  
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;  
        return (int) (pxValue / fontScale + 0.5f);  
    }  
  
    /** 
     * ��spֵת��Ϊpxֵ����֤���ִ�С���� 
     *  
     * @param spValue 
     * @param fontScale 
     *            ��DisplayMetrics��������scaledDensity�� 
     * @return 
     */  
    public static int sp2px(Context context, float spValue) {  
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;  
        return (int) (spValue * fontScale + 0.5f);  
    }  
    
    public static Drawable bytes2drawable(Context context, byte[] image){
		Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
	 	BitmapDrawable bd= new BitmapDrawable(context.getResources(), bmp); 
		return bd;
    }
    public static byte[] drawable2bytes(Drawable image)
    {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	Bitmap bitmap = ((BitmapDrawable) image).getBitmap();
    	bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
    	return baos.toByteArray();
    }
}  