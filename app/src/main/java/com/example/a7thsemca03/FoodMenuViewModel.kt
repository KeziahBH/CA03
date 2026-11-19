package com.example.a7thsemca03

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class FoodItem(
    val id: String,
    val name: String,
    val price: Double,
    val quantity: Int = 1
) {
    val totalPrice: Double
        get() = price * quantity
}

class FoodMenuViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialItems = listOf(
        FoodItem("1", "Cheeseburger Deluxe", 8.99),
        FoodItem("2", "Pepperoni Pizza Slice", 4.50)
    )

    val itemsState: StateFlow<List<FoodItem>> = savedStateHandle.getStateFlow<Map<String, Int>>(
        KEY_QUANTITIES,
        initialItems.associate { it.id to it.quantity }
    ).map { quantitiesMap ->
        initialItems.map { item ->
            val qty = quantitiesMap[item.id] ?: item.quantity
            item.copy(quantity = qty)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = initialItems
    )

    fun incrementQuantity(itemId: String) {
        val currentMap = savedStateHandle.get<Map<String, Int>>(KEY_QUANTITIES)
            ?: initialItems.associate { it.id to it.quantity }
        val currentQty = currentMap[itemId] ?: 1
        val newMap = currentMap.toMutableMap().apply {
            this[itemId] = currentQty + 1
        }
        savedStateHandle[KEY_QUANTITIES] = newMap
    }

    fun decrementQuantity(itemId: String) {
        val currentMap = savedStateHandle.get<Map<String, Int>>(KEY_QUANTITIES)
            ?: initialItems.associate { it.id to it.quantity }
        val currentQty = currentMap[itemId] ?: 1
        if (currentQty > 1) {
            val newMap = currentMap.toMutableMap().apply {
                this[itemId] = currentQty - 1
            }
            savedStateHandle[KEY_QUANTITIES] = newMap
        }
    }

    companion object {
        private const val KEY_QUANTITIES = "key_quantities"
    }
}
