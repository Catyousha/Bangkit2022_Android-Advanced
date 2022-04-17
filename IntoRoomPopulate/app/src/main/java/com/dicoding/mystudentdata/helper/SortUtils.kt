package com.dicoding.mystudentdata.helper

import androidx.sqlite.db.SimpleSQLiteQuery

object SortUtils {

  fun getSortedQuery(sortType: SortType): SimpleSQLiteQuery {
    var simpleQuery = "SELECT * FROM student "
    when (sortType) {
      SortType.ASCENDING -> {
        simpleQuery += "ORDER BY name ASC"
      }
      SortType.DESCENDING -> {
        simpleQuery += "ORDER BY name DESC"
      }
      SortType.RANDOM -> {
        simpleQuery += "ORDER BY RANDOM()"
      }
    }
    return SimpleSQLiteQuery(simpleQuery)
  }

}