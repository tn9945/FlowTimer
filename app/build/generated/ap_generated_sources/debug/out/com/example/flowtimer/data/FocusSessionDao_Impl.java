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
public final class FocusSessionDao_Impl implements FocusSessionDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<FocusSessionEntity> __insertAdapterOfFocusSessionEntity;

  public FocusSessionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfFocusSessionEntity = new EntityInsertAdapter<FocusSessionEntity>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `focus_sessions` (`id`,`userId`,`userName`,`startTimeMillis`,`endTimeMillis`,`totalDurationMillis`,`activeDurationMillis`,`breakDurationMillis`,`studyDurationMillis`,`distractionDurationMillis`,`neutralDurationMillis`,`effectiveFocusDurationMillis`,`focusScore`,`rewardCoin`,`rewardExp`,`rewardMinutes`,`appSwitchCount`,`appCount`,`topAppName`,`topAppDurationMillis`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement,
          final FocusSessionEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getUserId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getUserId());
        }
        if (entity.getUserName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getUserName());
        }
        statement.bindLong(4, entity.getStartTimeMillis());
        statement.bindLong(5, entity.getEndTimeMillis());
        statement.bindLong(6, entity.getTotalDurationMillis());
        statement.bindLong(7, entity.getActiveDurationMillis());
        statement.bindLong(8, entity.getBreakDurationMillis());
        statement.bindLong(9, entity.getStudyDurationMillis());
        statement.bindLong(10, entity.getDistractionDurationMillis());
        statement.bindLong(11, entity.getNeutralDurationMillis());
        statement.bindLong(12, entity.getEffectiveFocusDurationMillis());
        statement.bindLong(13, entity.getFocusScore());
        statement.bindLong(14, entity.getRewardCoin());
        statement.bindLong(15, entity.getRewardExp());
        statement.bindLong(16, entity.getRewardMinutes());
        statement.bindLong(17, entity.getAppSwitchCount());
        statement.bindLong(18, entity.getAppCount());
        if (entity.getTopAppName() == null) {
          statement.bindNull(19);
        } else {
          statement.bindText(19, entity.getTopAppName());
        }
        statement.bindLong(20, entity.getTopAppDurationMillis());
      }
    };
  }

  @Override
  public long insert(final FocusSessionEntity session) {
    return DBUtil.performBlocking(__db, false, true, (_connection) -> {
      return __insertAdapterOfFocusSessionEntity.insertAndReturnId(_connection, session);
    });
  }

  @Override
  public FocusSessionEntity findById(final long sessionId) {
    final String _sql = "SELECT * FROM focus_sessions WHERE id = ? LIMIT 1";
    return DBUtil.performBlocking(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, sessionId);
        final int _columnIndexOfId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "id");
        final int _columnIndexOfUserId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "userId");
        final int _columnIndexOfUserName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "userName");
        final int _columnIndexOfStartTimeMillis = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "startTimeMillis");
        final int _columnIndexOfEndTimeMillis = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "endTimeMillis");
        final int _columnIndexOfTotalDurationMillis = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "totalDurationMillis");
        final int _columnIndexOfActiveDurationMillis = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "activeDurationMillis");
        final int _columnIndexOfBreakDurationMillis = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "breakDurationMillis");
        final int _columnIndexOfStudyDurationMillis = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "studyDurationMillis");
        final int _columnIndexOfDistractionDurationMillis = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "distractionDurationMillis");
        final int _columnIndexOfNeutralDurationMillis = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "neutralDurationMillis");
        final int _columnIndexOfEffectiveFocusDurationMillis = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "effectiveFocusDurationMillis");
        final int _columnIndexOfFocusScore = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "focusScore");
        final int _columnIndexOfRewardCoin = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "rewardCoin");
        final int _columnIndexOfRewardExp = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "rewardExp");
        final int _columnIndexOfRewardMinutes = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "rewardMinutes");
        final int _columnIndexOfAppSwitchCount = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "appSwitchCount");
        final int _columnIndexOfAppCount = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "appCount");
        final int _columnIndexOfTopAppName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "topAppName");
        final int _columnIndexOfTopAppDurationMillis = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "topAppDurationMillis");
        final FocusSessionEntity _result;
        if (_stmt.step()) {
          final String _tmpUserId;
          if (_stmt.isNull(_columnIndexOfUserId)) {
            _tmpUserId = null;
          } else {
            _tmpUserId = _stmt.getText(_columnIndexOfUserId);
          }
          final String _tmpUserName;
          if (_stmt.isNull(_columnIndexOfUserName)) {
            _tmpUserName = null;
          } else {
            _tmpUserName = _stmt.getText(_columnIndexOfUserName);
          }
          final long _tmpStartTimeMillis;
          _tmpStartTimeMillis = _stmt.getLong(_columnIndexOfStartTimeMillis);
          final long _tmpEndTimeMillis;
          _tmpEndTimeMillis = _stmt.getLong(_columnIndexOfEndTimeMillis);
          final long _tmpTotalDurationMillis;
          _tmpTotalDurationMillis = _stmt.getLong(_columnIndexOfTotalDurationMillis);
          final long _tmpActiveDurationMillis;
          _tmpActiveDurationMillis = _stmt.getLong(_columnIndexOfActiveDurationMillis);
          final long _tmpBreakDurationMillis;
          _tmpBreakDurationMillis = _stmt.getLong(_columnIndexOfBreakDurationMillis);
          final long _tmpStudyDurationMillis;
          _tmpStudyDurationMillis = _stmt.getLong(_columnIndexOfStudyDurationMillis);
          final long _tmpDistractionDurationMillis;
          _tmpDistractionDurationMillis = _stmt.getLong(_columnIndexOfDistractionDurationMillis);
          final long _tmpNeutralDurationMillis;
          _tmpNeutralDurationMillis = _stmt.getLong(_columnIndexOfNeutralDurationMillis);
          final long _tmpEffectiveFocusDurationMillis;
          _tmpEffectiveFocusDurationMillis = _stmt.getLong(_columnIndexOfEffectiveFocusDurationMillis);
          final int _tmpFocusScore;
          _tmpFocusScore = (int) (_stmt.getLong(_columnIndexOfFocusScore));
          final int _tmpRewardCoin;
          _tmpRewardCoin = (int) (_stmt.getLong(_columnIndexOfRewardCoin));
          final int _tmpRewardExp;
          _tmpRewardExp = (int) (_stmt.getLong(_columnIndexOfRewardExp));
          final int _tmpRewardMinutes;
          _tmpRewardMinutes = (int) (_stmt.getLong(_columnIndexOfRewardMinutes));
          final int _tmpAppSwitchCount;
          _tmpAppSwitchCount = (int) (_stmt.getLong(_columnIndexOfAppSwitchCount));
          final int _tmpAppCount;
          _tmpAppCount = (int) (_stmt.getLong(_columnIndexOfAppCount));
          final String _tmpTopAppName;
          if (_stmt.isNull(_columnIndexOfTopAppName)) {
            _tmpTopAppName = null;
          } else {
            _tmpTopAppName = _stmt.getText(_columnIndexOfTopAppName);
          }
          final long _tmpTopAppDurationMillis;
          _tmpTopAppDurationMillis = _stmt.getLong(_columnIndexOfTopAppDurationMillis);
          _result = new FocusSessionEntity(_tmpUserId,_tmpUserName,_tmpStartTimeMillis,_tmpEndTimeMillis,_tmpTotalDurationMillis,_tmpActiveDurationMillis,_tmpBreakDurationMillis,_tmpStudyDurationMillis,_tmpDistractionDurationMillis,_tmpNeutralDurationMillis,_tmpEffectiveFocusDurationMillis,_tmpFocusScore,_tmpRewardCoin,_tmpRewardExp,_tmpRewardMinutes,_tmpAppSwitchCount,_tmpAppCount,_tmpTopAppName,_tmpTopAppDurationMillis);
          final long _tmpId;
          _tmpId = _stmt.getLong(_columnIndexOfId);
          _result.setId(_tmpId);
        } else {
          _result = null;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public List<FocusSessionEntity> findAllByUserId(final String userId) {
    final String _sql = "SELECT * FROM focus_sessions WHERE userId = ? ORDER BY startTimeMillis DESC";
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
        final int _columnIndexOfUserId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "userId");
        final int _columnIndexOfUserName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "userName");
        final int _columnIndexOfStartTimeMillis = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "startTimeMillis");
        final int _columnIndexOfEndTimeMillis = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "endTimeMillis");
        final int _columnIndexOfTotalDurationMillis = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "totalDurationMillis");
        final int _columnIndexOfActiveDurationMillis = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "activeDurationMillis");
        final int _columnIndexOfBreakDurationMillis = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "breakDurationMillis");
        final int _columnIndexOfStudyDurationMillis = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "studyDurationMillis");
        final int _columnIndexOfDistractionDurationMillis = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "distractionDurationMillis");
        final int _columnIndexOfNeutralDurationMillis = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "neutralDurationMillis");
        final int _columnIndexOfEffectiveFocusDurationMillis = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "effectiveFocusDurationMillis");
        final int _columnIndexOfFocusScore = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "focusScore");
        final int _columnIndexOfRewardCoin = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "rewardCoin");
        final int _columnIndexOfRewardExp = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "rewardExp");
        final int _columnIndexOfRewardMinutes = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "rewardMinutes");
        final int _columnIndexOfAppSwitchCount = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "appSwitchCount");
        final int _columnIndexOfAppCount = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "appCount");
        final int _columnIndexOfTopAppName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "topAppName");
        final int _columnIndexOfTopAppDurationMillis = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "topAppDurationMillis");
        final List<FocusSessionEntity> _result = new ArrayList<FocusSessionEntity>();
        while (_stmt.step()) {
          final FocusSessionEntity _item;
          final String _tmpUserId;
          if (_stmt.isNull(_columnIndexOfUserId)) {
            _tmpUserId = null;
          } else {
            _tmpUserId = _stmt.getText(_columnIndexOfUserId);
          }
          final String _tmpUserName;
          if (_stmt.isNull(_columnIndexOfUserName)) {
            _tmpUserName = null;
          } else {
            _tmpUserName = _stmt.getText(_columnIndexOfUserName);
          }
          final long _tmpStartTimeMillis;
          _tmpStartTimeMillis = _stmt.getLong(_columnIndexOfStartTimeMillis);
          final long _tmpEndTimeMillis;
          _tmpEndTimeMillis = _stmt.getLong(_columnIndexOfEndTimeMillis);
          final long _tmpTotalDurationMillis;
          _tmpTotalDurationMillis = _stmt.getLong(_columnIndexOfTotalDurationMillis);
          final long _tmpActiveDurationMillis;
          _tmpActiveDurationMillis = _stmt.getLong(_columnIndexOfActiveDurationMillis);
          final long _tmpBreakDurationMillis;
          _tmpBreakDurationMillis = _stmt.getLong(_columnIndexOfBreakDurationMillis);
          final long _tmpStudyDurationMillis;
          _tmpStudyDurationMillis = _stmt.getLong(_columnIndexOfStudyDurationMillis);
          final long _tmpDistractionDurationMillis;
          _tmpDistractionDurationMillis = _stmt.getLong(_columnIndexOfDistractionDurationMillis);
          final long _tmpNeutralDurationMillis;
          _tmpNeutralDurationMillis = _stmt.getLong(_columnIndexOfNeutralDurationMillis);
          final long _tmpEffectiveFocusDurationMillis;
          _tmpEffectiveFocusDurationMillis = _stmt.getLong(_columnIndexOfEffectiveFocusDurationMillis);
          final int _tmpFocusScore;
          _tmpFocusScore = (int) (_stmt.getLong(_columnIndexOfFocusScore));
          final int _tmpRewardCoin;
          _tmpRewardCoin = (int) (_stmt.getLong(_columnIndexOfRewardCoin));
          final int _tmpRewardExp;
          _tmpRewardExp = (int) (_stmt.getLong(_columnIndexOfRewardExp));
          final int _tmpRewardMinutes;
          _tmpRewardMinutes = (int) (_stmt.getLong(_columnIndexOfRewardMinutes));
          final int _tmpAppSwitchCount;
          _tmpAppSwitchCount = (int) (_stmt.getLong(_columnIndexOfAppSwitchCount));
          final int _tmpAppCount;
          _tmpAppCount = (int) (_stmt.getLong(_columnIndexOfAppCount));
          final String _tmpTopAppName;
          if (_stmt.isNull(_columnIndexOfTopAppName)) {
            _tmpTopAppName = null;
          } else {
            _tmpTopAppName = _stmt.getText(_columnIndexOfTopAppName);
          }
          final long _tmpTopAppDurationMillis;
          _tmpTopAppDurationMillis = _stmt.getLong(_columnIndexOfTopAppDurationMillis);
          _item = new FocusSessionEntity(_tmpUserId,_tmpUserName,_tmpStartTimeMillis,_tmpEndTimeMillis,_tmpTotalDurationMillis,_tmpActiveDurationMillis,_tmpBreakDurationMillis,_tmpStudyDurationMillis,_tmpDistractionDurationMillis,_tmpNeutralDurationMillis,_tmpEffectiveFocusDurationMillis,_tmpFocusScore,_tmpRewardCoin,_tmpRewardExp,_tmpRewardMinutes,_tmpAppSwitchCount,_tmpAppCount,_tmpTopAppName,_tmpTopAppDurationMillis);
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
    final String _sql = "DELETE FROM focus_sessions WHERE userId = ?";
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
