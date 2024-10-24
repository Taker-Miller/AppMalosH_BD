package com.seba.malosh.fragments.progresos.logros

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.seba.malosh.R

class LogroAdapter(private val logros: List<Logro>) : RecyclerView.Adapter<LogroAdapter.LogroViewHolder>() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogroViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_logro, parent, false)
        return LogroViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogroViewHolder, position: Int) {
        val logro = logros[position]
        holder.bind(logro)
    }

    override fun getItemCount(): Int = logros.size

    inner class LogroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val logroTitulo: TextView = itemView.findViewById(R.id.logroTitulo)
        private val logroDescripcion: TextView = itemView.findViewById(R.id.logroDescripcion)
        private val logroIcon: ImageView = itemView.findViewById(R.id.logroIcon)

        fun bind(logro: Logro) {
            logroTitulo.text = logro.titulo
            logroDescripcion.text = logro.descripcion

            logroIcon.setImageResource(
                if (logro.desbloqueado) logro.iconoDesbloqueado else logro.iconoBloqueado
            )

            itemView.setOnClickListener {
                if (!logro.desbloqueado) {
                    desbloquearLogro(logro)
                }
            }
        }

        private fun desbloquearLogro(logro: Logro) {
            logro.desbloqueado = true
            logroIcon.setImageResource(logro.iconoDesbloqueado)

            val currentUser = auth.currentUser
            currentUser?.let {
                val userId = it.uid
                val userRef = firestore.collection("usuarios").document(userId)

                userRef.update("logros_desbloqueados", FieldValue.arrayUnion(logro.id))
                    .addOnSuccessListener {}
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                    }
            }
        }
    }
}
