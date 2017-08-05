package thread

import java.util.Random

fun main(args: Array<String>) {
	val channel = Channel(5)
	channel.startWorkers()
	MyClientThread("Alice", channel).start()
	MyClientThread("Bobby", channel).start()
	MyClientThread("Chris", channel).start()
}

class MyClientThread(name: String, channel: Channel) : Thread(name) {
	private val channel: Channel
	
	companion object {
		private val random = Random()
	}
	
	init {
		this.channel = channel
	}
	
	override fun run() {
		try {
			for(i in 0..10000) {
				val request = MyRequest(getName(), i)
				channel.putRequest(request)
				Thread.sleep(random.nextInt(1000).toLong())
			}
		} catch (e: InterruptedException) {
			
		}
	}
}

class MyRequest(name: String, number: Int) {
	private val name: String
	private val number: Int
	
	companion object {
		private val random = Random()
	}
	
	init {
		this.name = name
		this.number = number
	}
	
	fun execute() {
		println("${Thread.currentThread().getName()} executes ${toMessage()}")
		try {
			Thread.sleep(random.nextInt(1000).toLong())
		} catch (e: InterruptedException) {
			
		}
	}
	
	fun toMessage(): String {
		return "[ Request from ${this.name} No. $number ]"
	}
}

class Channel(threads: Int) {
	private val reqeustQueue = ArrayList<MyRequest>()
	private var tail: Int
	private var head: Int
	private var count: Int
	private var threads: Int
	private val threadPool = ArrayList<WorkerThread>()
	
	companion object {
		val MAX_REQUEST = 100
	}
	
	init {
		this.head = 0
		this.tail = 0
		this.count = 0
		this.threads = threads
		
		for (i in 0..threads - 1) {
			this.threadPool.add(WorkerThread("Worker-$i", this))
		}
	}
	
	fun startWorkers() {
		for (i in 0..this.threads - 1) {
			this.threadPool[i].start()
		}
	}
	
	@Synchronized
	fun putRequest(request: MyRequest) {
		while (this.count >= this.threads) {
			try {
				(this as java.lang.Object).wait()
			} catch (e: InterruptedException) {
			}
		}
		this.reqeustQueue.add(request)
		this.tail = (this.tail + 1) % this.reqeustQueue.size
		this.count++
		(this as java.lang.Object).notifyAll()
	}
	
	@Synchronized
	fun takeRequest(): MyRequest {
		while (this.count <= 0) {
			try {
				(this as java.lang.Object).wait()
			} catch (e: InterruptedException) {
			}
		}
		val request = this.reqeustQueue[this.head]
		this.head = (this.head + 1) % this.reqeustQueue.size
		this.count--
		(this as java.lang.Object).notifyAll()
		return request
	}
}

class WorkerThread(name: String, channel: Channel) : Thread(name) {
	private val channel: Channel
	
	init {
		this.channel = channel
	}
	
	override fun run() {
		while (true) {
			val request = channel.takeRequest()
			request.execute()
		}
	}
	
}