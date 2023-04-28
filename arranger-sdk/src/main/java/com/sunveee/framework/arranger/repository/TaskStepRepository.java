package com.sunveee.framework.arranger.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sunveee.framework.arranger.common.enums.TaskStepExecStatus;
import com.sunveee.framework.arranger.dao.entity.TaskStep;

@Repository
public interface TaskStepRepository extends JpaRepository<TaskStep, Long> {

    @Query("select ts from TaskStep ts where ts.stepId=:stepId")
    TaskStep selectByStepId(@Param("stepId") String stepId);

    @Query("select ts from TaskStep ts where ts.taskId=:taskId")
    List<TaskStep> selectByTaskId(@Param("taskId") String taskId);

    @Query("select ts from TaskStep ts where ts.taskId=:taskId and ts.seqNo=:seqNo")
    List<TaskStep> selectByTaskIdAndSeqNo(@Param("taskId") String taskId, @Param("seqNo") Integer seqNo);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update TaskStep ts set ts.execStatus=:afterExecStatus where ts.taskId=:taskId and ts.execStatus=:beforeExecStatus and ts.seqNo=:seqNo")
    int launchStep(@Param("taskId") String taskId, @Param("seqNo") Integer seqNo, @Param("beforeExecStatus") TaskStepExecStatus beforeExecStatus, @Param("afterExecStatus") TaskStepExecStatus afterExecStatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update TaskStep ts set ts.execStatus=:afterExecStatus, ts.startTime=:startTime where ts.stepId=:stepId and ts.execStatus=:beforeExecStatus")
    int startStepExecution(@Param("stepId") String stepId, @Param("startTime") LocalDateTime startTime, @Param("beforeExecStatus") TaskStepExecStatus beforeExecStatus, @Param("afterExecStatus") TaskStepExecStatus afterExecStatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update TaskStep ts set ts.execStatus=:afterExecStatus, ts.finishTime=:endTime, ts.execMessage=:execMessage where ts.stepId=:stepId and ts.execStatus=:beforeExecStatus")
    int endStepExecution(@Param("stepId") String stepId, @Param("endTime") LocalDateTime endTime, @Param("beforeExecStatus") TaskStepExecStatus beforeExecStatus, @Param("afterExecStatus") TaskStepExecStatus afterExecStatus, @Param("execMessage") String execMessage);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update TaskStep ts set ts.execStatus=:afterExecStatus, ts.startTime=null, ts.finishTime=null where ts.stepId=:stepId and ts.execStatus=:beforeExecStatus")
    int resetStep(@Param("stepId") String stepId, @Param("beforeExecStatus") TaskStepExecStatus beforeExecStatus, @Param("afterExecStatus") TaskStepExecStatus afterExecStatus);

}
