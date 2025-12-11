package com.tagmypet.data.model

import com.squareup.moshi.Json
import com.tagmypet.data.adapter.PetOwnerId

data class Pet(
    // ID e Owner corrigidos para lerem String simples
    @Json(name = "_id") val id: String,
    @Json(name = "owner") val ownerId: String,

    val name: String,
    val species: String = "Dog",
    val breed: String,
    val age: Int,
    val weight: Double,
    val contactPhone: String,

    // CORREÇÃO CRÍTICA: Tornar 'photoUrl' NULO para evitar falha se o campo faltar no JSON.
    val photoUrl: String?,
    val qrCodeContent: String?,

    val isLost: Boolean = false,
    val hasTag: Boolean = true, // Mantido com default, é opcional na API

    val allergies: List<String> = emptyList(),
    val medications: List<String> = emptyList(),

    // Novos campos
    val vaccines: List<Vaccine> = emptyList(),
    val lastPosition: PetLocation? = null,
)

data class Vaccine(
    @Json(name = "_id") val id: String,
    val name: String,
    val dateApplied: String, // Vem como ISO String da API
    val nextDoseDate: String?,
    val isApplied: Boolean,
)

data class PetLocation(
    val lat: Double,
    val lng: Double,
    val timestamp: String,
)