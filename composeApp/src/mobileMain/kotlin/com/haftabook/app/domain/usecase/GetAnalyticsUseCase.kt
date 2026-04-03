package com.haftabook.app.domain.usecase

import com.haftabook.app.data.repository.CustomerRepository
import com.haftabook.app.data.repository.EmiRepository
import com.haftabook.app.data.repository.LoanRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class AnalyticsGranularity {
    Yearly,
    Monthly,
    Weekly,
    Daily,
}

data class AnalyticsBucket(
    val label: String,
    val given: Long,
    val paid: Long,
    val due: Long,
    /** New customers added in this period (createdDate in range). */
    val customerCount: Int,
)

class GetAnalyticsUseCase(
    private val loanRepository: LoanRepository,
    private val emiRepository: EmiRepository,
    private val customerRepository: CustomerRepository,
) {
    suspend fun execute(granularity: AnalyticsGranularity): List<AnalyticsBucket> = withContext(Dispatchers.Default) {
        val loans = loanRepository.getAllLoans()
        val emis = emiRepository.getAllEmis()
        val customers = customerRepository.getAllCustomers()
        val emisByLoan = emis.groupBy { it.loanId }
        val ranges = buildBucketRanges(granularity)
        ranges.map { r ->
            val given = loans.filter { it.loanStartDate in r.start..r.end }.sumOf { it.loanAmount }
            val paid = emis.filter { it.emiDate in r.start..r.end }.sumOf { it.emiAmount }
            val due = loans.filter { it.loanStartDate <= r.end }.sumOf { loan ->
                val paidUpTo = emisByLoan[loan.id]?.filter { it.emiDate <= r.end }?.sumOf { it.emiAmount } ?: 0L
                (loan.loanAmount - paidUpTo).coerceAtLeast(0L)
            }
            val customerCount = customers.count { it.createdDate in r.start..r.end }
            AnalyticsBucket(
                label = r.label,
                given = given,
                paid = paid,
                due = due,
                customerCount = customerCount,
            )
        }
    }
}

private data class TimeRange(val label: String, val start: Long, val end: Long)

/** Charts only show periods that end on or after 1 Jan 2025 (local time). */
private fun startOf2025Millis(): Long {
    val c = Calendar.getInstance()
    c.set(2025, Calendar.JANUARY, 1, 0, 0, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

private fun buildBucketRanges(granularity: AnalyticsGranularity): List<TimeRange> {
    val locale = Locale.getDefault()
    val minEnd = startOf2025Millis()
    val raw = when (granularity) {
        AnalyticsGranularity.Daily -> buildDailyRanges(locale)
        AnalyticsGranularity.Weekly -> buildWeeklyRanges(locale)
        AnalyticsGranularity.Monthly -> buildMonthlyRanges(locale)
        AnalyticsGranularity.Yearly -> buildYearlyRanges(locale)
    }
    return raw.filter { it.end >= minEnd }
}

private fun buildDailyRanges(locale: Locale): List<TimeRange> {
    val fmt = SimpleDateFormat("EEE d MMM", locale)
    val out = ArrayList<TimeRange>(14)
    for (i in 13 downTo 0) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = System.currentTimeMillis()
        cal.add(Calendar.DAY_OF_MONTH, -i)
        val start = startOfDay(cal.timeInMillis)
        val end = endOfDay(cal.timeInMillis)
        out.add(TimeRange(fmt.format(start), start, end))
    }
    return out
}

private fun buildWeeklyRanges(locale: Locale): List<TimeRange> {
    val fmt = SimpleDateFormat("d MMM", locale)
    val out = ArrayList<TimeRange>(8)
    val monday = mondayOfWeekContaining(System.currentTimeMillis())
    for (w in 7 downTo 0) {
        val startCal = (monday.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, -7 * w) }
        val endCal = (startCal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, 6)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val start = startCal.timeInMillis
        val end = endCal.timeInMillis
        val label = fmt.format(start)
        out.add(TimeRange(label, start, end))
    }
    return out
}

private fun buildMonthlyRanges(locale: Locale): List<TimeRange> {
    val fmt = SimpleDateFormat("MMMM yyyy", locale)
    val out = ArrayList<TimeRange>(12)
    for (m in 11 downTo 0) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = System.currentTimeMillis()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.MONTH, -m)
        val start = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis
        out.add(TimeRange(fmt.format(start), start, end))
    }
    return out
}

private fun buildYearlyRanges(locale: Locale): List<TimeRange> {
    val fmt = SimpleDateFormat("yyyy", locale)
    val minYear = 2025
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = ArrayList<Int>(5)
    var y = currentYear
    while (years.size < 5 && y >= minYear) {
        years.add(y)
        y--
    }
    years.sort()
    return years.map { year ->
        val startCal = Calendar.getInstance().apply {
            set(year, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = Calendar.getInstance().apply {
            set(year, Calendar.DECEMBER, 31, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }
        TimeRange(fmt.format(startCal.timeInMillis), startCal.timeInMillis, endCal.timeInMillis)
    }
}

private fun startOfDay(timeInMillis: Long): Long {
    val c = Calendar.getInstance()
    c.timeInMillis = timeInMillis
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

private fun endOfDay(timeInMillis: Long): Long {
    val c = Calendar.getInstance()
    c.timeInMillis = timeInMillis
    c.set(Calendar.HOUR_OF_DAY, 23)
    c.set(Calendar.MINUTE, 59)
    c.set(Calendar.SECOND, 59)
    c.set(Calendar.MILLISECOND, 999)
    return c.timeInMillis
}

private fun mondayOfWeekContaining(timeInMillis: Long): Calendar {
    val c = Calendar.getInstance()
    c.firstDayOfWeek = Calendar.MONDAY
    c.timeInMillis = timeInMillis
    val dow = c.get(Calendar.DAY_OF_WEEK)
    val diff = (dow - Calendar.MONDAY + 7) % 7
    c.add(Calendar.DAY_OF_MONTH, -diff)
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c
}
