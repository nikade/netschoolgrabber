package `in`.edak.netschool.main

import java.util.Calendar



object Utils {
    fun getCurrentWeekFrom1Sep(): Int {
        val cal = Calendar.getInstance()
        val week = cal.get(Calendar.WEEK_OF_YEAR)
        cal.set(Calendar.MONTH,8)
        cal.set(Calendar.DAY_OF_MONTH,1)
        val week1sep = cal.get(Calendar.WEEK_OF_YEAR)
        return week-week1sep+1
    }

}