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

import androidx.annotation.Nullable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


@Entity(tableName = "productivity_table")
data class ProductiveDay(
        @PrimaryKey(autoGenerate = true)
        var dayId: Long = 0L,

        @ColumnInfo(name = "first_goal")
        val firstGoal: String = "-1",

        @ColumnInfo(name = "day")
        val day: String = "",

        @ColumnInfo(name = "description")
        val desc: String = "",

        @ColumnInfo(name = "overall_day_rating")
        var overallDayRating: Int = -1

)