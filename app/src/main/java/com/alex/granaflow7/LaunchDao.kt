package com.alex.granaflow7

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface LaunchDao {

    @Insert
    fun insert(launch: LaunchEntity)

    @Update
    fun update(launch: LaunchEntity)

    @Delete
    fun delete(launch: LaunchEntity)

    // Listagens
    @Query("SELECT * FROM launches ORDER BY id DESC")
    fun getAll(): List<LaunchEntity>

    @Query("SELECT * FROM launches WHERE isIncome = 0 ORDER BY id DESC")
    fun getExpenses(): List<LaunchEntity>

    @Query("SELECT * FROM launches WHERE isIncome = 1 ORDER BY id DESC")
    fun getIncomes(): List<LaunchEntity>

    @Query("SELECT * FROM launches WHERE isIncome = 0 AND isPaid = 0 ORDER BY id DESC")
    fun getUnpaidExpenses(): List<LaunchEntity>

    // Totais antigos
    @Query("SELECT SUM(CASE WHEN isIncome = 1 THEN amount ELSE 0 END) FROM launches")
    fun totalIncome(): Double?

    @Query("SELECT SUM(CASE WHEN isIncome = 0 THEN amount ELSE 0 END) FROM launches")
    fun totalExpense(): Double?

    // Calculo tela inicial

    // 1. Falta pagar = despesas não pagas
    @Query("SELECT SUM(amount) FROM launches WHERE isIncome = 0 AND isPaid = 0")
    fun totalUnpaidExpenses(): Double?

    // 2. Total a ser pago = todas as despesas
    @Query("SELECT SUM(amount) FROM launches WHERE isIncome = 0")
    fun totalAllExpenses(): Double?

    // 3. Total a ser recebido = receitas ainda não recebidas
    @Query("SELECT SUM(amount) FROM launches WHERE isIncome = 1 AND isPaid = 0")
    fun totalPendingIncomes(): Double?

    // 4. Total mês = tudo que já foi recebido
    @Query("SELECT SUM(amount) FROM launches WHERE isIncome = 1 AND isPaid = 1")
    fun totalReceivedIncomes(): Double?

    // 5. Despesas pagas = recebido - gasto
    @Query("SELECT SUM(amount) FROM launches WHERE isIncome = 0 AND isPaid = 1")
    fun totalPaidExpenses(): Double?
}
