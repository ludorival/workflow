package io.github.ludorival.workflow

/**
 * Represents a single step in a workflow.
 */
class Step<T>(private val block: WorkflowContext.() -> T) {
    
    /**
     * Execute this step with the given context.
     * 
     * @param context The workflow context
     * @return The result of the step execution
     */
    fun from(context: WorkflowContext): T {
        return context.block()
    }
}

/**
 * Represents a pair of steps that can be executed in parallel.
 */
typealias PairSteps<O, P> = Pair<Step<O>, Step<P>>

/**
 * Represents the result of executing a pair of steps.
 */
typealias PairResult<O, P> = Result<Pair<O, P>>

/**
 * Represents an inline context step that can be executed with context and input.
 */
typealias InlineContextStep<I, O> = (WorkflowContext, I) -> O 