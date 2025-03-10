package com.seba.malosh.fragments.registromalosh

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.seba.malosh.R
import java.util.Calendar

class SeleccionarHabitosFragment : Fragment() {

    private lateinit var siguienteButton: Button
    private lateinit var checkboxes: List<CheckBox>
    private var registeredHabits = ArrayList<String>() // Hábitos ya registrados
    private var maxHabitosPermitidos = 2 // Máximo permitido de hábitos adicionales

    companion object {
        private const val REGISTERED_HABITS_KEY = "registered_habits"
        private const val MAX_HABITS = 4
        private const val MIN_HABITS = 2
        private const val MAX_HABITOS_PERMITIDOS_KEY = "max_habitos_permitidos"

        // Método estático para crear una nueva instancia del fragmento, con los hábitos registrados pasados como argumento
        fun newInstance(registeredHabits: ArrayList<String>, maxHabitosPermitidos: Int): SeleccionarHabitosFragment {
            val fragment = SeleccionarHabitosFragment()
            val bundle = Bundle()
            bundle.putStringArrayList(REGISTERED_HABITS_KEY, registeredHabits)
            bundle.putInt(MAX_HABITOS_PERMITIDOS_KEY, maxHabitosPermitidos)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_seleccionar_habitos, container, false)

        siguienteButton = view.findViewById(R.id.siguienteButton)

        checkboxes = listOf(
            view.findViewById(R.id.checkbox_fumar),
            view.findViewById(R.id.checkbox_alcohol),
            view.findViewById(R.id.checkbox_mala_higiene),
            view.findViewById(R.id.checkbox_cafeina),
            view.findViewById(R.id.checkbox_interrumpir),
            view.findViewById(R.id.checkbox_dormir),
            view.findViewById(R.id.checkbox_comer),
            view.findViewById(R.id.checkbox_no_beber_agua),
            view.findViewById(R.id.checkbox_mala_alimentacion),
            view.findViewById(R.id.checkbox_poco_ejercicio)
        )


        registeredHabits = arguments?.getStringArrayList(REGISTERED_HABITS_KEY) ?: ArrayList()
        maxHabitosPermitidos = arguments?.getInt(MAX_HABITOS_PERMITIDOS_KEY, 2) ?: 2

        checkboxes.forEach { checkbox ->
            if (registeredHabits.contains(checkbox.text.toString())) {
                checkbox.isEnabled = false
            }
        }

        siguienteButton.setOnClickListener {
            val selectedHabits = checkboxes.filter { it.isChecked }.map { it.text.toString() }

            when {
                registeredHabits.size >= MAX_HABITS -> {
                    Toast.makeText(context, "Ya has registrado el máximo de hábitos permitidos.", Toast.LENGTH_SHORT).show()
                    siguienteButton.isEnabled = false
                }
                selectedHabits.size + registeredHabits.size < MIN_HABITS -> {
                    Toast.makeText(context, "Selecciona al menos 2 hábitos", Toast.LENGTH_SHORT).show()
                }
                selectedHabits.size + registeredHabits.size > MAX_HABITS -> {
                    Toast.makeText(context, "No puedes registrar más de 4 hábitos.", Toast.LENGTH_SHORT).show()
                }
                selectedHabits.size > maxHabitosPermitidos -> {
                    Toast.makeText(context, "Solo puedes registrar $maxHabitosPermitidos hábitos más.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val fechaInicio = Calendar.getInstance()

                    val confirmarFragment = ConfirmarHabitosFragment.newInstance(ArrayList(selectedHabits), fechaInicio)
                    fragmentManager?.beginTransaction()
                        ?.replace(R.id.fragment_container, confirmarFragment)
                        ?.addToBackStack(null)
                        ?.commit()
                }
            }
        }

        return view
    }
}
