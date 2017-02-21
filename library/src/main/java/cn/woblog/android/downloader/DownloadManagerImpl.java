package cn.woblog.android.downloader;

import android.content.Context;
import cn.woblog.android.downloader.callback.DownloadManager;
import cn.woblog.android.downloader.core.DownloadResponse;
import cn.woblog.android.downloader.core.DownloadResponseImpl;
import cn.woblog.android.downloader.core.DownloadTaskImpl;
import cn.woblog.android.downloader.core.DownloadTaskImpl.DownloadTaskListener;
import cn.woblog.android.downloader.core.task.DownloadTask;
import cn.woblog.android.downloader.db.DefaultDownloadDBController;
import cn.woblog.android.downloader.db.DownloadDBController;
import cn.woblog.android.downloader.domain.DownloadInfo;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by renpingqing on 14/01/2017.
 */

public final class DownloadManagerImpl implements DownloadManager, DownloadTaskListener {

  private static DownloadManagerImpl instance;
  private final ExecutorService executorService;
  private final ConcurrentHashMap<Integer, DownloadTask> cacheDownloadTask;
  private final List<DownloadInfo> downloadingCaches;
  private final Context context;

  private final DownloadResponse downloadResponse;
  private final DownloadDBController downloadDBController;
  private final Config config;

  private DownloadManagerImpl(Context context, Config config) {
    this.context = context;
    if (config == null) {
      this.config = new Config();
    } else {
      this.config = config;
    }
    downloadDBController = new DefaultDownloadDBController(context);
    cacheDownloadTask = new ConcurrentHashMap<>();
    downloadingCaches = new LinkedList<>();

    executorService = Executors.newFixedThreadPool(this.config.getDownloadThread());

    downloadResponse = new DownloadResponseImpl(downloadDBController);
  }

  public static DownloadManager getInstance(Context context) {
    return getInstance(context, null);
  }

  public static DownloadManager getInstance(Context context, Config config) {
    synchronized (DownloadManagerImpl.class) {
      if (instance == null) {
        instance = new DownloadManagerImpl(context, config);
      }
    }
    return instance;
  }


  @Override
  public void download(DownloadInfo downloadInfo) {
    downloadingCaches.add(downloadInfo);
    if (cacheDownloadTask.size() >= config.getDownloadThread()) {
      downloadInfo.setStatus(DownloadInfo.STATUS_WAIT);
      downloadResponse.onStatusChanged(downloadInfo);
    } else {
      DownloadTaskImpl downloadTask = new DownloadTaskImpl(executorService, downloadResponse,
          downloadInfo, config, this);
      cacheDownloadTask.put(downloadInfo.getId(), downloadTask);
      downloadInfo.setStatus(DownloadInfo.STATUS_PREPARE_DOWNLOAD);
      downloadResponse.onStatusChanged(downloadInfo);
      downloadTask.start();
    }

  }

  @Override
  public void pause(DownloadInfo downloadInfo) {
    downloadInfo.setStatus(DownloadInfo.STATUS_PAUSED);
    cacheDownloadTask.remove(downloadInfo.getId());
    downloadResponse.onStatusChanged(downloadInfo);
    prepareDownloadNextTask();
  }

  private void prepareDownloadNextTask() {

  }

  @Override
  public void resume(DownloadInfo downloadInfo) {
    if (cacheDownloadTask.get(downloadInfo.getId()) == null) {
      download(downloadInfo);
    }
  }

  @Override
  public void remove(DownloadInfo downloadInfo) {
    downloadInfo.setStatus(DownloadInfo.STATUS_REMOVED);
    cacheDownloadTask.remove(downloadInfo.getId());
    downloadingCaches.remove(downloadInfo);
    downloadResponse.onStatusChanged(downloadInfo);
  }

  @Override
  public void onDestroy() {

  }

  @Override
  public DownloadInfo getDownloadById(int id) {
    DownloadInfo downloadInfo = null;
    for (DownloadInfo d : downloadingCaches) {
      if (d.getId() == id) {
        downloadInfo = d;
        break;
      }
    }

    if (downloadInfo == null) {

    }
    return downloadInfo;
  }

  @Override
  public void onDownloadSuccess(DownloadInfo downloadInfo) {
    cacheDownloadTask.remove(downloadInfo.getId());
    downloadingCaches.remove(downloadInfo);
    prepareDownloadNextTask();
  }

  public class Config {

    private int connectTimeout = 5000;
    private int readTimeout = 5000;

    private int downloadThread = 5;

    private int eachDownloadThread = 2;

    private String method = "GET";

    public int getConnectTimeout() {
      return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
      this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
      return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
      this.readTimeout = readTimeout;
    }

    public int getDownloadThread() {
      return downloadThread;
    }

    public void setDownloadThread(int downloadThread) {
      this.downloadThread = downloadThread;
    }

    public int getEachDownloadThread() {
      return eachDownloadThread;
    }

    public void setEachDownloadThread(int eachDownloadThread) {
      this.eachDownloadThread = eachDownloadThread;
    }

    public String getMethod() {
      return method;
    }

    public void setMethod(String method) {
      this.method = method;
    }
  }


}
