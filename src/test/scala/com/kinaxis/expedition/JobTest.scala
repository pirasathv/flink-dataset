package com.kinaxis.expedition

import java.io.File

import com.kinaxis.expedition.model.in.Expedition
import com.kinaxis.expedition.model.out.ExpeditionResult
import com.kinaxis.expedition.processor.ExpeditionResultProcessor
import org.apache.commons.io.FileUtils
import org.apache.flink.api.scala.{DataSet, ExecutionEnvironment}
import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration
import org.apache.flink.streaming.api.scala._
import org.apache.flink.test.util.MiniClusterWithClientResource
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import scala.io.BufferedSource

class JobTest extends WordSpec with Matchers with BeforeAndAfterAll {

  val flinkCluster = new MiniClusterWithClientResource(
    new MiniClusterResourceConfiguration.Builder()
      .setNumberSlotsPerTaskManager(1)
      .setNumberTaskManagers(1)
      .build
  )

  override def afterAll {
    FileUtils.deleteQuietly(new File("testResults.csv"))
  }

  "Job pipeline" should {

    "process successfully a expedition file and create a calculated file " in {

      val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
      env.setParallelism(1)

      val expeditionSource: DataSet[Expedition] =
        env.readCsvFile[Expedition](getClass.getResource("/test.csv").getPath, ignoreFirstLine = true)

      val headerExpeditionResult = ExpeditionResult("Mineral", "Quantity")
      val header: DataSet[ExpeditionResult] = env.fromElements(headerExpeditionResult)

      val expeditionProcessor: ExpeditionResultProcessor = new ExpeditionResultProcessor

      new Job().init(
        datasetExpedition = expeditionSource,
        expeditionResultProcessor = expeditionProcessor,
        header = header,
        fileName = "testResults.csv"
      )

      env.execute()

      val result: BufferedSource = io.Source.fromFile("testResults.csv")
      val resultLines = result.getLines().toIndexedSeq

      val headerActual = resultLines(0).split(",").map(_.trim)
      val firstRowActual = resultLines(1).split(",").map(_.trim)
      val secondRowActual = resultLines(2).split(",").map(_.trim)

      assertResult(headerExpeditionResult) {
        ExpeditionResult(headerActual(0), headerActual(1))
      }
      assertResult(ExpeditionResult("Chromium","12")) {
        ExpeditionResult(firstRowActual(0), firstRowActual(1))
      }
      assertResult(ExpeditionResult("Gold","2")) {
        ExpeditionResult(secondRowActual(0), secondRowActual(1))
      }

    }

  }
}
