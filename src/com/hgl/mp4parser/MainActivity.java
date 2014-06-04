package com.hgl.mp4parser;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		private final String TAG = this.getClass().getSimpleName();
		private TextView appendVideoTv;
		public PlaceholderFragment() {
		}

		@Override
		public void onAttach(Activity activity) {
			// TODO Auto-generated method stub
			super.onAttach(activity);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			appendVideoTv = (TextView)rootView.findViewById(R.id.append_video);
			appendVideoTv.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.v(TAG, "you click textview");
					String video1 = Environment.getExternalStorageDirectory() + "/DCIM/ifengUGC_001.mp4";
					String video2 = Environment.getExternalStorageDirectory() + "/DCIM/ifengUGC_002.mp4";
					Log.v(TAG, "in onclick video1 == " + video1);
					String[] videos = new String[]{video1, video2};
					try {
						appendVideo(videos);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			return rootView;
		}
		
		
		/**
		 * @param  	
		 * @return 	return combine video path string
		 * */
		private String appendVideo(String[] videos) throws IOException{
			Log.v(TAG, "in appendVideo() videos length is " + videos.length);
	        Movie[] inMovies = new Movie[videos.length];
	        int index = 0;
	        for(String video: videos)
	        {
	        	Log.i(TAG, "    in appendVideo one video path = " + video);
				inMovies[index] = MovieCreator.build(video);
	        	index++;
	    	}
	        List<Track> videoTracks = new LinkedList<Track>();
	        List<Track> audioTracks = new LinkedList<Track>();
	        for (Movie m : inMovies) {
	            for (Track t : m.getTracks()) {
	                if (t.getHandler().equals("soun")) {
	                    audioTracks.add(t);
	                }
	                if (t.getHandler().equals("vide")) {
	                    videoTracks.add(t);
	                }
	            }
	        }

	        Movie result = new Movie();
	        Log.v(TAG, "audioTracks size = " + audioTracks.size()
	        		+ " videoTracks size = " + videoTracks.size());
	        if (audioTracks.size() > 0) {
				result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
	        }
	        if (videoTracks.size() > 0) {
				result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
	        }
	        String videoCombinePath = RecordUtil.createFinalPath(getActivity());
	        Container out = new DefaultMp4Builder().build(result);
	        FileChannel fc = new RandomAccessFile(videoCombinePath, "rw").getChannel();
	        out.writeContainer(fc);
	        fc.close();
	        Log.v(TAG, "after combine videoCombinepath = " + videoCombinePath);
	        return videoCombinePath;
	    }
	}

}
