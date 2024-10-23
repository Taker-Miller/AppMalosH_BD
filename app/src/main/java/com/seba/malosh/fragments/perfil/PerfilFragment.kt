package com.seba.malosh.fragments.perfil

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.seba.malosh.R
import com.seba.malosh.activities.BienvenidaActivity

class PerfilFragment : Fragment() {

    private lateinit var nombreUsuarioTextView: TextView
    private lateinit var apellidoUsuarioTextView: TextView
    private lateinit var correoUsuarioTextView: TextView
    private lateinit var imagenPerfilImageView: ImageView
    private lateinit var btnCambiarImagenPerfil: Button
    private lateinit var btnEditarPerfil: Button
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val PICK_IMAGE_REQUEST = 71
    private var imageUri: Uri? = null

    // Imágenes por defecto
    private val imagenesPerfil = arrayOf(
        R.drawable.image_perfil_1,
        R.drawable.image_perfil_2,
        R.drawable.image_perfil_3,
        R.drawable.image_perfil_4
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        nombreUsuarioTextView = view.findViewById(R.id.nombre_usuario)
        apellidoUsuarioTextView = view.findViewById(R.id.apellido_usuario)
        correoUsuarioTextView = view.findViewById(R.id.correo_usuario)
        imagenPerfilImageView = view.findViewById(R.id.imagen_perfil)
        btnCambiarImagenPerfil = view.findViewById(R.id.btn_editar_imagen_perfil)
        btnEditarPerfil = view.findViewById(R.id.btn_editar_perfil)

        cargarDatosUsuario()

        btnCambiarImagenPerfil.setOnClickListener {
            mostrarOpcionesImagen()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(requireActivity(), BienvenidaActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        })

        return view
    }

    private fun mostrarOpcionesImagen() {
        val opciones = arrayOf("Imagen 1", "Imagen 2", "Imagen 3", "Imagen 4", "Elegir desde galería")

        val dialog = AlertDialog.Builder(context)
            .setTitle("Selecciona una imagen de perfil")
            .setItems(opciones) { _, which ->
                if (which == 4) {
                    seleccionarImagen()
                } else {
                    imagenPerfilImageView.setImageResource(imagenesPerfil[which])
                    guardarImagenPredeterminadaEnFirestore(which)
                }
            }
            .create()

        dialog.show()
    }

    private fun seleccionarImagen() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && data != null && data.data != null) {
            imageUri = data.data
            imagenPerfilImageView.setImageURI(imageUri)
            subirImagenPerfil()
        }
    }

    private fun subirImagenPerfil() {
        val currentUser = auth.currentUser

        currentUser?.let {
            val userId = it.uid
            val storageRef = storage.reference.child("imagenes_perfil/$userId.jpg")

            imageUri?.let { uri ->
                val uploadTask = storageRef.putFile(uri)
                uploadTask.addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        guardarImagenEnFirestore(downloadUrl.toString())
                    }.addOnFailureListener { e ->
                        Toast.makeText(context, "Error al obtener la URL de la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(context, "Error al subir la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun guardarImagenPredeterminadaEnFirestore(imagenIndex: Int) {
        val currentUser = auth.currentUser

        currentUser?.let {
            val userId = it.uid
            val userRef = firestore.collection("usuarios").document(userId)

            // Guardar el índice de la imagen predeterminada en Firestore
            userRef.update("imagen_perfil", imagenIndex)
                .addOnSuccessListener {
                    Toast.makeText(context, "Imagen de perfil actualizada", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error al actualizar la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun guardarImagenEnFirestore(imageUrl: String) {
        val currentUser = auth.currentUser

        currentUser?.let {
            val userId = it.uid
            val userRef = firestore.collection("usuarios").document(userId)

            // Guardar la URL de la imagen en Firestore
            userRef.update("imagen_perfil", imageUrl)
                .addOnSuccessListener {
                    Toast.makeText(context, "Imagen de perfil actualizada", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error al actualizar la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun cargarDatosUsuario() {
        val currentUser = auth.currentUser

        currentUser?.let {
            val userId = it.uid
            val userRef = firestore.collection("usuarios").document(userId)

            userRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val nombre = document.getString("nombre") ?: "Usuario"
                        val apellido = document.getString("apellido") ?: "Apellido"
                        val correo = document.getString("correo") ?: "correo@example.com"
                        val imagenPerfil = document.get("imagen_perfil") // Puede ser un índice o una URL

                        nombreUsuarioTextView.text = nombre
                        apellidoUsuarioTextView.text = apellido
                        correoUsuarioTextView.text = correo

                        // Ver si imagen_perfil es un índice (imagen predeterminada)
                        if (imagenPerfil is Long) {
                            val imagenIndex = imagenPerfil.toInt()
                            imagenPerfilImageView.setImageResource(imagenesPerfil[imagenIndex])
                        } else if (imagenPerfil is String) {
                            // Si es una URL (imagen subida por el usuario), cargarla con Glide
                            Glide.with(this).load(imagenPerfil).into(imagenPerfilImageView)
                        } else {
                            // Cargar una imagen por defecto si no hay imagen
                            imagenPerfilImageView.setImageResource(R.drawable.image_perfil_1)
                        }
                    } else {
                        Toast.makeText(context, "Error al cargar el perfil", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error al obtener los datos: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
