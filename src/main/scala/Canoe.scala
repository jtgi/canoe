import akka.actor._
import akka.event.Logging
import scala.concurrent.duration._

/**
 * Created by jtgi on 5/2/15.
 */
object Canoe extends App {
  // Temp Config
  val electionTimeoutMin = 10 millis
  val electionTimeoutMax = 100 millis

  // Init Actors
  val system = ActorSystem("canoe")
  val worker = system.actorOf(Props[CanoeWorker], "canoeworker")
}

// Messages
case class AppendEntries(term: Int, leader: ActorRef, prevLogIndex: Int, prevLogTerm: Int, entries: List[Any])
case class AppendEntriesRes(term: Int, success: Boolean)
case class RequestVote(term: Int, candidate: ActorRef, lastLogIndex: Int, lastLogTerm: Int)
case class RequestVoteRes(term: Int, voteGranted: Boolean)

// States
sealed trait State
case object Follower extends State
case object Candidate extends State
case object Leader extends State

// Data
sealed trait Data
case object Unitialized extends Data

class CanoeWorker extends LoggingFSM[State, Data] {
  val canoeLog = new InMemoryCanoeLog

  startWith(Follower, Unitialized)

  when(Follower, stateTimeout = 2 seconds) {
    case Event(FSM.StateTimeout, _) =>
      goto(Candidate)

    case Event(m @ AppendEntries(_, _, _, _, Nil), _) =>
      log.debug("Heartbeat")
      stay()

    case Event(m @ AppendEntries, _) => stay()
    case Event(m @ AppendEntriesRes, _) => stay()
    case Event(m @ RequestVote, _) => stay()
    case Event(m @ RequestVoteRes, _) => stay()
  }

  when(Candidate, stateTimeout = 2 seconds) {
    case Event(FSM.StateTimeout, Unitialized) =>
      goto(Leader) using Unitialized
  }

  when(Leader, stateTimeout = 2 seconds) {
    case Event(FSM.StateTimeout, Unitialized) =>
      goto(Follower) using Unitialized
  }

  initialize();

}

trait CanoeLog {
  def append(entry: CanoeLogEntry): Unit
  def get(index: Int): Option[CanoeLogEntry]
  def last: Option[CanoeLogEntry]
}

case class CanoeLogEntry(index: Int, term: Int, msg: String)

class InMemoryCanoeLog extends CanoeLog {
  val log = List[CanoeLogEntry]()

  def append(entry: CanoeLogEntry) = entry :: log

  def get(index: Int): Option[CanoeLogEntry] = {
    if(log isDefinedAt index) Some(log(index)) else None
  }

  def last = log.headOption
}


