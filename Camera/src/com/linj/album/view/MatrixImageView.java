package com.linj.album.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.ImageView;

/** 
 * @ClassName: MatrixImageView 
 * @Description:  ���Ŵ���С���ƶ�Ч����ImageView
 * @author LinJ
 * @date 2015-1-7 ����11:15:07 
 *  
 */
public class MatrixImageView extends ImageView{
	private final static String TAG="MatrixImageView";
	private GestureDetector mGestureDetector;
	public MatrixImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mGestureDetector=new GestureDetector(getContext(), new GestureListener());
		setScaleType(ScaleType.CENTER_CROP);
	}
    float scaleCount=0;
	private static final int MODE_INIT = 0;
	private static final int MODE_DRAG = 1;
	/** �Ŵ���С��Ƭģʽ */
	private static final int MODE_ZOOM = 2;
	/** ��¼��������Ƭģʽ���ǷŴ���С��Ƭģʽ */
	private int mode = MODE_INIT;// ��ʼ״̬ 
	/** ������Ƭģʽ */
	private float startDis;
	private Matrix matrix = new Matrix();
	/** ���ڼ�¼ͼƬҪ��������ʱ�������λ�� */
	private Matrix currentMatrix = new Matrix();
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			Log.i(TAG, "ACTION_DOWN");
			mode=MODE_INIT;
			currentMatrix.set(getImageMatrix());
			setScaleType(ScaleType.MATRIX);
			break;
		case MotionEvent.ACTION_UP:
			setScaleType(ScaleType.FIT_XY);
			Log.i(TAG, "ACTION_UP");
			break;
		case MotionEvent.ACTION_CANCEL:
			Log.i(TAG, "ACTION_CANCEL");
			break;
		case MotionEvent.ACTION_MOVE:

			if (mode == MODE_ZOOM) {
				//ֻ��ͬʱ�����������ʱ���ִ��
				if(event.getPointerCount()<2) return true;
				float endDis = distance(event);// ��������
				if (endDis > 10f) { // ������ָ��£��һ���ʱ�����ش���10
					float scale = endDis / startDis;// �õ����ű���
					if(scaleCount+scale>1){
						matrix.set(currentMatrix);
						Log.i(TAG, getWidth()+" "+getHeight());
						matrix.postScale(scale, scale,getWidth()/2,getHeight()/2);
						setImageMatrix(matrix);
						scaleCount+=scale-1;
					}
				}
			}
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			mode=MODE_ZOOM;
			/** ����������ָ��ľ��� */
			startDis = distance(event);
			if (startDis > 10f) { // ������ָ��£��һ���ʱ�����ش���10
				currentMatrix.set(getImageMatrix());
			}
			Log.i(TAG, "ACTION_POINTER_DOWN");
			break;
		case MotionEvent.ACTION_POINTER_UP:
			Log.i(TAG, "ACTION_POINTER_UP");
			break;
		default:
			break;
		}
		
		return true;

		//		return mGestureDetector.onTouchEvent(event);
	}
	/** ����������ָ��ľ��� */
	private float distance(MotionEvent event) {
		float dx = event.getX(1) - event.getX(0);
		float dy = event.getY(1) - event.getY(0);
		/** ʹ�ù��ɶ���������֮��ľ��� */
		return (float) Math.sqrt(dx * dx + dy * dy);
	}
	/** ����������ָ����м�� */
	private PointF mid(MotionEvent event) {
		float midX = (event.getX(1) + event.getX(0)) / 2;
		float midY = (event.getY(1) + event.getY(0)) / 2;
		return new PointF(midX, midY);
	}
	private class  GestureListener extends SimpleOnGestureListener{

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			// TODO Auto-generated method stub
			return super.onSingleTapUp(e);
		}

		@Override
		public void onLongPress(MotionEvent e) {
			// TODO Auto-generated method stub
			super.onLongPress(e);
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			// TODO Auto-generated method stub
			return super.onScroll(e1, e2, distanceX, distanceY);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			// TODO Auto-generated method stub
			return super.onFling(e1, e2, velocityX, velocityY);
		}

		@Override
		public void onShowPress(MotionEvent e) {
			// TODO Auto-generated method stub
			super.onShowPress(e);
		}

		@Override
		public boolean onDown(MotionEvent e) {
			// TODO Auto-generated method stub
			return super.onDown(e);
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			// TODO Auto-generated method stub
			return super.onDoubleTap(e);
		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent e) {
			// TODO Auto-generated method stub
			return super.onDoubleTapEvent(e);
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			// TODO Auto-generated method stub
			return super.onSingleTapConfirmed(e);
		}

	}


}
