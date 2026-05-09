package com.example.flowtimer.data;

import androidx.annotation.NonNull;
import androidx.room.InvalidationTracker;
import androidx.room.RoomOpenDelegate;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.SQLite;
import androidx.sqlite.SQLiteConnection;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation", "removal"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile UserDao _userDao;

  private volatile FocusSessionDao _focusSessionDao;

  private volatile AppUsageRecordDao _appUsageRecordDao;

  @Override
  @NonNull
  protected RoomOpenDelegate createOpenDelegate() {
    final RoomOpenDelegate _openDelegate = new RoomOpenDelegate(4, "383a5b06af1d0c120067025a1af50f7b", "87ec48a890b770d0a9c1f0f10f8df5a1") {
      @Override
      public void createAllTables(@NonNull final SQLiteConnection connection) {
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS `users` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, `userId` TEXT, `password` TEXT)");
        SQLite.execSQL(connection, "CREATE UNIQUE INDEX IF NOT EXISTS `index_users_userId` ON `users` (`userId`)");
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS `focus_sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userId` TEXT, `userName` TEXT, `startTimeMillis` INTEGER NOT NULL, `endTimeMillis` INTEGER NOT NULL, `totalDurationMillis` INTEGER NOT NULL, `activeDurationMillis` INTEGER NOT NULL, `breakDurationMillis` INTEGER NOT NULL, `studyDurationMillis` INTEGER NOT NULL, `distractionDurationMillis` INTEGER NOT NULL, `neutralDurationMillis` INTEGER NOT NULL, `effectiveFocusDurationMillis` INTEGER NOT NULL, `focusScore` INTEGER NOT NULL, `rewardCoin` INTEGER NOT NULL, `rewardExp` INTEGER NOT NULL, `rewardMinutes` INTEGER NOT NULL, `appSwitchCount` INTEGER NOT NULL, `appCount` INTEGER NOT NULL, `topAppName` TEXT, `topAppDurationMillis` INTEGER NOT NULL)");
        SQLite.execSQL(connection, "CREATE INDEX IF NOT EXISTS `index_focus_sessions_userId` ON `focus_sessions` (`userId`)");
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS `app_usage_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sessionId` INTEGER NOT NULL, `userId` TEXT, `packageName` TEXT, `appName` TEXT, `durationMillis` INTEGER NOT NULL, `launchCount` INTEGER NOT NULL, `category` TEXT)");
        SQLite.execSQL(connection, "CREATE INDEX IF NOT EXISTS `index_app_usage_records_sessionId` ON `app_usage_records` (`sessionId`)");
        SQLite.execSQL(connection, "CREATE INDEX IF NOT EXISTS `index_app_usage_records_userId` ON `app_usage_records` (`userId`)");
        SQLite.execSQL(connection, "CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        SQLite.execSQL(connection, "INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '383a5b06af1d0c120067025a1af50f7b')");
      }

      @Override
      public void dropAllTables(@NonNull final SQLiteConnection connection) {
        SQLite.execSQL(connection, "DROP TABLE IF EXISTS `users`");
        SQLite.execSQL(connection, "DROP TABLE IF EXISTS `focus_sessions`");
        SQLite.execSQL(connection, "DROP TABLE IF EXISTS `app_usage_records`");
      }

      @Override
      public void onCreate(@NonNull final SQLiteConnection connection) {
      }

      @Override
      public void onOpen(@NonNull final SQLiteConnection connection) {
        internalInitInvalidationTracker(connection);
      }

      @Override
      public void onPreMigrate(@NonNull final SQLiteConnection connection) {
        DBUtil.dropFtsSyncTriggers(connection);
      }

      @Override
      public void onPostMigrate(@NonNull final SQLiteConnection connection) {
      }

      @Override
      @NonNull
      public RoomOpenDelegate.ValidationResult onValidateSchema(
          @NonNull final SQLiteConnection connection) {
        final Map<String, TableInfo.Column> _columnsUsers = new HashMap<String, TableInfo.Column>(4);
        _columnsUsers.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("name", new TableInfo.Column("name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("userId", new TableInfo.Column("userId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUsers.put("password", new TableInfo.Column("password", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final Set<TableInfo.ForeignKey> _foreignKeysUsers = new HashSet<TableInfo.ForeignKey>(0);
        final Set<TableInfo.Index> _indicesUsers = new HashSet<TableInfo.Index>(1);
        _indicesUsers.add(new TableInfo.Index("index_users_userId", true, Arrays.asList("userId"), Arrays.asList("ASC")));
        final TableInfo _infoUsers = new TableInfo("users", _columnsUsers, _foreignKeysUsers, _indicesUsers);
        final TableInfo _existingUsers = TableInfo.read(connection, "users");
        if (!_infoUsers.equals(_existingUsers)) {
          return new RoomOpenDelegate.ValidationResult(false, "users(com.example.flowtimer.data.UserEntity).\n"
                  + " Expected:\n" + _infoUsers + "\n"
                  + " Found:\n" + _existingUsers);
        }
        final Map<String, TableInfo.Column> _columnsFocusSessions = new HashMap<String, TableInfo.Column>(20);
        _columnsFocusSessions.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFocusSessions.put("userId", new TableInfo.Column("userId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFocusSessions.put("userName", new TableInfo.Column("userName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFocusSessions.put("startTimeMillis", new TableInfo.Column("startTimeMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFocusSessions.put("endTimeMillis", new TableInfo.Column("endTimeMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFocusSessions.put("totalDurationMillis", new TableInfo.Column("totalDurationMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFocusSessions.put("activeDurationMillis", new TableInfo.Column("activeDurationMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFocusSessions.put("breakDurationMillis", new TableInfo.Column("breakDurationMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFocusSessions.put("studyDurationMillis", new TableInfo.Column("studyDurationMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFocusSessions.put("distractionDurationMillis", new TableInfo.Column("distractionDurationMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFocusSessions.put("neutralDurationMillis", new TableInfo.Column("neutralDurationMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFocusSessions.put("effectiveFocusDurationMillis", new TableInfo.Column("effectiveFocusDurationMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFocusSessions.put("focusScore", new TableInfo.Column("focusScore", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFocusSessions.put("rewardCoin", new TableInfo.Column("rewardCoin", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFocusSessions.put("rewardExp", new TableInfo.Column("rewardExp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFocusSessions.put("rewardMinutes", new TableInfo.Column("rewardMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFocusSessions.put("appSwitchCount", new TableInfo.Column("appSwitchCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFocusSessions.put("appCount", new TableInfo.Column("appCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFocusSessions.put("topAppName", new TableInfo.Column("topAppName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFocusSessions.put("topAppDurationMillis", new TableInfo.Column("topAppDurationMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final Set<TableInfo.ForeignKey> _foreignKeysFocusSessions = new HashSet<TableInfo.ForeignKey>(0);
        final Set<TableInfo.Index> _indicesFocusSessions = new HashSet<TableInfo.Index>(1);
        _indicesFocusSessions.add(new TableInfo.Index("index_focus_sessions_userId", false, Arrays.asList("userId"), Arrays.asList("ASC")));
        final TableInfo _infoFocusSessions = new TableInfo("focus_sessions", _columnsFocusSessions, _foreignKeysFocusSessions, _indicesFocusSessions);
        final TableInfo _existingFocusSessions = TableInfo.read(connection, "focus_sessions");
        if (!_infoFocusSessions.equals(_existingFocusSessions)) {
          return new RoomOpenDelegate.ValidationResult(false, "focus_sessions(com.example.flowtimer.data.FocusSessionEntity).\n"
                  + " Expected:\n" + _infoFocusSessions + "\n"
                  + " Found:\n" + _existingFocusSessions);
        }
        final Map<String, TableInfo.Column> _columnsAppUsageRecords = new HashMap<String, TableInfo.Column>(8);
        _columnsAppUsageRecords.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppUsageRecords.put("sessionId", new TableInfo.Column("sessionId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppUsageRecords.put("userId", new TableInfo.Column("userId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppUsageRecords.put("packageName", new TableInfo.Column("packageName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppUsageRecords.put("appName", new TableInfo.Column("appName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppUsageRecords.put("durationMillis", new TableInfo.Column("durationMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppUsageRecords.put("launchCount", new TableInfo.Column("launchCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppUsageRecords.put("category", new TableInfo.Column("category", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final Set<TableInfo.ForeignKey> _foreignKeysAppUsageRecords = new HashSet<TableInfo.ForeignKey>(0);
        final Set<TableInfo.Index> _indicesAppUsageRecords = new HashSet<TableInfo.Index>(2);
        _indicesAppUsageRecords.add(new TableInfo.Index("index_app_usage_records_sessionId", false, Arrays.asList("sessionId"), Arrays.asList("ASC")));
        _indicesAppUsageRecords.add(new TableInfo.Index("index_app_usage_records_userId", false, Arrays.asList("userId"), Arrays.asList("ASC")));
        final TableInfo _infoAppUsageRecords = new TableInfo("app_usage_records", _columnsAppUsageRecords, _foreignKeysAppUsageRecords, _indicesAppUsageRecords);
        final TableInfo _existingAppUsageRecords = TableInfo.read(connection, "app_usage_records");
        if (!_infoAppUsageRecords.equals(_existingAppUsageRecords)) {
          return new RoomOpenDelegate.ValidationResult(false, "app_usage_records(com.example.flowtimer.data.AppUsageRecordEntity).\n"
                  + " Expected:\n" + _infoAppUsageRecords + "\n"
                  + " Found:\n" + _existingAppUsageRecords);
        }
        return new RoomOpenDelegate.ValidationResult(true, null);
      }
    };
    return _openDelegate;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final Map<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final Map<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "users", "focus_sessions", "app_usage_records");
  }

  @Override
  public void clearAllTables() {
    super.performClear(false, "users", "focus_sessions", "app_usage_records");
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final Map<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(UserDao.class, UserDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(FocusSessionDao.class, FocusSessionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AppUsageRecordDao.class, AppUsageRecordDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final Set<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public UserDao userDao() {
    if (_userDao != null) {
      return _userDao;
    } else {
      synchronized(this) {
        if(_userDao == null) {
          _userDao = new UserDao_Impl(this);
        }
        return _userDao;
      }
    }
  }

  @Override
  public FocusSessionDao focusSessionDao() {
    if (_focusSessionDao != null) {
      return _focusSessionDao;
    } else {
      synchronized(this) {
        if(_focusSessionDao == null) {
          _focusSessionDao = new FocusSessionDao_Impl(this);
        }
        return _focusSessionDao;
      }
    }
  }

  @Override
  public AppUsageRecordDao appUsageRecordDao() {
    if (_appUsageRecordDao != null) {
      return _appUsageRecordDao;
    } else {
      synchronized(this) {
        if(_appUsageRecordDao == null) {
          _appUsageRecordDao = new AppUsageRecordDao_Impl(this);
        }
        return _appUsageRecordDao;
      }
    }
  }
}
