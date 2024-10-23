package com.seba.malosh.fragments.registromalosh

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.seba.malosh.R
import com.seba.malosh.activities.BienvenidaActivity
import com.seba.malosh.fragments.progresos.logros.listaLogros
import java.util.Calendar

class ConfirmarHabitosFragment : Fragment() {

    private lateinit var siButton: Button
    private lateinit var noButton: Button
    private lateinit var selectedHabitsTextView: TextView
    private var fechaInicio: Calendar? = null

    companion object {
        private const val SELECTED_HABITS_KEY = "selected_habits"


        fun newInstance(
            selectedHabits: ArrayList<String>,
            fechaInicio: Calendar?
        ): ConfirmarHabitosFragment {
            val fragment = ConfirmarHabitosFragment()
            val bundle = Bundle()
            bundle.putStringArrayList(SELECTED_HABITS_KEY, selectedHabits)
            fragment.arguments = bundle
            fragment.fechaInicio = fechaInicio
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_confirmar_habitos, container, false)


        siButton = view.findViewById(R.id.siButton)
        noButton = view.findViewById(R.id.noButton)
        selectedHabitsTextView = view.findViewById(R.id.selectedHabitsTextView)


        val selectedHabits = arguments?.getStringArrayList(SELECTED_HABITS_KEY)
        selectedHabitsTextView.text = selectedHabits?.joinToString(separator = "\n")


        siButton.setOnClickListener {
            val habitCount = selectedHabits?.size ?: 0

            if (habitCount in 2..4) {
                Toast.makeText(
                    context,
                    "Malos hábitos registrados exitosamente",
                    Toast.LENGTH_SHORT
                ).show()


                (requireActivity() as BienvenidaActivity).updateRegisteredHabits(
                    selectedHabits ?: emptyList(), fechaInicio
                )


                verificarYDesbloquearLogros(selectedHabits ?: emptyList())


                requireActivity().supportFragmentManager.popBackStack(null, 1)
            } else {
                Toast.makeText(
                    context,
                    "Debes registrar entre 2 y 4 malos hábitos.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        noButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }


    private fun verificarYDesbloquearLogros(nuevosHabitos: List<String>) {
        val sharedPreferences =
            requireContext().getSharedPreferences("LogrosPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()


        val habitosRegistrados =
            sharedPreferences.getStringSet("habitos_registrados", mutableSetOf())?.toMutableSet()
                ?: mutableSetOf()


        val habitosTotalesRegistrados = habitosRegistrados.union(nuevosHabitos).toMutableSet()


        editor.putStringSet("habitos_registrados", habitosTotalesRegistrados)


        val totalHabitosSesionActual = nuevosHabitos.size


        if (totalHabitosSesionActual >= 2) {
            val logro = listaLogros.find { it.id == 2 }
            if (logro != null && !logro.desbloqueado) {
                logro.desbloqueado = true
                editor.putBoolean("logro_2", true)
                Toast.makeText(context, "¡Logro Desbloqueado: ${logro.titulo}!", Toast.LENGTH_SHORT)
                    .show()
            }
        }


        if (totalHabitosSesionActual == 4) {
            val logro = listaLogros.find { it.id == 4 }
            if (logro != null && !logro.desbloqueado) {
                logro.desbloqueado = true
                editor.putBoolean("logro_4", true)
                Toast.makeText(context, "¡Logro Desbloqueado: ${logro.titulo}!", Toast.LENGTH_SHORT)
                    .show()
            }
        }


        editor.apply()
    }
}
