package io.github.ludorival.workflow

import kotlin.reflect.KClass
import java.util.concurrent.ConcurrentHashMap

/**
 * Context for managing workflow state and data storage.
 */
class WorkflowContext {
    
    private val storage = ConcurrentHashMap<KClass<*>, Any>()
    var currentFailure: Throwable? = null
    
    /**
     * Store a value in the context.
     * 
     * @param value The value to store
     * @return The stored value
     */
    fun <T> store(value: T): T {
        storage[value!!::class] = value
        return value
    }
    
    /**
     * Get a value from the context by its class.
     * 
     * @param klass The class of the value to retrieve
     * @return The stored value
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(klass: KClass<T>): T {
        return storage[klass] as T
    }
    
    /**
     * Check if a value of the given class exists in the context.
     * 
     * @param klass The class to check for
     * @return True if the value exists, false otherwise
     */
    fun has(klass: KClass<*>): Boolean {
        return storage.containsKey(klass)
    }
    
    /**
     * Clone this context.
     * 
     * @return A new context with the same data
     */
    fun clone(): WorkflowContext {
        val newContext = WorkflowContext()
        newContext.storage.putAll(storage)
        newContext.currentFailure = currentFailure
        return newContext
    }
} 