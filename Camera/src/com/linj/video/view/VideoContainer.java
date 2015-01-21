package com.linj.video.view;

import java.io.IOException;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.RelativeLayout.LayoutParams;

/** 
 * @ClassName: VideoContainer 
 * @Description:  ��Ƶ��������������������Ƶ������һЩ�ؼ�����Ƶ����SurfaceView
 * @author LinJ
 * @date 2015-1-21 ����4:49:31 
 *  
 */
public class VideoContainer extends FrameLayout {
	private VideoView mVideoView;
	public VideoContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}
	private void initView(Context context){
		mVideoView=new VideoView(context);
		FrameLayout.LayoutParams layoutParams=new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mVideoView.setLayoutParams(layoutParams);
		addView(mVideoView);
	}
	public void playVideo(String path) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException {
		// TODO Auto-generated method stub
		mVideoView.play(path);
	}

}
