package com.atilsamancioglu.yemekkitabi9.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

//Room 3 bileşenden oluşur:
//Room Database, DAO(Data Access Objects), Entities
//DAO = select, delete, insert vs. yapabildiğimiz erişim objesi
//Entities = create table if not exists falan filan, tablo oluşturma sütun belirleme, kolon oluşturma fln.


//data class veri saklama için oluşturulmuş bir sınıf çeşididir. internetten veri çekerken fln kullanılır.
//normal class tan farkı, primary constructor unun kesin olması gerekir.

@Entity
data class Tarif (

    @ColumnInfo("isim")
    var isim : String,

    @ColumnInfo("malzeme")
    var malzeme : String,

    @ColumnInfo("gorsel")
    var gorsel : ByteArray    //görseller veritabanında binary(0,1) olarak tutulur.

) {
    @PrimaryKey(autoGenerate = true) //otomatik id ataması yapar.
    var id = 0
}
