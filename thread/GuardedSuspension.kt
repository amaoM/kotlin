package thread.guarded.suspension

import java.util.*
import java.lang.*

fun main(args: Array<String>) {
	val requestQueue = RequestQueue()
	ClientThread(requestQueue, "Alice", 3141592L).start()
	ServerThread(requestQueue, "Bobby", 6535897L).start()
	ServerThread(requestQueue, "Chris", 9213459L).start()
}

class Request(name: String) {
	private val name: String
	
	init {
		this.name = name
	}
	
	fun toMessage(): String {
		return "[ Request + ${name} ]"
	}
}

class RequestQueue() {
	private var queue: LinkedList<Request> = LinkedList<Request>()
	private val lock = java.lang.Object()
	
	fun getRequest(): Request {
		synchronized(lock) {
			while (this.queue.peek() == null) {
				try {
					lock.wait()
				} catch (e: InterruptedException) {
				}
			}
			return this.queue.remove()
		}
	}
	
	fun putRequest(request: Request) {
		synchronized(lock) {
			this.queue.offer(request)
			lock.notifyAll()
		}
	}
}

class ClientThread(requestQueue: RequestQueue, name: String, seed: Long) : Thread(name) {
	private val random: Random
	private val requestQueue: RequestQueue
	
	init {
		this.requestQueue = requestQueue
		this.random = Random(seed)
	}
	
	override fun run() {
		for (i in 0..10000) {
			var request = Request("No.$i")
			println("${Thread.currentThread().getName()} requests ${request.toMessage()}")
			this.requestQueue.putRequest(request)
			try {
				Thread.sleep(this.random.nextInt(1000).toLong())
			} catch (e: InterruptedException) {
			}
		}
	}
}

class ServerThread(requestQueue: RequestQueue, name: String, seed: Long) : Thread(name) {
	private val random: Random
	private val requestQueue: RequestQueue
	
	init {
		this.requestQueue = requestQueue
		this.random = Random(seed)
	}
	
	override fun run() {
		for (i in 0..10000) {
			var request = this.requestQueue.getRequest()
			println("${Thread.currentThread().getName()} handles ${request.toMessage()}")
			try {
				Thread.sleep(this.random.nextInt(1000).toLong())
			} catch (e: InterruptedException) {
			}
		}
	}
}







