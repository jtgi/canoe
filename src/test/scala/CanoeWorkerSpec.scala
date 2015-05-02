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

  "An FSM" should "be initialized" in {
    val fsm = TestFSMRef(new CanoeWorker);
    fsm.setState(Follower, Unitialized)
    fsm.stateName should be (Follower)
    fsm.stateData should be (Unitialized)
  }
}
