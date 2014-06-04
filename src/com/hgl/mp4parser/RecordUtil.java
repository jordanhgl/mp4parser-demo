package com.hgl.mp4parser;

/*import static com.googlecode.javacv.cpp.opencv_highgui.cvCreateFileCapture;
import static com.googlecode.javacv.cpp.opencv_highgui.cvQueryFrame;*/

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Video;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class RecordUtil {
	public static  ContentValues videoContentValues = null;
	public static String getRecordingTimeFromMillis(long millis)
	{
		String strRecordingTime = null;
		int seconds = (int) (millis / 1000);
		int minutes = seconds / 60;
		int hours = minutes / 60;

		if(hours >= 0 && hours < 10)
			strRecordingTime = "0" + hours + ":";
		else
			strRecordingTime = hours + ":";

		if(hours > 0)
			minutes = minutes % 60;

		if(minutes >= 0 && minutes < 10)
			strRecordingTime += "0" + minutes + ":";
		else
			strRecordingTime += minutes + ":";

		seconds = seconds % 60;

		if(seconds >= 0 && seconds < 10)
			strRecordingTime += "0" + seconds ;
		else
			strRecordingTime += seconds ;

		return strRecordingTime;

	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static int determineDisplayOrientation(Activity activity, int defaultCameraId) {
		int displayOrientation = 0;
		if(Build.VERSION.SDK_INT >  Build.VERSION_CODES.FROYO)
		{
			CameraInfo cameraInfo = new CameraInfo();
			Camera.getCameraInfo(defaultCameraId, cameraInfo);

			int degrees  = getRotationAngle(activity);

			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				displayOrientation = (cameraInfo.orientation + degrees) % 360;
				displayOrientation = (360 - displayOrientation) % 360;
			} else {
				displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
			}
		}
		return displayOrientation;
	}

	public static int getRotationAngle(Activity activity)
	{
		int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
		int degrees  = 0;

		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;

		case Surface.ROTATION_90:
			degrees = 90;
			break;

		case Surface.ROTATION_180:
			degrees = 180;
			break;

		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}
		return degrees;
	}

	public static int getRotationAngle(int rotation)
	{
		int degrees  = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;

		case Surface.ROTATION_90:
			degrees = 90;
			break;

		case Surface.ROTATION_180:
			degrees = 180;
			break;

		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}
		return degrees;
	}
	
	public static String createImagePath(Context context){
		long dateTaken = System.currentTimeMillis();
		String title = CONSTANTS.FILE_START_NAME + dateTaken;
		String filename = title + CONSTANTS.IMAGE_EXTENSION;
		
		String dirPath = Environment.getExternalStorageDirectory()+"/Android/data/" + context.getPackageName()+"/video";
		File file = new File(dirPath);
		if(!file.exists() || !file.isDirectory())
			file.mkdirs();
		String filePath = dirPath + "/" + filename;
		return filePath;
	}

	public static String createFinalPath(Context context)
	{
		long dateTaken = System.currentTimeMillis();
		String title = CONSTANTS.FILE_START_NAME + dateTaken;
		String filename = title + CONSTANTS.VIDEO_EXTENSION;
		String filePath = genrateFilePath(context,String.valueOf(dateTaken), true, null);
		ContentValues values = new ContentValues(7);
		values.put(Video.Media.TITLE, title);
		values.put(Video.Media.DISPLAY_NAME, filename);
		values.put(Video.Media.DATE_TAKEN, dateTaken);
		values.put(Video.Media.MIME_TYPE, "video/3gpp");
		values.put(Video.Media.DATA, filePath);
		videoContentValues = values;

		return filePath;
	}
	
	public static void deleteTempVideo(Context context){
		final String dirPath = Environment.getExternalStorageDirectory()+"/Android/data/" + context.getPackageName()+"/video";
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				File file = new File(dirPath);
				if(file != null && file.isDirectory()){
					for(File file2 :file.listFiles()){
						file2.delete();
					}
				}
			}
		}).start();
	}

	private static String genrateFilePath(Context context,String uniqueId, boolean isFinalPath, File tempFolderPath)
	{
		String fileName = CONSTANTS.FILE_START_NAME + uniqueId + CONSTANTS.VIDEO_EXTENSION;
		String dirPath = Environment.getExternalStorageDirectory()+"/" + context.getPackageName()+"/video/ugc";
		if (isFinalPath) {
			File file = new File(dirPath);
			if(!file.exists() || !file.isDirectory()) {
				file.mkdirs();				
			}
		} else if (tempFolderPath != null) {
			dirPath = tempFolderPath.getAbsolutePath();			
		}
		String filePath = dirPath + "/" + fileName;
		return filePath;
	}
	
	public static String createTempPath(Context context,File tempFolderPath )
	{
		long dateTaken = System.currentTimeMillis();
		String filePath = genrateFilePath(context,String.valueOf(dateTaken), false, tempFolderPath);
		return filePath;
	}



	public static File getTempFolderPath()
	{
		File tempFolder = new File(CONSTANTS.TEMP_FOLDER_PATH +"_" +System.currentTimeMillis());
		return tempFolder;
	}


	public static List<Camera.Size> getResolutionList(Camera camera)
	{ 
		Parameters parameters = camera.getParameters();
		List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();


		return previewSizes;
	}


	public static int calculateMargin(int previewWidth, int screenWidth)
	{
		int margin = 0;
		if(previewWidth <= CONSTANTS.RESOLUTION_LOW)
		{
			margin = (int) (screenWidth*0.12);
		}
		else if(previewWidth > CONSTANTS.RESOLUTION_LOW && previewWidth <= CONSTANTS.RESOLUTION_MEDIUM)
		{
			margin = (int) (screenWidth*0.08);
		}
		else if(previewWidth > CONSTANTS.RESOLUTION_MEDIUM && previewWidth <= CONSTANTS.RESOLUTION_HIGH)
		{
			margin = (int) (screenWidth*0.08);
		}
		return margin;


	}

	public static int setSelectedResolution(int previewHeight)
	{
		int selectedResolution = 0;
		if(previewHeight <= CONSTANTS.RESOLUTION_LOW)
		{
			selectedResolution = 0;
		}
		else if(previewHeight > CONSTANTS.RESOLUTION_LOW && previewHeight <= CONSTANTS.RESOLUTION_MEDIUM)
		{
			selectedResolution = 1;
		}
		else if(previewHeight > CONSTANTS.RESOLUTION_MEDIUM && previewHeight <= CONSTANTS.RESOLUTION_HIGH)
		{
			selectedResolution = 2;
		}
		return selectedResolution;


	}

	public static class ResolutionComparator implements Comparator<Camera.Size> {
		@Override
		public int compare(Camera.Size size1, Camera.Size size2) {
			if(size1.height != size2.height)
				return size1.height -size2.height;
			else
				return size1.width - size2.width;
		}
	}


	public static void concatenateMultipleFiles(String inpath, String outpath)
	{
		File Folder = new File(inpath);
		File files[];
		files = Folder.listFiles();

		if(files.length>0)
		{
			for(int i = 0;i<files.length; i++){
				Reader in = null;
				Writer out = null;
				try {
					in =   new FileReader(files[i]);
					out = new FileWriter(outpath , true); 
					in.close();
					out.close(); 
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	//TODO  del by hangl 0511
	/*public static String getEncodingLibraryPath(Context paramContext)
	{
		return paramContext.getApplicationInfo().nativeLibraryDir + "/libencoding.so";
	}*/

	private static HashMap<String, String> getMetaData()
	{
		HashMap<String, String> localHashMap = new HashMap<String, String>();
		localHashMap.put("creation_time", new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSSZ").format(new Date()));
		return localHashMap;
	}

	public static int getTimeStampInNsFromSampleCounted(int paramInt)
	{
		return (int)(paramInt / 0.0441D);
	}



	public static Toast showToast(Context context, String textMessage, int timeDuration)
	{
		if (null == context)
		{
			return null;
		}
		textMessage = (null == textMessage ? "Oops! " : textMessage.trim());
		Toast t = Toast.makeText(context, textMessage, timeDuration);
		t.show();
		return t;
	}
	public static byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight) 
	{
	    byte [] yuv = new byte[imageWidth*imageHeight*3/2];
	    // Rotate the Y luma
	    int i = 0;
	    for(int x = 0;x < imageWidth;x++)
	    {
	        for(int y = imageHeight-1;y >= 0;y--)                               
	        {
	            yuv[i] = data[y*imageWidth+x];
	            i++;
	        }
	    }
	    // Rotate the U and V color components 
	    i = imageWidth*imageHeight*3/2-1;
	    for(int x = imageWidth-1;x > 0;x=x-2)
	    {
	        for(int y = 0;y < imageHeight/2;y++)                                
	        {
	            yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+x];
	            i--;
	            yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+(x-1)];
	            i--;
	        }
	    }
	    return yuv;
	}
	
	public static byte[] rotateYUV420Degree180(byte[] data, int imageWidth, int imageHeight) 
	{
		byte [] yuv = new byte[imageWidth*imageHeight*3/2];
		int i = 0;
		int count = 0;
		
		for (i = imageWidth * imageHeight - 1; i >= 0; i--) {
			yuv[count] = data[i];
			count++;
		}

		i = imageWidth * imageHeight * 3 / 2 - 1;
		for (i = imageWidth * imageHeight * 3 / 2 - 1; i >= imageWidth
				* imageHeight; i -= 2) {
			yuv[count++] = data[i - 1];
			yuv[count++] = data[i];
		}
		return yuv;
	}
	
	
	public static byte[] rotateYUV420Degree270(byte[] data, int imageWidth, int imageHeight) {
	    byte [] yuv = new byte[imageWidth*imageHeight*3/2];
	    // Rotate the Y luma
	    int i = 0;
	    for(int x = imageWidth-1;x >= 0;x--)
	    {
	        for(int y = 0;y < imageHeight;y++)                                 
	        {
	            yuv[i] = data[y*imageWidth+x];
	            i++;
	        }

	    }
	    // Rotate the U and V color components 
		i = imageWidth*imageHeight;
	    for(int x = imageWidth-1;x > 0;x=x-2)
	    {
	        for(int y = 0;y < imageHeight/2;y++)                                
	        {
	            yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+x];
	            i++;
	            yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+(x-1)];
	            i++;
	        }
	    }
	    return yuv;
	}
	
	public static void saveFrameToPic(byte[] data) {
		File picFile = new File(Environment.getExternalStorageDirectory() + "/DCIM/");
		if (!picFile.exists()) {
			picFile.mkdirs();
		}
		File yuvFile = new File(picFile, "yuv_pic.yuv");
		if (!yuvFile.exists()) {
			try {
				yuvFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(yuvFile);
			fos.write(data);
			fos.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}			
	}
}
