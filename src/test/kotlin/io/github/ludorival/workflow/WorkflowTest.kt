package io.github.ludorival.workflow

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class WorkflowTest {
    
    @Test
    fun `should create workflow with initial value`() {
        val workflow = Workflow<String, String>("test")
        assertNotNull(workflow)
    }
    
    @Test
    fun `should execute simple workflow with start`() = runTest {
        val result = Workflow.start("initial") {
            initial `➡️` `⚑`("step1", "step2") `➡️` { context, input ->
                "processed: $input"
            }
        }
        assertEquals("processed: step2", result)
    }
    
    @Test
    fun `should execute workflow with multiple steps`() = runTest {
        val result = Workflow.start("start") {
            initial `➡️` `⚑`("step1") `➡️` { context, input ->
                "step1: $input"
            } `➡️` `⚑`("step2") `➡️` { context, input ->
                "step2: $input"
            }
        }
        assertEquals("step2: step2", result)
    }
    
    @Test
    fun `should handle workflow with end condition`() = runTest {
        val result = Workflow.start("initial") {
            initial `➡️` `⚑`("test", "value") `➡️` { context, input ->
                "processed: $input"
            } `🔚` String::class
        }
        assertEquals("processed: value", result)
    }
    
    @Test
    fun `should handle workflow with decide`() = runTest {
        val result = Workflow.start("initial") {
            initial `➡️` `⚑`("test") `♦️` { context, input ->
                Result.success("decided: ${input.get()}")
            }
        }
        assertEquals("decided: test", result)
    }
    
    @Test
    fun `should handle workflow with recovery`() = runTest {
        val result = Workflow.start("initial") {
            initial `➡️` `⚑`("test") `➡️` { context, input ->
                throw RuntimeException("test error")
            } `♻️` { context, error ->
                Result.success("recovered: ${error.exceptionOrNull()?.message}")
            }
        }
        assertEquals("recovered: null", result)
    }
    
    @Test
    fun `should store and retrieve values from context`() = runTest {
        val result = Workflow.start("initial") {
            initial `➡️` `⚑`("value1", "value2", "value3") `➡️` { context, input ->
                context.store("customValue")
                "stored: $input"
            }
        }
        assertEquals("stored: value3", result)
    }
    
    @Test
    fun `should create step and execute it`() {
        val step = Step<String> { "test result" }
        val context = WorkflowContext()
        val result = step.from(context)
        assertEquals("test result", result)
    }
    
    @Test
    fun `should test context operations`() {
        val context = WorkflowContext()
        
        // Test store and get
        val stored = context.store("test")
        assertEquals("test", stored)
        
        val retrieved = context.get(String::class)
        assertEquals("test", retrieved)
        
        // Test has
        assertTrue(context.has(String::class))
        assertFalse(context.has(Int::class))
        
        // Test clone
        val cloned = context.clone()
        assertTrue(cloned.has(String::class))
        assertEquals("test", cloned.get(String::class))
    }
} 