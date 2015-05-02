import akka.actor._
import akka.event.Logging
import scala.concurrent.duration._

/**
 * Created by jtgi on 5/2/15.
 */
object Canoe extends App {
  val system = ActorSystem("canoe")
  val worker = system.actorOf(Props[CanoeWorker], "canoeworker")
}

// Messages
case object AppendEntries

// FSM
sealed trait State
case object Follower extends State
case object Candidate extends State
case object Leader extends State

sealed trait Data
case object Unitialized extends Data

class CanoeWorker extends LoggingFSM[State, Data] {

  startWith(Follower, Unitialized)

  when(Follower, stateTimeout = 2 seconds) {
    case Event(FSM.StateTimeout, Unitialized) =>
      goto(Candidate) using Unitialized
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


