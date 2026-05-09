package com.example.flowtimer.data;

import androidx.annotation.NonNull;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation", "removal"})
public final class AppUsageRecordDao_Impl implements AppUsageRecordDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<AppUsageRecordEntity> __insertAdapterOfAppUsageRecordEntity;

  public AppUsageRecordDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfAppUsageRecordEntity = new EntityInsertAdapter<AppUsageRecordEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `app_usage_records` (`id`,`sessionId`,`userId`,`packageName`,`appName`,`durationMillis`,`launchCount`,`category`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          final AppUsageRecordEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getSessionId());
        if (entity.getUserId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getUserId());
        }
        if (entity.getPackageName() == null) {
          statement.bindNull(4);
        } else {
          statement.bindText(4, entity.getPackageName());
        }
        if (entity.getAppName() == null) {
          statement.bindNull(5);
        } else {
          statement.bindText(5, entity.getAppName());
        }
        statement.bindLong(6, entity.getDurationMillis());
        statement.bindLong(7, entity.getLaunchCount());
        if (entity.getCategory() == null) {
          statement.bindNull(8);
        } else {
          statement.bindText(8, entity.getCategory());
        }
      }
    };
  }

  @Override
  public void insertAll(final List<AppUsageRecordEntity> records) {
    DBUtil.performBlocking(__db, false, true, (_connection) -> {
      __insertAdapterOfAppUsageRecordEntity.insert(_connection, records);
      return null;
    });
  }

  @Override
  public long insert(final AppUsageRecordEntity record) {
    return DBUtil.performBlocking(__db, false, true, (_connection) -> {
      return __insertAdapterOfAppUsageRecordEntity.insertAndReturnId(_connection, record);
    });
  }

  @Override
  public List<AppUsageRecordEntity> findBySessionId(final long sessionId) {
    final String _sql = "SELECT * FROM app_usage_records WHERE sessionId = ? ORDER BY durationMillis DESC";
    return DBUtil.performBlocking(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, sessionId);
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfSessionId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "sessionId");
        final int _columnIndexOfUserId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "userId");
        final int _columnIndexOfPackageName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "packageName");
        final int _columnIndexOfAppName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "appName");
        final int _columnIndexOfDurationMillis = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "durationMillis");
        final int _columnIndexOfLaunchCount = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "launchCount");
        final int _columnIndexOfCategory = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "category");
        final List<AppUsageRecordEntity> _result = new ArrayList<AppUsageRecordEntity>();
        while (_stmt.step()) {
          final AppUsageRecordEntity _item;
          final long _tmpSessionId;
          _tmpSessionId = _stmt.getLong(_columnIndexOfSessionId);
          final String _tmpUserId;
          if (_stmt.isNull(_columnIndexOfUserId)) {
            _tmpUserId = null;
          } else {
            _tmpUserId = _stmt.getText(_columnIndexOfUserId);
          }
          final String _tmpPackageName;
          if (_stmt.isNull(_columnIndexOfPackageName)) {
            _tmpPackageName = null;
          } else {
            _tmpPackageName = _stmt.getText(_columnIndexOfPackageName);
          }
          final String _tmpAppName;
          if (_stmt.isNull(_columnIndexOfAppName)) {
            _tmpAppName = null;
          } else {
            _tmpAppName = _stmt.getText(_columnIndexOfAppName);
          }
          final long _tmpDurationMillis;
          _tmpDurationMillis = _stmt.getLong(_columnIndexOfDurationMillis);
          final int _tmpLaunchCount;
          _tmpLaunchCount = (int) (_stmt.getLong(_columnIndexOfLaunchCount));
          final String _tmpCategory;
          if (_stmt.isNull(_columnIndexOfCategory)) {
            _tmpCategory = null;
          } else {
            _tmpCategory = _stmt.getText(_columnIndexOfCategory);
          }
          _item = new AppUsageRecordEntity(_tmpSessionId,_tmpUserId,_tmpPackageName,_tmpAppName,_tmpDurationMillis,_tmpLaunchCount,_tmpCategory);
          final long _tmpId;
          _tmpId = _stmt.getLong(_columnIndexOfId);
          _item.setId(_tmpId);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public List<AppUsageRecordEntity> findAllByUserId(final String userId) {
    final String _sql = "SELECT * FROM app_usage_records WHERE userId = ? ORDER BY durationMillis DESC";
    return DBUtil.performBlocking(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (userId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, userId);
        }
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfSessionId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "sessionId");
        final int _columnIndexOfUserId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "userId");
        final int _columnIndexOfPackageName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "packageName");
        final int _columnIndexOfAppName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "appName");
        final int _columnIndexOfDurationMillis = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "durationMillis");
        final int _columnIndexOfLaunchCount = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "launchCount");
        final int _columnIndexOfCategory = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "category");
        final List<AppUsageRecordEntity> _result = new ArrayList<AppUsageRecordEntity>();
        while (_stmt.step()) {
          final AppUsageRecordEntity _item;
          final long _tmpSessionId;
          _tmpSessionId = _stmt.getLong(_columnIndexOfSessionId);
          final String _tmpUserId;
          if (_stmt.isNull(_columnIndexOfUserId)) {
            _tmpUserId = null;
          } else {
            _tmpUserId = _stmt.getText(_columnIndexOfUserId);
          }
          final String _tmpPackageName;
          if (_stmt.isNull(_columnIndexOfPackageName)) {
            _tmpPackageName = null;
          } else {
            _tmpPackageName = _stmt.getText(_columnIndexOfPackageName);
          }
          final String _tmpAppName;
          if (_stmt.isNull(_columnIndexOfAppName)) {
            _tmpAppName = null;
          } else {
            _tmpAppName = _stmt.getText(_columnIndexOfAppName);
          }
          final long _tmpDurationMillis;
          _tmpDurationMillis = _stmt.getLong(_columnIndexOfDurationMillis);
          final int _tmpLaunchCount;
          _tmpLaunchCount = (int) (_stmt.getLong(_columnIndexOfLaunchCount));
          final String _tmpCategory;
          if (_stmt.isNull(_columnIndexOfCategory)) {
            _tmpCategory = null;
          } else {
            _tmpCategory = _stmt.getText(_columnIndexOfCategory);
          }
          _item = new AppUsageRecordEntity(_tmpSessionId,_tmpUserId,_tmpPackageName,_tmpAppName,_tmpDurationMillis,_tmpLaunchCount,_tmpCategory);
          final long _tmpId;
          _tmpId = _stmt.getLong(_columnIndexOfId);
          _item.setId(_tmpId);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public void deleteByUserId(final String userId) {
    final String _sql = "DELETE FROM app_usage_records WHERE userId = ?";
    DBUtil.performBlocking(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (userId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, userId);
        }
        _stmt.step();
        return null;
      } finally {
        _stmt.close();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
