
package com.citasmedicas.appcitasmedicas.Repository;

import com.citasmedicas.appcitasmedicas.Entity.Appointment;
import com.citasmedicas.appcitasmedicas.Enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientIdAndStatus(Long patientId, AppointmentStatus status);
    List<Appointment> findByStartAtBetween(LocalDateTime from, LocalDateTime to);
    List<Appointment> findByDoctorId(Long doctorId);
    List<Appointment> findByPatientId(Long patientId);

    @Query("""
        SELECT COUNT(a) > 0 FROM Appointment a
        WHERE a.doctor.id = :doctorId
          AND a.status <> :cancelled
          AND a.startAt < :endAt
          AND a.endAt > :startAt
          AND (:excludeId IS NULL OR a.id <> :excludeId)
    """)
    boolean existsDoctorOverlap(
            @Param("doctorId") Long doctorId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("excludeId") Long excludeId,
            @Param("cancelled") AppointmentStatus cancelled
    );

    @Query("""
        SELECT COUNT(a) > 0 FROM Appointment a
        WHERE a.office.id = :officeId
          AND a.status <> :cancelled
          AND a.startAt < :endAt
          AND a.endAt > :startAt
          AND (:excludeId IS NULL OR a.id <> :excludeId)
    """)
    boolean existsOfficeOverlap(
            @Param("officeId") Long officeId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("excludeId") Long excludeId,
            @Param("cancelled") AppointmentStatus cancelled
    );

    @Query("""
        SELECT COUNT(a) > 0 FROM Appointment a
        WHERE a.patient.id = :patientId
          AND a.status <> :cancelled
          AND a.startAt < :endAt
          AND a.endAt > :startAt
          AND (:excludeId IS NULL OR a.id <> :excludeId)
    """)
    boolean existsPatientOverlap(
            @Param("patientId") Long patientId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("excludeId") Long excludeId,
            @Param("cancelled") AppointmentStatus cancelled
    );

    @Query("""
        SELECT a FROM Appointment a
        WHERE a.doctor.id = :doctorId
          AND a.status <> :cancelled
          AND a.startAt >= :from
          AND a.startAt < :to
        ORDER BY a.startAt
    """)
    List<Appointment> findActiveDoctorAppointmentsInRange(
            @Param("doctorId") Long doctorId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("cancelled") AppointmentStatus cancelled
    );

    @Query("""
        SELECT a.office.id, a.office.name, COUNT(a)
        FROM Appointment a
        WHERE a.status <> :cancelled
          AND a.startAt >= :from
          AND a.startAt < :to
        GROUP BY a.office.id, a.office.name
    """)
    List<Object[]> findOfficeOccupancy(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("cancelled") AppointmentStatus cancelled
    );

    @Query("""
        SELECT a.doctor.id, a.doctor.firstName, a.doctor.lastName,
               a.doctor.specialty.name, COUNT(a)
        FROM Appointment a
        WHERE a.status = :completed
          AND a.startAt >= :from
          AND a.startAt < :to
        GROUP BY a.doctor.id, a.doctor.firstName, a.doctor.lastName, a.doctor.specialty.name
        ORDER BY COUNT(a) DESC
    """)
    List<Object[]> findDoctorProductivity(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("completed") AppointmentStatus completed
    );

    @Query("""
        SELECT a.patient.id, a.patient.firstName, a.patient.lastName,
               a.patient.documentNumber, COUNT(a)
        FROM Appointment a
        WHERE a.status = :noShow
          AND a.startAt >= :from
          AND a.startAt < :to
        GROUP BY a.patient.id, a.patient.firstName, a.patient.lastName, a.patient.documentNumber
        ORDER BY COUNT(a) DESC
    """)
    List<Object[]> findNoShowPatients(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("noShow") AppointmentStatus noShow
    );

    @Query("""
        SELECT a.doctor.specialty.name, COUNT(a)
        FROM Appointment a
        WHERE a.status IN (:cancelled, :noShow)
          AND a.startAt >= :from
          AND a.startAt < :to
        GROUP BY a.doctor.specialty.name
    """)
    List<Object[]> countCancelledAndNoShowBySpecialty(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("cancelled") AppointmentStatus cancelled,
            @Param("noShow") AppointmentStatus noShow
    );
}