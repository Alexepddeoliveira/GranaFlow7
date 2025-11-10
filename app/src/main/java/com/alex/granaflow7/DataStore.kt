package com.alex.granaflow7

object DataStore {

    data class Launch(
        val title: String,
        val amount: Double,
        val isIncome: Boolean
    )

    val launches = mutableListOf<Launch>()

    fun addLaunch(launch: Launch) {
        launches.add(launch)
    }

    fun totalIncome(): Double {
        return launches.filter { it.isIncome }.sumOf { it.amount }
    }

    fun totalExpense(): Double {
        return launches.filter { !it.isIncome }.sumOf { it.amount }
    }

    fun balance(): Double {
        return totalIncome() - totalExpense()
    }
}
