package com.dicoding.mystudentdata.database

import androidx.room.*

@Entity
data class Student(
    @PrimaryKey
    val studentId: Int,
    val name: String,
    val univId: Int,
)

@Entity
data class University(
    @PrimaryKey
    val universityId: Int,
    val name: String,
)

@Entity
data class Course(
    @PrimaryKey
    val courseId: Int,
    val name: String,
)

// One to One
data class StudentAndUniversity(
    @Embedded
    val student: Student,

    @Relation(
        parentColumn = "univId",
        entityColumn = "universityId"
    )
    val university: University? = null
)

// One to Many
data class UniversityAndStudent(
    @Embedded
    val university: University,

    @Relation(
        parentColumn = "universityId",
        entityColumn = "univId"
    )
    val student: List<Student>
)

// Many to Many
// membuat tabel baru untuk menyimpan data many to many
@Entity(primaryKeys = ["sId", "cId"])
data class CourseStudentCrossRef(
    val sid: Int,
    @ColumnInfo(index = true)
    val cid: Int
)

data class StudentWithCourse(
    @Embedded
    val student: Student,

    // nyambungin dua kolom id
    // dengan bantuan tabel crossref
    @Relation(
        parentColumn = "studentId",
        entity = Course::class,
        entityColumn = "courseId",
        associateBy = Junction(
            value = CourseStudentCrossRef::class,
            parentColumn = "sid",
            entityColumn = "cid"
        )
    )
    val course: List<Course>
)
