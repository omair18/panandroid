package fr.ensicaen.panandroid.renderer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import junit.framework.Assert;



import fr.ensicaen.panandroid.R;
import fr.ensicaen.panandroid.sphere.Sphere;
import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;

/**
 * Renderer for a GL SphereView. Draws a Sphere at the middle of the GL View.
 * Based on Frank Dürr 's OpenPanandroid, and 
 * Guillaume Lesniak's picSphere app.
 *
 * @author Nicolas
 */
public class SphereRenderer implements Renderer 
{
	
	
	private static final String TAG = SphereRenderer.class.getSimpleName();

	/* *************
	 * GLOBAL RENDERING PARAMS
	 * *************/
	
	/** Clear colour */
	private static final float CLEAR_RED = 0.0f;
	private static final float CLEAR_GREEN = 0.0f;
	private static final float CLEAR_BLUE = 0.0f;
	private static final float CLEAR_ALPHA = 2.5f;
		
	/** Perspective setup, field of view component. */
	private static final float DEFAULT_FOV = 60.0f;
	
	/** Perspective setup, near component. */
	private static final float Z_NEAR = 0.1f;
	
	/** Perspective setup, far component. */
	private static final float Z_FAR = 100.0f;
		
	/** Sphere's distance on the screen. */
	private static final float OBJECT_DISTANCE = 0.00f;

	private final static float COORD = (float) Math.sin(Math.PI/4.0);
	
	/* *************
	 * ATTRIBUTES
	 * *************/
	
	/** The sphere. */
	protected final Sphere mSphere;
	
	/** The context. */
	private final Context mContext;
	
	/** Diagonal field of view **/
	private float fovDeg; 
	
	/** The rotation angle of the sphere */
	private float mYaw; 	/* Rotation around y axis */
	private float mPitch; 	/* Rotation around x axis */

	
	// While accessing this matrix, the renderer object has to be locked.
	private float[] mRotationMatrix = new float[16];
	
	private int mSurfaceWidth, mSurfaceHeight;
	
	
	/** rotation speed decreasing coef **/
	private float mRotationFriction = 200; // [deg/s^2]
	
	/** inertia rotation active **/
	private boolean inertiaEnabled = false;
	
	/** inertia rotation speed **/
	private float mRotationYawSpeed, mRotationPitchSpeed; // [deg/s]
	private float mYaw0, mPitch0;
	private long mT0; // [ms]
	
	/* *************
	 * CONSTRUCTOR
	 * *************/
	
	/**
	* Constructor to set the handed over context.
	* @param context The context.
	*/
	public SphereRenderer(final Context context, Sphere sphere) 
	{
		mContext = context;
		mSphere = sphere;
		mYaw = mPitch = 0.0f;
	  	setRotation(mYaw, mPitch);
	  	fovDeg = DEFAULT_FOV;	
	}

	
	/* *************
	 * RENDERER OVERRIDES
	 * *************/
	@Override
	public void onDrawFrame(final GL10 gl) 
	{
		
		//clear the whole frame
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
	  	
		//reset referential
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		//compute an eventual new matrix rotation according to inertia
	  	doInertiaRotation();

	  	//load the rotation matrix
	  	synchronized (this) {
	  		gl.glLoadMatrixf(mRotationMatrix, 0);
	  	}

	  	//draw the sphere in this new referential.
		this.mSphere.draw(gl);
	}
	
	
	@Override
	public void onSurfaceChanged(final GL10 gl, final int width, final int height) 
	{
		mSurfaceWidth = width;
		mSurfaceHeight = height;
		
		setProjection(gl);		
		gl.glViewport(0, 0, width, height);
		
	}

	@Override
	public void onSurfaceCreated(final GL10 gl, final EGLConfig config)
	{
		
		
		this.mSphere.loadGLTexture(gl, mContext, R.raw.white_pano);
		
		//texture mode
		gl.glEnable(GL10.GL_TEXTURE_2D);
		
		// smooth shader
		gl.glShadeModel(GL10.GL_SMOOTH);
					//gl.glEnable(GL10.GL_BLEND);

		
		// Don't draw back sides.
					/*
				    gl.glEnable(GL10.GL_CULL_FACE);
				    gl.glFrontFace(GL10.GL_CCW);
				    gl.glCullFace(GL10.GL_BACK);*/
	    
	    gl.glEnable(GL10.GL_DEPTH_TEST);
	    
		gl.glClearColor(CLEAR_RED, CLEAR_GREEN, CLEAR_BLUE, CLEAR_ALPHA);
		gl.glClearDepthf(1.0f);
		
					//gl.glDepthFunc(GL10.GL_LEQUAL);
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
		
	    
				    /*//done in drawSphere
					gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
					gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
					
					gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
					*/
		
					//setupTextures(gl);
		
	}
	
	/* *************
	 * PRIVATE FUNCTIONS
	 * ************/
	
	/**
	 * Computes rotation matrix from euler's angles mYaw and mPitch
	 */
	private synchronized void computeRotationMatrix()
	{
		// Rotation matrix in column mode.
		double yawRad = Math.toRadians(mYaw);
		double pitchRad = Math.toRadians(mPitch);
		
		float cosYaw = (float) Math.cos(yawRad);
		float sinYaw = (float) Math.sin(yawRad);
		float cosPitch = (float) Math.cos(pitchRad);
		float sinPitch = (float) Math.sin(pitchRad);
		
		mRotationMatrix[0] = cosYaw;
		mRotationMatrix[1] = sinPitch*sinYaw;
		mRotationMatrix[2] = (float) ((-1.0)*cosPitch*sinYaw);
		mRotationMatrix[3] = 0.0f;
		
		mRotationMatrix[4] = 0.0f;
		mRotationMatrix[5] = cosPitch;
		mRotationMatrix[6] = sinPitch;
		mRotationMatrix[7] = 0.0f;
		
		mRotationMatrix[8] = sinYaw;
		mRotationMatrix[9] = (float) (-1.0*sinPitch*cosYaw);
		mRotationMatrix[10] = (float) (cosYaw*cosPitch);
		mRotationMatrix[11] = 0.0f;
		
		mRotationMatrix[12] = 0.0f;
		mRotationMatrix[13] = 0.0f;
		mRotationMatrix[14] = 0.0f;
		mRotationMatrix[15] = 1.0f;
  	}
	
