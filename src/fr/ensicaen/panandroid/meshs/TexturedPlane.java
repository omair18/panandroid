package fr.ensicaen.panandroid.meshs;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.opengl.GLES10;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

public class TexturedPlane extends Mesh
{
	
	private static final String TAG = TexturedPlane.class.getSimpleName();

	/* *********
	 * DEFAULT PARAMETERS
	 * ********/
	private static final float DEFAULT_SCALE = 1.0f;
	
	private static final float DEFAULT_RATIO = 1.0f;
	
	private static final float VERTEX_ARRAY_PATTERN[] =  
						{
					        -DEFAULT_RATIO,  -1.0f, 0,
					        -DEFAULT_RATIO, 1.0f, 0,
					        DEFAULT_RATIO, 1.0f, 0,
					        DEFAULT_RATIO, -1.0f, 0
						};
	
	/* *********
	 * ATTRIBUTES
	 * ********/
	public float[]mModelMatrix;
	private int imTextureId;
	private Bitmap mBitmapTexture;
	
	
	private boolean mIsVisible = true;
	
	///private float mAlpha = 1.0f;
	
	private float mAutoAlphaX;
	private float mAutoAlphaY;
	
	private Axis mAxis = Axis.VERTICAL; 
	
	
	
	
	//private int mPositionHandler ;
	//private int mTexCoordHandler ;
	
	private FloatBuffer mVertexBuffer;
	
	//private FloatBuffer m43VertexBuffer;
	
	private FloatBuffer mTexCoordBuffer;
	
	//private float[] mViewMatrix;
	//private float[] mProjectionMatrix = new float[16];
	
	
	private float[] mMVPMatrix = new float[16];
	
	
	
	
	// x, y, z
	private float mVertices[];

    private static final float mTexCoordData[] =
            {
                    0.0f, 1.0f,
                    0.0f, 0.0f,
                    1.0f, 0.0f,
                    1.0f, 1.0f
            };


	private boolean mTextureToLoad = false;;
    

	public enum Axis{
		VERTICAL, HORIZONTAL
	}
	

	/* *********
	 * CONSTRUCTORS
	 * ********/

    public TexturedPlane()
    {
    	this(DEFAULT_SCALE, DEFAULT_RATIO);
    }

    public TexturedPlane(float sizeX)
    {
    	this(sizeX, DEFAULT_RATIO);
    }

    
    public TexturedPlane(float sizeX, float ratio)
    {
       // mIsFourToThree = isFourToThree;    
    	
    	mVertices = Arrays.copyOf(VERTEX_ARRAY_PATTERN, VERTEX_ARRAY_PATTERN.length);
    	
    	for(int i=0; i< mVertices.length; i+=3)
    	{
    		mVertices[i]*=sizeX;
    		mVertices[i+1]*=sizeX*ratio;
    	}
    	
    	
    	
		// Initialize plane vertex data 
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(mVertices.length * Float.SIZE);
		byteBuffer.order(ByteOrder.nativeOrder());
		
		//ByteBuffer bb_43data = ByteBuffer.allocateDirect(m43VertexData.length * 4);
		//bb_43data.order(ByteOrder.nativeOrder());
		
		mVertexBuffer = byteBuffer.asFloatBuffer();
		mVertexBuffer.put(mVertices);
		mVertexBuffer.position(0);
		
		//initialize texcoords data
		byteBuffer = ByteBuffer.allocateDirect(mTexCoordData.length * Float.SIZE);
		byteBuffer.order(ByteOrder.nativeOrder());
		mTexCoordBuffer = byteBuffer.asFloatBuffer();
		mTexCoordBuffer.put(mTexCoordData);
		mTexCoordBuffer.position(0);
		
		
		///m43VertexBuffer = bb_43data.asFloatBuffer();
		///m43VertexBuffer.put(m43VertexData);
		///m43VertexBuffer.position(0);
		
		this.setTexture(Bitmap.createBitmap(new int[]{Color.CYAN}, 1, 1, Config.RGB_565));
		mModelMatrix = new float[16];
		Matrix.setIdentityM(mModelMatrix, 0);

    }

    

