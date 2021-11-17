package com.kinaxis.expedition

import java.io.File

import com.kinaxis.expedition.model.out.ExpeditionResult
import com.kinaxis.expedition.model.in.Expedition
import com.kinaxis.expedition.processor.ExpeditionResultProcessor
import org.apache.flink.api.scala.DataSet
import org.apache.flink.core.fs.FileSystem.WriteMode
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector

class Job() extends Serializable {

  def init(
      datasetExpedition: DataSet[Expedition],
      expeditionResultProcessor: ExpeditionResultProcessor,
      header: DataSet[ExpeditionResult],
      fileName: String
  ): Unit = {

    // handle dataset / extract
    val result: DataSet[ExpeditionResult] =
      datasetExpedition
        .groupBy(_.mineral)
        .combineGroup { (minerals: Iterator[Expedition], out: Collector[ExpeditionResult]) =>
          // call processor for logic / transform
          expeditionResultProcessor.expeditionProcessor
            .flatMap(minerals, out)
        }

    // union dataset and emit results to sink / load
    header.union(result).writeAsCsv(new File(s"$fileName").getPath, "\n", ",", WriteMode.OVERWRITE)

  }
}
