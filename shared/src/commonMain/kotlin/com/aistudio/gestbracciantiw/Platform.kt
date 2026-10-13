package com.aistudio.gestbracciantiw

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform