package com.kinaxis.expedition

import com.kinaxis.expedition.model.out.ExpeditionResult
import com.kinaxis.expedition.model.in.Expedition
import com.kinaxis.expedition.processor.ExpeditionResultProcessor
import com.kinaxis.expedition.util.Config
import org.apache.flink.api.scala.{DataSet, ExecutionEnvironment}
import org.apache.flink.streaming.api.scala._

object Main extends App {

  val config = new Config(args)

  val jobName = config.getString("job.name")
  val jobParrallelism = config.getInt("job.parallelism")
  val csvFilePath = config.getString("file.path")

  implicit val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
  env.setParallelism(jobParrallelism)

  // source
  val expeditionSource: DataSet[Expedition] =
    env.readCsvFile[Expedition](getClass.getResource(csvFilePath).getPath, ignoreFirstLine = true)

  // processor
  val expeditionProcessor: ExpeditionResultProcessor = new ExpeditionResultProcessor

  // sink will leverage Flink's dataset csv write
  val header = env.fromElements(ExpeditionResult("Mineral", "Quantity"))

  new Job().init(expeditionSource, expeditionProcessor, header, "minerals.csv")

  // run the job
  env.execute(jobName)

}
