package com.atilsamancioglu.yemekkitabi9.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.atilsamancioglu.yemekkitabi9.databinding.RecyclerRowBinding
import com.atilsamancioglu.yemekkitabi9.model.Tarif
import com.atilsamancioglu.yemekkitabi9.view.ListeFragmentDirections

class TarifAdapter(val tarifListesi: List<Tarif>) : RecyclerView.Adapter<TarifAdapter.TarifHolder>() {

    class TarifHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TarifHolder { //tarif holder ı oluşturmak için
        val recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return TarifHolder(recyclerRowBinding)
    }

    override fun getItemCount(): Int {  //item sayısı
        return tarifListesi.size
    }

    override fun onBindViewHolder(holder: TarifHolder, position: Int) { //yapacağımız işlemi belirlediğimiz kısım
        holder.binding.recyclerViewTextView.text = tarifListesi[position].isim
        holder.itemView.setOnClickListener {
            val action = ListeFragmentDirections.actionListeFragmentToTarifFragment(bilgi = "eski", id= tarifListesi[position].id)
            Navigation.findNavController(it).navigate(action)
        }
    }

}