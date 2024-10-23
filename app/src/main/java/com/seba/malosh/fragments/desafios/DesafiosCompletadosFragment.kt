package com.seba.malosh.fragments.desafios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.seba.malosh.R

class DesafiosCompletadosFragment : Fragment() {

    private lateinit var listaDesafios: ListView
    private val viewModel: DesafiosCompletadosViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_desafios_completados, container, false)
        listaDesafios = view.findViewById(R.id.listaDesafiosCompletados)

        viewModel.desafiosCompletados.observe(viewLifecycleOwner) { desafios ->
            val adapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, desafios)
            listaDesafios.adapter = adapter
        }


        viewModel.cargarDesafiosCompletados(requireContext())


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

    companion object {
        fun newInstance(): DesafiosCompletadosFragment {
            return DesafiosCompletadosFragment()
        }
    }
}
