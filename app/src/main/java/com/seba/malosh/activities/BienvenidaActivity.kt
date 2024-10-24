package com.seba.malosh.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.seba.malosh.fragments.desafios.DesafiosDiariosFragment
import com.seba.malosh.fragments.metas.MetasFragment
import com.seba.malosh.fragments.progresos.metas.ProgresoFragment
import com.seba.malosh.fragments.reflexionesd.ReflexionesDiariasFragment
import com.seba.malosh.fragments.registromalosh.SeleccionarHabitosFragment
import com.seba.malosh.fragments.registromalosh.TusMalosHabitosFragment
import com.seba.malosh.R
import com.seba.malosh.fragments.perfil.PerfilFragment
import com.seba.malosh.receivers.PlanInicioReceiver
import java.util.*

class BienvenidaActivity : AppCompatActivity() {

    private val registeredHabits = ArrayList<String>()
    private var planEnProgreso = false

    private lateinit var logoImageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var notificationAdviceTextView: TextView

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val scheduleExactAlarmLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (isExactAlarmPermissionGranted()) {
            Toast.makeText(this, "Permiso de alarma exacta otorgado", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permiso de alarma exacta denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bienvenida)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Menú"

        logoImageView = findViewById(R.id.logoImageView)
        titleTextView = findViewById(R.id.titleTextView)
        descriptionTextView = findViewById(R.id.descriptionTextView)
        notificationAdviceTextView = findViewById(R.id.notificationAdviceTextView)

        cargarHabitosDesdeFirestore() // Cargar hábitos registrados desde Firestore al iniciar sesión

        supportFragmentManager.setFragmentResultListener("modificarPerfilRequestKey", this) { _, bundle ->
            val nombre = bundle.getString("nombre_modificado")
            val apellido = bundle.getString("apellido_modificado")
            val correo = bundle.getString("correo_modificado")

            titleTextView.text = getString(R.string.mensaje_bienvenida, nombre, apellido)
            descriptionTextView.text = getString(R.string.mensaje_correo, correo)
            Toast.makeText(this, "Perfil actualizado: $nombre $apellido, $correo", Toast.LENGTH_SHORT).show()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                mostrarElementosUI()
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else {
                    finish()
                }
            }
        })
    }

    private fun cargarHabitosDesdeFirestore() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("usuarios")
                .document(userId)
                .collection("habitos_registrados")
                .get()
                .addOnSuccessListener { documents ->
                    val habitosRegistrados = mutableListOf<String>()
                    for (document in documents) {
                        val habitos = document.get("habitos") as? List<String>
                        if (habitos != null) {
                            habitosRegistrados.addAll(habitos)
                        }
                    }
                    registeredHabits.clear()
                    registeredHabits.addAll(habitosRegistrados)
                    Toast.makeText(this, "Hábitos cargados: $registeredHabits", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al cargar hábitos: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_navegacion, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_registrar_habitos -> {
                if (registeredHabits.size >= 4) {
                    Toast.makeText(this, "Ya has registrado el máximo de hábitos permitidos", Toast.LENGTH_SHORT).show()
                } else {
                    ocultarElementosUI()
                    val maxHabitosPermitidos = 4 - registeredHabits.size
                    val fragment = SeleccionarHabitosFragment.newInstance(registeredHabits, maxHabitosPermitidos)
                    val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.fragment_container, fragment)
                    transaction.addToBackStack(null)
                    transaction.commit()
                }
                true
            }

            R.id.menu_item_perfil -> {
                ocultarElementosUI()
                val fragment = PerfilFragment()
                val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragment_container, fragment)
                transaction.addToBackStack(null)
                transaction.commit()
                true
            }

            R.id.menu_item_metas -> {
                if (planEnProgreso) {
                    Toast.makeText(this, "Debes completar la meta actual antes de definir una nueva.", Toast.LENGTH_SHORT).show()
                } else if (registeredHabits.size < 2) {
                    Toast.makeText(this, "Debes registrar al menos 2 hábitos para definir metas.", Toast.LENGTH_SHORT).show()
                } else {
                    ocultarElementosUI()
                    val fragment = MetasFragment.newInstance(registeredHabits)
                    val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.fragment_container, fragment)
                    transaction.addToBackStack(null)
                    transaction.commit()
                }
                true
            }
            R.id.menu_item_desafios -> {
                if (registeredHabits.isEmpty()) {
                    Toast.makeText(this, "Primero debes registrar tus malos hábitos para comenzar los desafíos diarios.", Toast.LENGTH_LONG).show()
                } else {
                    ocultarElementosUI()
                    val fragment = DesafiosDiariosFragment.newInstance(registeredHabits)
                    val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.fragment_container, fragment)
                    transaction.addToBackStack(null)
                    transaction.commit()
                }
                true
            }

            R.id.menu_item_progreso -> {
                ocultarElementosUI()
                val fragment = ProgresoFragment()
                val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragment_container, fragment)
                transaction.addToBackStack(null)
                transaction.commit()
                true
            }
            R.id.menu_item_tus_habitos -> {
                ocultarElementosUI()
                val fragment = TusMalosHabitosFragment.newInstance(registeredHabits)
                val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragment_container, fragment)
                transaction.addToBackStack(null)
                transaction.commit()
                true
            }
            R.id.menu_item_reflexiones -> {
                ocultarElementosUI()
                val fragment = ReflexionesDiariasFragment.newInstance()
                val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragment_container, fragment)
                transaction.addToBackStack(null)
                transaction.commit()
                true
            }
            R.id.menu_item_logout -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun ocultarElementosUI() {
        logoImageView.visibility = ImageView.GONE
        titleTextView.visibility = TextView.GONE
        descriptionTextView.visibility = TextView.GONE
        notificationAdviceTextView.visibility = TextView.GONE
    }

    fun mostrarElementosUI() {
        logoImageView.visibility = ImageView.VISIBLE
        titleTextView.visibility = TextView.VISIBLE
        descriptionTextView.visibility = TextView.VISIBLE
        notificationAdviceTextView.visibility = TextView.VISIBLE
    }

    fun updateRegisteredHabits(newHabits: List<String>, fechaInicio: Calendar?) {
        registeredHabits.addAll(newHabits)
        fechaInicio?.let {
            if (isExactAlarmPermissionGranted()) {
                programarNotificacion(it)
            } else {
                requestExactAlarmPermission()
            }
        }
        mostrarElementosUI()
    }

    fun comenzarPlan() {
        planEnProgreso = true
        mostrarElementosUI()
    }

    private fun programarNotificacion(fechaInicio: Calendar) {
        val alarmManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getSystemService(Context.ALARM_SERVICE) as AlarmManager
        } else {
            null
        }
        val intent = Intent(this, PlanInicioReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager?.setExact(AlarmManager.RTC_WAKEUP, fechaInicio.timeInMillis, pendingIntent)
        Toast.makeText(this, "Notificación programada para la fecha seleccionada", Toast.LENGTH_SHORT).show()
    }

    private fun isExactAlarmPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            scheduleExactAlarmLauncher.launch(intent)
        }
    }
}