	/* *********
	 * ACCESSORS
	 * ********/
   
    
    public void setVisible(boolean visible) {
        mIsVisible = visible;
    }


    public void setTexture(Bitmap tex) {
        
    	mBitmapTexture = tex;
    	mTextureToLoad = true;
        
    }

    public void setTextureId(int id) {
        imTextureId = id;
    	mTextureToLoad = true;
    }

    /*
    public void setAlpha(float alpha) {
        mAlpha = alpha;
    }
	*/
    
    public void setAutoAlphaAngle(float x, float y) {
        mAutoAlphaX = x;
        mAutoAlphaY = y;
    }

    public float getAutoAlphaX() {
        return mAutoAlphaX;
    }

    public float getAutoAlphaY() {
        return mAutoAlphaY;
    }

    
    
    public void draw(GL10 gl, float[] modelViewMatrix)
    {
    	//enter 2d texture mode
		gl.glEnable(GL10.GL_TEXTURE_2D);
		
		//enable alpha
		gl.glAlphaFunc( GLES10.GL_GREATER, 0 );
		gl.glEnable( GLES10.GL_ALPHA_TEST );

		//enanle blending
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL10.GL_BLEND);
    	
        if (!mIsVisible) return;

        if (mTextureToLoad) {
            loadGLTexture(gl);
        }
        
        // bind the previously generated texture.
        gl.glBindTexture(GL10.GL_TEXTURE_2D, imTextureId);
        
        // Point to our buffers.
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        
     
      
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, this.mVertexBuffer);
            
        
        gl.glTexCoordPointer(2,GLES10.GL_FLOAT, 0, mTexCoordBuffer);

        // This multiplies the view matrix by the model matrix, and stores the
        // result in the MVP matrix (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, modelViewMatrix, 0, mModelMatrix, 0);
  		gl.glLoadMatrixf(mMVPMatrix, 0);


        gl.glDrawArrays(GLES10.GL_TRIANGLE_FAN, 0, 4);
  		gl.glLoadMatrixf(modelViewMatrix, 0);
  		
		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glDisable( GLES10.GL_ALPHA_TEST );
		
		//leave
		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glDisable( GLES10.GL_ALPHA_TEST );
		gl.glDisable(GL10.GL_BLEND);



    }

	@Override
	public void loadGLTexture(GL10 gl) {
		// Load the snapshot bitmap as a texture to bind to our GLES20 program
        int texture[] = new int[1];

        GLES10.glGenTextures(1, texture, 0);
        GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, texture[0]);

        GLES10.glTexParameterf(GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_MIN_FILTER, GLES10.GL_NEAREST);
        GLES10.glTexParameterf(GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_MAG_FILTER, GLES10.GL_NEAREST);
        
        //GLES10.glTexParameterf(GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_WRAP_S, GLES10.GL_CLAMP_TO_EDGE);
        //GLES10.glTexParameterf(GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_WRAP_T, GLES10.GL_CLAMP_TO_EDGE);

        GLUtils.texImage2D(GLES10.GL_TEXTURE_2D, 0, mBitmapTexture, 0);
        
        if(texture[0] == 0){
            Log.e(TAG, "Unable to attribute texture to quad");
        }

        imTextureId = texture[0];
    	mTextureToLoad = false;

	}
	

	public void rotate(float rx, float ry, float rz)
	{
		switch(mAxis)
		{
		//rotate yaw first
		case VERTICAL :
			Matrix.rotateM(mModelMatrix, 0, ry, 0, 1, 0);
			Matrix.rotateM(mModelMatrix, 0, rx, 1, 0, 0);
			break;
		default:
			Matrix.rotateM(mModelMatrix, 0, rx, 1, 0, 0);
			Matrix.rotateM(mModelMatrix, 0, ry, 0, 1, 0);
			
		}
		Matrix.rotateM(mModelMatrix, 0, rz, 0, 0, 1);
		
	}

	public void translate(float tx,float ty,float tz)
	{
        Matrix.translateM(mModelMatrix, 0, -tx, -ty, -tz);
		
	}
	
	public void setAxis(Axis axis)
	{
		mAxis = axis;
	}
}