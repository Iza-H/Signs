package com.example.projectbase;

import java.io.File;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;

public class MainActivity extends Activity implements CvCameraViewListener2 { 

	private static final String TAG = "Ejemplo OCV (MainActivity)";
	private CameraBridgeViewBase cameraView;
	private int indiceCamara; // 0- camara trasera; 1- camara frontal 
	private int cam_anchura =320; // resolucion deseada de la imagen 
	private int cam_altura=240;
	private static final String STATE_CAMERA_INDEX = "cameraIndex";
	private int tipoEntrada = 0; // 0 - cámara, 1 - fichero1, 2 - fichero2 
	Mat imagenRecurso_;
	boolean recargarRecurso = false;
	private boolean guardarSiguienteImagen = false;
	Procesador procesador;

	private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) { 
		@Override
		public void onManagerConnected(int status) { 
			switch (status) {
				case LoaderCallbackInterface.SUCCESS:
					Log.i(TAG, "OpenCV se cargo correctamente"); 
					cameraView.setMaxFrameSize(cam_anchura, cam_altura); 
					cameraView.enableView();
					break;
			default:
				Log.e(TAG, "OpenCV no se cargo");
				Toast.makeText(MainActivity.this, "OpenCV no se cargo", Toast.LENGTH_LONG).show();
				finish();
				break; 
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState); 
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
		setContentView(R.layout.activity_main);
		cameraView = (CameraBridgeViewBase) findViewById(R.id.vista_camara);
		cameraView.setCvCameraViewListener(this); 
		if (savedInstanceState != null) {
			indiceCamara = savedInstanceState.getInt(STATE_CAMERA_INDEX, 0); 
			indiceCamara = 0;
		} cameraView.setCameraIndex(indiceCamara);
	}

	@Override
	public void onPause() { 
		super.onPause();
		if (cameraView != null) cameraView.disableView();
	}

	@Override
	public void onResume() { 
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9,this,loaderCallback); 
	}
	
	@Override
	public void onDestroy() { 
		super.onDestroy();
		if (cameraView != null)
			cameraView.disableView();
	}
	
	//Interface CvCameraViewListener2
	public void onCameraViewStarted(int width, int height) {
		cam_altura = height; 
		cam_anchura = width;
		procesador = new Procesador();
	}
	
	public void onCameraViewStopped() { }
	
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) { 
		Mat entrada;
		if (tipoEntrada == 0) {
			entrada = inputFrame.rgba();
		}
		else {
			if(recargarRecurso == true) { 
				imagenRecurso_ = new Mat();
				int RECURSOS_FICHEROS[] = {0, R.raw.fichero1, R.raw.fichero2, R.raw.f1, R.raw.f2, R.raw.f3, R.raw.f4, R.raw.f5, R.raw.f6, R.raw.f7, R.raw.f8, R.raw.f9, R.raw.f10, R.raw.f11, R.raw.f12, R.raw.f13, R.raw.f14, R.raw.f15, R.raw.f16,R.raw.f17, R.raw.f18, R.raw.f19, R.raw.f19, R.raw.f22, R.raw.f23, R.raw.f24, R.raw.f26, R.raw.img3,R.raw.img4,R.raw.img5, R.raw.img6, R.raw.img7, R.raw.img8, R.raw.img9}; 
				Bitmap bitmap = BitmapFactory.decodeResource(getResources(), RECURSOS_FICHEROS[tipoEntrada]);
				Utils.bitmapToMat(bitmap, imagenRecurso_);
				recargarRecurso = false;
			}
		entrada = imagenRecurso_;
		}
		//Mat salida = entrada.clone(); 
		Mat salida = procesador.procesa(entrada);
		if (guardarSiguienteImagen) {//Para foto salida debe ser rgba 
			takePhoto(entrada, salida);
			guardarSiguienteImagen = false;
		}
		if(tipoEntrada > 0)
			Imgproc.resize(salida, salida, new Size(cam_anchura, cam_altura)); 
		return salida;
	}

	public void onPackageInstall(int operation,InstallCallbackInterface callback) { 
		
	}
	
	public boolean onTouchEvent(MotionEvent event){
		openOptionsMenu();
		return true;
		
	}
	
	public void onSaveInstanceState(Bundle savedInstanceState){
		 //Save the current camera index
		 savedInstanceState.putInt(STATE_CAMERA_INDEX, indiceCamara);
		 super.onSaveInstanceState(savedInstanceState);
	 }
	
	
	//Menu:
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.cambiarCamara:
				indiceCamara++;
				if (indiceCamara == Camera.getNumberOfCameras()) {
					indiceCamara = 0;
				}
				recreate();
				break;
			case R.id.resolucion_800x600: 
				cam_anchura = 800;
				cam_altura = 600; 
				reiniciarResolucion(); 
				break;
			case R.id.resolucion_640x480: 
				cam_anchura = 640;
				cam_altura = 480; 
				reiniciarResolucion(); 
				break;
			case R.id.resolucion_320x240: 
				cam_anchura = 320;
				cam_altura = 240; 
				reiniciarResolucion(); 
				break;
			/*case R.id.entrada_camara: 
				tipoEntrada = 0;
				break;
			case R.id.entrada_fichero1:
				tipoEntrada = 1; 
				recargarRecurso = true; 
				break;
			case R.id.entrada_fichero2: 
				tipoEntrada = 2;
				recargarRecurso = true; 
				break;
			case R.id.f1: 
				tipoEntrada = 3;
				recargarRecurso = true; 
				break;
			case R.id.f2: 
				tipoEntrada = 4;
				recargarRecurso = true; 
				break;
			case R.id.f3: 
				tipoEntrada = 5;
				recargarRecurso = true; 
				break;
			case R.id.f4: 
				tipoEntrada = 6;
				recargarRecurso = true; 
				break;
			case R.id.f5: 
				tipoEntrada = 7;
				recargarRecurso = true; 
				break;
			case R.id.f6: 
				tipoEntrada = 8;
				recargarRecurso = true; 
				break;
			case R.id.f7: 
				tipoEntrada = 9;
				recargarRecurso = true; 
				break;
			case R.id.f8: 
				tipoEntrada = 10;
				recargarRecurso = true; 
				break;
			case R.id.f9: 
				tipoEntrada = 11;
				recargarRecurso = true; 
				break;
			case R.id.f10: 
				tipoEntrada = 12;
				recargarRecurso = true; 
				break;
			case R.id.f11: 
				tipoEntrada = 13;
				recargarRecurso = true; 
				break;
			case R.id.f12: 
				tipoEntrada = 14;
				recargarRecurso = true; 
				break;
			case R.id.f13: 
				tipoEntrada = 15;
				recargarRecurso = true; 
				break;
			case R.id.f14: 
				tipoEntrada = 16;
				recargarRecurso = true; 
				break;
			case R.id.f15: 
				tipoEntrada = 17;
				recargarRecurso = true; 
				break;
			case R.id.f16: 
				tipoEntrada = 18;
				recargarRecurso = true; 
				break;
			case R.id.f17: 
				tipoEntrada = 19;
				recargarRecurso = true; 
				break;
			case R.id.f18: 
				tipoEntrada = 20;
				recargarRecurso = true; 
				break;
			case R.id.f19: 
				tipoEntrada = 21;
				recargarRecurso = true; 
				break;
			case R.id.f22: 
				tipoEntrada = 22;
				recargarRecurso = true; 
				break;
			case R.id.f23: 
				tipoEntrada = 23;
				recargarRecurso = true; 
				break;
			case R.id.f24: 
				tipoEntrada = 24;
				recargarRecurso = true; 
				break;
			case R.id.f26: 
				tipoEntrada = 25;
				recargarRecurso = true; 
				break;
			case R.id.img3: 
				tipoEntrada = 26;
				recargarRecurso = true; 
				break;
			case R.id.img4: 
				tipoEntrada = 27;
				recargarRecurso = true; 
				break;
			case R.id.img5: 
				tipoEntrada = 28;
				recargarRecurso = true; 
				break;
			case R.id.img6: 
				tipoEntrada = 29;
				recargarRecurso = true; 
				break;
			case R.id.img7: 
				tipoEntrada = 30;
				recargarRecurso = true; 
				break;
			case R.id.img8: 
				tipoEntrada = 31;
				recargarRecurso = true; 
				break;
			case R.id.img9: 
				tipoEntrada = 32;
				recargarRecurso = true; 
				break;*/

				
			case R.id.guardar_imagenes: 
				guardarSiguienteImagen = true; 
				break;
		}
		String msg= "W="+Integer.toString(cam_anchura)+ " H= " + Integer.toString(cam_altura) + " Cam= " + Integer.toBinaryString(indiceCamara);
		Toast.makeText(MainActivity.this, msg , Toast.LENGTH_LONG).show();
		return true;
	
	}
	
	public void reiniciarResolucion() { 
		cameraView.disableView();
		cameraView.setMaxFrameSize(cam_anchura, cam_altura); 
		cameraView.enableView();
	}
	
	
	private void takePhoto(final Mat input, final Mat output) {
		// Determina la ruta para crear los archivos
		final long currentTimeMillis = System.currentTimeMillis(); 
		final String appName = getString(R.string.app_name);
		final String galleryPath = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES).toString();
		final String albumPath = galleryPath + "/" + appName;
		final String photoPathIn = albumPath + "/In_" + currentTimeMillis + ".png";
		final String photoPathOut = albumPath + "/Out_" + currentTimeMillis + ".png";
		// Asegurarse que el directorio existe
		File album = new File(albumPath);
		if (!album.isDirectory() && !album.mkdirs()) {
			Log.e(TAG, "Error al crear el directorio " + albumPath);
			return; 
		}
		// Intenta crear los archivos
		Mat mBgr = new Mat();
		if (output.channels() == 1)
			Imgproc.cvtColor(output, mBgr, Imgproc.COLOR_GRAY2BGR, 3); 
		else
			Imgproc.cvtColor(output, mBgr, Imgproc.COLOR_RGBA2BGR, 3); 
		if (!Highgui.imwrite(photoPathOut, mBgr)) {
			Log.e(TAG, "Fallo al guardar " + photoPathOut);
		}
		if (input.channels() == 1)
			Imgproc.cvtColor(input, mBgr, Imgproc.COLOR_GRAY2BGR, 3); 
		else
			Imgproc.cvtColor(input, mBgr, Imgproc.COLOR_RGBA2BGR, 3); 
		if (!Highgui.imwrite(photoPathIn, mBgr))
			Log.e(TAG, "Fallo al guardar " + photoPathIn); mBgr.release();
		return;
	}
	

}
