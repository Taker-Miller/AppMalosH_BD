package com.seba.malosh.fragments.metas

import android.Manifest
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
import com.seba.malosh.fragments.progresos.logros.Logro
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.text.SimpleDateFormat
import java.util.Locale

class ResumenFragment : Fragment() {

    private lateinit var volverButton: Button
    private lateinit var comenzarPlanButton: Button
    private lateinit var habitoSeleccionadoTextView: TextView
    private var habitosConFechas: ArrayList<Pair<String, Pair<String, String>>>? = null

    companion object {
        private const val HABITOS_KEY = "habitos"

        fun newInstance(
            habitosConFechas: ArrayList<Pair<String, Pair<String, String>>>
        ): ResumenFragment {
            val fragment = ResumenFragment()
            val bundle = Bundle()
            bundle.putSerializable(HABITOS_KEY, habitosConFechas)
            fragment.arguments = bundle
            return fragment
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_resumen, container, false)

        volverButton = view.findViewById(R.id.volverButton)
        comenzarPlanButton = view.findViewById(R.id.comenzarPlanButton)
        habitoSeleccionadoTextView = view.findViewById(R.id.habitoSeleccionado)

        habitosConFechas = arguments?.getSerializable(HABITOS_KEY) as? ArrayList<Pair<String, Pair<String, String>>>

        val detalles = habitosConFechas?.joinToString(separator = "\n") { habitoConFechas ->
            val habito = habitoConFechas.first
            val fechaInicio = habitoConFechas.second.first
            val fechaFin = habitoConFechas.second.second
            "$habito: $fechaInicio - $fechaFin"
        }
        habitoSeleccionadoTextView.text = detalles

        volverButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        comenzarPlanButton.setOnClickListener {
            iniciarNuevoPlan(habitosConFechas)
            verificarDesbloqueoLogros()

            (activity as? BienvenidaActivity)?.comenzarPlan()
            Toast.makeText(context, "¡El plan ha comenzado!", Toast.LENGTH_SHORT).show()

            requireActivity().supportFragmentManager.popBackStack(null, 1)
        }

        return view
    }

    private fun iniciarNuevoPlan(habitosConFechas: ArrayList<Pair<String, Pair<String, String>>>?) {
        val sharedPreferences = requireContext().getSharedPreferences("MetaPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        limpiarDatosPrevios(editor)

        habitosConFechas?.forEach { (habito, fechas) ->
            val fechaInicioLong = dateFormat.parse(fechas.first)?.time ?: 0L
            val fechaFinLong = dateFormat.parse(fechas.second)?.time ?: 0L

            if (fechaInicioLong in 1..<fechaFinLong) {
                editor.putBoolean("plan_iniciado", true)
                editor.putBoolean("meta_en_progreso", true)
                editor.putLong("fecha_inicio_meta_$habito", fechaInicioLong)
                editor.putLong("fecha_fin_meta_$habito", fechaFinLong)
            } else {
                Toast.makeText(
                    context,
                    "Error al guardar las fechas del plan para el hábito $habito.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        editor.apply()
    }

    private fun limpiarDatosPrevios(editor: SharedPreferences.Editor) {
        editor.clear()
        editor.apply()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun verificarDesbloqueoLogros() {
        val sharedPreferences =
            requireContext().getSharedPreferences("LogrosPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val logroPrimerMeta = listaLogros.firstOrNull { it.id == 1 }
        if (logroPrimerMeta != null && !logroPrimerMeta.desbloqueado) {
            logroPrimerMeta.desbloqueado = true
            editor.putBoolean("logro_${logroPrimerMeta.id}", true)
            mostrarNotificacionLogro(logroPrimerMeta)
        }

        editor.apply()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun mostrarNotificacionLogro(logro: Logro) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "logros_channel",
                "Logros Desbloqueados",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificación de logros desbloqueados"
            }
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(requireContext(), "logros_channel")
            .setSmallIcon(R.drawable.ic_desbloqueado)
            .setContentTitle("¡Logro Desbloqueado!")
            .setContentText(logro.titulo)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val requestCodeNotification = 0
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                requestCodeNotification
            )
            return
        }

        NotificationManagerCompat.from(requireContext()).notify(logro.id, builder.build())
    }
}
