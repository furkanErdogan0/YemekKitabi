package com.atilsamancioglu.yemekkitabi9.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.atilsamancioglu.yemekkitabi9.R

class MainActivity : AppCompatActivity() {
    //build.gradle.kts(YemekKitabi9)  a eklediğimiz eklentileri build.gradle.kts(:app) te uygulamamız gerekir.
    //kapt = kotlin annotation processing tool
    //ksp kotlin symbol processing
    //modern android sisteminde kullanılan araçlardır. pluginlerimizi dependencieslere uygularken ortaya çıkacak sorunları ele alan yapılardır.
    //ksp daha yenidir.


    //Room bir ORM (Object Relational Mapping) dir. ORM ilişkisel veri tabanı ile uygulamamız arasında bir köprü görevi gören,
    // ilişkileri ve nesneleri yönetmek için kullanılan bir tekniktir.
    //SQLite, Android uygulamalarında veritabanı işlemleri yapmak için düşük seviyede bir araçtır.
    //Room, SQLite üzerinde çalışan bir veritabanı kütüphanesi olarak Android uygulama geliştiricilerine daha basit bir API sağlar.
    //Room, SQLite’i daha kolay ve güvenli bir şekilde kullanmanıza olanak tanır.


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}