	/**
	 * keep pitch and yaw to positive values
	 */
	private void normalizeRotation() 
	{
		
		mYaw %= 360.0f;
		if (mYaw < -180.0f) 
			mYaw = 360.0f + mYaw;
		else if (mYaw > 180.0f)
			mYaw = -360.0f + mYaw;
		
		
		if (mPitch < -90.0f) 
			mPitch = -90.0f;
		else if (mPitch > 90.0f)
			mPitch = 90.0f;
		
		Assert.assertTrue(mYaw >= -180.0f && mYaw <= 180.0f);
		Assert.assertTrue(mPitch >= -90.0f && mPitch <= 90.0f);
	}
	
	/**
	 * Computes projection matrix.
	 * @param gl
	 */
	private void setProjection(GL10 gl)
	{
		final float aspectRatio = (float) mSurfaceWidth / (float) (mSurfaceHeight == 0 ? 1 : mSurfaceHeight);
		
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		
		float fovYDeg = getVFovDeg();
		GLU.gluPerspective(gl, fovYDeg, aspectRatio, Z_NEAR, Z_FAR);
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
	}
	
	
	/* *************
	 * PUBLIC METHODS
	 * *************/
	/**
	 * Rotate the sphere given euler's angles.
	 * @param pitch - rotation around y axis.
	 * @param yaw - rotation around x axis.
	 */
	public void setRotation(float pitch, float yaw)
	{
		mYaw = yaw;
		mPitch = pitch;
		normalizeRotation();
		computeRotationMatrix();
	}
	
	/**
	 * 
	 * @param pitchSpeed
	 * @param yawSpeed
	 */
	public synchronized void startInertiaRotation(float pitchSpeed, float yawSpeed)
	{
		inertiaEnabled = true;
		mRotationYawSpeed = yawSpeed;
		mRotationPitchSpeed = pitchSpeed;
		mYaw0 = mYaw;
		mPitch0 = mPitch;
		mT0 = System.currentTimeMillis();
	}
	
	
	public synchronized void stopInertialRotation() 
	{
	  	inertiaEnabled = false;
	}
	  
  

	public void setRotationFriction(float coef) {
		this.mRotationFriction = coef;
		
	}


	
	  
	/* ************
	 * GETTERS
	 * ************/
	public float getPitch() 
	{
	  	return mPitch;
	}
	  
	public float getYaw() 
	{
		return mYaw;
	}
		
	public int getSurfaceWidth()
	{
		return mSurfaceWidth;
	}
	  
	public int getSurfaceHeight()
	{
		return mSurfaceHeight;
	}
	  
	public float getFov() 
	{
		return fovDeg;
	}
	  
	public float getHFovDeg() 
	{
		float viewDiagonal = (float) Math.sqrt(mSurfaceHeight*mSurfaceHeight + mSurfaceWidth*mSurfaceWidth);
		float hFovDeg = (float) mSurfaceWidth/viewDiagonal * fovDeg;  	
		return hFovDeg;
	}

	public float getVFovDeg() {
		float viewDiagonal = (float) Math.sqrt(mSurfaceHeight*mSurfaceHeight + mSurfaceWidth*mSurfaceWidth);	
		float vFovDeg = (float) mSurfaceHeight/viewDiagonal * fovDeg;
		return vFovDeg;
	}

	
	/* ************
	 * PRIVATE METHODS
	 * ************/
	
	/**
	 * if inertia rotation is enabled, do the rotation.
	 */
	private synchronized void doInertiaRotation()
	{
		if (!inertiaEnabled)
			return;
  	
		long currentTime = System.currentTimeMillis();
		float deltaT = (currentTime-mT0)/1000.0f;
		if (deltaT == 0.0f)
			return;
		
  	
		float rPitch = deltaT*mRotationFriction;
		float rYaw = deltaT*mRotationFriction;
  	
		if (Math.abs(mRotationPitchSpeed) < rPitch && Math.abs(mRotationYawSpeed) < rYaw)
		{
			stopInertialRotation();
		}
  	
		float deltaPitch, deltaYaw;
  
		if (Math.abs(mRotationPitchSpeed) >= rPitch)
		{
			float rotSpeedLat_t = mRotationPitchSpeed - Math.signum(mRotationPitchSpeed)*rPitch;
			deltaPitch = (mRotationPitchSpeed+rotSpeedLat_t)/2.0f*deltaT;
		} 
		else 
		{
			float tMax = Math.abs(mRotationPitchSpeed)/mRotationFriction;
			deltaPitch = 0.5f*tMax*mRotationPitchSpeed;
		}
  	
		if (Math.abs(mRotationYawSpeed) >= rYaw)
		{
			float rotSpeedLon_t = mRotationYawSpeed - Math.signum(mRotationYawSpeed)*rYaw;
			deltaYaw = (mRotationYawSpeed+rotSpeedLon_t)/2.0f*deltaT;
		} 
		else 
		{
			float tMax = Math.abs(mRotationYawSpeed)/mRotationFriction;
			deltaYaw = 0.5f*tMax*mRotationYawSpeed;
		}
  	
		setRotation(mPitch0+deltaPitch, mYaw0+deltaYaw);
	}


}
