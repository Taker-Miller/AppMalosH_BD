package com.seba.malosh.fragments.perfil

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
    private val REQUEST_PERMISSION = 100

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
            mostrarOpcionesSeleccionImagen()
        }

        btnEditarPerfil.setOnClickListener {
            val dialogFragment = ModificarPerfilDialogFragment().apply {
                setDatosActuales(
                    nombreUsuarioTextView.text.toString(),
                    apellidoUsuarioTextView.text.toString(),
                    correoUsuarioTextView.text.toString()
                )
            }
            dialogFragment.show(parentFragmentManager, "ModificarPerfilDialog")
        }

        parentFragmentManager.setFragmentResultListener("modificarPerfilRequestKey", viewLifecycleOwner) { _, bundle ->
            val nombreModificado = bundle.getString("nombre_modificado")
            val apellidoModificado = bundle.getString("apellido_modificado")
            val correoModificado = bundle.getString("correo_modificado")

            nombreUsuarioTextView.text = nombreModificado
            apellidoUsuarioTextView.text = apellidoModificado
            correoUsuarioTextView.text = correoModificado
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

    private fun mostrarOpcionesSeleccionImagen() {
        val opciones = arrayOf("Seleccionar de galería", "Usar imagen por defecto")

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Cambiar imagen de perfil")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> {
                        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(requireActivity(),
                                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION)
                        } else {
                            seleccionarImagen()
                        }
                    }
                    1 -> {
                        mostrarDialogoImagenesPorDefecto()
                    }
                }
            }
            .create()
            .show()
    }

    private fun mostrarDialogoImagenesPorDefecto() {
        val imagenesOpciones = arrayOf("Imagen 1", "Imagen 2", "Imagen 3", "Imagen 4")

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Selecciona una imagen por defecto")
            .setItems(imagenesOpciones) { _, which ->
                imagenPerfilImageView.setImageResource(imagenesPerfil[which])
                guardarImagenPorDefectoEnFirestore(which)
            }
            .create()
            .show()
    }

    private fun seleccionarImagen() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                seleccionarImagen()
            } else {
                Toast.makeText(context, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show()
                mostrarExplicacionPermiso()
            }
        }
    }

    private fun mostrarExplicacionPermiso() {
        val dialog = AlertDialog.Builder(context)
            .setTitle("Permiso necesario")
            .setMessage("El acceso a tus fotos es necesario para que puedas seleccionar una imagen de perfil. Por favor, habilita el permiso en la configuración.")
            .setPositiveButton("Configuración") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancelar", null)
            .create()
        dialog.show()
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

    private fun guardarImagenEnFirestore(imageUrl: String) {
        val currentUser = auth.currentUser

        currentUser?.let {
            val userId = it.uid
            val userRef = firestore.collection("usuarios").document(userId)

            userRef.update("imagen_perfil", imageUrl)
                .addOnSuccessListener {
                    Toast.makeText(context, "Imagen de perfil actualizada", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error al actualizar la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun guardarImagenPorDefectoEnFirestore(imagenIndex: Int) {
        val currentUser = auth.currentUser

        currentUser?.let {
            val userId = it.uid
            val userRef = firestore.collection("usuarios").document(userId)

            userRef.update("imagen_perfil", imagenIndex)
                .addOnSuccessListener {
                    Toast.makeText(context, "Imagen por defecto seleccionada", Toast.LENGTH_SHORT).show()
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
                        val imagenPerfil = document.get("imagen_perfil")

                        nombreUsuarioTextView.text = nombre
                        apellidoUsuarioTextView.text = apellido
                        correoUsuarioTextView.text = correo

                        when (imagenPerfil) {
                            is Long -> {
                                val imagenIndex = imagenPerfil.toInt()
                                imagenPerfilImageView.setImageResource(imagenesPerfil[imagenIndex])
                            }
                            is String -> {
                                Glide.with(this).load(imagenPerfil).into(imagenPerfilImageView)
                            }
                            else -> {
                                imagenPerfilImageView.setImageResource(R.drawable.image_perfil_1)
                            }
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
