package cn.woblog.android.downloader.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import cn.woblog.android.downloader.domain.DownloadInfo;
import cn.woblog.android.downloader.domain.DownloadThreadInfo;

/**
 * Created by renpingqing on 17/1/23.
 */

public class DefaultDownloadDBController implements DownloadDBController {


  private final Context context;
  private final DefaultDownloadHelper dbHelper;
  private final SQLiteDatabase writableDatabase;

  public DefaultDownloadDBController(Context context) {
    this.context = context;
    dbHelper = new DefaultDownloadHelper(context);
    writableDatabase = dbHelper.getWritableDatabase();
  }

  @Override
  public void createOrUpdate(DownloadInfo downloadInfo) {
    writableDatabase.execSQL(
        "REPLACE INTO download_info(_id,id,supportRanges,createAt,url,path,size,progress,status) VALUES(?,?,?,?,?,?,?,?,?);",
        new Object[]{
            downloadInfo.getKey(), downloadInfo.getId(), downloadInfo.getSupportRanges(),
            downloadInfo.getCreateAt(), downloadInfo.getUrl(), downloadInfo.getPath(),
            downloadInfo.getSize(), downloadInfo.getProgress(), downloadInfo.getStatus()});
  }

  @Override
  public void createOrUpdate(DownloadThreadInfo downloadThreadInfo) {

  }

  @Override
  public void delete(DownloadInfo downloadInfo) {

  }

  @Override
  public void delete(DownloadThreadInfo download) {

  }
}
