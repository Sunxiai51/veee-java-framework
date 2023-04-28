package com.sunveee.framework.arranger.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sunveee.framework.arranger.common.enums.TaskExecStatus;
import com.sunveee.framework.arranger.dao.entity.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("select t from Task t where t.taskId=:taskId")
    Task selectByTaskId(@Param("taskId") String taskId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Task t set t.execStatus=:afterExecStatus, t.startTime =:launchTime where t.taskId=:taskId and t.execStatus=:beforeExecStatus")
    int launchTask(@Param("taskId") String taskId, @Param("launchTime") LocalDateTime launchTime, @Param("beforeExecStatus") TaskExecStatus beforeExecStatus, @Param("afterExecStatus") TaskExecStatus afterExecStatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Task t set t.execStatus=:afterExecStatus, t.finishTime =:finishTime where t.taskId=:taskId and t.execStatus=:beforeExecStatus")
    int finishTask(@Param("taskId") String taskId, @Param("finishTime") LocalDateTime finishTime, @Param("beforeExecStatus") TaskExecStatus beforeExecStatus, @Param("afterExecStatus") TaskExecStatus afterExecStatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Task t set t.execStatus=:afterExecStatus where t.taskId=:taskId and t.execStatus=:beforeExecStatus")
    int updateTaskExecStatus(@Param("taskId") String taskId, @Param("beforeExecStatus") TaskExecStatus beforeExecStatus, @Param("afterExecStatus") TaskExecStatus afterExecStatus);

}
