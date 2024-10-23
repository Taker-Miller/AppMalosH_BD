package com.seba.malosh.fragments.perfil

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.seba.malosh.R

class ModificarPerfilDialogFragment : DialogFragment() {

    private lateinit var editNombre: EditText
    private lateinit var editApellido: EditText
    private lateinit var editCorreo: EditText

    private var nombreActual: String = ""
    private var apellidoActual: String = ""
    private var correoActual: String = ""

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun setDatosActuales(nombre: String, apellido: String, correo: String) {
        nombreActual = nombre
        apellidoActual = apellido
        correoActual = correo
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_modificar_perfil, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editNombre = view.findViewById(R.id.edit_nombre)
        editApellido = view.findViewById(R.id.edit_apellido)
        editCorreo = view.findViewById(R.id.edit_correo)

        editNombre.setText(nombreActual)
        editApellido.setText(apellidoActual)
        editCorreo.setText(correoActual)

        val btnGuardar: Button = view.findViewById(R.id.btn_guardar)

        btnGuardar.setOnClickListener {
            val nuevoNombre = editNombre.text.toString().ifBlank { nombreActual }
            val nuevoApellido = editApellido.text.toString().ifBlank { apellidoActual }
            val nuevoCorreo = editCorreo.text.toString().ifBlank { correoActual }

            val currentUser = auth.currentUser

            if (currentUser != null) {
                val userId = currentUser.uid
                val userRef = firestore.collection("usuarios").document(userId)

                val updatedUserData: MutableMap<String, Any> = hashMapOf(
                    "nombre" to nuevoNombre,
                    "apellido" to nuevoApellido,
                    "correo" to nuevoCorreo
                )

                userRef.update(updatedUserData)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()

                        val result = Bundle().apply {
                            putString("nombre_modificado", nuevoNombre)
                            putString("apellido_modificado", nuevoApellido)
                            putString("correo_modificado", nuevoCorreo)
                        }

                        parentFragmentManager.setFragmentResult("modificarPerfilRequestKey", result)

                        dismiss()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error al actualizar perfil: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
