package com.atilsamancioglu.yemekkitabi9.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.atilsamancioglu.yemekkitabi9.model.Tarif
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable


@Dao
interface TarifDAO {
    //Burada RxJava nın Flowable ve Completable yapılarını kullanacağız.
    //Flowable : yapacağımız işlem sonucu veri dönecekse kullanırız.
    //Completable : yapacağımız işlem sonucunda bir şey dönmeyecekse kullanırız. Örn: insert etmek, delete etmek

    @Query("SELECT * FROM Tarif")
    fun getAll() : Flowable<List<Tarif>>

    @Query("SELECT * FROM Tarif WHERE id = :id")
    fun findById(id : Int) : Flowable<Tarif>

    @Insert
    fun insert(tarif: Tarif) : Completable

    @Delete
    fun delete(tarif: Tarif) : Completable

}