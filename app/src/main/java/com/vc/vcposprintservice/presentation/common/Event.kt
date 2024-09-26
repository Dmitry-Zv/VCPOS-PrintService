package com.vc.vcposprintservice.presentation.common

interface Event<E> {
    fun onEvent(event: E)
}