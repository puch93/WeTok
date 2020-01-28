package kr.co.core.wetok.util;

import android.app.Activity;
import android.widget.Toast;

public class BackPressCloseHandler {
	
	private long backKeyPressedTime = 0;
	private Toast toast;
	private Activity act;
	
	public BackPressCloseHandler(Activity activity ){
		this.act = activity;
	}
	
	public void onBackPressed() {
		if(System.currentTimeMillis() > backKeyPressedTime + 2000) {
			backKeyPressedTime = System.currentTimeMillis();
			showGuide();
			return;
		}
		
		if(System.currentTimeMillis() <= backKeyPressedTime + 2000) {
			act.moveTaskToBack(true);
			act.finish();
			
			toast.cancel();
		}
	}
	
	private void showGuide() {
		toast = Toast.makeText(act, "뒤로 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
		toast.show();
	}

}
