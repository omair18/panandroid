package fr.ensicaen.panandroid.test;

import fr.ensicaen.panandroid.meshs.Sphere;
import fr.ensicaen.panandroid.snapshot.Snapshot;
import fr.ensicaen.panandroid.snapshot.SnapshotManager;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;



public class JSONTestActivity extends Activity
{

	private SnapshotManager mSnapshotManager;
	private View mView;

	/**
	 * Called when the activity is first created.
	 * @param savedInstanceState - The instance state.
	 */
	@Override
	public void onCreate(final Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	    
	    //view in fullscreen
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	
		
		
		mSnapshotManager = new SnapshotManager();
		
		for( int i = 0; i < 20; i++)
		{
			Snapshot s = new Snapshot(i,i);
			s.setFileName(i+".jpeg");

			mSnapshotManager.addSnapshot(s);
		}
		mView = new View(this);
		this.setContentView(this.mView);	
		
		//TODO
		//mSnapshotManager.createJSONFile();
	}
	
	  
	/**
	 * Remember to resume the glSurface.
	 */
	@Override
	protected void onResume()
	{
		super.onResume();
	}
	
	/**
	 * Also pause the glSurface.
	 */
	@Override
	protected void onPause()
	{
		super.onPause();
	}
}
