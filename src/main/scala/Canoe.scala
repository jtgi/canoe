import akka.actor._
import akka.event.Logging
import akka.japi.pf.FSMTransitionHandlerBuilder
import com.typesafe.config.{Config, ConfigFactory}
import scala.concurrent.duration._
import scala.util.Random

/**
 * Created by jtgi on 5/2/15.
 */
object Canoe extends App {
  // Init Actors
  val system = ActorSystem("canoe")
  implicit val config = ConfigFactory.load()
  val worker = system.actorOf(Props(new CanoeWorker), "canoeworker")
}

// Messages
case class WorkerState(term: Int, leader: ActorRef, prevLogIndex: Int, prevLogTerm: Int)
case class AppendEntries(state: WorkerState, entries: List[CanoeLogEntry])
case class AppendEntriesRes(term: Int, success: Boolean)
case class RequestVote(term: Int, candidate: ActorRef, lastLogIndex: Int, lastLogTerm: Int)
case class RequestVoteRes(term: Int, voteGranted: Boolean)
case object Timeout

// States
sealed trait State
case object Follower extends State
case object Candidate extends State
case object Leader extends State

// Data
sealed trait Data
case object Unitialized extends Data

class CanoeWorker(implicit config: Config) extends LoggingFSM[State, Data] {
  val stateTimerLabel = "state-timer"
  val canoeLog = new InMemoryCanoeLog
  val seed = new Random()
  val conf = new CanoeConfig(config)

  startWith(Follower, Unitialized)

  when(Follower) {
    case Event(Timeout, _) =>
      cancelTimer(stateTimerLabel)
      goto(Candidate)

    case Event(m @ WorkerState, _) =>
      log.debug("Heartbeat")
      resetTimer()
      stay()

    case Event(AppendEntries(workerState, entries), _) =>
      resetTimer()
      canoeLog.last match {
        case Some(CanoeLogEntry(i, term, _)) if canoeLog.contains(i, term) =>
          entries.foreach(canoeLog.append(_))
          stay replying AppendEntriesRes(term, true)
        case Some(CanoeLogEntry(_, term, _)) =>
          stay replying AppendEntriesRes(term, false)
        case None =>
          stay replying AppendEntriesRes(workerState.term, true)
      }

    case Event(m @ RequestVote, _) => stay()
    case Event(m @ RequestVoteRes, _) => stay()
  }

  when(Candidate, genElectionTimeout(seed)) {
    case Event(FSM.StateTimeout, _) =>
      goto(Leader)
    case Event(_, _) =>
      stay() forMax genElectionTimeout(seed)
  }

  when(Leader, conf.stateTimeout) {
    case Event(FSM.StateTimeout, _) =>
      goto(Follower)
  }

  initialize()
  setTimer(stateTimerLabel, Timeout, genElectionTimeout(seed))

  onTransition {
    case _ -> Follower =>
      log.debug("transition handler fired")
      setTimer(stateTimerLabel, Timeout, genElectionTimeout(seed))
  }

  def resetTimer(name: String = stateTimerLabel, duration: FiniteDuration = genElectionTimeout(seed)) = {
    cancelTimer(name)
    setTimer(name, Timeout, duration)
  }

  def genElectionTimeout(seed: Random): FiniteDuration = {
    val min = conf.electionTimeoutMin
    val max = conf.electionTimeoutMax
    val timeout = min + seed.nextInt((max - min) + 1)

    new FiniteDuration(timeout, MILLISECONDS)
  }
}

// Helpers

class CanoeConfig(config: Config) {
  val electionTimeoutMin = config.getInt("canoe.election-timeout-min")
  val electionTimeoutMax = config.getInt("canoe.election-timeout-max")
  val stateTimeout = config.getInt("canoe.state-timeout") millis
}

trait CanoeLog {
  def append(entry: CanoeLogEntry): Unit
  def get(index: Int): Option[CanoeLogEntry]
  def last: Option[CanoeLogEntry]
  def contains(index: Int, term: Int): Boolean
}

case class CanoeLogEntry(index: Int, term: Int, msg: String)

class InMemoryCanoeLog extends CanoeLog {
  val log = List[CanoeLogEntry]()

  def append(entry: CanoeLogEntry) = entry :: log

  def get(index: Int): Option[CanoeLogEntry] = {
    if(log isDefinedAt index) Some(log(index)) else None
  }

  def last = log.headOption

  def contains(index: Int, term: Int): Boolean = {
    log.exists(entry => entry.index == index && entry.term == term)
  }
}


