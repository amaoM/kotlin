package thread

import java.util.Random

fun main(args: Array<String>) {
	val data = LockData(10)
	ReaderThread(data).start()
	ReaderThread(data).start()
	ReaderThread(data).start()
	ReaderThread(data).start()
	ReaderThread(data).start()
	ReaderThread(data).start()
	WriterThread(data, "ABCDEFGHIJKLMNOPQRSTUVWXYZ").start()
	WriterThread(data, "abcdefghijklmnopqrstuvwxyz").start()
}

class LockData(size: Int) {
	private var buffer: CharArray
	private val lock = ReadWriteLock()
	
	init {
		this.buffer = CharArray(size)
		for (i in 0..this.buffer.size - 1) {
			this.buffer[i] = '*'
		}
	}
	
	fun read(): CharArray {
		this.lock.readLock()
		try {
			return doRead()
		} finally {
			lock.readUnlock()
		}
	}
	
	fun write(c: Char) {
		this.lock.writeLock()
		try {
			doWrite(c)
		} finally {
			lock.writeUnlock()
		}
	}
	
	fun doRead(): CharArray {
		var newbuf: CharArray = CharArray(this.buffer.size)
		for (i in 0..this.buffer.size - 1) {
			newbuf[i] = this.buffer[i]
		}
		slowly()
		return newbuf
	}
	
	fun doWrite(c: Char) {
		for (i in 0..this.buffer.size - 1) {
			buffer[i] = c
			slowly()
		}
	}
	
	private fun slowly() {
		try {
			Thread.sleep(50)
		} catch (e: InterruptedException) {
		}
	}
}

class WriterThread(data: LockData, filter: String) : Thread() {
	private val data: LockData
	private val filter: String
	private var index = 0
	companion object {
		private val random = Random()
	}
	init {
		this.data = data
		this.filter = filter
	}
	
	override fun run() {
		try {
			while (true) {
				val c = nextchar()
				data.write(c)
				Thread.sleep(random.nextInt(3000).toLong())
			}
		} catch (e: InterruptedException) {
		}
	}
	
	private fun nextchar(): Char {
		val c = this.filter.elementAt(this.index)
		this.index++
		if (this.index >= this.filter.length) {
			this.index = 0
		}
		return c
	}
}

class ReaderThread(data: LockData) : Thread() {
	private val data: LockData
	
	init {
		this.data = data
	}
	
	override fun run() {
		try {
			while (true) {
				val readbuf = this.data.read().map { c -> c}
				println("${Thread.currentThread().getName()} reads ${readbuf}")
			}
		} catch (e: InterruptedException) {
		}
	}
}

class ReadWriteLock {
	private var readingReaders = 0
	private var waitingWriters = 0
	private var writingWriters = 0
	private var preferWriter = true
	
	@Synchronized
	fun readLock() {
		while(this.writingWriters > 0 || (this.preferWriter && this.waitingWriters > 0)) {
			(this as java.lang.Object).wait()
		}
		this.readingReaders++
	}
	
	@Synchronized
	fun readUnlock() {
		this.readingReaders--
		this.preferWriter = true
		(this as java.lang.Object).notifyAll()
	}
	
	@Synchronized
	fun writeLock() {
		this.waitingWriters++
		try {
			while (this.readingReaders > 0 || this.writingWriters > 0) {
				(this as java.lang.Object).wait()
			}
		} finally {
			this.waitingWriters--
		}
		this.writingWriters++
	}
	
	@Synchronized
	fun writeUnlock() {
		this.writingWriters--
		this.preferWriter = false
		(this as java.lang.Object).notifyAll()
	}
}