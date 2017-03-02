package omrecorder.widget;

import android.util.Log;

import com.alibaba.idst.nls.NlsClient;
import com.alibaba.idst.nls.NlsFuture;
import com.alibaba.idst.nls.event.NlsEvent;
import com.alibaba.idst.nls.event.NlsListener;
import com.alibaba.idst.nls.protocol.NlsRequest;
import com.alibaba.idst.nls.protocol.NlsResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by songsong.sss on 16/6/22.
 */
public class RealtimeDemo implements NlsListener {
	private NlsClient client = new NlsClient();
	private static final String asrSC = "pcm";
	private static Logger logger = LoggerFactory.getLogger(RealtimeDemo.class);

	public String filePath = "sdcard/music/mic.wav";
	public String appKey = "";
	public String ak_id = "";
	public String ak_secret = "";

	public RealtimeDemo() {
	}

	public void shutDown() {
		logger.debug("close NLS client manually!");
		client.close();
		logger.debug("demo done");
	}

	public void start() {
		logger.debug("init Nls client...");
		client.init();
	}

	public void hearIt() {
		logger.debug("open audio file...");
		FileInputStream fis = null;
		try {
			File file = new File(filePath);
			fis = new FileInputStream(file);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (fis != null) {
			logger.debug("create NLS future");
			try {
				NlsRequest req = new NlsRequest();
				req.setAppkey(appKey);
				req.setFormat(asrSC);
				req.setResponseMode("streaming");
				req.setSampleRate(16000);
				// the id and the id secret
				req.authorize(ak_id, ak_secret);
				//
				NlsFuture future = client.createNlsFuture(req, this);
				logger.debug("call NLS service");
				byte[] b = new byte[8000];
				int len = 0;
				// processPCM
				while ((len = fis.read(b)) > 0) {
					future.sendVoice(b, 0, len);
					Thread.sleep(250);
				}

				logger.debug("send finish signal!");
				future.sendFinishSignal();

				logger.debug("main thread enter waiting .");
				future.await(100000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			logger.debug("calling NLS service end");
		}
	}

	// processPCM
	public void hearByte(byte[] b) {
		Log.d("xqp", "hearByte " + b.length);
		try {
			NlsRequest req = new NlsRequest();
			req.setAppkey(appKey);
			req.setFormat(asrSC);
			req.setResponseMode("streaming");
			req.setSampleRate(16000);
			req.authorize(ak_id, ak_secret);
			NlsFuture future = client.createNlsFuture(req, this);
			future.sendVoice(b, 0, b.length);
			future.sendFinishSignal();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onMessageReceived(NlsEvent e) {
		NlsResponse response = e.getResponse();
		response.getFinish();
		String result = "";
//		if (response.result != null) {
//		Log.i("xqp", response.getStatus_code() + " " + response.getResult().toString());
//		}
		if (response.result != null) {
//			logger.info("status code = {},get recognize result: {}", response.getStatus_code(), response.getResult().toString());
			logger.info(response.getText());
		} else {
			logger.info("get an acknowledge package from server.");
		}
	}

	@Override
	public void onOperationFailed(NlsEvent e) {
		logger.error("status code is {}, on operation failed: {}", e.getResponse().getStatus_code(), e.getErrorMessage());
	}

	@Override
	public void onChannelClosed(NlsEvent e) {
		logger.debug("on websocket closed.");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RealtimeDemo lun = new RealtimeDemo();
		args = new String[4];
		args[0] = "nls-service-shurufa16khz";
		args[1] = "L3Y1Fk8J0R5YzlQY";
		args[2] = "jD6NheENLFrRGrnu3SlDL79O6D6rSA";
		args[3] = "D://beijingweather.pcm";

		logger.info("start ....");
		if (args.length < 4) {
			logger.debug("RealtimeDemo need params: <app-key> <Id> <Secret> <opu-file> ");
			System.exit(-1);
		}

		lun.appKey = args[0];
		lun.ak_id = args[1];
		lun.ak_secret = args[2];
		lun.filePath = args[3];
		lun.start();
		lun.hearIt();
		lun.shutDown();
	}

}
