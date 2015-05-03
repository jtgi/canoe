import akka.actor.{Props, ActorSystem}
import akka.testkit.{TestFSMRef, TestActorRef, ImplicitSender, TestKit}
import org.scalatest.{FlatSpecLike, BeforeAndAfterAll, Matchers}
import scala.concurrent.duration._

/**
 * Created by jtgi on 5/2/15.
 */
class CanoeWorkerSpec(_system: ActorSystem)
  extends TestKit(_system)
  with ImplicitSender
  with Matchers
  with FlatSpecLike
  with BeforeAndAfterAll {

  def this() = this(ActorSystem("CanoeWorkerSpec"))

  override def afterAll: Unit = {
    system.shutdown()
    system.awaitTermination(10 seconds)
  }

  "A canoe worker" should "be initialized to the Follower state" in {
    val fsm = TestFSMRef(new CanoeWorker)
    fsm.stateName should be (Follower)
  }

  "A follower" should "transition to candidate after timing out" in ???
  "A follower" should "treat AppendEntries as a heartbeat when there is no entry data" in ???
  "A follower" should "refuse an AppendEntries command when its log does not contain the (prevLogIndex, prevLogTerm)" in ???
  "A follower" should "be sent a diff of the log when its log is out of date" in ???
  "A follower" should "deny its vote when a candidate has a shorter log" in ???
  "A follower" should "deny its vote when a candidates log has a lower term" in ???
  "A follower" should "grant its vote when it has not yet voted for the current term and candidates log is at up to date with its log" in ???

  "A candidate" should "broadcast a RequestVote message upon entering candidate state" in ???
  "A candidate" should "vote for itself when it begins an election" in ???
  "A candidate" should "transition to a leader when it receives a majority of votes" in ???
  "A candidate" should "transition to follower if it receives an AppendEntries with a term >= to its own" in ???
  "A candidate" should "return a failure response when an AppendEntries message is sender's term is < its own" in ???

  "A leader" should "send heartbeats to its followers periodically" in ???
  "A leader" should "append a command to its log when it receives a client request" in ???
  "A leader" should "send AppendEntries to each of its followers when it receives a client request" in ???
  "A leader" should "apply the operation to its data store when it receives a majority vote" in ???
  "A leader" should "retry AppendEntries decrementing nextIndex when a follower refuses its request" in ???
  "A leader" should "append all entries up to the its latest index when a follower is behind" in ???
  "A leader" should "keep track of each of its follower's highest replicated log entry index" in ???

  // Perhaps?
  "A leader" should "transition to follower if it receives an AppendEntries with a term >= to its own" in ???

  "Two log entries" should "be identical when they have the same index and term" in ???
  "Two logs" should "be identical up to the same index and term" in ???
}
