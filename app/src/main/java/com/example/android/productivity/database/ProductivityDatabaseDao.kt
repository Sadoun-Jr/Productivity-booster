/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.productivity.database

import androidx.room.*


@Dao
interface ProductivityDatabaseDao {

    @Insert
    suspend fun insert(night: ProductiveDay)

    @Update
    suspend fun update(night: ProductiveDay)

    @Query("SELECT * from productivity_table WHERE dayId = :key")
    suspend fun getDayById(key: Long): ProductiveDay?

    @Query("SELECT * from productivity_table WHERE day = :dayInYear")
    suspend fun getDayInYear(dayInYear: String): ProductiveDay?

    @Query("DELETE FROM productivity_table")
    suspend fun clear()

    @Query("SELECT * FROM productivity_table ORDER BY dayId DESC")
    suspend fun getAllDays(): List<ProductiveDay>

    @Query("SELECT * FROM productivity_table ORDER BY dayId DESC LIMIT 1")
    suspend fun getToday(): ProductiveDay?

    /**
     * Updating only goal, rating and desc
     * By dayOfYear
     */
    @Query("UPDATE productivity_table SET overall_day_rating= :rating, description = :desc WHERE day =:dayOfYear")
    suspend fun updateSpecificDayRatingAndDesc(rating: Int?, desc: String?, dayOfYear: String)

    /**
     * Updating only goal, rating and desc
     * By id
     */
    @Query("UPDATE productivity_table SET first_goal = :goal, overall_day_rating= :rating, description = :desc WHERE dayId =:id")
    suspend fun updateSpecificWholeDay(goal: String?, rating: Int?, desc: String?, id: Long)

}
