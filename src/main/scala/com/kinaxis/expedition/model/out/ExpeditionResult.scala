package com.kinaxis.expedition.model.out

case class ExpeditionResult(mineral: String, quantity: String)

object ExpeditionResult {

  def apply(mineral: String, quantity: String): ExpeditionResult =
    new ExpeditionResult(mineral, quantity)

}
