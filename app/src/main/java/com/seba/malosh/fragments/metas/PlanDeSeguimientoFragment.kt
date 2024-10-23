package com.seba.malosh.fragments.metas

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.seba.malosh.R
import java.util.Calendar

class PlanDeSeguimientoFragment : Fragment() {

    private lateinit var volverButton: Button
    private lateinit var definirButton: Button
    private lateinit var fechaInicio1Button: Button
    private lateinit var fechaFin1Button: Button
    private lateinit var fechaInicio1TextView: TextView
    private lateinit var fechaFin1TextView: TextView
    private var fechaInicio1: Long = 0
    private var fechaFin1: Long = 0
    private lateinit var fechaInicio2Button: Button
    private lateinit var fechaFin2Button: Button
    private lateinit var fechaInicio2TextView: TextView
    private lateinit var fechaFin2TextView: TextView
    private var fechaInicio2: Long = 0
    private var fechaFin2: Long = 0
    private var selectedHabits: ArrayList<String> = arrayListOf()

    companion object {
        private const val SELECTED_HABITS_KEY = "selected_habits"

        fun newInstance(selectedHabits: ArrayList<String>): PlanDeSeguimientoFragment {
            val fragment = PlanDeSeguimientoFragment()
            val bundle = Bundle()
            bundle.putStringArrayList(SELECTED_HABITS_KEY, selectedHabits)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_plan_de_seguimiento, container, false)

        volverButton = view.findViewById(R.id.volverButton)
        definirButton = view.findViewById(R.id.definirButton)
        fechaInicio1Button = view.findViewById(R.id.fechaInicio1Button)
        fechaFin1Button = view.findViewById(R.id.fechaFin1Button)
        fechaInicio1TextView = view.findViewById(R.id.fechaInicio1TextView)
        fechaFin1TextView = view.findViewById(R.id.fechaFin1TextView)
        fechaInicio2Button = view.findViewById(R.id.fechaInicio2Button)
        fechaFin2Button = view.findViewById(R.id.fechaFin2Button)
        fechaInicio2TextView = view.findViewById(R.id.fechaInicio2TextView)
        fechaFin2TextView = view.findViewById(R.id.fechaFin2TextView)

        selectedHabits = arguments?.getStringArrayList(SELECTED_HABITS_KEY) ?: arrayListOf()

        if (selectedHabits.isNotEmpty()) {
            view.findViewById<TextView>(R.id.habito1TextView).text = selectedHabits[0]

            if (selectedHabits.size > 1) {
                view.findViewById<TextView>(R.id.habito2TextView).text = selectedHabits[1]
            } else {
                view.findViewById<TextView>(R.id.habito2TextView).visibility = View.GONE
                fechaInicio2Button.visibility = View.GONE
                fechaFin2Button.visibility = View.GONE
                fechaInicio2TextView.visibility = View.GONE
                fechaFin2TextView.visibility = View.GONE
            }
        }

        setDatePicker(fechaInicio1Button, fechaInicio1TextView) { fechaInicio1 = it }
        setDatePicker(fechaFin1Button, fechaFin1TextView) { fechaFin1 = it }
        setDatePicker(fechaInicio2Button, fechaInicio2TextView) { fechaInicio2 = it }
        setDatePicker(fechaFin2Button, fechaFin2TextView) { fechaFin2 = it }

        definirButton.setOnClickListener {
            if (validateDates()) {
                val habitosConFechas = arrayListOf<Pair<String, Pair<String, String>>>()
                habitosConFechas.add(
                    Pair(
                        view.findViewById<TextView>(R.id.habito1TextView).text.toString(),
                        Pair(fechaInicio1TextView.text.toString(), fechaFin1TextView.text.toString())
                    )
                )
                if (selectedHabits.size > 1) {
                    habitosConFechas.add(
                        Pair(
                            view.findViewById<TextView>(R.id.habito2TextView).text.toString(),
                            Pair(fechaInicio2TextView.text.toString(), fechaFin2TextView.text.toString())
                        )
                    )
                }
                val resumenFragment = ResumenFragment.newInstance(habitosConFechas)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, resumenFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        volverButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }

    private fun setDatePicker(button: Button, textView: TextView, onDateSet: (Long) -> Unit) {
        button.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
                onDateSet(selectedCalendar.timeInMillis)
                textView.text = getString(R.string.fecha_seleccionada, selectedDay, selectedMonth + 1, selectedYear)
            }, year, month, day)

            datePicker.datePicker.minDate = calendar.timeInMillis
            datePicker.show()
        }
    }

    private fun validateDates(): Boolean {
        return when {
            fechaInicio1 == 0L || fechaFin1 == 0L -> {
                Toast.makeText(context, "Selecciona fechas para el hábito 1.", Toast.LENGTH_SHORT).show()
                false
            }
            fechaInicio1 >= fechaFin1 -> {
                Toast.makeText(context, "La fecha de fin del hábito 1 debe ser posterior a la fecha de inicio.", Toast.LENGTH_SHORT).show()
                false
            }
            selectedHabits.size > 1 && (fechaInicio2 == 0L || fechaFin2 == 0L) -> {
                Toast.makeText(context, "Selecciona fechas para el hábito 2.", Toast.LENGTH_SHORT).show()
                false
            }
            selectedHabits.size > 1 && fechaInicio2 >= fechaFin2 -> {
                Toast.makeText(context, "La fecha de fin del hábito 2 debe ser posterior a la fecha de inicio.", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }
}
