package com.kinaxis.expedition.processor

import com.kinaxis.expedition.model.out.ExpeditionResult
import com.kinaxis.expedition.model.in.Expedition
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.util.Collector

class ExpeditionResultProcessor() extends Serializable {

  val expeditionProcessor = new ExpeditionProcessor

  class ExpeditionProcessor extends RichFlatMapFunction[Iterator[Expedition], ExpeditionResult] {

    override def flatMap(expeditions: Iterator[Expedition], out: Collector[ExpeditionResult]): Unit = {
      var key: String = null
      var sum: Int = 0
      expeditions.foreach { expedition =>
        key = expedition.mineral
        sum += expedition.quantity
      }
      out.collect(ExpeditionResult(key, sum.toString))
      sum = 0
    }
  }

}
