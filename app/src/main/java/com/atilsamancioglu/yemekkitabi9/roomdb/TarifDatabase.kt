package com.atilsamancioglu.yemekkitabi9.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.atilsamancioglu.yemekkitabi9.model.Tarif

//entities olma nedeni birden fazla entity miz olabileceğinden ötürüdür.(birden fazla tablomuz tek veritabanında olabilir.)
@Database(entities = [Tarif::class], version = 1)
abstract class TarifDatabase : RoomDatabase() {
    abstract fun tarifDao(): TarifDAO
}