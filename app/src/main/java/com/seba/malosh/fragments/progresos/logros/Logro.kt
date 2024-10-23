package com.seba.malosh.fragments.progresos.logros

import com.seba.malosh.R

data class Logro(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    var desbloqueado: Boolean,
    val iconoBloqueado: Int,
    val iconoDesbloqueado: Int
)

val listaLogros = listOf(
    Logro(
        id = 1,
        titulo = "Primer Meta",
        descripcion = "Define tu primera meta.",
        desbloqueado = false,
        iconoBloqueado = R.drawable.ic_bloqueado,
        iconoDesbloqueado = R.drawable.ic_desbloqueado
    ),
    Logro(
        id = 2,
        titulo = "Registrando Malos Hábitos",
        descripcion = "Registra al menos dos malos hábitos.",
        desbloqueado = false,
        iconoBloqueado = R.drawable.ic_bloqueado,
        iconoDesbloqueado = R.drawable.ic_desbloqueado
    ),
    Logro(
        id = 3,
        titulo = "Desafío Diario Completado",
        descripcion = "Completa un desafío diario.",
        desbloqueado = false,
        iconoBloqueado = R.drawable.ic_bloqueado,
        iconoDesbloqueado = R.drawable.ic_desbloqueado
    ),
    Logro(
        id = 4,
        titulo = "Cuatro Malos Hábitos",
        descripcion = "Registra el máximo de cuatro malos hábitos.",
        desbloqueado = false,
        iconoBloqueado = R.drawable.ic_bloqueado,
        iconoDesbloqueado = R.drawable.ic_desbloqueado
    ),
    Logro(
        id = 5,
        titulo = "Primer Seguimiento",
        descripcion = "Crea un plan de seguimiento.",
        desbloqueado = false,
        iconoBloqueado = R.drawable.ic_bloqueado,
        iconoDesbloqueado = R.drawable.ic_desbloqueado
    ),
    Logro(
        id = 6,
        titulo = "Una Semana Exitosa",
        descripcion = "Completa una semana sin fallar.",
        desbloqueado = false,
        iconoBloqueado = R.drawable.ic_bloqueado,
        iconoDesbloqueado = R.drawable.ic_desbloqueado
    ),
    Logro(
        id = 7,
        titulo = "Desafío de un Mes",
        descripcion = "Completa un mes de desafíos.",
        desbloqueado = false,
        iconoBloqueado = R.drawable.ic_bloqueado,
        iconoDesbloqueado = R.drawable.ic_desbloqueado
    ),
    Logro(
        id = 8,
        titulo = "Cero Fallos",
        descripcion = "No falles en ningún desafío en una semana.",
        desbloqueado = false,
        iconoBloqueado = R.drawable.ic_bloqueado,
        iconoDesbloqueado = R.drawable.ic_desbloqueado
    ),
    Logro(
        id = 9,
        titulo = "Reflexiones Diarias",
        descripcion = "Escribe una reflexión diaria durante una semana.",
        desbloqueado = false,
        iconoBloqueado = R.drawable.ic_bloqueado,
        iconoDesbloqueado = R.drawable.ic_desbloqueado
    ),
    Logro(
        id = 10,
        titulo = "Dos Metas Cumplidas",
        descripcion = "Cumple dos metas definidas.",
        desbloqueado = false,
        iconoBloqueado = R.drawable.ic_bloqueado,
        iconoDesbloqueado = R.drawable.ic_desbloqueado
    ),
    Logro(
        id = 11,
        titulo = "Maestro del Seguimiento",
        descripcion = "Completa dos planes de seguimiento.",
        desbloqueado = false,
        iconoBloqueado = R.drawable.ic_bloqueado,
        iconoDesbloqueado = R.drawable.ic_desbloqueado
    ),
    Logro(
        id = 12,
        titulo = "Rey de los Desafíos",
        descripcion = "Completa 30 desafíos diarios.",
        desbloqueado = false,
        iconoBloqueado = R.drawable.ic_bloqueado,
        iconoDesbloqueado = R.drawable.ic_desbloqueado
    ),
    Logro(
        id = 13,
        titulo = "Cambio Total",
        descripcion = "Elimina por completo un mal hábito.",
        desbloqueado = false,
        iconoBloqueado = R.drawable.ic_bloqueado,
        iconoDesbloqueado = R.drawable.ic_desbloqueado
    ),
    Logro(
        id = 14,
        titulo = "Camino Constante",
        descripcion = "Registra tu progreso todos los días por 14 días.",
        desbloqueado = false,
        iconoBloqueado = R.drawable.ic_bloqueado,
        iconoDesbloqueado = R.drawable.ic_desbloqueado
    ),
    Logro(
        id = 15,
        titulo = "Logros sin Límites",
        descripcion = "Desbloquea todos los logros.",
        desbloqueado = false,
        iconoBloqueado = R.drawable.ic_bloqueado,
        iconoDesbloqueado = R.drawable.ic_desbloqueado
    )
)
