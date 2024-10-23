package com.seba.malosh.fragments.progresos.metas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.seba.malosh.R
import com.seba.malosh.fragments.desafios.DesafiosCompletadosFragment
import com.seba.malosh.fragments.progresos.logros.LogrosFragment

class ProgresoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_progresos, container, false)

        val cardMeta: CardView = view.findViewById(R.id.card_meta)
        cardMeta.setOnClickListener {
            val fragment = ProgresoMetaFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }


        val cardLogros: CardView = view.findViewById(R.id.card_logros)
        cardLogros.setOnClickListener {
            val fragment = LogrosFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        val cardMetasCumplidas: CardView = view.findViewById(R.id.card_metas_cumplidas)
        cardMetasCumplidas.setOnClickListener {
            val fragment = MetasCumplidasFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        val cardDesafiosCumplidos: CardView = view.findViewById(R.id.card_desafios_cumplidos)
        cardDesafiosCumplidos.setOnClickListener {
            val fragment = DesafiosCompletadosFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}
