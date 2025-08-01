package io.github.ludorival.workflow

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import java.lang.IllegalStateException
import kotlin.reflect.KClass

@Suppress("NonAsciiCharacters", "TooManyFunctions")
class Workflow<T, V>(private val initialValue: T, private val context: WorkflowContext = WorkflowContext().apply { store(initialValue) }) {

    private fun <T> store(value: T): T {
        return context.store(value)
    }

    private suspend fun start(block: suspend DSL.() -> Result<V>): V {
        val result = DSL().block()
        return result.getOrThrow()
    }

    inner class DSL {

        val initial get() = runCatching { initialValue }

        @JvmName("do")
        suspend infix fun <I, O, P, S : I> Result<S>.`➡️`(pair: PairSteps<O, P>): PairResult<O, P> {
            val receiver = this
            return coroutineScope {
                val first = async { receiver `➡️` pair.first }
                val second = async { receiver `➡️` pair.second }
                val result = runCatching { (first.await().get() to second.await().get()) }
                result
            }
        }

        @JvmName("do")
        suspend infix fun <I, O> Result<I>.`➡️`(step: Step<O>): Result<O> = this `➡️` { context, input ->
            step.from(context)
        }

        @JvmName("do")
        suspend infix fun <I, O> Step<I>.`➡️`(step: Step<O>): Result<O> = initial `➡️` this `➡️` { context, input ->
            step.from(context)
        }

        @Suppress("ktlint:standard:function-naming")
        @JvmName("given")
        fun `⚑`(vararg array: Any): Step<*> {
            return Step {
                array.forEach { context.store(it) }
                array.last()
            }
        }

        @Suppress("ktlint:standard:function-naming")
        @JvmName("given")
        suspend fun <O, T> `⚑`(element: T, block: suspend Workflow<T, O>.DSL.() -> Step<O>): Step<O> {
            val subContext = context.clone().apply { store(element) }
            return Step { 
                runBlocking {
                    Workflow<T, O>(element, subContext).DSL().run { initial `➡️` block() }.get() 
                }
            }
        }

        @JvmName("flatMap")
        suspend fun <O> `⚑`(block: suspend Workflow<Any, O>.DSL.() -> Step<O>): Result<O> {
            val subContext = context.clone()
            return Workflow<Any, O>(Any(), subContext).DSL().run { initial `➡️` block() }
        }

        @JvmName("end")
        infix fun <I, T : Any> Result<I>.`🔚`(klass: KClass<T>): Result<T> {
            if (context.has(klass))
                return Result.success(context.get(klass))
            if (this.isFailure)
                return Result.failure(this.exceptionOrNull()!!)
            if (context.currentFailure != null)
                return Result.failure(context.currentFailure!!)
            return Result.failure(IllegalStateException("Dead-end scenario, silent failure has happened in between"))
        }

        @JvmName("decide")
        suspend infix fun <I, O, S : I> Result<S>.`♦️`(block: InlineContextStep<Result<I>, Result<O>>): Result<O> =
            mapCatching {
                block.invoke(
                    context, runCatching { it }
                )
            }.mapCatching { result ->
                result.getOrThrow().also { output ->
                    store(output)
                }
            }

        fun <T> Result<T>.get(): T = getOrThrow()

        operator fun <O, P> Step<O>.plus(step: Step<P>): PairSteps<O, P> = this to step

        @JvmName("do")
        suspend infix fun <I, O> Result<I>.`➡️`(block: InlineContextStep<I, O>): Result<O> = mapCatching { input ->
            block.invoke(
                context,
                input
            ).also { output ->
                store(output)
            }
        }

        @JvmName("doOnFailure")
        suspend infix fun <V, O> Result<V>.`❌`(step: Step<O>): Result<V> {
            return this `❌` { context, error ->
                step.from(context)
            }
        }

        @JvmName("doOnFailure")
        suspend infix fun <V, O> Result<V>.`❌`(step: InlineContextStep<Result<Throwable>, O>): Result<V> {
            return onFailure { throwable ->
                store(throwable)
                step.invoke(
                    context,
                    runCatching { throwable }
                )
            }
        }

        @JvmName("doOnRecover")
        suspend infix fun <V> Result<V>.`♻️`(step: InlineContextStep<Result<Throwable>, Result<V>>): Result<V> {
            return recover { throwable ->
                store(throwable)
                step.invoke(
                    context,
                    runCatching { throwable }
                ).getOrThrow()
            }
        }
    }

    companion object {
        suspend fun <T, V> start(
            initial: T,
            block: suspend Workflow<T, V>.DSL.() -> Result<V>
        ): V {
            return Workflow<T, V>(initial).start(block)
        }
    }
}