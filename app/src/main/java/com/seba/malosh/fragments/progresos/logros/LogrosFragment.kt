package com.seba.malosh.fragments.progresos.logros

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.seba.malosh.R

class LogrosFragment : Fragment() {

    private lateinit var recyclerViewLogros: RecyclerView
    private lateinit var logroAdapter: LogroAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_logros, container, false)

        recyclerViewLogros = view.findViewById(R.id.recyclerViewLogros)
        recyclerViewLogros.layoutManager = LinearLayoutManager(requireContext())


        logroAdapter = LogroAdapter(listaLogros)
        recyclerViewLogros.adapter = logroAdapter


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (parentFragmentManager.backStackEntryCount > 0) {
                    parentFragmentManager.popBackStack()
                } else {
                    requireActivity().finish()
                }
            }
        })

        return view
    }
}
