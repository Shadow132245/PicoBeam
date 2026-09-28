package com.picobeam.core

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * Non-blocking asynchronous logger. Producers always return immediately; a single
 * daemon drainer thread forwards lines to the configured sink. Bounded memory,
 * hot-path zero allocation beyond queue.offer.
 */
object PicoLog {

    enum class Level(val tag: String) { ERROR("E"), WARN("W"), INFO("I"), DEBUG("D") }

    private const val CAPACITY = 1000
    private val queue = ArrayBlockingQueue<String>(CAPACITY)
    private val minLevel = Level.INFO

    @Volatile
    var sink: (String) -> Unit = {}

    private val drainer by lazy {
        Thread {
            while (true) {
                val line = try {
                    queue.poll(1, TimeUnit.SECONDS) ?: continue
                } catch (_: InterruptedException) {
                    return@Thread
                } catch (_: Throwable) {
                    continue
                }
                try {
                    sink(line)
                } catch (_: Throwable) {
                    // a broken sink must never kill the drainer
                }
            }
        }.apply {
            isDaemon = true
            name = "pico-log"
            start()
        }
    }

    fun configure(onLine: (String) -> Unit) {
        sink = onLine
        // force the lazy drainer to start up
        drainer
    }

    fun log(level: Level, tag: String, msg: String) {
        if (level.ordinal > minLevel.ordinal) return
        val line = "${System.currentTimeMillis()} [${level.tag}] $tag: $msg"
        if (!queue.offer(line)) {
            // bounded ring: drop the oldest line rather than blocking the caller
            queue.poll()
            queue.offer(line)
        }
    }

    fun e(tag: String, msg: String) = log(Level.ERROR, tag, msg)
    fun w(tag: String, msg: String) = log(Level.WARN, tag, msg)
    fun i(tag: String, msg: String) = log(Level.INFO, tag, msg)
    fun d(tag: String, msg: String) = log(Level.DEBUG, tag, msg)
}