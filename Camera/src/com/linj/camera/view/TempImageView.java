package com.linj.camera.view;

import com.example.camera.R;
import com.linj.camera.view.CameraContainer.TakePictureListener;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;


/** 
* @ClassName: TempImageView 
* @Description:   ��ʱImageView����ִ����һ���������Զ�����
* @author LinJ
* @date 2014-12-31 ����9:45:34 
*  
*/
public class TempImageView extends ImageView implements AnimationListener{
	
	private final static String TAG="TempImageView";
	
	/**
	 * �����ڵĶ���ID
	 */
	public static final int NO_ID=-1;
	/**
	 * ���õĶ���Ч��ID
	 */
	private int mAnimationID=NO_ID;

    /** ���ն��������ӿ�  */ 
    private TakePictureListener mListener;
	
	public TempImageView(Context context) {
		// TODO Auto-generated constructor stub
		super(context);
	}

	public TempImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TempImageView);
		mAnimationID = a.getResourceId(R.styleable.TempImageView_animat_id, NO_ID);
		a.recycle();
		setVisibility(View.GONE);
	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub
		setVisibility(View.VISIBLE);
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		// TODO Auto-generated method stub
		setVisibility(View.GONE);
		Drawable drawable =  getDrawable();
		Bitmap bm = null;
		if(drawable!=null&&drawable instanceof BitmapDrawable)
			 bm=((BitmapDrawable)drawable).getBitmap();
		//��������ӿڲ�Ϊ�գ���ִ�����ս�������
		if (mListener!=null) mListener.onAnimtionEnd(bm);
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub

	}

	
	/**  
	* @Description: ��ʼ����
	* @param    
	* @return void    
	* @throws 
	*/
	public void startAnimation(){
		startAnimation(null);
	}


	/** 
	* @Description: ��ʼ����
	* @param @param resourceID ������Դ��ID  
	* @return void    
	* @throws 
	*/
	public void startAnimation(int resourceID){
		mAnimationID=resourceID;
		startAnimation();
	}
	
	public void startAnimation(Animation animation){
		if(animation!=null){
			animation.setAnimationListener(this);
			super.startAnimation(animation);
			return;
		}
		if(mAnimationID!=NO_ID){
			animation=AnimationUtils.loadAnimation(getContext(), mAnimationID);
			animation.setAnimationListener(this);
			super.startAnimation(animation);
		}
	}

    @Override
    public void setImageBitmap(Bitmap bm) {
    	// TODO Auto-generated method stub
    	super.setImageBitmap(bm);
    }
	public void setListener(TakePictureListener mListener) {
		this.mListener = mListener;
	}
}