package com.tagmypet.utils

object Constants {
    // 10.0.2.2 para Emulador. Se usar dispositivo físico, coloque o IP da sua máquina.
    const val BASE_URL = "http://192.168.1.2:3000/api/v1/"

    // IMPORTANTE: Pegue esta chave no seu Dashboard do Stripe > Developers > API Keys
    // Ela começa com 'pk_test_'
    const val STRIPE_PUBLISHABLE_KEY = "pk_test_51ScdwqFHBjjkMJ5jd8wfPHnBiEe7LhUe4j8dB47AGJADTyn6s8qd3RLT1Wqj8uBfu7CCCszZ3HcSAZWyMJEaxy7S00GrVZNtvN"
}