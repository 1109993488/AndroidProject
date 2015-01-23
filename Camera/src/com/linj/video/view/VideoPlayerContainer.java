package com.linj.video.view;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.example.camera.R;
import com.linj.video.view.VideoPlayerView.PlayerListener;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.view.View.OnClickListener;;

/** 
 * @ClassName: VideoContainer 
 * @Description:  ��Ƶ��������������������Ƶ������һЩ�ؼ�����Ƶ����SurfaceView
 * @author LinJ
 * @date 2015-1-21 ����4:49:31 
 *  
 */
public class VideoPlayerContainer extends LinearLayout implements OnClickListener,PlayerListener{
	private final static String TAG="VideoPlayerContainer";
	private VideoPlayerView mVideoPlayerView;
	private LinearLayout mBottomBar;
	private TextView mCurrentTimeView;
	private TextView mDurationView;
	private ImageView mPauseButton;
	private SeekBar mProgressBar;
	private Handler mHandler;
	private SimpleDateFormat mTimeFormat=new SimpleDateFormat("mm:ss",Locale.getDefault());
	public VideoPlayerContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
		mHandler=new Handler();
	}
	private void initView(Context context){
		setOrientation(VERTICAL);
		mVideoPlayerView=new VideoPlayerView(context);
		LinearLayout.LayoutParams layoutParams=new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		layoutParams.weight=1;
		mVideoPlayerView.setLayoutParams(layoutParams);
		mVideoPlayerView.setPalyerListener(this);
		addView(mVideoPlayerView);

		mBottomBar=(LinearLayout) inflate(context, R.layout.video_bottom_bar, null);
		layoutParams=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		layoutParams.gravity=Gravity.BOTTOM;
		mBottomBar.setLayoutParams(layoutParams);
		addView(mBottomBar);

		mCurrentTimeView=(TextView) mBottomBar.findViewById(R.id.tvVideoPlayTime);
		mDurationView=(TextView) mBottomBar.findViewById(R.id.tvVideoPlayRemainTime);
		mPauseButton=(ImageView) mBottomBar.findViewById(R.id.btnVideoPlayOrPause);
		mProgressBar=(SeekBar) mBottomBar.findViewById(R.id.sbVideoDetailPlayer);
		mPauseButton.setOnClickListener(this);
		mProgressBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
	}

	OnSeekBarChangeListener onSeekBarChangeListener=new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
            seekPostion(seekBar.getProgress()*1000);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
              pausedPlay();
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			mCurrentTimeView.setText(mTimeFormat.format(new Date(progress*1000)));
		}
	};


	public void playVideo(String path) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException {
		// TODO Auto-generated method stub
		mVideoPlayerView.play(path);

	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// ���Ž��������ظÿؼ�
		setVisibility(View.GONE);
		mProgressBar.setProgress(0);
		mCurrentTimeView.setText("00:00");
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		// ��ת��ָ��ʱ��󣬻ָ�����
           resumePlay();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// ������ʹ����framelayout���֣�Ϊ�˷�ֹ����¼��·����·���view���ڴ˴����������¼���
		return true;
	}
	@Override
	public void onPrepared(MediaPlayer mp) {
		//׼�����ţ���ʾ�ÿؼ�
		setVisibility(View.VISIBLE);
		int duration=mp.getDuration();
		//��������¼�����λ��
		mDurationView.setText(mTimeFormat.format(new Date(duration)));
		mProgressBar.setMax((int) Math.floor(duration/1000));
		mp.start();
		mHandler.removeCallbacks(null, null);
		mHandler.post(playerRunnable);
	}
	Runnable playerRunnable=new Runnable() {	
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(mVideoPlayerView.isPlaying()){
				int current=mVideoPlayerView.getCurrentPosition();
				Log.i(TAG, current+" "+mProgressBar.getMax());
				current=(int) Math.floor(current/1000);
				mProgressBar.setProgress(current);
				mHandler.postAtTime(this,mCurrentTimeView, SystemClock.uptimeMillis()+500);
			}
		}
	};
	@Override
	public void onClick(View v) {

		if (mVideoPlayerView.isPlaying()) {
			pausedPlay();
		}else {
			resumePlay();
		}
	}
	/**  
	*   �ָ�����
	*/
	private void resumePlay() {
		mVideoPlayerView.switchPlayOrPaused(false);
		mHandler.removeCallbacks(null, null);
		mHandler.postDelayed(playerRunnable, 500);
		mPauseButton.setImageResource(R.drawable.video_detail_player_pause);
	}
	
	/**  
	*   ��ͣ����
	*/
	private void pausedPlay() {
		mVideoPlayerView.switchPlayOrPaused(true);
		mPauseButton.setImageResource(R.drawable.video_detail_player_start);
	}
	private void seekPostion(int position){
		mVideoPlayerView.seekPosition(position);
	}
	public void stopPlay(){
		mVideoPlayerView.stopPlay();
		setVisibility(View.GONE);
	}
}
