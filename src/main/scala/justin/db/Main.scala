package justin.db

import akka.actor.ActorSystem
import buildinfo.BuildInfo
import com.typesafe.scalalogging.StrictLogging

// $COVERAGE-OFF$
object Main extends App with StrictLogging {

  logger.info(
    """
      |   ___              _    _        ______ ______
      |  |_  |            | |  (_)       |  _  \| ___ \
      |    | | _   _  ___ | |_  _  _ __  | | | || |_/ /
      |    | || | | |/ __|| __|| || '_ \ | | | || ___ \
      |/\__/ /| |_| |\__ \| |_ | || | | || |/ / | |_/ /
      |\____/  \__,_||___/ \__||_||_| |_||___/  \____/
      |
    """.stripMargin
  )

  val justindbConfig = JustinDBConfig.init
  val actorSystem    = ActorSystem(justindbConfig.system, justindbConfig.config)
  val justindb       = JustinDB.init(justindbConfig)(actorSystem)

  logger.info("Build Info: " + BuildInfo.toString)
}
// $COVERAGE-ON$
