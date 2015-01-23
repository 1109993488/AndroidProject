package com.linj.video.view;

import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

/** 
 * @ClassName: VideoSurfaceView 
 * @Description:  ��MediaPlayer�󶨵�SurfaceView�����Բ�����Ƶ
 * @author LinJ
 * @date 2015-1-21 ����2:38:53 
 *  
 */
public class VideoPlayerView extends SurfaceView {
	private final static String TAG="VideoSurfaceView";
	private MediaPlayer mMediaPlayer;
	public VideoPlayerView(Context context){
		super(context);
		init();
	}
	public VideoPlayerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();

	}
	/**  
	 *   ��ʼ��
	 */
	private void init() {
		mMediaPlayer=new MediaPlayer();
		//��ʼ������
		getHolder().addCallback(callback);
	}
	/**  
	 *  ���ò�������������
	 *  @param listener   
	 */
	public void setPalyerListener(PlayerListener listener){
		mMediaPlayer.setOnCompletionListener(listener);
		mMediaPlayer.setOnSeekCompleteListener(listener);
		mMediaPlayer.setOnPreparedListener(listener);
	}
	/**  
	 *  ��ȡ��ǰ�������Ƿ��ڲ���״̬
	 *  @return   
	 */
	public boolean isPlaying(){
		return mMediaPlayer.isPlaying();
	}

	/**  
	 *  ��ȡ��ǰ����ʱ�䣬��λ����
	 *  @return   
	 */
	public int getCurrentPosition(){
		if(isPlaying())
			return mMediaPlayer.getCurrentPosition();
		return 0;
	}


	/**  
	 * ����/��ͣ�л�
	 *  @param paused   �Ƿ��л�Ϊ��ͣ��trueΪ��ͣ
	 */
	public void switchPlayOrPaused(boolean paused){
		if(paused)
			mMediaPlayer.pause();
		else {
			mMediaPlayer.start();
		}
	}
	
	/**  
	*   ���õ�ǰ����λ��
	*/
	public void seekPosition(int position){
		if(isPlaying())
			mMediaPlayer.pause();
		//�����õ�ʱ��ֵ������Ƶ��󳤶�ʱ��ֹͣ����
		if(position<0||position>mMediaPlayer.getDuration()){
			mMediaPlayer.stop();
			return;
		}
		//����ʱ��
		mMediaPlayer.seekTo(position);
	}
	
	/**  
	*   ֹͣ����
	*/
	public void stopPlay() {
		mMediaPlayer.stop();
		mMediaPlayer.reset();
	}
	
	private SurfaceHolder.Callback callback=new SurfaceHolder.Callback() {

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			mMediaPlayer.setDisplay(getHolder());       	
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {

		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if(mMediaPlayer.isPlaying())
				mMediaPlayer.stop();
			mMediaPlayer.reset();
		}
	};

	public void play(String path) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException{
		if(mMediaPlayer!=null&&mMediaPlayer.isPlaying()){
			mMediaPlayer.stop();
		}
		mMediaPlayer.reset(); //reset�������ò���������
		mMediaPlayer.setDataSource(path);
		mMediaPlayer.prepare();
	}

	/** 
	 * @ClassName: PlayerListener 
	 * @Description:  ���Ͻӿڣ�containerʵ�ָýӿ�
	 * @author LinJ
	 * @date 2015-1-23 ����3:09:15 
	 *  
	 */
	public interface PlayerListener extends  OnCompletionListener,
	OnSeekCompleteListener,OnPreparedListener{

	}

	

}
