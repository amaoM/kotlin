package thread.read.write.lock

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
				this.data.write(c)
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
				println("${Thread.currentThread().getName()} reads ${String(this.data.read())}")
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
	private val lock = java.lang.Object()
	
	fun readLock() {
		synchronized(lock) {
			while(this.writingWriters > 0 || (this.preferWriter && this.waitingWriters > 0)) {
				lock.wait()
			}
			this.readingReaders++
		}
	}
	
	fun readUnlock() {
		synchronized(lock) {
			this.readingReaders--
			this.preferWriter = true
			lock.notifyAll()
		}
	}
	
	fun writeLock() {
		synchronized(lock) {
			this.waitingWriters++
			try {
				while (this.readingReaders > 0 || this.writingWriters > 0) {
					lock.wait()
				}
			} finally {
				this.waitingWriters--
			}
			this.writingWriters++
		}
	}
	
	fun writeUnlock() {
		synchronized(lock) {
			this.writingWriters--
			this.preferWriter = false
			lock.notifyAll()
		}
	}
}