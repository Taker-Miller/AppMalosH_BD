package com.seba.malosh.fragments.registromalosh

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.seba.malosh.R
import com.seba.malosh.activities.BienvenidaActivity
import com.seba.malosh.fragments.progresos.logros.listaLogros
import java.util.Calendar

class ConfirmarHabitosFragment : Fragment() {

    private lateinit var siButton: Button
    private lateinit var noButton: Button
    private lateinit var selectedHabitsTextView: TextView
    private var fechaInicio: Calendar? = null

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

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
                guardarHabitosEnFirestore(selectedHabits)

                view?.let { safeView ->
                    Toast.makeText(
                        safeView.context,
                        "Malos hábitos registrados exitosamente",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                (requireActivity() as BienvenidaActivity).updateRegisteredHabits(
                    selectedHabits ?: emptyList(), fechaInicio
                )

                verificarYDesbloquearLogros(selectedHabits ?: emptyList())

                requireActivity().supportFragmentManager.popBackStack(null, 1)
            } else {
                view?.let { safeView ->
                    Toast.makeText(
                        safeView.context,
                        "Debes registrar entre 2 y 4 malos hábitos.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        noButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }

    private fun guardarHabitosEnFirestore(habitos: List<String>?) {
        val currentUser = auth.currentUser
        if (currentUser != null && habitos != null && habitos.isNotEmpty()) {
            val userId = currentUser.uid

            val fechaRegistro = fechaInicio?.time ?: Calendar.getInstance().time

            val habitosData = hashMapOf(
                "habitos" to habitos,
                "fecha_registro" to fechaRegistro
            )

            firestore.collection("usuarios")
                .document(userId)
                .collection("habitos_registrados")
                .add(habitosData)
                .addOnSuccessListener {
                    view?.let { safeView ->
                        Toast.makeText(
                            safeView.context,
                            "Hábitos guardados en Firebase",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener { e ->
                    view?.let { safeView ->
                        Toast.makeText(
                            safeView.context,
                            "Error al guardar hábitos: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            view?.let { safeView ->
                Toast.makeText(safeView.context, "No hay hábitos para guardar", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun verificarYDesbloquearLogros(nuevosHabitos: List<String>) {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid
        val userRef = firestore.collection("usuarios").document(userId ?: "")

        userRef.get().addOnSuccessListener { document ->
            val logrosDesbloqueados =
                document.get("logros_desbloqueados") as? List<String> ?: emptyList()

            val habitosRegistradosAnteriormente =
                document.get("habitos_totales_registrados") as? List<String> ?: emptyList()

            val totalHabitosRegistrados =
                habitosRegistradosAnteriormente.union(nuevosHabitos).toList()

            val logroDeDosHabitos = listaLogros.find { it.id == 2 }
            if (totalHabitosRegistrados.size >= 2 && logroDeDosHabitos != null && !logrosDesbloqueados.contains(logroDeDosHabitos.titulo)) {
                logroDeDosHabitos.desbloqueado = true

                val logrosData: Map<String, Any> = hashMapOf(
                    "logros_desbloqueados" to FieldValue.arrayUnion(logroDeDosHabitos.titulo),
                    "habitos_totales_registrados" to FieldValue.arrayUnion(*nuevosHabitos.toTypedArray())
                )
                userRef.update(logrosData).addOnSuccessListener {
                    view?.let { safeView ->
                        Toast.makeText(
                            safeView.context,
                            "¡Logro Desbloqueado: ${logroDeDosHabitos.titulo}!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }.addOnFailureListener { e ->
                    view?.let { safeView ->
                        Toast.makeText(
                            safeView.context,
                            "Error al actualizar el logro: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            val logroDeCuatroHabitos = listaLogros.find { it.id == 4 }
            if (totalHabitosRegistrados.size >= 4 && logroDeCuatroHabitos != null && !logrosDesbloqueados.contains(logroDeCuatroHabitos.titulo)) {
                logroDeCuatroHabitos.desbloqueado = true

                val logrosData: Map<String, Any> = hashMapOf(
                    "logros_desbloqueados" to FieldValue.arrayUnion(logroDeCuatroHabitos.titulo),
                    "habitos_totales_registrados" to FieldValue.arrayUnion(*nuevosHabitos.toTypedArray())
                )
                userRef.update(logrosData).addOnSuccessListener {
                    view?.let { safeView ->
                        Toast.makeText(
                            safeView.context,
                            "¡Logro Desbloqueado: ${logroDeCuatroHabitos.titulo}!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }.addOnFailureListener { e ->
                    view?.let { safeView ->
                        Toast.makeText(
                            safeView.context,
                            "Error al actualizar el logro: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}
