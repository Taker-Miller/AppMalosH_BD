package com.seba.malosh.fragments.desafios

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.seba.malosh.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class DesafiosDiariosFragment : Fragment() {

    private lateinit var contenedorDesafios: LinearLayout
    private lateinit var aceptarDesafioButton: Button
    private lateinit var cancelarDesafioButton: Button
    private lateinit var desafioDescripcion: TextView
    private lateinit var inicioCheckBox: CheckBox
    private lateinit var enProgresoCheckBox: CheckBox
    private lateinit var casiPorTerminarCheckBox: CheckBox
    private lateinit var completadoCheckBox: CheckBox
    private val desafiosList = mutableListOf<String>()
    private var currentDesafio: String? = null
    private var desafioEnProgreso = false
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var registeredHabits: ArrayList<String>

    companion object {
        private const val HABITOS_KEY = "habitos_registrados"
        private const val TEMPORIZADOR_INICIO_KEY = "temporizador_inicio"
        private const val TEMPORIZADOR_DURACION = 60000L
        private const val TEMPORIZADOR_ESPERA = 20000L

        fun newInstance(habits: ArrayList<String>): DesafiosDiariosFragment {
            val fragment = DesafiosDiariosFragment()
            val bundle = Bundle()
            bundle.putStringArrayList(HABITOS_KEY, habits)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_desafios_diarios, container, false)

        contenedorDesafios = view.findViewById(R.id.contenedorDesafios)
        aceptarDesafioButton = view.findViewById(R.id.aceptarDesafioButton)
        cancelarDesafioButton = view.findViewById(R.id.cancelarDesafioButton)
        desafioDescripcion = view.findViewById(R.id.desafioDescripcion)
        inicioCheckBox = view.findViewById(R.id.inicioCheckBox)
        enProgresoCheckBox = view.findViewById(R.id.enProgresoCheckBox)
        casiPorTerminarCheckBox = view.findViewById(R.id.casiPorTerminarCheckBox)
        completadoCheckBox = view.findViewById(R.id.completadoCheckBox)

        registeredHabits = arguments?.getStringArrayList(HABITOS_KEY) ?: arrayListOf()

        actualizarCheckBoxesRestaurados()

        val sharedPreferences = requireContext().getSharedPreferences("temporizador_prefs", Context.MODE_PRIVATE)
        inicioCheckBox.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("inicio_check", isChecked).apply()
        }
        enProgresoCheckBox.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("en_progreso_check", isChecked).apply()
        }
        casiPorTerminarCheckBox.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("casi_terminado_check", isChecked).apply()
        }
        completadoCheckBox.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("completado_check", isChecked).apply()
        }

        val inicioTemporizador = sharedPreferences.getLong(TEMPORIZADOR_INICIO_KEY, 0L)
        val temporizadorActivo = sharedPreferences.getBoolean("temporizador_activo", false)
        val tiempoRestante = sharedPreferences.getLong("tiempo_restante", TEMPORIZADOR_ESPERA)

        currentDesafio = obtenerDesafioEnProgreso(requireContext())

        if (temporizadorActivo && tiempoRestante > 0L) {
            iniciarTemporizadorRestaurado(tiempoRestante)
        } else if (inicioTemporizador > 0L && currentDesafio != null) {
            reanudarTemporizador(inicioTemporizador)
            mostrarDesafioEnProgreso()
        } else {
            generarDesafiosSiEsNecesario()
        }

        aceptarDesafioButton.setOnClickListener { aceptarDesafio() }
        cancelarDesafioButton.setOnClickListener { cancelarDesafio() }

        return view
    }

    private fun iniciarTemporizadorRestaurado(tiempoRestante: Long) {
        val sharedPreferences = requireContext().getSharedPreferences("temporizador_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        desafioDescripcion.text = getString(R.string.tiempo_restante, TimeUnit.MILLISECONDS.toSeconds(tiempoRestante))
        aceptarDesafioButton.isEnabled = false

        val runnable = object : Runnable {
            var tiempoActualRestante = tiempoRestante
            override fun run() {
                if (tiempoActualRestante > 0) {
                    tiempoActualRestante -= 1000
                    desafioDescripcion.text = getString(
                        R.string.tiempo_restante,
                        TimeUnit.MILLISECONDS.toSeconds(tiempoActualRestante)
                    )
                    handler.postDelayed(this, 1000)
                    editor.putLong("tiempo_restante", tiempoActualRestante).apply()
                } else {
                    desafioDescripcion.text = getString(R.string.nuevo_desafio)
                    editor.putBoolean("temporizador_activo", false).remove("tiempo_restante").apply()
                    limpiarDesafioAnterior()
                    generarDesafios(registeredHabits)
                    mostrarDesafio()
                    aceptarDesafioButton.visibility = View.VISIBLE
                    cancelarDesafioButton.visibility = View.GONE
                    aceptarDesafioButton.isEnabled = true
                    sharedPreferences.edit().remove(TEMPORIZADOR_INICIO_KEY).apply()
                }
            }
        }
        handler.post(runnable)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        actualizarCheckBoxesRestaurados()
    }

    private fun generarDesafiosSiEsNecesario() {
        val desafioGuardado = obtenerDesafioEnProgreso(requireContext())
        if (desafioGuardado != null) {
            currentDesafio = desafioGuardado
            mostrarDesafioEnProgreso()
        } else {
            generarDesafios(registeredHabits)
            mostrarDesafio()
        }
    }

    private fun iniciarTemporizador1Minuto() {
        val sharedPreferences = requireContext().getSharedPreferences("temporizador_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val tiempoInicio = System.currentTimeMillis()
        editor.putLong(TEMPORIZADOR_INICIO_KEY, tiempoInicio).apply()

        setCheckBoxesVisibility(View.VISIBLE)
        resetCheckBoxes()
        reanudarTemporizador(tiempoInicio)
    }

    private fun reanudarTemporizador(tiempoInicio: Long) {
        val tiempoActual = System.currentTimeMillis()
        val tiempoRestante = TEMPORIZADOR_DURACION - (tiempoActual - tiempoInicio)

        if (tiempoRestante > 0) {
            aceptarDesafioButton.visibility = View.GONE
            cancelarDesafioButton.visibility = View.VISIBLE
            desafioDescripcion.text = getString(
                R.string.tiempo_restante_desafio,
                TimeUnit.MILLISECONDS.toSeconds(tiempoRestante)
            )
            setCheckBoxesVisibility(View.VISIBLE)

            handler.postDelayed(object : Runnable {
                var tiempoRestanteActualizado = tiempoRestante

                override fun run() {
                    if (tiempoRestanteActualizado > 0) {
                        tiempoRestanteActualizado -= 1000
                        desafioDescripcion.text = getString(
                            R.string.tiempo_restante_desafio,
                            TimeUnit.MILLISECONDS.toSeconds(tiempoRestanteActualizado)
                        )
                        actualizarCheckBoxes(tiempoRestanteActualizado)
                        handler.postDelayed(this, 1000)
                    } else {
                        validarDesafioCompletado()
                    }
                }
            }, 1000)
        } else {
            validarDesafioCompletado()
        }
    }

    private fun actualizarCheckBoxes(tiempoRestante: Long) {
        val porcentajeRestante = 100 - ((tiempoRestante.toDouble() / TEMPORIZADOR_DURACION) * 100).toInt()
        val sharedPreferences = requireContext().getSharedPreferences("temporizador_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        if (porcentajeRestante >= 25) {
            inicioCheckBox.isEnabled = true
            editor.putBoolean("inicio_check", inicioCheckBox.isChecked)
        }

        if (porcentajeRestante >= 50) {
            enProgresoCheckBox.isEnabled = true
            editor.putBoolean("en_progreso_check", enProgresoCheckBox.isChecked)
        }

        if (porcentajeRestante >= 75) {
            casiPorTerminarCheckBox.isEnabled = true
            editor.putBoolean("casi_terminado_check", casiPorTerminarCheckBox.isChecked)
        }

        if (porcentajeRestante >= 90) {
            completadoCheckBox.isEnabled = true
            editor.putBoolean("completado_check", completadoCheckBox.isChecked)
        }
        editor.apply()
    }

    private fun resetCheckBoxes() {
        inicioCheckBox.isChecked = false
        enProgresoCheckBox.isChecked = false
        casiPorTerminarCheckBox.isChecked = false
        completadoCheckBox.isChecked = false
        inicioCheckBox.isEnabled = false
        enProgresoCheckBox.isEnabled = false
        casiPorTerminarCheckBox.isEnabled = false
        completadoCheckBox.isEnabled = false
    }

    private fun limpiarEstadoCheckBoxes() {
        val sharedPreferences = requireContext().getSharedPreferences("temporizador_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
    }

    private fun validarDesafioCompletado() {
        val sharedPreferences = requireContext().getSharedPreferences("DesafiosCompletados", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        if (inicioCheckBox.isChecked && enProgresoCheckBox.isChecked && casiPorTerminarCheckBox.isChecked && completadoCheckBox.isChecked) {
            Toast.makeText(context, getString(R.string.desafio_completado), Toast.LENGTH_SHORT).show()
            val contador = sharedPreferences.getInt("contador_desafios", 0)
            val formatter = SimpleDateFormat("EEEE, dd MMM yyyy", Locale("es", "ES"))
            val fechaActual = formatter.format(Date())
            editor.putString("fecha_$contador", fechaActual)
            editor.putString("desafio_$contador", currentDesafio)
            editor.putInt("contador_desafios", contador + 1).apply()
        } else {
            Toast.makeText(context, getString(R.string.desafio_fallido), Toast.LENGTH_SHORT).show()
        }

        setCheckBoxesVisibility(View.GONE)
        limpiarEstadoCheckBoxes()
        iniciarTemporizador20Segundos()
    }

    private fun iniciarTemporizador20Segundos() {
        var tiempoRestante = TEMPORIZADOR_ESPERA
        val sharedPreferences = requireContext().getSharedPreferences("temporizador_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val tiempoInicioGuardado = sharedPreferences.getLong("tiempo_restante", -1L)
        if (tiempoInicioGuardado > 0) {
            tiempoRestante = tiempoInicioGuardado
        }
        editor.putBoolean("temporizador_activo", true).apply()
        aceptarDesafioButton.isEnabled = false
        desafioDescripcion.text = getString(R.string.tiempo_restante, TimeUnit.MILLISECONDS.toSeconds(tiempoRestante))

        val runnable = object : Runnable {
            override fun run() {
                if (tiempoRestante > 0) {
                    tiempoRestante -= 1000
                    desafioDescripcion.text = getString(R.string.tiempo_restante, TimeUnit.MILLISECONDS.toSeconds(tiempoRestante))
                    handler.postDelayed(this, 1000)
                    editor.putLong("tiempo_restante", tiempoRestante).apply()
                } else {
                    desafioDescripcion.text = getString(R.string.nuevo_desafio)
                    editor.putBoolean("temporizador_activo", false).remove("tiempo_restante").apply()
                    limpiarDesafioAnterior()
                    generarDesafios(registeredHabits)
                    mostrarDesafio()
                    aceptarDesafioButton.visibility = View.VISIBLE
                    cancelarDesafioButton.visibility = View.GONE
                    aceptarDesafioButton.isEnabled = true
                    sharedPreferences.edit().remove(TEMPORIZADOR_INICIO_KEY).apply()
                }
            }
        }
        handler.post(runnable)
    }

    private fun limpiarDesafioAnterior() {
        desafioEnProgreso = false
        currentDesafio = null
        guardarDesafioEnProgreso(requireContext(), null, false)
    }

    private fun mostrarDesafioEnProgreso() {
        aceptarDesafioButton.isEnabled = false
        cancelarDesafioButton.visibility = View.VISIBLE
        contenedorDesafios.removeAllViews()
        val textView = TextView(context).apply {
            text = getString(R.string.desafio_en_progreso, currentDesafio)
            textSize = 18f
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        }
        contenedorDesafios.addView(textView)
        setCheckBoxesVisibility(View.VISIBLE)
        actualizarCheckBoxesRestaurados()
    }

    private fun cancelarDesafio() {
        handler.removeCallbacksAndMessages(null)
        guardarDesafioEnProgreso(requireContext(), null, false)
        val sharedPreferences = requireContext().getSharedPreferences("temporizador_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("temporizador_activo", true).apply()
        desafioEnProgreso = false
        currentDesafio = null
        setCheckBoxesVisibility(View.GONE)
        desafioDescripcion.text = getString(R.string.tiempo_restante_20s)
        limpiarEstadoCheckBoxes()
        iniciarTemporizador20Segundos()
    }

    private fun aceptarDesafio() {
        if (desafioEnProgreso) {
            Toast.makeText(context, getString(R.string.desafio_ya_en_progreso), Toast.LENGTH_SHORT).show()
        } else {
            desafioEnProgreso = true
            guardarDesafioEnProgreso(requireContext(), currentDesafio, true)
            Toast.makeText(context, getString(R.string.desafio_aceptado), Toast.LENGTH_SHORT).show()
            setCheckBoxesVisibility(View.VISIBLE)
            resetCheckBoxes()
            iniciarTemporizador1Minuto()
        }
    }

    private fun generarDesafios(habitos: List<String>) {
        desafiosList.clear()
        for (habito in habitos) {
            when (habito.lowercase().trim()) {
                "cafeína", "consumo de cafeína" -> desafiosList.addAll(
                    listOf(
                        getString(R.string.desafio_cafeina_1),
                        getString(R.string.desafio_cafeina_2),
                        getString(R.string.desafio_cafeina_3)
                    )
                )
                "dormir mal", "dormir a deshoras" -> desafiosList.addAll(
                    listOf(
                        getString(R.string.desafio_dormir_1),
                        getString(R.string.desafio_dormir_2),
                        getString(R.string.desafio_dormir_3),
                        getString(R.string.desafio_dormir_4),
                        getString(R.string.desafio_dormir_5),
                        getString(R.string.desafio_dormir_6)
                    )
                )
                "interrumpir a otros" -> desafiosList.addAll(
                    listOf(
                        getString(R.string.desafio_interrumpir_1),
                        getString(R.string.desafio_interrumpir_2),
                        getString(R.string.desafio_interrumpir_3)
                    )
                )
                "mala alimentación" -> desafiosList.addAll(
                    listOf(
                        getString(R.string.desafio_alimentacion_1),
                        getString(R.string.desafio_alimentacion_2),
                        getString(R.string.desafio_alimentacion_3),
                        getString(R.string.desafio_alimentacion_4)
                    )
                )
                "fumar" -> desafiosList.addAll(
                    listOf(
                        getString(R.string.desafio_fumar_1),
                        getString(R.string.desafio_fumar_2),
                        getString(R.string.desafio_fumar_3),
                        getString(R.string.desafio_fumar_4)
                    )
                )
                "alcohol" -> desafiosList.addAll(
                    listOf(
                        getString(R.string.desafio_alcohol_1),
                        getString(R.string.desafio_alcohol_2),
                        getString(R.string.desafio_alcohol_3),
                        getString(R.string.desafio_alcohol_4)
                    )
                )
                "poco ejercicio" -> desafiosList.addAll(
                    listOf(
                        getString(R.string.desafio_ejercicio_1),
                        getString(R.string.desafio_ejercicio_2),
                        getString(R.string.desafio_ejercicio_3),
                        getString(R.string.desafio_ejercicio_4)
                    )
                )
                "comer a deshoras" -> desafiosList.addAll(
                    listOf(
                        getString(R.string.desafio_comer_1),
                        getString(R.string.desafio_comer_2),
                        getString(R.string.desafio_comer_3),
                        getString(R.string.desafio_comer_4)
                    )
                )
                "mala higiene" -> desafiosList.addAll(
                    listOf(
                        getString(R.string.desafio_higiene_1),
                        getString(R.string.desafio_higiene_2),
                        getString(R.string.desafio_higiene_3),
                        getString(R.string.desafio_higiene_4)
                    )
                )
                else -> {
                    Toast.makeText(context, getString(R.string.sin_desafio, habito), Toast.LENGTH_SHORT).show()
                }
            }
        }
        desafiosList.shuffle()
        mostrarDesafio()
    }

    private fun mostrarDesafio() {
        if (desafiosList.isNotEmpty()) {
            currentDesafio = desafiosList.first()
            contenedorDesafios.removeAllViews()
            val textView = TextView(context).apply {
                text = currentDesafio
                textSize = 18f
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            }
            contenedorDesafios.addView(textView)
            aceptarDesafioButton.visibility = View.VISIBLE
            aceptarDesafioButton.isEnabled = true
        } else {
            Toast.makeText(context, getString(R.string.no_desafios), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setCheckBoxesVisibility(visibility: Int) {
        val progresoChecklist = view?.findViewById<LinearLayout>(R.id.progresoChecklist)
        progresoChecklist?.visibility = visibility
    }

    private fun actualizarCheckBoxesRestaurados() {
        val sharedPreferences = requireContext().getSharedPreferences("temporizador_prefs", Context.MODE_PRIVATE)
        inicioCheckBox.isChecked = sharedPreferences.getBoolean("inicio_check", false)
        enProgresoCheckBox.isChecked = sharedPreferences.getBoolean("en_progreso_check", false)
        casiPorTerminarCheckBox.isChecked = sharedPreferences.getBoolean("casi_terminado_check", false)
        completadoCheckBox.isChecked = sharedPreferences.getBoolean("completado_check", false)
        setCheckBoxesVisibility(View.VISIBLE)
    }

    private fun guardarDesafioEnProgreso(context: Context, desafio: String?, enProgreso: Boolean) {
        val sharedPreferences = context.getSharedPreferences("desafio_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        if (enProgreso) {
            editor.putString("desafio_actual", desafio)
            editor.putBoolean("en_progreso", true)
        } else {
            editor.remove("desafio_actual")
            editor.putBoolean("en_progreso", false)
        }
        editor.apply()
    }

    private fun obtenerDesafioEnProgreso(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("desafio_prefs", Context.MODE_PRIVATE)
        return if (sharedPreferences.getBoolean("en_progreso", false)) {
            sharedPreferences.getString("desafio_actual", null)
        } else {
            null
        }
    }
}
