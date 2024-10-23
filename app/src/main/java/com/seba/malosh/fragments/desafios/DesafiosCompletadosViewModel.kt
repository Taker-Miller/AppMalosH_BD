package com.seba.malosh.fragments.desafios

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DesafiosCompletadosViewModel : ViewModel() {


    private val _desafiosCompletados = MutableLiveData<List<String>>()
    val desafiosCompletados: LiveData<List<String>> get() = _desafiosCompletados


    fun cargarDesafiosCompletados(context: Context) {
        val sharedPreferences = context.getSharedPreferences("DesafiosCompletados", Context.MODE_PRIVATE)
        val desafiosCompletadosList = mutableListOf<String>()
        val contador = sharedPreferences.getInt("contador_desafios", 0)

        for (i in 0 until contador) {
            val dia = sharedPreferences.getString("fecha_$i", null)
            val desafio = sharedPreferences.getString("desafio_$i", null)
            if (dia != null && desafio != null) {
                desafiosCompletadosList.add("$dia: $desafio")
            }
        }

        if (desafiosCompletadosList.isEmpty()) {
            desafiosCompletadosList.add("Aún no has completado desafíos.")
        }


        _desafiosCompletados.value = desafiosCompletadosList
    }
}
