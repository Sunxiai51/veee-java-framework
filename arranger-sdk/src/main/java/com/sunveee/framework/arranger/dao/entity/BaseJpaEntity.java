
package com.sunveee.framework.arranger.dao.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Data;

@Data
@MappedSuperclass
public abstract class BaseJpaEntity implements Serializable {

//    /** ID */
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(columnDefinition = "bigint")
//    protected long id;

    /** 创建时间 */
    @CreationTimestamp
    @Column(name = "create_datetime", columnDefinition = "datetime", updatable = false, nullable = false)
    protected LocalDateTime createDatetime;

    /** 更新时间 */
    @UpdateTimestamp
    @Column(name = "update_datetime", columnDefinition = "datetime", nullable = true)
    protected LocalDateTime updateDatetime;

}